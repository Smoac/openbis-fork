/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.hdf5;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.hdf5.HDF5FactoryProvider;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.io.HDF5IOAdapterFactory;

/**
 * An implementation of {@link IHDF5ContainerReader}.
 * 
 * @author Bernd Rinn
 */
final class HDF5ContainerReader implements IHDF5ContainerReader
{
    private final IHDF5Reader innerReader;

    HDF5ContainerReader(final File hdf5Container)
    {
        this.innerReader = HDF5FactoryProvider.get().openForReading(hdf5Container);
    }

    public void close()
    {
        innerReader.close();
    }

    public boolean exists(String objectPath)
    {
        return innerReader.exists(objectPath);
    }

    public boolean isGroup(String objectPath)
    {
        return innerReader.isGroup(objectPath);
    }

    public List<String> getGroupMembers(String groupPath)
    {
        return innerReader.getGroupMembers(groupPath);
    }

    public void readFromHDF5Container(String objectPath, OutputStream ostream)
    {
        final InputStream istream = HDF5IOAdapterFactory.asInputStream(innerReader, objectPath);
        Exception e = null;
        try
        {
            IOUtils.copyLarge(istream, ostream);
        } catch (IOException ex)
        {
            e = ex;
            throw new IOExceptionUnchecked(ex);
        } finally
        {
            try
            {
                istream.close();
            } catch (IOException ex)
            {
                if (e == null)
                {
                    throw new IOExceptionUnchecked(ex);
                }
            }
        }
    }
}