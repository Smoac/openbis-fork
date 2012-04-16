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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialImmutable implements IMaterialImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material;

    private final boolean existingMaterial;

    private Set<String> dynamicPropertiesCodes;
    
    public MaterialImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material)
    {
        this(material, true);
    }

    public MaterialImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material,
            boolean existingMaterial)
    {
        this.material = material;
        this.existingMaterial = existingMaterial;
    }

    protected boolean isDynamicProperty(String code)
    {
        return getDynamicPropertiesCodes().contains(code);
    }

    private Set<String> getDynamicPropertiesCodes()
    {
        if (dynamicPropertiesCodes == null)
        {
            dynamicPropertiesCodes = new HashSet<String>();
            if (material.getMaterialType() != null
                    && material.getMaterialType().getAssignedPropertyTypes() != null)
            {
                for (MaterialTypePropertyType pt : material.getMaterialType()
                        .getAssignedPropertyTypes())
                {
                    if (pt.isDynamic())
                    {
                        dynamicPropertiesCodes.add(pt.getPropertyType().getCode());
                    }
                }
            }
        }
        return dynamicPropertiesCodes;
    }
    
    public String getMaterialIdentifier()
    {
        return material.getIdentifier();
    }

    public String getCode()
    {
        return material.getCode();
    }

    public String getMaterialType()
    {
        if (material.getMaterialType() != null)
        {
            return material.getMaterialType().getCode();
        }
        return null;
    }

    public boolean isExistingMaterial()
    {
        return existingMaterial;
    }

    public ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material getMaterial()
    {
        return material;
    }

    /**
     * Throw an exception if the sample does not exist
     */
    protected void checkExists()
    {
        if (false == isExistingMaterial())
        {
            throw new UserFailureException("Material does not exist.");
        }
    }

    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(material, propertyCode);
    }

}
