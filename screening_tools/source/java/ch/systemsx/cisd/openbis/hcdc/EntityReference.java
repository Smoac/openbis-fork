package ch.systemsx.cisd.openbis.hcdc;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

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

/**
 * Reference to an entity (e.g. plate or experiment) in openBIS.
 * 
 * @author Tomasz Pylak
 */
public class EntityReference
{
    // technical id of the entity
    private final long id;

    // user friendly identifier
    private final String identifier;

    public EntityReference(long id, String identifier)
    {
        this.id = id;
        this.identifier = identifier;
    }

    public TechId getId()
    {
        return new TechId(id);
    }

    public String getIdentifier()
    {
        return identifier;
    }

    @Override
    public String toString()
    {
        return identifier;
    }

}
