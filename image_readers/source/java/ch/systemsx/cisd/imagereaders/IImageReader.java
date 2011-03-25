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

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * {@link IImageReader} can read images in a particular format (e.g. TIFF, JPG, GIF etc.). To obtain
 * the correct {@link IImageReader} instance use the API of {@link ImageReaderFactory}.
 * 
 * @author Bernd Rinn
 */
public interface IImageReader
{
    /**
     * Return the name of the library for this reader.
     */
    public String getLibraryName();

    /**
     * Return the name of the reader.
     */
    public String getName();

    /**
     * Reads a {@link BufferedImage} from a {@link File}.
     * 
     * @param file the image file
     * @param page specified the image page to be read (in case of multiple pages)
     */
    public BufferedImage readImage(File file, int page) throws IOExceptionUnchecked;

    /**
     * Reads a {@link BufferedImage} from a byte array.
     * 
     * @param bytes the image file as a byte array
     * @param page specified the image page to be read (in case of multiple pages)
     */
    public BufferedImage readImage(byte[] bytes, int page);

    /**
     * Reads a {@link BufferedImage} from a handle.
     * 
     * @param handle the image file as {@link IRandomAccessFile}
     * @param page specified the image page to be read (in case of multiple pages)
     */
    public BufferedImage readImage(IRandomAccessFile handle, int page)
            throws IOExceptionUnchecked;
}
