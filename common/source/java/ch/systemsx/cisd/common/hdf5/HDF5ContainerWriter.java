/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import ch.systemsx.cisd.hdf5.io.HDF5IOAdapterFactory;

/**
 * An implementation of {@link IHDF5ContainerWriter}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class HDF5ContainerWriter implements IHDF5ContainerWriter
{
    private final static int KB = 1024;

    private final static int MB = 1024 * KB;

    private static final int COMPACT_SIZE_LIMIT = 8 * KB;

    private static final String OPAQUE_TAG_FILE = "FILE";

    final static int BUFFER_SIZE = 10 * MB;

    private final IHDF5Writer writer;

    private final HDF5GenericStorageFeatures genericStorageFeatures;

    HDF5ContainerWriter(HDF5Container parent, IHDF5Writer writer, boolean compress)
    {
        this.writer = writer;
        if (compress)
        {
            this.genericStorageFeatures = HDF5GenericStorageFeatures.GENERIC_DEFLATE;
        } else
        {
            this.genericStorageFeatures = HDF5GenericStorageFeatures.GENERIC_CHUNKED;
        }
    }

    public void writeToHDF5Container(String objectPath, InputStream istream, long size)
            throws IOExceptionUnchecked
    {
        final OutputStream ostream;
        if (size <= COMPACT_SIZE_LIMIT)
        {
            ostream =
                    HDF5IOAdapterFactory.asOutputStream(writer, objectPath,
                            HDF5GenericStorageFeatures.GENERIC_COMPACT_DELETE, (int) size,
                            OPAQUE_TAG_FILE);
        } else
        {
            ostream =
                    HDF5IOAdapterFactory.asOutputStream(writer, objectPath, genericStorageFeatures,
                            (int) Math.min(size, BUFFER_SIZE), OPAQUE_TAG_FILE);
        }
        IOException e = null;
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
                ostream.close();
            } catch (IOException ex)
            {
                if (e == null)
                {
                    throw new IOExceptionUnchecked(ex);
                }
            }
        }
    }

    /**
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#close()
     */
    public void close()
    {
        writer.close();
    }

}
