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

import ch.systemsx.cisd.hdf5.HDF5GenericStorageFeatures;
import ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class CompressingHdf5WriterWrapper implements IHDF5ContainerWriter
{
    private final IHDF5Writer writer;

    // Store this, though I'm not yet using it
    @SuppressWarnings("unused")
    private final HDF5GenericStorageFeatures genericStorageFeatures;

    private final HDF5IntStorageFeatures intStorageFeatures;
    
    CompressingHdf5WriterWrapper(Hdf5Container parent, IHDF5Writer writer, boolean compress)
    {
        this.writer = writer;
        if (compress)
        {
            this.genericStorageFeatures = HDF5GenericStorageFeatures.GENERIC_DEFLATE;
            this.intStorageFeatures = HDF5IntStorageFeatures.INT_DEFLATE;
        } else
        {
            this.genericStorageFeatures = HDF5GenericStorageFeatures.GENERIC_CONTIGUOUS;
            this.intStorageFeatures = HDF5IntStorageFeatures.INT_CONTIGUOUS;
        }
    }

    /**
     * @param objectPath
     * @param data
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleWriter#writeByteArray(java.lang.String, byte[])
     */
    public void writeByteArray(String objectPath, byte[] data)
    {
        writer.writeByteArray(objectPath, data, intStorageFeatures);
    }

    /**
     * @see ch.systemsx.cisd.hdf5.IHDF5SimpleReader#close()
     */
    public void close()
    {
        writer.close();
    }

}
