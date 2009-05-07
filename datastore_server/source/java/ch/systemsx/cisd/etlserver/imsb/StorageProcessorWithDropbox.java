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

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
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
 * 
 * @author Tomasz Pylak
 */
public class StorageProcessorWithDropbox implements IStorageProcessor
{
    @Private
    final static String DELEGATE_PROCESSOR_CLASS_PROPERTY = "default-processor";

    @Private
    final static String DROPBOX_INCOMING_DIRECTORY_PROPERTY = "dropbox-incoming-dir";

    private final IStorageProcessor delegate;

    private final IFileOperations fileOperations;

    private final File dropboxIncomingDir;

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

    private static IStorageProcessor createDelegateStorageProcessor(Properties properties)
    {
        String delegateClass = (String) properties.get(DELEGATE_PROCESSOR_CLASS_PROPERTY);
        return createClass(IStorageProcessor.class, delegateClass);
    }

    private final File getDropboxIncomingDir(Properties properties)
    {
        return getDirectory(DROPBOX_INCOMING_DIRECTORY_PROPERTY, properties);
    }

    private File getDirectory(String propertyName, Properties properties)
    {
        String filePath = getMandatoryProperty(propertyName, properties);
        File file = new File(filePath);
        if (fileOperations.isDirectory(file) == false)
        {
            throw ConfigurationFailureException.fromTemplate(
                    "The directory '%s' set for '%s' property does not exist.", filePath,
                    propertyName);
        }
        return file;
    }

    private static final String getMandatoryProperty(final String propertyKey, Properties properties)
    {
        return PropertyUtils.getMandatoryProperty(properties, propertyKey);
    }

    private final static <T> T createClass(final Class<T> superClazz, String className)
    {
        try
        {
            return ClassUtils.create(superClazz, className);
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
        String destinationFileName =
                createDropboxDestinationFileName(dataSetInformation, incomingDataSetDirectory);
        try
        {
            fileOperations.copyToDirectoryAs(incomingDataSetDirectory, dropboxIncomingDir,
                    destinationFileName);
        } catch (IOExceptionUnchecked ex)
        {
            String destPath = new File(dropboxIncomingDir, destinationFileName).getPath();
            throw EnvironmentFailureException.fromTemplate("Cannot copy '%s' to '%s': %s.",
                    incomingDataSetDirectory.getPath(), destPath, ex.getMessage());
        }
        return storeData;
    }

    private static String createDropboxDestinationFileName(DataSetInformation dataSetInformation,
            File incomingDataSetDirectory)
    {
        String dataSetCode = dataSetInformation.getDataSetCode();
        String originalName = incomingDataSetDirectory.getName();
        String newFileName =
                stripFileName(originalName) + "_" + dataSetCode + stripFileExtension(originalName);
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
