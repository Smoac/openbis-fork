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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.Complete;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.ExternalDataFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class ExternalDataTranslator extends AbstractCachingTranslator<Long, ExternalData, ExternalDataFetchOptions> implements
        IExternalDataTranslator
{

    @Autowired
    private IExternalDataBaseTranslator baseTranslator;

    @Autowired
    private IExternalDataFileFormatTypeTranslator fileFormatTypeTranslator;

    @Autowired
    private IExternalDataLocatorTypeTranslator locatorTypeTranslator;

    @Autowired
    private IExternalDataStorageFormatTranslator storageFormatTranslator;

    @Override
    protected ExternalData createObject(TranslationContext context, Long dataSetId, ExternalDataFetchOptions fetchOptions)
    {
        ExternalData externalData = new ExternalData();
        externalData.setFetchOptions(new ExternalDataFetchOptions());
        return externalData;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> dataSetIds, ExternalDataFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IExternalDataBaseTranslator.class, baseTranslator.translate(context, dataSetIds, null));

        if (fetchOptions.hasFileFormatType())
        {
            relations.put(IExternalDataFileFormatTypeTranslator.class,
                    fileFormatTypeTranslator.translate(context, dataSetIds, fetchOptions.withFileFormatType()));
        }

        if (fetchOptions.hasLocatorType())
        {
            relations.put(IExternalDataLocatorTypeTranslator.class,
                    locatorTypeTranslator.translate(context, dataSetIds, fetchOptions.withLocatorType()));
        }

        if (fetchOptions.hasStorageFormat())
        {
            relations.put(IExternalDataStorageFormatTranslator.class,
                    storageFormatTranslator.translate(context, dataSetIds, fetchOptions.withStorageFormat()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long dataSetId, ExternalData result, Object objectRelations,
            ExternalDataFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ExternalDataBaseRecord baseRecord = relations.get(IExternalDataBaseTranslator.class, dataSetId);

        result.setShareId(baseRecord.shareId);
        result.setLocation(baseRecord.location);
        result.setSize(baseRecord.size);
        result.setComplete(translateComplete(baseRecord.isComplete));
        result.setStatus(translateStatus(baseRecord.status));
        result.setPresentInArchive(baseRecord.isPresentInArchive);
        result.setStorageConfirmation(baseRecord.isStorageConfirmed);
        result.setSpeedHint(baseRecord.speedHint);

        if (fetchOptions.hasFileFormatType())
        {
            result.setFileFormatType(relations.get(IExternalDataFileFormatTypeTranslator.class, dataSetId));
            result.getFetchOptions().withFileFormatTypeUsing(fetchOptions.withFileFormatType());
        }

        if (fetchOptions.hasLocatorType())
        {
            result.setLocatorType(relations.get(IExternalDataLocatorTypeTranslator.class, dataSetId));
            result.getFetchOptions().withLocatorTypeUsing(fetchOptions.withLocatorType());
        }

        if (fetchOptions.hasStorageFormat())
        {
            result.setStorageFormat(relations.get(IExternalDataStorageFormatTranslator.class, dataSetId));
            result.getFetchOptions().withStorageFormatUsing(fetchOptions.withStorageFormat());
        }

    }

    private Complete translateComplete(String value)
    {
        if (value != null)
        {
            switch (value)
            {
                case "T":
                    return Complete.YES;
                case "F":
                    return Complete.NO;
                case "U":
                    return Complete.UNKNOWN;
                default:
                    throw new IllegalArgumentException("Unknown value: " + value);
            }
        }

        return null;
    }

    private ArchivingStatus translateStatus(String value)
    {
        if (value != null)
        {
            switch (value)
            {
                case "ARCHIVE_PENDING":
                    return ArchivingStatus.ARCHIVE_PENDING;
                case "ARCHIVED":
                    return ArchivingStatus.ARCHIVED;
                case "AVAILABLE":
                    return ArchivingStatus.AVAILABLE;
                case "BACKUP_PENDING":
                    return ArchivingStatus.BACKUP_PENDING;
                case "LOCKED":
                    return ArchivingStatus.LOCKED;
                case "UNARCHIVE_PENDING":
                    return ArchivingStatus.UNARCHIVE_PENDING;
                default:
                    throw new IllegalArgumentException("Unknown value: " + value);
            }
        }

        return null;
    }

}
