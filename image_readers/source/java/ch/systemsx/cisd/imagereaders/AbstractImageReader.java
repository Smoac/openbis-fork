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

package ch.systemsx.cisd.imagereaders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;

/**
 * Abstract class that facilitates the implementations of {@link IImageReader}.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractImageReader implements IImageReader
{
    private final String libraryName;

    private final String readerName;

    public AbstractImageReader(String libraryName, String readerName)
    {
        this.libraryName = libraryName;
        this.readerName = readerName;
    }

    @Override
    public String getLibraryName()
    {
        return libraryName;
    }

    @Override
    public String getName()
    {
        return readerName;
    }

    @Override
    public final List<ImageID> getImageIDs(File file) throws IOExceptionUnchecked
    {
        RandomAccessFileImpl raf = new RandomAccessFileImpl(file, "r");
        try
        {
            return getImageIDs(raf);
        } finally
        {
            raf.close();
        }
    }

    @Override
    public final List<ImageID> getImageIDs(byte[] bytes)
    {
        return getImageIDs(new ByteBufferRandomAccessFile(bytes));
    }

    @Override
    public List<ImageID> getImageIDs(IRandomAccessFile handle) throws IOExceptionUnchecked
    {
        return Arrays.asList(ImageID.NULL);
    }

    @Override
    public BufferedImage readImage(File file, ImageID imageID, IReadParams params) throws IOExceptionUnchecked
    {
        IRandomAccessFile raf = new RandomAccessFileImpl(file, "r");
        try
        {
            return readImage(raf, imageID, params);
        } finally
        {
            raf.close();
        }
    }

    @Override
    public BufferedImage readImage(byte[] bytes, ImageID imageID, IReadParams params)
    {
        IRandomAccessFile raf = new ByteBufferRandomAccessFile(bytes);
        return readImage(raf, imageID, params);
    }

    @Override
    public boolean isMetaDataAware()
    {
        return false;
    }

    @Override
    public Map<String, Object> readMetaData(File file, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked
    {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> readMetaData(byte[] bytes, ImageID imageID, IReadParams params)
    {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> readMetaData(IRandomAccessFile handle, ImageID imageID, IReadParams params)
    {
        return Collections.emptyMap();
    }

    @Override
    public void close()
    {
    }
    
}
