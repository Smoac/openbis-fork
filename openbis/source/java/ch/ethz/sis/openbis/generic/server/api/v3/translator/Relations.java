/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "unchecked" })
public class Relations
{
    private Map<Class<? extends Relation>, Relation> relationMap = new HashMap<Class<? extends Relation>, Relation>();

    public void add(Relation relation)
    {
        relationMap.put(relation.getClass(), relation);
    }

    public <T extends Relation> T get(Class<T> relationClass)
    {
        return (T) relationMap.get(relationClass);
    }

    public void load()
    {
        for (Relation relation : relationMap.values())
        {
            relation.load();
        }
    }
}