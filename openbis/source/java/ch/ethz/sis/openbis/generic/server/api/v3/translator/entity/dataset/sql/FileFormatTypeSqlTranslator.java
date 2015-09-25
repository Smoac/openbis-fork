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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.sql;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.FileFormatType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.FileFormatTypeFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class FileFormatTypeSqlTranslator extends AbstractCachingTranslator<Long, FileFormatType, FileFormatTypeFetchOptions> implements
        IFileFormatTypeSqlTranslator
{

    @Autowired
    private IFileFormatTypeBaseSqlTranslator baseTranslator;

    @Override
    protected FileFormatType createObject(TranslationContext context, Long fileFormatTypeId, FileFormatTypeFetchOptions fetchOptions)
    {
        FileFormatType type = new FileFormatType();
        type.setFetchOptions(new FileFormatTypeFetchOptions());
        return type;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> fileFormatTypeIds, FileFormatTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IFileFormatTypeBaseSqlTranslator.class, baseTranslator.translate(context, fileFormatTypeIds, null));

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long fileFormatTypeId, FileFormatType result, Object objectRelations,
            FileFormatTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        FileFormatTypeBaseRecord baseRecord = relations.get(IFileFormatTypeBaseSqlTranslator.class, fileFormatTypeId);

        result.setCode(baseRecord.code);
        result.setDescription(baseRecord.description);
    }

}
