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

package ch.systemsx.cisd.imagereaders.ij;

import ij.io.Opener;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.imagereaders.AbstractImageReader;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;

/**
 * Implementation of {@link IImageReader} using ImageJ under the hood.
 * <p>
 * Currently, only the only supported image format for this library is TIFF. In the future we can
 * add support for other image types.
 * 
 * @author Kaloyan Enimanev
 */
public class ImageJReaderLibrary implements IImageReaderLibrary
{

    private static final String TIFF_READER = "tiff";

    private static final List<String> TIFF_FILE_EXTS = Arrays.asList("tiff", "tif");

    private final IImageReader TIFF_IMAGE_READER = new AbstractImageReader(getName(), TIFF_READER)
        {
            public BufferedImage readImage(IRandomAccessFile handle, IReadParams params)
                    throws IOExceptionUnchecked
            {
                AdapterIInputStreamToInputStream is = new AdapterIInputStreamToInputStream(handle);
                return new Opener().openTiff(is, "").getBufferedImage();
            }
        };

    public String getName()
    {
        return ImageReaderConstants.IMAGEJ_LIBRARY;
    }

    public List<String> getReaderNames()
    {
        return Collections.singletonList(TIFF_READER);
    }

    public IImageReader tryGetReader(String readerName)
    {
        return tryGetReaderForExtension(readerName);
    }

    public IImageReader tryGetReaderForFile(String fileName)
    {
        String fileExt = FilenameUtils.getExtension(fileName);
        return tryGetReaderForExtension(fileExt);
    }

    private IImageReader tryGetReaderForExtension(String fileExtension)
    {
        if (fileExtension != null)
        {
            String lowerCaseExt = fileExtension.toLowerCase();
            if (TIFF_FILE_EXTS.contains(lowerCaseExt))
            {
                return TIFF_IMAGE_READER;
            }
        }
        return null;
    }


}
