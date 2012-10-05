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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exception.UserFailureException;

/**
 * Tools to be used by servers.
 * 
 * @author Izabela Adamczyk
 */
public class ServerUtils
{
    /**
     * @throws UserFailureException when list of entities contains duplicates.
     */
    static public <T> void prevalidate(List<T> entities, String entityName)
    {
        Collection<T> duplicated = extractDuplicatedElements(entities);
        if (duplicated.size() > 0)
        {
            throw UserFailureException.fromTemplate("Following %s(s) '%s' are duplicated.",
                    entityName, CollectionUtils.abbreviate(duplicated, 20));
        }
    }

    private static <T> Collection<T> extractDuplicatedElements(List<T> entities)
    {
        Set<T> entitiesSet = new HashSet<T>(entities);
        Collection<T> duplicated = new ArrayList<T>();
        for (T entity : entities)
        {
            // this element must have been duplicated
            if (entitiesSet.remove(entity) == false)
            {
                duplicated.add(entity);
            }
        }
        return duplicated;
    }

    /**
     * Extracts from the comma-separated list of strings all distinct strings.
     * 
     * @return an empty list if the argument is <code>null</code>, an empty string or starts with
     *         '$'.
     */
    public static Set<String> extractSet(String commaSeparatedList)
    {
        Set<String> result = new HashSet<String>();
        if (commaSeparatedList != null && commaSeparatedList.startsWith("$") == false)
        {
            String[] terms = commaSeparatedList.split(",");
            for (String term : terms)
            {
                result.add(term.trim());
            }
        }
        return result;
    }
}
