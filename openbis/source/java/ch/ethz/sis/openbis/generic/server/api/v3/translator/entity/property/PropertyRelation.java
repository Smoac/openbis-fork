/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property;

import java.util.Collection;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relation;

/**
 * @author pkupczyk
 */
public abstract class PropertyRelation implements Relation
{

    private Collection<Long> entityIds;

    private Map<Long, Map<String, String>> propertiesMap;

    public PropertyRelation(Collection<Long> entityIds)
    {
        this.entityIds = entityIds;
    }

    protected abstract Map<Long, Map<String, String>> loadProperties(@SuppressWarnings("hiding")
    Collection<Long> entityIds);

    @Override
    public void load()
    {
        propertiesMap = loadProperties(entityIds);
    }

    public Map<String, String> getProperties(Long entityId)
    {
        return propertiesMap.get(entityId);
    }

}