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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractStrorageProcessorWithUploader;
import ch.systemsx.cisd.etlserver.DelegatingStorageProcessorWithDropbox;
import ch.systemsx.cisd.etlserver.IDataSetUploader;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class StorageProcessor extends DelegatingStorageProcessorWithDropbox
{
    private static final class StorageProcessorWithUploader extends AbstractStrorageProcessorWithUploader
    {
        public StorageProcessorWithUploader(IStorageProcessor processor, IDataSetUploader uploader)
        {
            super(processor, uploader);
        }

        @Override
        protected void logDataSetFileError(File incomingDataSetDirectory, Throwable exception)
        {
        }
        
    }
    
    private final StorageProcessorWithUploader storageProcessorWithUploader;

    public StorageProcessor(Properties properties)
    {
        this(properties, new TimeSeriesDataSetHandler(properties, ServiceProvider.getOpenBISService()));
    }
    
    StorageProcessor(Properties properties, TimeSeriesDataSetHandler handler)
    {
        super(properties, handler);
        storageProcessorWithUploader = new StorageProcessorWithUploader(new IStorageProcessor()
            {
                
                public void setStoreRootDirectory(File storeRootDirectory)
                {
                    
                }
                
                public File getStoreRootDirectory()
                {
                    return null;
                }
                
                public File tryGetProprietaryData(File storedDataDirectory)
                {
                    return null;
                }
                
                public File storeData(DataSetInformation dataSetInformation, ITypeExtractor typeExtractor,
                        IMailClient mailClient, File incomingDataSetDirectory, File rootDir)
                {
                    return null;
                }
                
                public UnstoreDataAction rollback(File incomingDataSetDirectory, File storedDataDirectory,
                        Throwable exception)
                {
                    return null;
                }
                
                public StorageFormat getStorageFormat()
                {
                    return null;
                }
                
                public void commit()
                {
                    
                }
            }, handler);
    }

    @Override
    public UnstoreDataAction rollback(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        super.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
        return storageProcessorWithUploader.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
    }

    @Override
    public void commit()
    {
        super.commit();
        storageProcessorWithUploader.commit();
    }
}
