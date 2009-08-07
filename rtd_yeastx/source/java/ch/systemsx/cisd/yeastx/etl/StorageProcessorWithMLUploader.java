/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Tomasz Pylak
 */
public final class StorageProcessorWithMLUploader extends AbstractDelegatingStorageProcessor
{
    private final ML2DatabaseUploader uploader;

    public StorageProcessorWithMLUploader(Properties properties)
    {
        super(properties);
        this.uploader = new ML2DatabaseUploader(properties);
    }

    @Override
    public File storeData(final SamplePE sample, final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir)
    {
        File storeData =
                super.storeData(sample, dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);
        File originalData = super.tryGetProprietaryData(storeData);
        uploader.upload(originalData, dataSetInformation);
        return storeData;
    }

    @Override
    public UnstoreDataAction unstoreData(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        super.unstoreData(incomingDataSetDirectory, storedDataDirectory, exception);
        LogUtils log = new LogUtils(incomingDataSetDirectory.getParentFile());
        log.datasetFileError(incomingDataSetDirectory, exception);
        return UnstoreDataAction.LEAVE_UNTOUCHED;
    }
}
