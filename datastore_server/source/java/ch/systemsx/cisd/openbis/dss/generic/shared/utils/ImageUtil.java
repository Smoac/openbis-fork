/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.DataTypeUtil;

/**
 * Utility function on images.
 * 
 * @author Franz-Josef Elmer
 */
public class ImageUtil
{
    private static final Set<String> FILE_TYPES =
            Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("gif", "jpg", "jpeg",
                    "png", "tif", "tiff")));

    private static interface ImageLoader
    {
        public BufferedImage load(InputStream inputStream);

        public BufferedImage load(InputStream inputStream, int page);
    }

    private static final class TiffImageLoader implements ImageLoader
    {
        static final ImageLoader INSTANCE = new TiffImageLoader();

        public BufferedImage load(InputStream inputStream)
        {
            return load(inputStream, 0);
        }

        public BufferedImage load(InputStream inputStream, int page)
        {
            final ImageDecoder dec = ImageCodec.createImageDecoder("tiff", inputStream, null);
            Raster raster;
            try
            {
                raster = dec.decodeAsRaster(page);
            } catch (IOException ex)
            {
                throw EnvironmentFailureException.fromTemplate("Cannot decode image.", ex);
            }
            final BufferedImage image =
                    new BufferedImage(raster.getWidth(), raster.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
            image.setData(raster);
            return image;
        }
    }

    private static final class JavaImageLoader implements ImageLoader
    {
        static final ImageLoader INSTANCE = new JavaImageLoader();

        public BufferedImage load(InputStream inputStream)
        {
            try
            {
                return ImageIO.read(inputStream);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        public BufferedImage load(InputStream inputStream, int page)
        {
            if (page == 0)
            {
                return load(inputStream);
            } else
            {
                throw new UnsupportedOperationException();
            }
        }

    }

    private static final Map<String, ImageLoader> imageLoaders = new HashMap<String, ImageLoader>();

    static
    {
        imageLoaders.put(DataTypeUtil.GIF_FILE, JavaImageLoader.INSTANCE);
        imageLoaders.put(DataTypeUtil.JPEG_FILE, JavaImageLoader.INSTANCE);
        imageLoaders.put(DataTypeUtil.PNG_FILE, JavaImageLoader.INSTANCE);
        imageLoaders.put(DataTypeUtil.TIFF_FILE, TiffImageLoader.INSTANCE);
    }

    /**
     * Returns <code>true</code> if the specified file is a supported image file. Supported formats
     * are GIF, JPG, PNG, TIFF. Only file type is taken into account for figuring out the image
     * format. Following file types are recognized:
     * <code>.gif, .jpg, .jpeg, .png, .tif, .tiff</code>
     */
    public static boolean isImageFile(File file)
    {
        String name = file.getName();
        int lastIndexOfDot = name.lastIndexOf('.');
        if (lastIndexOfDot < 0)
        {
            return false;
        }
        String fileType = name.substring(lastIndexOfDot + 1).toLowerCase();
        return FILE_TYPES.contains(fileType);
    }

    /**
     * Loads an image from specified input stream. Supported images formats are GIF, JPG, PNG, and
     * TIFF. The input stream will be closed after loading.
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number
     *             identifying supported image format.
     */
    public static BufferedImage loadImage(InputStream inputStream)
    {
        return loadImage(inputStream, 0);
    }

    /**
     * Loads the specified <var>page</var> from the image from the tiven </var>inputStream</var>.
     * Supported images formats are GIF, JPG, PNG, and TIFF. The input stream will be closed after
     * loading. Note that only for TIFF files a <var>page</var> other than 0 may be specified. 
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number
     *             identifying supported image format.
     */
    public static BufferedImage loadImage(InputStream inputStream, int page)
    {
        InputStream markSupportingInputStream = inputStream;
        if (inputStream.markSupported() == false)
        {
            markSupportingInputStream = new BufferedInputStream(inputStream);
        }
        String fileType = DataTypeUtil.tryToFigureOutFileTypeOf(markSupportingInputStream);
        return loadImage(markSupportingInputStream, fileType, page);
    }
    
    /**
     * Loads the specified <var>page</var> from the image from the tiven </var>inputStream</var>.
     * Supported images formats are GIF, JPG, PNG, and TIFF. The input stream will be closed after
     * loading. Note that only for TIFF files a <var>page</var> other than 0 may be specified. 
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number
     *             identifying supported image format.
     */
    public static BufferedImage loadImage(InputStream inputStream, String fileType, int page)
    {
        try
        {
            if (fileType == null)
            {
                throw new IllegalArgumentException(
                        "File type of an image input stream couldn't be determined.");
            } else if (DataTypeUtil.isTiff(fileType) == false && page > 0)
            {
                throw new IllegalArgumentException("File type has to be 'tiff'.");
            }
            ImageLoader imageLoader = imageLoaders.get(fileType);
            if (imageLoader == null)
            {
                throw new IllegalArgumentException("Unable to load image of file type '" + fileType
                        + "'.");
            }
            return imageLoader.load(inputStream);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Loads an image from specified file. Supported file formats are GIF, JPG, PNG, and TIFF.
     * 
     * @throws IllegalArgumentException if either the file does not exist or it isn't a valid image
     *             file.
     */
    public static BufferedImage loadImage(File file)
    {
        if (file.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }
        try
        {
            FileSeekableStream inStream = new FileSeekableStream(file);
            return loadImage(inStream);
        } catch (IOException ex)
        {
            throw new IllegalArgumentException("Isn't a valid image file: "
                    + file.getAbsolutePath() + ". Error: " + ex.getMessage());
        }
    }

    /**
     * Creates a thumbnail from the specified image by preserving the aspect ratio.
     * 
     * @param maxWidth Maximum width of the thumbnail.
     * @param maxHeight Maximum height of the thumbnail.
     */
    public static BufferedImage createThumbnail(BufferedImage image, int maxWidth, int maxHeight)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        double widthScale = maxWidth / (double) width;
        double heightScale = maxHeight / (double) height;
        double scale = Math.min(1, Math.min(widthScale, heightScale));
        int thumbnailWidth = (int) (scale * width + 0.5);
        int thumbnailHeight = (int) (scale * height + 0.5);

        BufferedImage thumbnail =
                new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbnail.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbnailWidth, thumbnailHeight, null);
        return thumbnail;
    }
}
