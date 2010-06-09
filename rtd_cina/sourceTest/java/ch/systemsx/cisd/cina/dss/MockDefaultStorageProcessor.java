/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

public class MockDefaultStorageProcessor extends AbstractStorageProcessor
{

    public MockDefaultStorageProcessor(final Properties properties)
    {
        super(properties);
    }

    public StorageFormat getStorageFormat()
    {
        return StorageFormat.PROPRIETARY;
    }

    public UnstoreDataAction rollback(File incomingDataSetDirectory, File storedDataDirectory,
            Throwable exception)
    {
        return null;
    }

    public File storeData(DataSetInformation dataSetInformation, ITypeExtractor typeExtractor,
            IMailClient mailClient, File incomingDataSetDirectory, File rootDir)
    {
        return null;
    }

    public File tryGetProprietaryData(File storedDataDirectory)
    {
        return null;
    }

}