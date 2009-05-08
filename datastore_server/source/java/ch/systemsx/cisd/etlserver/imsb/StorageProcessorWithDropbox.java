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

package ch.systemsx.cisd.etlserver.imsb;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Storage processor which is able to create a copy of incoming data for additional processing. The
 * copy has a changed name to trace back the dataset to which the original data belong.
 * <p>
 * The processor uses followng properties: {@link #DELEGATE_PROCESSOR_CLASS_PROPERTY} and
 * {@link #DROPBOX_INCOMING_DIRECTORY_PROPERTY}. All the properties are also passed for the default
 * processor.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class StorageProcessorWithDropbox implements IStorageProcessor
{
    private static final String DATASET_CODE_SEPARATOR = ".";

    /**
     * Property name which is used to specify the class of the default storage processor, to which
     * all calls are delegated.
     */
    public final static String DELEGATE_PROCESSOR_CLASS_PROPERTY = "default-processor";

    /**
     * The path to the directory where an additional copy of the original incoming data will be
     * created for additional processing.
     */
    public final static String DROPBOX_INCOMING_DIRECTORY_PROPERTY = "dropbox-incoming-dir";

    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, StorageProcessorWithDropbox.class);

    private final IStorageProcessor delegate;

    private final IFileOperations fileOperations;

    private final File dropboxIncomingDir;

    private File recentlyStoredDropboxDataset;

    public StorageProcessorWithDropbox(Properties properties)
    {
        this(properties, createDelegateStorageProcessor(properties), FileOperations.getInstance());
    }

    @Private
    StorageProcessorWithDropbox(Properties properties, IStorageProcessor delegateStorageProcessor,
            IFileOperations fileOperations)
    {
        this.delegate = delegateStorageProcessor;
        this.fileOperations = fileOperations;
        this.dropboxIncomingDir = getDropboxIncomingDir(properties);
    }

    @Private
    static IStorageProcessor createDelegateStorageProcessor(Properties properties)
    {
        String delegateClass = getMandatoryProperty(properties, DELEGATE_PROCESSOR_CLASS_PROPERTY);
        return createClass(IStorageProcessor.class, delegateClass, properties);
    }

    private final File getDropboxIncomingDir(Properties properties)
    {
        return getDirectory(DROPBOX_INCOMING_DIRECTORY_PROPERTY, properties);
    }

    private File getDirectory(String propertyName, Properties properties)
    {
        String filePath = getMandatoryProperty(properties, propertyName);
        File file = new File(filePath);
        if (fileOperations.isDirectory(file) == false)
        {
            throw ConfigurationFailureException.fromTemplate(
                    "The directory '%s' set for '%s' property does not exist.", filePath,
                    propertyName);
        }
        return file;
    }

    private static final String getMandatoryProperty(Properties properties, final String propertyKey)
    {
        return PropertyUtils.getMandatoryProperty(properties, propertyKey);
    }

    private final static <T> T createClass(final Class<T> superClazz, String className,
            Object... argumentsOrNull)
    {
        try
        {
            return ClassUtils.create(superClazz, className, argumentsOrNull);
        } catch (IllegalArgumentException ex)
        {
            throw new ConfigurationFailureException(ex.getMessage());
        }
    }

    //
    // AbstractStorageProcessor
    //

    public final File storeData(final ExperimentPE experiment,
            final DataSetInformation dataSetInformation, final ITypeExtractor typeExtractor,
            final IMailClient mailClient, final File incomingDataSetDirectory, final File rootDir)
    {
        File storeData =
                delegate.storeData(experiment, dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);
        File originalData = delegate.tryGetProprietaryData(storeData);
        String destinationFileName =
                createDropboxDestinationFileName(dataSetInformation, originalData);
        copy(originalData, destinationFileName);
        return storeData;
    }

    private void copy(File originalData, String destinationFileName)
    {
        File destFile = new File(dropboxIncomingDir, destinationFileName);
        try
        {
            fileOperations.copyToDirectoryAs(originalData, dropboxIncomingDir, destinationFileName);

            operationLog.info(String.format("Dataset '%s' copied into dropbox as '%s'.",
                    originalData.getPath(), destFile.getPath()));
        } catch (IOExceptionUnchecked ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot copy '%s' to '%s': %s.",
                    originalData.getPath(), destFile.getPath(), ex.getMessage());
        }
        this.recentlyStoredDropboxDataset = destFile;
    }

    private static String createDropboxDestinationFileName(DataSetInformation dataSetInformation,
            File incomingDataSetDirectory)
    {
        String dataSetCode = dataSetInformation.getDataSetCode();
        String originalName = incomingDataSetDirectory.getName();
        String newFileName =
                stripFileName(originalName) + DATASET_CODE_SEPARATOR + dataSetCode
                        + stripFileExtension(originalName);
        return newFileName;
    }

    // returns file extension with the "." at the beginning or empty string if file has no extension
    private static String stripFileExtension(String originalName)
    {
        int ix = originalName.lastIndexOf(".");
        if (ix == -1)
        {
            return "";
        } else
        {
            return originalName.substring(ix);
        }
    }

    private static String stripFileName(String originalName)
    {
        int ix = originalName.lastIndexOf(".");
        if (ix == -1)
        {
            return originalName;
        } else
        {
            return originalName.substring(0, ix);
        }
    }

    public final void unstoreData(final File incomingDataSetDirectory,
            final File storedDataDirectory)
    {
        if (recentlyStoredDropboxDataset != null && recentlyStoredDropboxDataset.exists())
        {
            fileOperations.deleteRecursively(recentlyStoredDropboxDataset);
        }
        recentlyStoredDropboxDataset = null;
        delegate.unstoreData(incomingDataSetDirectory, storedDataDirectory);
    }

    public final StorageFormat getStorageFormat()
    {
        return delegate.getStorageFormat();
    }

    public final File tryGetProprietaryData(final File storedDataDirectory)
    {
        return delegate.tryGetProprietaryData(storedDataDirectory);
    }

    public File getStoreRootDirectory()
    {
        return delegate.getStoreRootDirectory();
    }

    public void setStoreRootDirectory(File storeRootDirectory)
    {
        delegate.setStoreRootDirectory(storeRootDirectory);
    }
}
