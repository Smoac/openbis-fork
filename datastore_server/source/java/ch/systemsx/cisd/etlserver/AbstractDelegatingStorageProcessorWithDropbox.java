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
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Storage processor which is able to create a copy of incoming data for additional processing. The
 * copy has a changed name to trace back the dataset to which the original data belong.
 * <p>
 * The processor uses following properties: {@link #DELEGATE_PROCESSOR_CLASS_PROPERTY} and
 * {@link #DATASET_CODE_SEPARATOR_PROPERTY}. All the properties are also passed for the default
 * processor.
 * </p>
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDelegatingStorageProcessorWithDropbox extends
        AbstractDelegatingStorageProcessor
{
    /**
     * @return the directory where copy of the original data should be created
     */
    abstract protected File tryGetDropboxDir(File originalData,
            DataSetInformation dataSetInformation);

    /**
     * @return the name of the dataset copy file
     */
    abstract protected String createDropboxDestinationFileName(
            DataSetInformation dataSetInformation, File incomingDataSetDirectory);

    // --------

    private static final String DEFAULT_DATASET_CODE_SEPARATOR = ".";

    /**
     * Property name which is used to specify the text which will be used to separate the sample
     * code and dataset code in the name of the file which will be created in the dropbox.
     */
    public final static String DATASET_CODE_SEPARATOR_PROPERTY = "entity-separator";

    protected final String datasetCodeSeparator;

    private final IFileOperations fileOperations;

    private File recentlyStoredDropboxDataset;

    public AbstractDelegatingStorageProcessorWithDropbox(Properties properties)
    {
        this(properties, AbstractDelegatingStorageProcessor
                .createDelegateStorageProcessor(properties), FileOperations.getInstance());
    }

    @Private
    AbstractDelegatingStorageProcessorWithDropbox(Properties properties,
            IStorageProcessor delegateStorageProcessor, IFileOperations fileOperations)
    {
        super(delegateStorageProcessor);
        this.fileOperations = fileOperations;
        this.datasetCodeSeparator =
                PropertyUtils.getProperty(properties, DATASET_CODE_SEPARATOR_PROPERTY,
                        DEFAULT_DATASET_CODE_SEPARATOR);
    }

    protected final File tryGetDirectory(String propertyName, Properties properties)
    {
        String filePath = PropertyUtils.getProperty(properties, propertyName);
        if (filePath == null)
        {
            return null;
        }
        File file = new File(filePath);
        if (fileOperations.isDirectory(file) == false)
        {
            throw ConfigurationFailureException.fromTemplate(
                    "The directory '%s' set for '%s' property does not exist.", filePath,
                    propertyName);
        }
        return file;
    }

    //
    // AbstractStorageProcessor
    //

    @Override
    public final File storeData(final SamplePE sample, final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir)
    {
        File storeData =
                super.storeData(sample, dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);
        File originalData = super.tryGetProprietaryData(storeData);
        File dropboxDir = tryGetDropboxDir(originalData, dataSetInformation);
        if (dropboxDir != null)
        {
            String destinationFileName =
                    createDropboxDestinationFileName(dataSetInformation, originalData);
            copy(originalData, dropboxDir, destinationFileName);
        }
        return storeData;
    }

    private void copy(File originalData, File dropboxDir, String destinationFileName)
    {
        File destFile = new File(dropboxDir, destinationFileName);
        try
        {
            fileOperations.copyToDirectoryAs(originalData, dropboxDir, destinationFileName);

            operationLog.info(String.format("Dataset '%s' copied into dropbox as '%s'.",
                    originalData.getPath(), destFile.getPath()));
        } catch (IOExceptionUnchecked ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot copy '%s' to '%s': %s.",
                    originalData.getPath(), destFile.getPath(), ex.getMessage());
        }
        this.recentlyStoredDropboxDataset = destFile;
    }

    @Override
    public UnstoreDataAction unstoreData(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        if (recentlyStoredDropboxDataset != null && recentlyStoredDropboxDataset.exists())
        {
            fileOperations.deleteRecursively(recentlyStoredDropboxDataset);
        }
        recentlyStoredDropboxDataset = null;
        return super.unstoreData(incomingDataSetDirectory, storedDataDirectory, exception);
    }
}
