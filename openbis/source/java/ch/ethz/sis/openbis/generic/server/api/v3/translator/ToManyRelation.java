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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pkupczyk
 */
public abstract class ToManyRelation<OWNER, ORIGINAL, TRANSLATED> implements Relation
{

    private Map<OWNER, Collection<TRANSLATED>> translatedMap;

    @Override
    public void load()
    {
        translatedMap = getTranslatedMap();
    }

    private Map<OWNER, Collection<TRANSLATED>> getTranslatedMap()
    {
        Map<OWNER, Collection<ORIGINAL>> ownerToOriginalCollectionMap = getOriginalMap();
        Set<ORIGINAL> originalSet = new HashSet<ORIGINAL>();

        for (Map.Entry<OWNER, Collection<ORIGINAL>> entry : ownerToOriginalCollectionMap.entrySet())
        {
            if (entry.getValue() != null)
            {
                for (ORIGINAL original : entry.getValue())
                {
                    if (original != null)
                    {
                        originalSet.add(original);
                    }
                }
            }
        }

        Map<ORIGINAL, TRANSLATED> originalToTranslatedMap = getTranslatedMap(originalSet);
        Map<OWNER, Collection<TRANSLATED>> result = new HashMap<OWNER, Collection<TRANSLATED>>();

        for (Map.Entry<OWNER, Collection<ORIGINAL>> ownerToOriginalCollectionEntry : ownerToOriginalCollectionMap.entrySet())
        {
            OWNER owner = ownerToOriginalCollectionEntry.getKey();
            Collection<ORIGINAL> originalCollection = ownerToOriginalCollectionEntry.getValue();

            if (originalCollection != null)
            {
                Collection<TRANSLATED> translatedCollection = null;

                if (originalCollection instanceof List)
                {
                    translatedCollection = new LinkedList<TRANSLATED>();
                } else if (originalCollection instanceof Set)
                {
                    translatedCollection = new LinkedHashSet<TRANSLATED>();
                } else
                {
                    throw new IllegalArgumentException("Collection of type: " + originalCollection.getClass() + " is not supported.");
                }

                for (ORIGINAL original : originalCollection)
                {
                    if (original != null)
                    {
                        TRANSLATED translated = originalToTranslatedMap.get(original);
                        if (translated != null)
                        {
                            translatedCollection.add(translated);
                        }
                    } else
                    {
                        translatedCollection.add(null);
                    }
                }

                result.put(owner, translatedCollection);
            } else
            {
                result.put(owner, null);
            }
        }

        return result;
    }

    public List<TRANSLATED> getTranslatedList(OWNER owner)
    {
        return (List<TRANSLATED>) translatedMap.get(owner);
    }

    public Set<TRANSLATED> getTranslatedSet(OWNER owner)
    {
        return (Set<TRANSLATED>) translatedMap.get(owner);
    }

    protected abstract Map<OWNER, Collection<ORIGINAL>> getOriginalMap();

    protected abstract Map<ORIGINAL, TRANSLATED> getTranslatedMap(Collection<ORIGINAL> originalCollection);

}
