/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class EntityAttachmentSqlTranslator extends ObjectToManyRelationTranslator<Attachment, AttachmentFetchOptions>
{
    @Autowired
    private IAttachmentSqlTranslator attachmentTranslator;

    protected abstract List<ObjectRelationRecord> loadRecords(LongOpenHashSet entityIds, AttachmentQuery query);
    
    @Override
    protected Map<Long, Attachment> filterRelatedObjects(Map<Long, Attachment> relatedObjects)
    {
        Set<Attachment> derived = new HashSet<>();
        Collection<Attachment> values = relatedObjects.values();
        for (Attachment attachment : values)
        {
            Attachment current = attachment;
            while (current != null && current.getFetchOptions().hasPreviousVersion())
            {
                current = current.getPreviousVersion();
                derived.add(current);
            }
        }
        Map<Long, Attachment> filteredRelatedObjects = new HashMap<>();
        for (Entry<Long, Attachment> entry : relatedObjects.entrySet())
        {
            Long id = entry.getKey();
            Attachment relatedObject = entry.getValue();
            if (relatedObject.getFileName() != null && derived.contains(relatedObject) == false)
            {
                filteredRelatedObjects.put(id, relatedObject);
            }
        }
        return filteredRelatedObjects;
    }

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds)
    {
        AttachmentQuery query = QueryTool.getManagedQuery(AttachmentQuery.class);
        return loadRecords(objectIds, query);
    }

    @Override
    protected Map<Long, Attachment> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            AttachmentFetchOptions relatedFetchOptions)
    {
        return attachmentTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

    @Override
    protected Collection<Attachment> createCollection()
    {
        return new ArrayList<Attachment>();
    }
}
