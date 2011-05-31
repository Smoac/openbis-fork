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

import static ch.systemsx.cisd.common.utilities.DataTypeUtil.GIF_FILE;
import static ch.systemsx.cisd.common.utilities.DataTypeUtil.JPEG_FILE;
import static ch.systemsx.cisd.common.utilities.DataTypeUtil.PNG_FILE;
import static ch.systemsx.cisd.common.utilities.DataTypeUtil.TIFF_FILE;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.HierarchicalNodeBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.common.utilities.DataTypeUtil;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.imagereaders.ReadParams;

/**
 * Utility function on images.
 * 
 * @author Franz-Josef Elmer
 */
public class ImageUtil
{
    private static final Set<String> FILE_TYPES = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("gif", "jpg", "jpeg", "png", "tif", "tiff")));

    private static interface ImageLoader
    {
        public BufferedImage load(IRandomAccessFile raf, ImageID imageID);
    }

    private static final class TiffImageLoader implements ImageLoader
    {
        private final static int MAX_READ_AHEAD = 30000000;

        public BufferedImage load(IRandomAccessFile handle, ImageID imageID)
        {
            handle.mark(MAX_READ_AHEAD);
            try
            {
                return loadJavaAdvancedImagingTiff(handle, imageID, false);
            } catch (RuntimeException ex)
            {
                if (imageID.equals(ImageID.NULL))
                {
                    handle.reset();
                    // There are some TIFF files which cannot be opened by JAI, try ImageJ
                    // instead...
                    return loadWithImageJ(handle);
                } else
                {
                    throw ex;
                }
            }
        }

        private BufferedImage loadWithImageJ(IRandomAccessFile handle)
        {
            IImageReader imageReader =
                    ImageReaderFactory.tryGetReader(ImageReaderConstants.IMAGEJ_LIBRARY, "tiff");
            return imageReader.readImage(handle, ImageID.NULL, null);

        }
    }

    /**
     * For experts only! Loads some kinds of TIFF images handled by JAI library.
     * 
     * @param allow16BitGrayscaleModel if true and the image is 16 bit grayscale, then the
     *            appropriate buffered imaged type will be used, otherwise the image will be
     *            converted to 24 bits RGB. Useful if access to original pixel values is needed.
     */
    public static BufferedImage loadJavaAdvancedImagingTiff(IRandomAccessFile handle,
            ImageID imageID, boolean allow16BitGrayscaleModel)
            throws EnvironmentFailureException
    {
        IImageReader imageReader =
                ImageReaderFactory.tryGetReader(ImageReaderConstants.JAI_LIBRARY, "tiff");
        if (imageReader == null)
        {
            throw EnvironmentFailureException
                    .fromTemplate("Cannot find JAI image decoder for TIFF files.");
        }

        ReadParams readParams = new ReadParams();
        readParams.setAllow16BitGrayscaleModel(allow16BitGrayscaleModel);
        try
        {
            return imageReader.readImage(handle, imageID, readParams);
        } catch (Exception ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot decode image.", ex);
        }
    }

    private static final class JavaImageLoader implements ImageLoader
    {
        private final String fileType;

        JavaImageLoader(String fileType)
        {
            this.fileType = fileType;
        }

        public BufferedImage load(IRandomAccessFile handle, ImageID imageID)
        {
            if (imageID.equals(ImageID.NULL))
            {
                IImageReader imageReader =
                        ImageReaderFactory.tryGetReader(ImageReaderConstants.IMAGEIO_LIBRARY,
                                fileType);
                if (imageReader == null)
                {
                    throw EnvironmentFailureException.fromTemplate(
                            "Cannot find ImageIO reader for file type '%s'", fileType);
                }
                return imageReader.readImage(handle, ImageID.NULL, null);
            } else
            {
                throw new UnsupportedOperationException();
            }
        }

    }

    private static final Map<String, ImageLoader> imageLoaders = new HashMap<String, ImageLoader>();

    static
    {
        imageLoaders.put(GIF_FILE, new JavaImageLoader(GIF_FILE));
        imageLoaders.put(JPEG_FILE, new JavaImageLoader(JPEG_FILE));
        imageLoaders.put(PNG_FILE, new JavaImageLoader(PNG_FILE));
        imageLoaders.put(TIFF_FILE, new TiffImageLoader());
    }

    /**
     * Returns <code>true</code> if the specified file is a supported image file. Supported formats
     * are GIF, JPG, PNG, TIFF. Only file type is taken into account for figuring out the image
     * format. Following file types are recognized:
     * <code>.gif, .jpg, .jpeg, .png, .tif, .tiff</code>
     */
    public static boolean isImageFile(File file)
    {
        String fileName = file.getName();
        String fileType = FilenameUtils.getExtension(fileName);
        return fileType != null && FILE_TYPES.contains(fileType.toLowerCase());
    }

    /**
     * Loads the image specified by <var>imageIdOrNull</var> from the given </var>inputStream</var>.
     * Supported images formats are GIF, JPG, PNG, and TIFF. The input stream will be closed after
     * loading. 
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number
     *             identifying supported image format.
     */
    public static BufferedImage loadImage(IContent content, String imageIdOrNull,
            String imageLibraryNameOrNull, String imageLibraryReaderNameOrNull, IReadParams params)
    {
        assert (imageLibraryReaderNameOrNull == null || imageLibraryNameOrNull != null) : "if image reader "
                + "is specified then library name should be specified as well";
        ImageID imageID = parseImageID(imageIdOrNull);
        if (imageLibraryNameOrNull != null && imageLibraryReaderNameOrNull != null)
        {
            IImageReader reader =
                    ImageReaderFactory.tryGetReader(imageLibraryNameOrNull,
                            imageLibraryReaderNameOrNull);
            if (reader != null)
            {
                return reader.readImage(content.getReadOnlyRandomAccessFile(), imageID, params);
            }
        }
        return loadImageGuessingLibrary(content, imageID);
    }

    /**
     * Parses specified string representation of an {@link ImageID}. If the argument is
     * <code>null</code> {@link ImageID#NULL} will be returned.
     */
    public static ImageID parseImageID(String imageIdOrNull)
    {
        return imageIdOrNull == null ? ImageID.NULL : ImageID.parse(imageIdOrNull);
    }

    private static BufferedImage loadImageGuessingLibrary(IContent content, ImageID imageID)
    {
        IRandomAccessFile handle = content.getReadOnlyRandomAccessFile();
        String fileType = DataTypeUtil.tryToFigureOutFileTypeOf(handle);
        return loadImageGuessingLibrary(handle, fileType, imageID);
    }

    /**
     * Loads the image specified by <var>imageID</var> from the image from the given
     * </var>handle</var>. Supported images formats are GIF, JPG, PNG, and TIFF. The input stream
     * will be closed after loading.
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number
     *             identifying supported image format.
     */
    private static BufferedImage loadImageGuessingLibrary(IRandomAccessFile handle,
            String fileType, ImageID imageID)
    {
        try
        {
            if (fileType == null)
            {
                throw new IllegalArgumentException(
                        "File type of an image input stream couldn't be determined.");
            }
            ImageLoader imageLoader = imageLoaders.get(fileType);
            if (imageLoader == null)
            {
                throw new IllegalArgumentException("Unable to load image of file type '" + fileType
                        + "'.");
            }
            return imageLoader.load(handle, imageID);
        } finally
        {
            closeQuietly(handle);
        }
    }

    /**
     * Only for tests
     */
    @Private
    static BufferedImage loadImage(File file)
    {
        if (file.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }
        return loadImage(new FileBasedContent(file));
    }

    /**
     * Only for tests.
     */
    @Private
    static BufferedImage loadImage(IContent content)
    {
        return loadImage(content, null, null, null, null);
    }

    /**
     * Loads an image from specified file node. Supported file formats are GIF, JPG, PNG, and TIFF.
     * 
     * @throws IllegalArgumentException if the file isn't a valid image file.
     */
    public static BufferedImage loadImage(IHierarchicalContentNode fileNode)
    {
        if (fileNode.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + fileNode.getRelativePath());
        }
        return loadImage(new HierarchicalNodeBasedContent(fileNode));
    }

    /**
     * Re-scales the image to be the biggest one which fits into a (0,0,maxWidth, maxHeight)
     * rectangle. Preserves the aspect ratio. If the rectangle is bigger than the image does
     * nothing. Ignores alpha channel of the original image.
     * 
     * @param maxWidth Maximum width of the result image.
     * @param maxHeight Maximum height of the result image.
     */
    public static BufferedImage createThumbnail(BufferedImage image, int maxWidth, int maxHeight)
    {
        return rescale(image, maxWidth, maxHeight, true, false);
    }

    /**
     * Re-scales the image to be the biggest one which fits into a (0,0,maxWidth, maxHeight)
     * rectangle. Preserves the aspect ratio. If the rectangle is bigger than the image and
     * 'enlargeIfNecessary' is false then nothing is done.
     * 
     * @param maxWidth Maximum width of the result image.
     * @param maxHeight Maximum height of the result image.
     * @param enlargeIfNecessary if false and the image has smaller width and height than the
     *            specified limit, then the image is not changed.
     * @param highQuality if true thumbnails will be of higher quality, but rescaling will take
     *            longer (BICUBIC rescaling will be used instead of BILINEAR).
     */
    public static BufferedImage rescale(BufferedImage image, int maxWidth, int maxHeight,
            boolean enlargeIfNecessary, boolean highQuality)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        // the image has already the required size
        if ((width == maxWidth && maxHeight >= height)
                || (height == maxHeight && maxWidth >= width))
        {
            return image;
        }
        double widthScale = maxWidth / (double) width;
        double heightScale = maxHeight / (double) height;
        double scale = Math.min(widthScale, heightScale);
        // image is smaller than required
        if (enlargeIfNecessary == false && scale > 1)
        {
            return image;
        }
        int thumbnailWidth = (int) (scale * width + 0.5);
        int thumbnailHeight = (int) (scale * height + 0.5);

        // preserve alpha channel if it was present before
        int imageType =
                image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB
                        : BufferedImage.TYPE_INT_RGB;
        BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, imageType);
        Graphics2D graphics2D = thumbnail.createGraphics();
        Object renderingHint =
                highQuality ? RenderingHints.VALUE_INTERPOLATION_BICUBIC
                        : RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderingHint);
        graphics2D.drawImage(image, 0, 0, thumbnailWidth, thumbnailHeight, null);
        return thumbnail;
    }

    private static void closeQuietly(IRandomAccessFile handle)
    {
        try
        {
            handle.close();
        } catch (Exception ex)
        {
            // keep quiet
        }
    }

}
