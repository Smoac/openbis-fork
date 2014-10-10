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
import java.util.Map;

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
        Map<RELATED_ID, ORIGINAL> originalIdToOriginalMap = new HashMap<RELATED_ID, ORIGINAL>();

        // get all original objects without duplicates (duplicates are identified by the same ids)

        for (ORIGINAL original : ownerToOriginalMap.values())
        {
            if (original != null)
            {
                RELATED_ID originalId = getOriginalId(original);
                if (false == originalIdToOriginalMap.containsKey(originalId))
                {
                    originalIdToOriginalMap.put(originalId, original);
                }
            }
        }

        // translate the original objects

        Map<RELATED_ID, TRANSLATED> translatedIdToTranslatedMap = new HashMap<RELATED_ID, TRANSLATED>();
        for (TRANSLATED translated : getTranslatedCollection(originalIdToOriginalMap.values()))
        {
            RELATED_ID translatedId = getTranslatedId(translated);
            translatedIdToTranslatedMap.put(translatedId, translated);
        }

        // create a map from an owner to a translated object

        Map<OWNER, TRANSLATED> result = new HashMap<OWNER, TRANSLATED>();
        for (Map.Entry<OWNER, ORIGINAL> ownerToOriginalEntry : ownerToOriginalMap.entrySet())
        {
            OWNER owner = ownerToOriginalEntry.getKey();
            ORIGINAL original = ownerToOriginalEntry.getValue();
            if (original != null)
            {
                RELATED_ID originalId = getOriginalId(original);
                TRANSLATED translated = translatedIdToTranslatedMap.get(originalId);
                result.put(owner, translated);
            } else
            {
                result.put(owner, null);
            }
        }

        return result;
    }

    public TRANSLATED getTranslated(OWNER owner)
    {
        return translatedMap.get(owner);
    }

    protected abstract Map<OWNER, ORIGINAL> getOriginalMap();

    protected abstract Collection<TRANSLATED> getTranslatedCollection(Collection<ORIGINAL> originalCollection);

    protected abstract RELATED_ID getOriginalId(ORIGINAL original);

    protected abstract RELATED_ID getTranslatedId(TRANSLATED translated);

}