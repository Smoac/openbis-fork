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

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class AttachmentSqlTranslator extends AbstractCachingTranslator<Long, Attachment, AttachmentFetchOptions>
        implements IAttachmentSqlTranslator
{
    @Autowired 
    private IAttachmentBaseSqlTranslator baseTranslator;
    
    @Autowired
    private IAttachmentRegistratorSqlTranslator registratorTranslator;
    
    @Autowired
    private IAttachmentContentSqlTranslator contentTranslator;

    @Override
    protected Attachment createObject(TranslationContext context, Long input, AttachmentFetchOptions fetchOptions)
    {
        Attachment result = new Attachment();
        result.setFetchOptions(new AttachmentFetchOptions());
        return result;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> attachmentIds, AttachmentFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();
        relations.put(IAttachmentBaseSqlTranslator.class, baseTranslator.translate(context, attachmentIds, null));
        if (fetchOptions.hasRegistrator())
        {
            relations.put(IAttachmentRegistratorSqlTranslator.class, 
                    registratorTranslator.translate(context, attachmentIds, fetchOptions.withRegistrator()));
        }
        if (fetchOptions.hasContent())
        {
            relations.put(IAttachmentContentSqlTranslator.class, contentTranslator.translate(context, attachmentIds, null));
        }
        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long attachmentId, Attachment result, 
            Object objectRelations, AttachmentFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        AttachmentBaseRecord baseRecord = relations.get(IAttachmentBaseSqlTranslator.class, attachmentId);
        result.setFileName(baseRecord.fileName);
        result.setTitle(baseRecord.title);
        result.setDescription(baseRecord.description);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setVersion(baseRecord.version);
        String baseIndexURL = context.getSession().getBaseIndexURL();
        result.setPermlink(createPermlink(baseRecord, baseIndexURL, false));
        result.setLatestVersionPermlink(createPermlink(baseRecord, baseIndexURL, true));

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IAttachmentRegistratorSqlTranslator.class, attachmentId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
        if (fetchOptions.hasContent())
        {
            result.setContent(relations.get(IAttachmentContentSqlTranslator.class, attachmentId));
            result.getFetchOptions().withContent();
        }
    }
    
    private String createPermlink(AttachmentBaseRecord baseRecord, String baseIndexURL,
            boolean latestVersionPermlink)
    {
        String fileName = baseRecord.fileName;
        Integer version = latestVersionPermlink ? null : baseRecord.version;
        if (baseRecord.projectCode != null)
        {
            return PermlinkUtilities.createProjectAttachmentPermlinkURL(baseIndexURL, fileName, version, 
                    baseRecord.projectCode, baseRecord.spaceCode);
        }
        if (baseRecord.samplePermId != null)
        {
            return PermlinkUtilities.createAttachmentPermlinkURL(baseIndexURL, fileName, version, 
                    AttachmentHolderKind.SAMPLE, baseRecord.samplePermId);
        }
        return PermlinkUtilities.createAttachmentPermlinkURL(baseIndexURL, fileName, version, 
                AttachmentHolderKind.EXPERIMENT, baseRecord.experimentPermId);
    }
}
