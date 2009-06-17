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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * An <i>abstract</i> <code>Code</code> extension for types.
 * 
 * @author Christian Ribeaud
 */
public class EntityType extends Code<EntityType> implements IEntityType
{
    private static final long serialVersionUID = IServer.VERSION;

    private String description;

    public EntityType()
    {
    }

    public EntityType(final String code, final String description)
    {
        setCode(code);
        setDescription(description);
    }

    //
    // IEntityType
    //

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }
}
