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

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Storage processor which delegates all the tasks to the storage processor specified in
 * configuration with {@link #DELEGATE_PROCESSOR_CLASS_PROPERTY} property.
 * <p>
 * This class is supposed to be extended to add specific functionality which has to be performed
 * besides the basic operations.
 * </p>
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDelegatingStorageProcessor implements IStorageProcessor
{
    /**
     * Property name which is used to specify the class of the default storage processor, to which
     * all calls are delegated.
     */
    protected final static String DELEGATE_PROCESSOR_CLASS_PROPERTY = "processor";

    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractDelegatingStorageProcessor.class);

    private final IStorageProcessor delegate;

    protected AbstractDelegatingStorageProcessor(Properties properties)
    {
        this(createDelegateStorageProcessor(properties));
    }

    protected AbstractDelegatingStorageProcessor(IStorageProcessor delegateStorageProcessor)
    {
        this.delegate = delegateStorageProcessor;
    }

    @Private
    static IStorageProcessor createDelegateStorageProcessor(Properties properties)
    {
        String delegateClass =
                PropertyUtils.getMandatoryProperty(properties, DELEGATE_PROCESSOR_CLASS_PROPERTY);
        Properties p =
                ExtendedProperties.getSubset(properties, DELEGATE_PROCESSOR_CLASS_PROPERTY + ".",
                        true);
        return createClass(IStorageProcessor.class, delegateClass, p);
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
    // delegation
    //

    public File storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir)
    {
        return delegate.storeData(dataSetInformation, typeExtractor, mailClient,
                incomingDataSetDirectory, rootDir);
    }

    public void commit()
    {
        delegate.commit();
    }

    public UnstoreDataAction rollback(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        return delegate.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
    }

    public StorageFormat getStorageFormat()
    {
        return delegate.getStorageFormat();
    }

    public File tryGetProprietaryData(final File storedDataDirectory)
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
