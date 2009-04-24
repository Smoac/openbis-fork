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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
public class PropertyTypesFilterUtil
{
    public static List<PropertyType> filterPropertyTypesForEntityKind(List<PropertyType> list,
            EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return filterDataSetPropertyTypes(list);
            case EXPERIMENT:
                return filterExperimentPropertyTypes(list);
            case MATERIAL:
                return filterMaterialPropertyTypes(list);
            case SAMPLE:
                return filterSamplePropertyTypes(list);
        }
        throw new IllegalStateException("unknown enumerator " + entityKind);
    }

    /** returns property types which are assigned to at least one sample type */
    public static List<PropertyType> filterSamplePropertyTypes(List<PropertyType> propertyTypes)
    {
        List<PropertyType> result = new ArrayList<PropertyType>();
        for (final PropertyType st : propertyTypes)
        {
            if (st.getSampleTypePropertyTypes().size() > 0)
            {
                result.add(st);
            }
        }
        return result;
    }

    /** returns property types which are assigned to at least one experiment type */
    public static List<PropertyType> filterExperimentPropertyTypes(List<PropertyType> propertyTypes)
    {
        List<PropertyType> result = new ArrayList<PropertyType>();
        for (final PropertyType st : propertyTypes)
        {
            if (st.getExperimentTypePropertyTypes().size() > 0)
            {
                result.add(st);
            }
        }
        return result;
    }

    /** returns property types which are assigned to at least one dataset type */
    public static List<PropertyType> filterDataSetPropertyTypes(List<PropertyType> propertyTypes)
    {
        List<PropertyType> result = new ArrayList<PropertyType>();
        for (final PropertyType st : propertyTypes)
        {
            if (st.getDataSetTypePropertyTypes().size() > 0)
            {
                result.add(st);
            }
        }
        return result;
    }

    /** returns property types which are assigned to at least one material type */
    public static List<PropertyType> filterMaterialPropertyTypes(List<PropertyType> propertyTypes)
    {
        List<PropertyType> result = new ArrayList<PropertyType>();
        for (final PropertyType st : propertyTypes)
        {
            if (st.getMaterialTypePropertyTypes().size() > 0)
            {
                result.add(st);
            }
        }
        return result;
    }

    public static List<PropertyType> extractPropertyTypes(EntityType selectedType)
    {
        List<? extends EntityTypePropertyType<?>> entityTypePropertyTypes =
                selectedType.getAssignedPropertyTypes();
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        for (EntityTypePropertyType<?> etpt : entityTypePropertyTypes)
        {
            propertyTypes.add(etpt.getPropertyType());
        }
        return propertyTypes;
    }
}
