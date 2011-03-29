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
import ch.systemsx.cisd.imagereaders.AbstractImageReader;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.TiffReadParams;

/**
 * JAI {@link IImageReader} for TIFF files.
 * 
 * @author Kaloyan Enimanev
 */
class TiffImageReader extends AbstractImageReader
{

    public TiffImageReader(String libraryName, String readerName)
    {
        super(libraryName, readerName);
    }


    public BufferedImage readImage(IRandomAccessFile handle, IReadParams params)
            throws IOExceptionUnchecked
    {
        int page = 0;
        boolean allow16BitGrayscaleModel = false;
        TiffReadParams readParams = (TiffReadParams) params;
        if (readParams != null)
        {
            page = readParams.getPage();
            allow16BitGrayscaleModel = readParams.isAllow16BitGrayscaleModel();
        }

        try
        {
            InputStream input = new AdapterIInputStreamToInputStream(handle);
            ImageDecoder decoder = ImageCodec.createImageDecoder(getName(), input, null);
            Raster raster = decoder.decodeAsRaster(page);
            int bufferType = findBestImageBufferType(raster, allow16BitGrayscaleModel);
            BufferedImage image =
                    new BufferedImage(raster.getWidth(), raster.getHeight(), bufferType);
            image.setData(raster);
            return image;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private int findBestImageBufferType(Raster raster, boolean allow16BitGrayscaleModel)
    {
        boolean is16BitGrayscale =
                raster.getNumBands() == 1 && raster.getSampleModel().getSampleSize()[0] == 16;
        return is16BitGrayscale && allow16BitGrayscaleModel ? BufferedImage.TYPE_USHORT_GRAY
                : BufferedImage.TYPE_INT_RGB;
    }

}