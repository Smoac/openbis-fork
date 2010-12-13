/*
 * Copyright 2010 ETH Zuerich, CISD
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
 * Table cell for a {@link MaterialIdentifier}.
 *
 * @author Franz-Josef Elmer
 */
public class MaterialTableCell implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    private MaterialIdentifier materialIdentifier;
    
    public MaterialTableCell(Material material)
    {
        if (material == null)
        {
            throw new IllegalArgumentException("Unspecified material");
        }
        EntityType type = material.getEntityType();
        if (type == null)
        {
            throw new IllegalArgumentException("Unspecified material type");
        }
        materialIdentifier = new MaterialIdentifier(material.getCode(), type.getCode());
    }

    public int compareTo(ISerializableComparable o)
    {
        return getMaterialIdentifier().toString().compareTo(o.toString());
    }

    public MaterialIdentifier getMaterialIdentifier()
    {
        return materialIdentifier;
    }

    @Override
    public int hashCode()
    {
        return materialIdentifier.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof MaterialTableCell == false)
        {
            return false;
        }
        MaterialTableCell cell = (MaterialTableCell) obj;
        return cell.materialIdentifier.equals(materialIdentifier);
    }

    @Override
    public String toString()
    {
        return materialIdentifier.toString();
    }

    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private MaterialTableCell()
    {
    }
}
