/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.vocabulary.VocabularyTermTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.Complete;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.ExternalDataFetchOptions;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * @author pkupczyk
 */
public class ExternalDataTranslator extends AbstractCachingTranslator<ExternalDataPE, ExternalData, ExternalDataFetchOptions>
{
    public ExternalDataTranslator(TranslationContext translationContext, ExternalDataFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected ExternalData createObject(ExternalDataPE data)
    {
        ExternalData result = new ExternalData();

        result.setShareId(data.getShareId());
        result.setLocation(data.getLocation());
        result.setSize(data.getSize());
        result.setComplete(translateComplete(data.getComplete()));
        result.setStatus(translateStatus(data.getStatus()));
        result.setPresentInArchive(data.isPresentInArchive());
        result.setStorageConfirmation(data.isStorageConfirmation());
        result.setSpeedHint(data.getSpeedHint());
        result.setFetchOptions(new ExternalDataFetchOptions());

        return result;
    }

    private Complete translateComplete(BooleanOrUnknown value)
    {
        if (value != null)
        {
            switch (value)
            {
                case T:
                    return Complete.YES;
                case F:
                    return Complete.NO;
                case U:
                    return Complete.UNKNOWN;
                default:
                    throw new IllegalArgumentException("Unknown value: " + value);
            }
        }

        return null;
    }

    private ArchivingStatus translateStatus(DataSetArchivingStatus value)
    {
        if (value != null)
        {
            switch (value)
            {
                case ARCHIVE_PENDING:
                    return ArchivingStatus.ARCHIVE_PENDING;
                case ARCHIVED:
                    return ArchivingStatus.ARCHIVED;
                case AVAILABLE:
                    return ArchivingStatus.AVAILABLE;
                case BACKUP_PENDING:
                    return ArchivingStatus.BACKUP_PENDING;
                case LOCKED:
                    return ArchivingStatus.LOCKED;
                case UNARCHIVE_PENDING:
                    return ArchivingStatus.UNARCHIVE_PENDING;
                default:
                    throw new IllegalArgumentException("Unknown value: " + value);
            }
        }

        return null;
    }

    @Override
    protected void updateObject(ExternalDataPE data, ExternalData result, Relations relations)
    {
        if (getFetchOptions().hasFileFormatType())
        {
            result.setFileFormatType(new FileFormatTypeTranslator(getTranslationContext(), getFetchOptions().withFileFormatType()).translate(data
                    .getFileFormatType()));
            result.getFetchOptions().withFileFormatTypeUsing(getFetchOptions().withFileFormatType());
        }

        if (getFetchOptions().hasLocatorType())
        {
            result.setLocatorType(new LocatorTypeTranslator(getTranslationContext(), getFetchOptions().withLocatorType()).translate(data
                    .getLocatorType()));
            result.getFetchOptions().withLocatorTypeUsing(getFetchOptions().withLocatorType());
        }

        if (getFetchOptions().hasStorageFormat())
        {
            result.setStorageFormat(new VocabularyTermTranslator(getTranslationContext(), getFetchOptions()
                    .withStorageFormat()).translate(data.getStorageFormatVocabularyTerm()));
            result.getFetchOptions().withStorageFormatUsing(getFetchOptions().withStorageFormat());
        }
    }

}
