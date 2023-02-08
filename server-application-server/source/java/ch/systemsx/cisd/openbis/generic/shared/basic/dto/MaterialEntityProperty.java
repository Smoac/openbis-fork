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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * A {@link IEntityProperty} class that only stores the material value, but not a generic value or a vocabulary term value.
 * 
 * @author Bernd Rinn
 */
public class MaterialEntityProperty extends AbstractEntityProperty
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Material materialOrNull;

    @Override
    public void setPropertyType(PropertyType propertyType)
    {
        if (DataTypeCode.MATERIAL.equals(propertyType.getDataType().getCode()) == false)
        {
            throw new IllegalArgumentException(
                    "Only property types with data type MATERIAL supported, found '"
                            + propertyType.getDataType().getCode() + "'.");
        }
        super.setPropertyType(propertyType);
    }

    @Override
    public Material getMaterial()
    {
        return materialOrNull;
    }

    @Override
    public void setMaterial(Material material)
    {
        this.materialOrNull = material;
    }

}
