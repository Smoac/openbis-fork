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

package ch.systemsx.cisd.etlserver.hdf5;

import java.io.File;

import ch.systemsx.cisd.hdf5.HDF5FactoryProvider;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;

/**
 * A class that provides high-level access to HDF5 content. In particular, it provides access to a
 * {@link IHDF5SimpleWriter} that transparently compresses byte arrays added by
 * {@link IHDF5SimpleWriter#writeByteArray}
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class Hdf5Container
{
    private final File hdf5Container;

    /**
     * An interface for classes that want to write to HDF5 files.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IHdf5WriterClient
    {
        /**
         * Run code using a writer. Implementations do <b>not</b> need to close the writer.
         */
        public void runWithSimpleWriter(IHDF5SimpleWriter writer);
    }

    /**
     * An interface for classes that want to read from HDF5 files.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IHdf5ReaderClient
    {
        /**
         * Run code using a reader. Implementations do <b>not</b> need to close the reader.
         */
        public void runWithSimpleReader(IHDF5SimpleReader reader);
    }

    /**
     * Constructor.
     * 
     * @param hdf5Container A file designated to be the hdf5 container. The file need not exist --
     *            it will be created when a writer is accessed.
     */
    public Hdf5Container(File hdf5Container)
    {
        this.hdf5Container = hdf5Container;
    }

    public File getHdf5File()
    {
        return hdf5Container;
    }

    /**
     * Create a new simple reader on the HDF5 container.
     * 
     * @return A new IHDF5SimpleReader
     */
    public IHDF5SimpleReader createSimpleReader()
    {
        return HDF5FactoryProvider.get().openForReading(hdf5Container);
    }

    /**
     * Create a new simple writer on the HDF5 container. If isContentCompressed is true, the data
     * written by {@link IHDF5SimpleWriter#writeByteArray} will be transparently compressed.
     * 
     * @param isContentCompressed Pass in true to have byte arrays transparently compressed.
     * @return A new IHDF5SimpleWriter
     */
    private IHDF5SimpleWriter createSimpleWriter(boolean isContentCompressed)
    {
        if (isContentCompressed)
        {
            return new CompressingHdf5WriterWrapper(this, HDF5FactoryProvider.get().open(
                    hdf5Container));
        } else
        {
            return HDF5FactoryProvider.get().open(hdf5Container);
        }
    }

    /**
     * Run a writer client on this Hdf5 container. Ensures that the writer is closed when the client
     * finishes running.
     */
    public void runWriterClient(boolean isContentCompressed, IHdf5WriterClient client)
    {
        IHDF5SimpleWriter writer = createSimpleWriter(isContentCompressed);
        try
        {
            client.runWithSimpleWriter(writer);
        } finally
        {
            writer.close();
        }
    }

    /**
     * Run a reader client on this Hdf5 container. Ensures that the reader is closed when the client
     * finishes running.
     */
    public void runReaderClient(IHdf5ReaderClient client)
    {
        IHDF5SimpleReader reader = createSimpleReader();
        try
        {
            client.runWithSimpleReader(reader);
        } finally
        {
            reader.close();
        }
    }
}
