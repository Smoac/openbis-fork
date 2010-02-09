/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.compression.tiff;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.compression.file.InPlaceCompressionMethod;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * A compression method for TIFF files using an <code>executable</code> utility with an option to
 * specify compression type.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractTiffCompressionMethod extends InPlaceCompressionMethod
{

    abstract protected String getExecutableName();

    abstract protected File getExecutable();

    private final String compressionType;

    public AbstractTiffCompressionMethod(String compressionType)
    {
        assert compressionType != null;
        this.compressionType = compressionType;
    }

    public String getCompressionType()
    {
        return compressionType;
    }

    public void setCompressionType(String compressionType)
    {

    }

    @Override
    final protected List<String> getAcceptedExtensions()
    {
        return Arrays.asList(".tif", ".tiff");
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        if (getExecutable() == null)
        {
            throw new ConfigurationFailureException("Cannot find executable of the "
                    + getExecutableName() + " utility.");
        }
    }

    public boolean isRemote()
    {
        return false;
    }

}
