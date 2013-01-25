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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codecimpl.GIFImageDecoder;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.AdapterIInputStreamToInputStream;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.imagereaders.AbstractImageReader;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageConvertionUtils;
import ch.systemsx.cisd.imagereaders.ImageID;

/**
 * Default implementation of JAI {@link IImageReader}.
 * 
 * @author Kaloyan Enimanev
 */
class DefaultImageReader extends AbstractImageReader
{

    public DefaultImageReader(String libraryName, String readerName)
    {
        super(libraryName, readerName);
    }

    @Override
    public List<ImageID> getImageIDs(IRandomAccessFile handle) throws IOExceptionUnchecked
    {
        try
        {
            InputStream input = new AdapterIInputStreamToInputStream(handle);
            List<ImageID> ids = new ArrayList<ImageID>();
            ImageDecoder decoder = ImageCodec.createImageDecoder(getName(), input, null);
            int numPages;
            if (decoder instanceof GIFImageDecoder)
            {
                numPages = 1;
            } else
            {
                numPages = decoder.getNumPages();
            }
            for (int i = 0; i < numPages; i++)
            {
                ids.add(new ImageID(0, i, 0, 0));
            }
            return ids;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public BufferedImage readImage(IRandomAccessFile handle, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked
    {
        try
        {
            InputStream input = new AdapterIInputStreamToInputStream(handle);
            ImageDecoder decoder = ImageCodec.createImageDecoder(getName(), input, null);
            RenderedImage renderedImage =
                    decoder.decodeAsRenderedImage(imageID.getTimeSeriesIndex());
            return ImageConvertionUtils.convertToBufferedImage(renderedImage);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public Dimension readDimensions(IRandomAccessFile handle, ImageID imageID)
    {
        try
        {
            InputStream input = new AdapterIInputStreamToInputStream(handle);
            ImageDecoder decoder = ImageCodec.createImageDecoder(getName(), input, null);
            RenderedImage renderedImage =
                    decoder.decodeAsRenderedImage(imageID.getTimeSeriesIndex());
            return new Dimension(renderedImage.getWidth(), renderedImage.getHeight());
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public Integer readColorDepth(IRandomAccessFile handle, ImageID imageID)
    {
        try
        {
            InputStream input = new AdapterIInputStreamToInputStream(handle);
            ImageDecoder decoder = ImageCodec.createImageDecoder(getName(), input, null);
            RenderedImage renderedImage =
                    decoder.decodeAsRenderedImage(imageID.getTimeSeriesIndex());
            return renderedImage.getColorModel().getPixelSize();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
}