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

package ch.systemsx.cisd.etlserver;


import java.io.File;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Storage processor which is able to create a copy of incoming data for additional processing. The
 * copy has a changed name to trace back the dataset to which the original data belong.
 * <p>
 * The processor uses following properties: {@link #DELEGATE_PROCESSOR_CLASS_PROPERTY},
 * {@link #DROPBOX_INCOMING_DIRECTORY_PROPERTY} and {@link #DATASET_CODE_SEPARATOR_PROPERTY}. All
 * the properties are also passed for the default processor.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class StorageProcessorWithDropbox extends AbstractDelegatingStorageProcessorWithDropbox
{
    /**
     * The path to the directory where an additional copy of the original incoming data will be
     * created for additional processing.
     */
    public final static String DROPBOX_INCOMING_DIRECTORY_PROPERTY = "dropbox-dir";

    private final File dropboxIncomingDir;

    public StorageProcessorWithDropbox(Properties properties)
    {
        this(properties, createDelegateStorageProcessor(properties), FileOperations.getInstance());
    }

    @Private
    StorageProcessorWithDropbox(Properties properties, IStorageProcessor delegateStorageProcessor,
            IFileOperations fileOperations)
    {
        super(properties, delegateStorageProcessor, fileOperations);
        this.dropboxIncomingDir = tryGetDirectory(DROPBOX_INCOMING_DIRECTORY_PROPERTY, properties);
    }

    @Private
    static IStorageProcessor createDelegateStorageProcessor(Properties properties)
    {
        String delegateClass = getMandatoryProperty(properties, DELEGATE_PROCESSOR_CLASS_PROPERTY);
        Properties p =
                ExtendedProperties.getSubset(properties, DELEGATE_PROCESSOR_CLASS_PROPERTY + ".",
                        true);
        return createClass(IStorageProcessor.class, delegateClass, p);
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

    @Override
    protected File tryGetDropboxDir(File originalData, DataSetInformation dataSetInformation)
    {
        return dropboxIncomingDir;
    }

}
