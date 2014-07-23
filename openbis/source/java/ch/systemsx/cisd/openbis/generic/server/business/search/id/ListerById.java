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

package ch.systemsx.cisd.openbis.generic.server.business.search.id;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListerById
{

    private List<IListerById> listers;

    public ListerById(List<IListerById> listers)
    {
        this.listers = listers;
    }

    private Map<Class, List> groupIdsByClass(Collection ids)
    {
        Map<Class, List> idClassToIdListMap =
                new HashMap<Class, List>();

        for (Object id : ids)
        {
            List<Object> idList = idClassToIdListMap.get(id.getClass());

            if (idList == null)
            {
                idList = new LinkedList<Object>();
                idClassToIdListMap.put(id.getClass(), idList);
            }

            idList.add(id);
        }
        return idClassToIdListMap;
    }

    private Map<Object, Object> listByIds(Map<Class, List> idClassToIdListMap)
    {
        final Map<Object, Object> idToObject = new HashMap<Object, Object>();

        final Set unmatchedIds = new HashSet();

        for (Class idClass : idClassToIdListMap.keySet())
        {
            List idList = idClassToIdListMap.get(idClass);

            for (IListerById lister : listers)
            {
                if (lister.getIdClass().equals(idClass))
                {
                    HashSet idSet = new HashSet(idList);

                    List objects = lister.listByIds(idList);
                    for (Object object : objects)
                    {
                        Object createId = lister.createId(object);
                        idToObject.put(createId, object);
                        idSet.remove(createId);
                    }
                    unmatchedIds.addAll(idSet);
                }
            }
        }
        if (unmatchedIds.isEmpty() == false)
        {
            throw new UserFailureException("Unknown ids " + CollectionUtils.abbreviate(unmatchedIds, 200));
        }
        return idToObject;
    }

    public <T> List<T> list(Collection ids)
    {
        Map<Class, List> idClassToIdListMap = groupIdsByClass(ids);
        Map<Object, Object> idToObjectMap = listByIds(idClassToIdListMap);
        return CollectionUtils.map(ids, idToObjectMap);
    }
}
