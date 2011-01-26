/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertiesBean;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ValidationException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluator;

/**
 * Handles Managed Properties of batch uploads/updates.
 *
 * @author Franz-Josef Elmer
 */
public class PropertiesBatchManager implements IPropertiesBatchManager
{
    final IDAOFactory daoFactory;

    public PropertiesBatchManager(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    public void manageProperties(NewSamplesWithTypes newSamplesWithTypes)
    {
        List<NewSample> newSamples = newSamplesWithTypes.getNewSamples();
        ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
        String code = newSamplesWithTypes.getSampleType().getCode();
        SampleTypePE sampleType = sampleTypeDAO.tryFindSampleTypeByCode(code);
        Set<? extends EntityTypePropertyTypePE> sampleTypePropertyTypes =
                sampleType.getSampleTypePropertyTypes();
    
        managePropertiesBeans(newSamples, sampleTypePropertyTypes);
    }
    
    private void managePropertiesBeans(List<? extends IPropertiesBean> propertiesBeans,
            Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes)
    {
        Map<String, ManagedPropertyEvaluator> evaluators =
                createEvaluators(entityTypePropertyTypes);
        for (int i = 0; i < propertiesBeans.size(); i++)
        {
            IPropertiesBean propertiesBean = propertiesBeans.get(i);
            IEntityProperty[] properties = propertiesBean.getProperties();
            List<IEntityProperty> newProperties = new ArrayList<IEntityProperty>();
            Map<String, Map<String, String>> subColumnBindings =
                    createColumnBindingsMap(properties);
            for (Entry<String, Map<String, String>> entry : subColumnBindings.entrySet())
            {
                String code = entry.getKey();
                Map<String, String> bindings = entry.getValue();
                ManagedPropertyEvaluator evaluator = evaluators.get(code);
                EntityProperty entityProperty = new EntityProperty();
                PropertyType propertyType = new PropertyType();
                propertyType.setCode(code);
                propertyType.setDataType(new DataType(DataTypeCode.VARCHAR));
                entityProperty.setPropertyType(propertyType);
                if (evaluator == null)
                {
                    entityProperty.setValue(bindings.get(""));
                } else
                {
                    try
                    {
                        String result = evaluator.updateFromBatchInput(bindings);
                        entityProperty.setValue(result);
                    } catch (EvaluatorException ex)
                    {
                        Throwable cause = ex.getCause();
                        if (cause instanceof ValidationException)
                        {
                            throw new ValidationException("Error in row " + (i + 1) + ": "
                                    + cause.getMessage());
                        }
                        entityProperty.setValue(BasicConstant.ERROR_PROPERTY_PREFIX
                                + ex.getMessage());
                    }
                }
                newProperties.add(entityProperty);
            }
            propertiesBean.setProperties(newProperties.toArray(new IEntityProperty[newProperties
                    .size()]));
        }
    }

    private Map<String, Map<String, String>> createColumnBindingsMap(IEntityProperty[] properties)
    {
        Map<String, Map<String, String>> subColumnBindings =
                new HashMap<String, Map<String, String>>();
        for (IEntityProperty property : properties)
        {
            String code = property.getPropertyType().getCode().toUpperCase();
            int indexOfColon = code.indexOf(':');
            String propertyCode = code;
            String subColumn = "";
            if (indexOfColon >= 0)
            {
                propertyCode = code.substring(0, indexOfColon);
                subColumn = code.substring(indexOfColon + 1);
            }
            Map<String, String> bindings = subColumnBindings.get(propertyCode);
            if (bindings == null)
            {
                bindings = new HashMap<String, String>();
                subColumnBindings.put(propertyCode, bindings);
            }
            bindings.put(subColumn, property.getValue());
        }
        return subColumnBindings;
    }

    private Map<String, ManagedPropertyEvaluator> createEvaluators(
            Set<? extends EntityTypePropertyTypePE> entityTypePropertyTypes)
    {
        Map<String, ManagedPropertyEvaluator> evaluators =
                new HashMap<String, ManagedPropertyEvaluator>();
        for (EntityTypePropertyTypePE entityTypePropertyType : entityTypePropertyTypes)
        {
            if (entityTypePropertyType.isManaged())
            {
                String propertyTypeCode = entityTypePropertyType.getPropertyType().getCode();
                ScriptPE script = entityTypePropertyType.getScript();
                evaluators.put(propertyTypeCode, new ManagedPropertyEvaluator(script.getScript()));
            }
        }
        return evaluators;
    }
}   
