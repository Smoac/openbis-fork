/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.clc;

import java.util.Iterator;

import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.persistence.model.PersistenceContainer;
import com.clcbio.api.base.persistence.model.PersistenceStructure;

/**
 * @author anttil
 */
public class ContentSearch
{
    private String structureId;

    public ContentSearch(String structureId)
    {
        this.structureId = structureId;
    }

    public PersistenceStructure runOn(PersistenceContainer container) throws PersistenceException
    {
        Iterator<PersistenceStructure> children = container.list();
        while (children.hasNext())
        {
            PersistenceStructure child = children.next();
            if (structureId.startsWith(child.getId()))
            {
                if (structureId.equals(child.getId()))
                {
                    return child;
                } else
                {
                    return runOn((PersistenceContainer) child);
                }
            }
        }
        throw new PersistenceException("Could not find " + structureId + " from " + container.getId());
    }
}
