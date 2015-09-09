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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToOneRelationTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.EmptyFetchOptions;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class AttachmentContentSqlTranslator extends ObjectToOneRelationTranslator<byte[], EmptyFetchOptions> 
        implements IAttachmentContentSqlTranslator
{
    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet attachmentIds)
    {
        AttachmentQuery query = QueryTool.getManagedQuery(AttachmentQuery.class);
        return query.getContentIds(attachmentIds);
    }

    @Override
    protected Map<Long, byte[]> translateRelated(TranslationContext context, Collection<Long> contentIds, 
            EmptyFetchOptions relatedFetchOptions)
    {
        AttachmentQuery query = QueryTool.getManagedQuery(AttachmentQuery.class);
        List<AttachmentContentRecord> attachmentContents = query.getAttachmentContents(new LongOpenHashSet(contentIds));
        Map<Long, byte[]> result = new HashMap<Long, byte[]>();
        for (AttachmentContentRecord content : attachmentContents)
        {
            result.put(content.id, content.content);
        }
        return result;
    }

}
