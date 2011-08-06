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

package ch.systemsx.cisd.common.io;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.hdf5.HDF5FactoryProvider;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.io.HDF5DataSetRandomAccessFile;
import ch.systemsx.cisd.hdf5.io.HDF5IOAdapterFactory;

/**
 * An {@link IContent} implementation based on an HDF5 dataset.
 * 
 * @author Bernd Rinn
 */
public class HDF5DataSetBasedContent implements IContent, Closeable
{
    private final File hdf5File;

    private final String dataSetPath;

    private final String name;

    private final boolean exists;

    private final long size;

    private final List<HDF5DataSetRandomAccessFile> randomAccessFiles;

    public HDF5DataSetBasedContent(File hdf5File, String dataSetPath)
    {
        this.hdf5File = hdf5File;
        this.dataSetPath = dataSetPath;
        this.name = FileUtilities.getFileNameFromRelativePath(dataSetPath);
        final IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(hdf5File);
        if (reader.exists(dataSetPath) && reader.isDataSet(dataSetPath))
        {
            this.exists = true;
            this.size = reader.getSize(dataSetPath);
        } else
        {
            this.exists = false;
            this.size = 0L;
        }
        reader.close();
        this.randomAccessFiles = new ArrayList<HDF5DataSetRandomAccessFile>();
    }

    public String tryGetName()
    {
        return name;
    }

    public long getSize()
    {
        return size;
    }

    public boolean exists()
    {
        return exists;
    }

    public IRandomAccessFile getReadOnlyRandomAccessFile()
    {
        final HDF5DataSetRandomAccessFile randomAccessFile =
                HDF5IOAdapterFactory.asRandomAccessFileReadOnly(hdf5File, dataSetPath);
        randomAccessFiles.add(randomAccessFile);
        return randomAccessFile;
    }

    public InputStream getInputStream()
    {
        return new AdapterIInputStreamToInputStream(getReadOnlyRandomAccessFile());
    }

    public void close()
    {
        for (HDF5DataSetRandomAccessFile raFile : randomAccessFiles)
        {
            raFile.close();
        }
    }

}
