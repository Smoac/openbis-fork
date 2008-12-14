/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;

/**
 * {@link ModelData} for {@link PropertyType}.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeModel extends BaseModelData
{

    public PropertyTypeModel(PropertyType propertyType)
    {
        set(ModelDataPropertyNames.CODE, propertyType.getCode());
        set(ModelDataPropertyNames.LABEL, propertyType.getLabel());
        set(ModelDataPropertyNames.DATA_TYPE, propertyType.getDataType().getCode());
        set(ModelDataPropertyNames.CONTROLLED_VOCABULARY,
                propertyType.getVocabulary() != null ? propertyType.getVocabulary().getCode()
                        : null);
        set(ModelDataPropertyNames.DESCRIPTION, propertyType.getDescription());
        set(ModelDataPropertyNames.EXPERIMENT_TYPES, propertyType.getExperimentTypePropertyTypes());
        set(ModelDataPropertyNames.MATERIAL_TYPES, propertyType.getMaterialTypePropertyTypes());
        set(ModelDataPropertyNames.SAMPLE_TYPES, propertyType.getSampleTypePropertyTypes());
    }

    private static final long serialVersionUID = 1L;

}
