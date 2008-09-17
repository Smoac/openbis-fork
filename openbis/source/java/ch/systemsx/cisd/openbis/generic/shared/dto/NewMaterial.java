/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Class representing a new material.
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 */
public final class NewMaterial extends AbstractEntity<NewMaterial>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private EntityType materialType;

    private NewMaterial inhibitorOfOrNull;

    public NewMaterial(final String code, final EntityType materialType)
    {
        setCode(code);
        this.materialType = materialType;
        this.inhibitorOfOrNull = null;
        setProperties(new SimpleEntityProperty[0]);
    }

    public NewMaterial()
    {
    }

    public NewMaterial getInhibitorOf()
    {
        return inhibitorOfOrNull;
    }

    public void setInhibitorOf(final NewMaterial inhibitorOfOrNull)
    {
        this.inhibitorOfOrNull = inhibitorOfOrNull;
    }

    public final EntityType getMaterialType()
    {
        return materialType;
    }

    public final void setMaterialType(final EntityType materialType)
    {
        this.materialType = materialType;
    }

    //
    // AbstractEntity
    //

    public final EntityType getEntityType()
    {
        return getMaterialType();
    }
}
