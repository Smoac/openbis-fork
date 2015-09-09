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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.lemnik.eodsql.QueryTool;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.sql.AttachmentQuery;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.sql.IAttachmentSqlTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SampleAttachmentSqlTranslator extends ObjectToManyRelationTranslator<Attachment, AttachmentFetchOptions> 
        implements ISampleAttachmentSqlTranslator
{
    @Autowired
    private IAttachmentSqlTranslator attachmentTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds)
    {
        AttachmentQuery query = QueryTool.getManagedQuery(AttachmentQuery.class);
        return query.getSampleAttachments(objectIds);
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
