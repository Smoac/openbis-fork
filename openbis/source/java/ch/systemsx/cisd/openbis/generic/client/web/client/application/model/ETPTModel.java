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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;

/**
 * {@link BaseModelData} extension for {@link EntityTypePropertyType}s.
 * 
 * @author Izabela Adamczyk
 */
public class ETPTModel extends BaseModelData
{

    private static final long serialVersionUID = 1L;

    public ETPTModel()
    {
    }

    private ETPTModel(EntityTypePropertyType<?> etpt, String entityKind)
    {
        set(ModelDataPropertyNames.IS_MANDATORY, etpt.isMandatory());
        set(ModelDataPropertyNames.PROPERTY_TYPE_CODE, etpt.getPropertyType().getCode());
        set(ModelDataPropertyNames.ENTITY_TYPE_CODE, etpt.getEntityType().getCode());
        set(ModelDataPropertyNames.ENTITY_KIND, entityKind);
    }

    public final static List<ETPTModel> asModels(final List<PropertyType> propertyTypes)
    {
        final List<ETPTModel> models = new ArrayList<ETPTModel>(propertyTypes.size());
        for (final PropertyType propertyType : propertyTypes)
        {
            for (ExperimentTypePropertyType etpt : propertyType.getExperimentTypePropertyTypes())
            {
                models.add(new ETPTModel(etpt, "EXPERIMENT"));
            }
            for (SampleTypePropertyType etpt : propertyType.getSampleTypePropertyTypes())
            {
                models.add(new ETPTModel(etpt, "SAMPLE"));
            }

            for (MaterialTypePropertyType etpt : propertyType.getMaterialTypePropertyTypes())
            {
                models.add(new ETPTModel(etpt, "MATERIAL"));
            }
        }
        return models;
    }
}
