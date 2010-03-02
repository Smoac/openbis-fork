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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Treatment;

/**
 * Helper class which gathers properties in a sample and its ancestor samples defining 
 * sample treatments. Treatments of the same type will be overridden by descendants.
 *
 * @author Franz-Josef Elmer
 */
public class TreatmentFinder
{
    public static final String TREATMENT_TYPE_CODE = "TREATMENT_TYPE";
    public static final String TREATMENT_VALUE_CODE = "TREATMENT_VALUE";
    
    /**
     * Returns all treatments found for specified sample and its ancestors.
     */
    public List<Treatment> findTreatmentsOf(Sample sample)
    {
        Map<String, Treatment> treatments = new LinkedHashMap<String, Treatment>();
        findAndAddTreatments(treatments, sample);
        List<Treatment> treatmentList = new ArrayList<Treatment>(treatments.values());
        Collections.sort(treatmentList);
        return treatmentList;
    }
    
    private void findAndAddTreatments(Map<String, Treatment> treatments, Sample sampleOrNull)
    {
        if (sampleOrNull == null)
        {
            return;
        }
        findAndAddTreatments(treatments, sampleOrNull.getGeneratedFrom());
        List<IEntityProperty> properties = sampleOrNull.getProperties();
        Map<String, Treatment> codeTreatmentMap = new HashMap<String, Treatment>();
        for (IEntityProperty property : properties)
        {
            PropertyType propertyType = property.getPropertyType();
            String code = propertyType.getCode();
            if (code.startsWith(TREATMENT_TYPE_CODE))
            {
                String treatmentCode = code.substring(TREATMENT_TYPE_CODE.length());
                VocabularyTerm vocabularyTerm = property.getVocabularyTerm();
                if (vocabularyTerm == null)
                {
                    throw new UserFailureException("Data type of property type '" + code
                            + "' must be a vocabulary.");
                }
                Treatment treatment = getOrCreateTreatment(codeTreatmentMap, treatmentCode);
                treatment.setType(getLabelOrCode(vocabularyTerm));
                treatment.setTypeCode(vocabularyTerm.getCode());
            } else if (code.startsWith(TREATMENT_VALUE_CODE))
            {
                String treatmentCode = code.substring(TREATMENT_VALUE_CODE.length());
                Treatment treatment = getOrCreateTreatment(codeTreatmentMap, treatmentCode);
                DataTypeCode dataType = propertyType.getDataType().getCode();
                treatment.setValueType(dataType.toString());
                String value = getValue(property);
                treatment.setValue(value);
            }
        }
        Collection<Treatment> treatmentsToBeAdded = codeTreatmentMap.values();
        for (Treatment treatment : treatmentsToBeAdded)
        {
            Treatment superTreatment = treatments.get(treatment.getType());
            if (superTreatment == null)
            {
                treatments.put(treatment.getType(), treatment);
            } else
            {
                superTreatment.setValue(treatment.getValue());
            }
        }
    }

    private String getValue(IEntityProperty property)
    {
        Material material = property.getMaterial();
        if (material != null)
        {
            return material.getCode();
        }
        VocabularyTerm vocabularyTerm = property.getVocabularyTerm();
        return vocabularyTerm == null ? property.getValue() : getLabelOrCode(vocabularyTerm);
    }

    private String getLabelOrCode(VocabularyTerm vocabularyTerm)
    {
        String label = vocabularyTerm.getLabel();
        if (StringUtils.isBlank(label))
        {
            label = vocabularyTerm.getCode();
        }
        return label;
    }

    private Treatment getOrCreateTreatment(Map<String, Treatment> codeTreatmentMap,
            String treatmentCode)
    {
        Treatment treatment = codeTreatmentMap.get(treatmentCode);
        if (treatment == null)
        {
            treatment = new Treatment();
            treatment.setType("");
            treatment.setTypeCode("");
            treatment.setValue("");
            treatment.setValueType(DataTypeCode.VARCHAR.toString());
            codeTreatmentMap.put(treatmentCode, treatment);
        }
        return treatment;
    }
}
