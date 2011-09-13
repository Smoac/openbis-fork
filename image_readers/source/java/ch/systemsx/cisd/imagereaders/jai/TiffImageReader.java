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

package ch.systemsx.cisd.imagereaders.jai;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageID;

/**
 * JAI {@link IImageReader} for TIFF files.
 * 
 * @author Kaloyan Enimanev
 */
class TiffImageReader extends DefaultImageReader
{

    public TiffImageReader(String libraryName, String readerName)
    {
        super(libraryName, readerName);
    }

    @Override
    public BufferedImage readImage(IRandomAccessFile handle, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked
    {
        try
        {
            InputStream input = new AdapterIInputStreamToInputStream(handle);
            ImageDecoder decoder = ImageCodec.createImageDecoder(getName(), input, null);
            Raster raster = decoder.decodeAsRaster(imageID.getTimeSeriesIndex());
            int bufferType = findBestImageBufferType(raster);
            BufferedImage image =
                    new BufferedImage(raster.getWidth(), raster.getHeight(), bufferType);
            image.setData(raster);
            return image;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private int findBestImageBufferType(Raster raster)
    {
        boolean isGrayscale = raster.getNumBands() == 1;
        int numberOfBits = raster.getSampleModel().getSampleSize()[0];
        if (isGrayscale)
        {
            if (numberOfBits <= 8)
            {
                return BufferedImage.TYPE_BYTE_GRAY;
            } else
            {
                return BufferedImage.TYPE_USHORT_GRAY;
            }
        } else
        {
            return BufferedImage.TYPE_INT_RGB;
        }
    }

}