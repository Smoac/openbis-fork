/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.basysbio.cisd.dss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyTypeWithVocabulary;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DataSetPropertiesValidator
{
    private final Map<String, Map<String, PropertyTypeWithVocabulary>> dataSetType2PropertyTypeMap =
            new HashMap<String, Map<String, PropertyTypeWithVocabulary>>();

    DataSetPropertiesValidator(Collection<String> translatedDataSetTypes,
            IEncapsulatedOpenBISService service)
    {
        StringBuilder builder = new StringBuilder();
        for (String dataSetTypeCode : translatedDataSetTypes)
        {
            DataSetTypeWithVocabularyTerms dataSetType = null;
            try
            {
                dataSetType = service.getDataSetType(dataSetTypeCode);
                add(dataSetType);
            } catch (HighLevelException ex)
            {
                builder.append(ex.getMessage()).append('\n');
            }
        }
        if (builder.length() > 0)
        {
            throw new ConfigurationFailureException(builder.toString());
        }
    }
    
    void assertValidFor(String dataSetType, TimePointPropertyType key, String value)
    {
        Map<String, PropertyTypeWithVocabulary> map = dataSetType2PropertyTypeMap.get(dataSetType);
        PropertyTypeWithVocabulary propertyType = map.get(key.toString());
        if (propertyType.isVocabulary())
        {
            Set<String> terms = propertyType.getVocabularyTerms();
            if (terms.contains(value) == false)
            {
                throw new IllegalArgumentException("Value of property " + key
                        + " is not from the vocabulary " + terms + ": " + value);
            }
        }
    }

    private void add(DataSetTypeWithVocabularyTerms dataSetType)
    {
        List<PropertyTypeWithVocabulary> assignedPropertyTypes = dataSetType.getPropertyTypes();
        Map<String, PropertyTypeWithVocabulary> map = new HashMap<String, PropertyTypeWithVocabulary>();
        for (PropertyTypeWithVocabulary dataSetTypePropertyType : assignedPropertyTypes)
        {
            map.put(dataSetTypePropertyType.getCode(), dataSetTypePropertyType);
        }
        TimePointPropertyType[] values = TimePointPropertyType.values();
        List<TimePointPropertyType> missingPropertyTypes = new ArrayList<TimePointPropertyType>();
        List<TimePointPropertyType> wrongDataTypePropertyTypes = new ArrayList<TimePointPropertyType>();
        for (TimePointPropertyType timePointPropertyType : values)
        {
            PropertyTypeWithVocabulary propertyType = map.get(timePointPropertyType.toString());
            if (propertyType == null)
            {
                missingPropertyTypes.add(timePointPropertyType);
            } else
            {
                if (timePointPropertyType.isVocabulary() != propertyType.isVocabulary())
                {
                    wrongDataTypePropertyTypes.add(timePointPropertyType);
                }
            }
        }
        StringBuilder errorMessage =
                createErrorMessage(missingPropertyTypes, wrongDataTypePropertyTypes);
        if (errorMessage.length() > 0)
        {
            throw new ConfigurationFailureException("Wrong property assignments for data set type "
                    + dataSetType.getCode() + ":\n" + errorMessage);
        }
        dataSetType2PropertyTypeMap.put(dataSetType.getCode(), map);
    }

    private StringBuilder createErrorMessage(List<TimePointPropertyType> missingPropertyTypes,
            List<TimePointPropertyType> wrongDataTypePropertyTypes)
    {
        StringBuilder errorMessage = new StringBuilder();
        if (missingPropertyTypes.isEmpty() == false)
        {
            gotoNewLine(errorMessage);
            errorMessage.append("The following property types are not assigned: ");
            errorMessage.append(missingPropertyTypes);
        }
        if (wrongDataTypePropertyTypes.isEmpty() == false)
        {
            for (TimePointPropertyType timePointPropertyType : wrongDataTypePropertyTypes)
            {
                gotoNewLine(errorMessage);
                errorMessage.append("Property type ").append(timePointPropertyType).append(" has ");
                if (timePointPropertyType.isVocabulary())
                {
                    errorMessage.append("not ");
                }
                errorMessage.append("to be a vocabulary.");
            }
        }
        return errorMessage;
    }
    
    private void gotoNewLine(StringBuilder builder)
    {
        if (builder.length() != 0)
        {
            builder.append('\n');
        }
    }

}
