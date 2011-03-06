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

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * An <code>abtract</code> implementation of <code>IStorageProcessor</code>.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractStorageProcessor implements IStorageProcessorTransactional
{
    private static final String[] ZIP_FILE_EXTENSIONS =
        { "zip" };

    protected final Properties properties;

    private File storeRootDir;

    protected AbstractStorageProcessor(final Properties properties)
    {
        this.properties = properties;
    }

    protected final String getMandatoryProperty(final String propertyKey)
    {
        return PropertyUtils.getMandatoryProperty(properties, propertyKey);
    }

    protected static final void checkParameters(final File incomingDataSetPath,
            final File targetPath)
    {
        assert incomingDataSetPath != null : "Given incoming data set path can not be null.";
        assert targetPath != null : "Given target path can not be null.";
    }

    //
    // IStorageProcessorTransactional
    //

    public final File getStoreRootDirectory()
    {
        return storeRootDir;
    }

    public final void setStoreRootDirectory(final File storeRootDirectory)
    {
        this.storeRootDir = storeRootDirectory;
    }

    /**
     * @see IStorageProcessorTransactional#getStorageFormat()
     */
    public StorageFormat getStorageFormat()
    {
        return StorageFormat.PROPRIETARY;
    }

    protected static boolean isZipFile(File file)
    {
        if (file.isDirectory())
        {
            return false;
        }
        String fileExtension = FilenameUtils.getExtension(file.getName());
        for (String currentExt : ZIP_FILE_EXTENSIONS)
        {
            if (currentExt.equalsIgnoreCase(fileExtension))
            {
                return true;
            }
        }
        return false;
    }
}
