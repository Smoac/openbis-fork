/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Storage processor which delegates to a wrapped {@link IStorageProcessor}. In addition
 * a {@link IPostRegistrationDatasetHandler} handles the data set.
 * <p>
 * The processor uses following properties: {@link #DELEGATE_PROCESSOR_CLASS_PROPERTY}. All the
 * properties are also passed for the default processor.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class DelegatingStorageProcessorWithDropbox extends
        AbstractDelegatingStorageProcessor
{
    private final IPostRegistrationDatasetHandler dropboxHandler;

    /**
     * Note that this class is not a valid storage processor as it does not provide the appropriate
     * constructor which takes only properties parameter.
     */
    public DelegatingStorageProcessorWithDropbox(Properties properties,
            IPostRegistrationDatasetHandler dropboxHandler)
    {
        this(properties, dropboxHandler, AbstractDelegatingStorageProcessor
                .createDelegateStorageProcessor(properties), FileOperations.getInstance());
    }

    @Private
    DelegatingStorageProcessorWithDropbox(Properties properties,
            IPostRegistrationDatasetHandler dropboxHandler,
            IStorageProcessor delegateStorageProcessor, IFileOperations fileOperations)
    {
        super(delegateStorageProcessor);
        this.dropboxHandler = dropboxHandler;
    }

    //
    // AbstractStorageProcessor
    //

    @Override
    public final File storeData(final DataSetInformation dataSetInformation, final ITypeExtractor typeExtractor,
            final IMailClient mailClient, final File incomingDataSetDirectory,
            final File rootDir)
    {
        File storeData =
                super.storeData(dataSetInformation, typeExtractor, mailClient, incomingDataSetDirectory,
                        rootDir);
        File originalData = super.tryGetProprietaryData(storeData);
        dropboxHandler.handle(originalData, dataSetInformation, null);
        return storeData;
    }

    @Override
    public UnstoreDataAction rollback(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        dropboxHandler.undoLastOperation();
        return super.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
    }
}
