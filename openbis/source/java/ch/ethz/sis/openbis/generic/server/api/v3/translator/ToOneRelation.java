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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author pkupczyk
 */
public abstract class ToOneRelation<OWNER, RELATED_ID, ORIGINAL, TRANSLATED> implements Relation
{

    private Map<OWNER, TRANSLATED> translatedMap;

    @Override
    public void load()
    {
        translatedMap = getTranslatedMap();
    }

    private Map<OWNER, TRANSLATED> getTranslatedMap()
    {
        Map<OWNER, ORIGINAL> ownerToOriginalMap = getOriginalMap();
        Set<ORIGINAL> originalSet = new HashSet<ORIGINAL>();

        for (ORIGINAL original : ownerToOriginalMap.values())
        {
            if (original != null)
            {
                originalSet.add(original);
            }
        }

        Map<ORIGINAL, TRANSLATED> originalToTranslatedMap = getTranslatedMap(originalSet);
        Map<OWNER, TRANSLATED> ownerToTranslatedMap = new LinkedHashMap<OWNER, TRANSLATED>();

        for (Map.Entry<OWNER, ORIGINAL> entry : ownerToOriginalMap.entrySet())
        {
            OWNER owner = entry.getKey();
            ORIGINAL original = entry.getValue();
            TRANSLATED translated = originalToTranslatedMap.get(original);
            ownerToTranslatedMap.put(owner, translated);
        }

        return ownerToTranslatedMap;
    }

    public TRANSLATED getTranslated(OWNER owner)
    {
        return translatedMap.get(owner);
    }

    protected abstract Map<OWNER, ORIGINAL> getOriginalMap();

    protected abstract Map<ORIGINAL, TRANSLATED> getTranslatedMap(Collection<ORIGINAL> originalCollection);

}