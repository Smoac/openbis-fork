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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
public abstract class ObjectToOneRelation<RELATED_OBJECT, RELATED_FETCH_OPTIONS> implements Relation
{

    private TranslationContext context;

    private Collection<Long> objectIds;

    private RELATED_FETCH_OPTIONS relatedFetchOptions;

    private Map<Long, RELATED_OBJECT> objectIdToRelatedMap = new HashMap<Long, RELATED_OBJECT>();

    public ObjectToOneRelation(TranslationContext context, Collection<Long> objectIds, RELATED_FETCH_OPTIONS relatedFetchOptions)
    {
        this.context = context;
        this.objectIds = objectIds;
        this.relatedFetchOptions = relatedFetchOptions;
    }

    @Override
    public void load()
    {
        List<ObjectToOneRecord> records = load(new LongOpenHashSet(objectIds));

        Collection<Long> relatedIds = new HashSet<Long>();
        for (ObjectToOneRecord record : records)
        {
            relatedIds.add(record.relatedId);
        }

        Map<Long, RELATED_OBJECT> relatedIdToRelated = translate(context, relatedIds, relatedFetchOptions);

        for (ObjectToOneRecord record : records)
        {
            objectIdToRelatedMap.put(record.objectId, relatedIdToRelated.get(record.relatedId));
        }
    }

    @SuppressWarnings("hiding")
    protected abstract List<ObjectToOneRecord> load(LongOpenHashSet objectIds);

    @SuppressWarnings("hiding")
    protected abstract Map<Long, RELATED_OBJECT> translate(TranslationContext context, Collection<Long> relatedIds,
            RELATED_FETCH_OPTIONS relatedFetchOptions);

    public RELATED_OBJECT getRelated(Long objectId)
    {
        return objectIdToRelatedMap.get(objectId);
    }

}
