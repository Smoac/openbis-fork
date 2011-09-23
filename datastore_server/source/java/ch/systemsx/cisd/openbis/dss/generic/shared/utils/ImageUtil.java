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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLine;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.PngFilterType;
import ar.com.hjg.pngj.PngWriter;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.GrayscalePixels;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.io.hierarchical_content.HierarchicalNodeBasedContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.DataTypeUtil;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;

/**
 * Utility function on images.
 * 
 * @author Franz-Josef Elmer
 */
public class ImageUtil
{
    /**
     * When a grayscale image with color depth > 8 bits has to be displayed and user has not decided
     * how it should be converted, then this threshold will be used.
     */
    public static final float DEFAULT_IMAGE_OPTIMAL_RESCALING_FACTOR = 0.001f;

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ImageUtil.class);

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
                return loadJavaAdvancedImagingTiff(handle, imageID);
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
     */
    public static BufferedImage loadJavaAdvancedImagingTiff(IRandomAccessFile handle,
            ImageID imageID) throws EnvironmentFailureException
    {
        IImageReader imageReader =
                ImageReaderFactory.tryGetReader(ImageReaderConstants.JAI_LIBRARY, "tiff");
        if (imageReader == null)
        {
            throw EnvironmentFailureException
                    .fromTemplate("Cannot find JAI image decoder for TIFF files.");
        }

        try
        {
            return imageReader.readImage(handle, imageID, null);
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
     * <p>
     * Note that the original color depth will be kept, so e.g. 12 or 16 bit grayscale images will
     * not be converted to RGB.
     * 
     * @throws IllegalArgumentException if the input stream doesn't start with a magic number
     *             identifying supported image format.
     */
    public static BufferedImage loadUnchangedImage(IContent content, String imageIdOrNull,
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
                IRandomAccessFile handle = content.getReadOnlyRandomAccessFile();
                try
                {
                    return reader.readImage(handle, imageID, params);
                } finally
                {
                    closeQuietly(handle);
                }
            }
        }
        return loadImageGuessingLibrary(content, imageID);
    }

    /**
     * Converts the given <var>image</var> to a PNG image. Uses fast parameters for the filter and
     * deflate level (no filter and no deflation).
     * <p>
     * <b>This method is about 7 times faster than
     * {@link ImageIO#write(java.awt.image.RenderedImage, String, java.io.OutputStream)} and should
     * be preferred whenever speed is important.</b>
     * 
     * @param image The image to convert to the PNG <code>byte[]</code>.
     * @return The bytes of the uncompressed PNG.
     */
    public static byte[] imageToPngFast(BufferedImage image)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeImageToPngFast(image, out);
        return out.toByteArray();
    }

    /**
     * Converts the given <var>image</var> to a PNG image and writes it to the given output stream.
     * Uses fast parameters for the filter and deflate level (no filter and no deflation).
     * <p>
     * <b>This method is about 7 times faster than
     * {@link ImageIO#write(java.awt.image.RenderedImage, String, java.io.OutputStream)} and should
     * be preferred whenever speed is important.</b>
     * 
     * @param image The image to write to the output stream.
     * @param out The output stream to write the png converted image to.
     */
    public static void writeImageToPngFast(BufferedImage image, OutputStream out)
    {
        writeImageToPng(image, out, PngFilterType.FILTER_NONE, 0);
    }

    /**
     * Converts the given <var>image</var> to a PNG image. Uses default parameters for the filter
     * and deflate level.
     * <p>
     * <b>This method is about 7 times faster than
     * {@link ImageIO#write(java.awt.image.RenderedImage, String, java.io.OutputStream)} and should
     * be preferred whenever speed is important.</b>
     * 
     * @param image The image to convert to the PNG <code>byte[]</code>.
     * @return The bytes of the uncompressed PNG.
     */
    public static byte[] imageToPng(BufferedImage image)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeImageToPng(image, out);
        return out.toByteArray();
    }

    /**
     * Converts the given <var>image</var> to a PNG image and writes it to the given output stream.
     * Uses default parameters for the filter and deflate level.
     * <p>
     * <b>This method is about 3 times faster than
     * {@link ImageIO#write(java.awt.image.RenderedImage, String, java.io.OutputStream)} and should
     * be preferred whenever speed is important.</b>
     * 
     * @param image The image to write to the output stream.
     * @param out The output stream to write the png converted image to.
     */
    public static void writeImageToPng(BufferedImage image, OutputStream out)
    {
        writeImageToPng(image, out, PngFilterType.FILTER_DEFAULT, 6);
    }

    /**
     * Converts the given <var>image</var> to a PNG image.
     * <p>
     * 
     * @param image The image to write to the output stream.
     * @param filterType The type of the filter (see <a
     *            href="http://www.w3.org/TR/PNG-Filters.html">PNG filters</a>) to apply when
     *            converting to PNG, <code>null</code> means {@link PngFilterType#FILTER_DEFAULT}.
     * @param compressionLevel the compression level for the deflation filter of the PNG conversion,
     *            from -1 to 9. 0 means no compression, 9 means maximal compression, -1 means 6
     *            which is the default deflation level.
     * @return The bytes of the uncompressed PNG.
     */
    public static byte[] imageToPng(BufferedImage image, PngFilterType filterType,
            int compressionLevel)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeImageToPng(image, out, filterType, compressionLevel);
        return out.toByteArray();
    }

    /**
     * Converts the given <var>image</var> to a PNG image and writes it to the given output stream.
     * 
     * @param image The image to write to the output stream.
     * @param out The output stream to write the png converted image to
     * @param filterType The type of the filter (see <a
     *            href="http://www.w3.org/TR/PNG-Filters.html">PNG filters</a>) to apply when
     *            converting to PNG, <code>null</code> means {@link PngFilterType#FILTER_DEFAULT}.
     * @param compressionLevel the compression level for the deflation filter of the PNG conversion,
     *            from -1 to 9. 0 means no compression, 9 means maximal compression, -1 means 6
     *            which is the default deflation level.
     */
    public static void writeImageToPng(BufferedImage image, OutputStream out,
            PngFilterType filterType, int compressionLevel)
    {
        final int cols = image.getWidth();
        final int rows = image.getHeight();
        int bitDepth = image.getColorModel().getComponentSize(0);
        boolean hasAlpha = false; // NOTE: it would be nice to support alpha channel
        boolean isGrayscale = isGrayscale(image);
        ImageInfo imgInfo = new ImageInfo(cols, rows, bitDepth, hasAlpha, isGrayscale, false);
        PngWriter png = new PngWriter(out, imgInfo);
        png.setFilterType(filterType == null ? PngFilterType.FILTER_DEFAULT : filterType);
        png.setCompLevel(compressionLevel == -1 ? 6 : compressionLevel);
        ImageLine imageLine = new ImageLine(imgInfo);
        for (int row = 0; row < rows; ++row)
        {
            if (isGrayscale)
            {
                fillGrayscaleLine(image, cols, isGrayscale, imageLine, row);
            } else
            {
                fillRGBLine(image, cols, isGrayscale, imageLine, row);
            }
            imageLine.setRown(row);
            png.writeRow(imageLine);
        }
        png.end();
    }

    private static void fillGrayscaleLine(BufferedImage image, final int cols, boolean isGrayscale,
            ImageLine imageLine, int row)
    {
        for (int col = 0; col < cols; ++col)
        {
            imageLine.scanline[col] = image.getRaster().getSample(col, row, 0);
        }
    }

    private static void fillRGBLine(BufferedImage image, final int cols, boolean isGrayscale,
            ImageLine imageLine, int row)
    {
        for (int col = 0; col < cols; ++col)
        {
            int pixel = image.getRGB(col, row);
            ImageLineHelper.setPixelRGB8(imageLine, col, pixel);
        }
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
        return loadUnchangedImage(content, null, null, null, null);
    }

    /**
     * Loads an image from specified file node. Supported file formats are GIF, JPG, PNG, and TIFF.
     * 
     * @throws IllegalArgumentException if the file isn't a valid image file.
     */
    public static BufferedImage loadImageForDisplay(IHierarchicalContentNode fileNode)
    {
        if (fileNode.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + fileNode.getRelativePath());
        }
        BufferedImage result = loadImage(new HierarchicalNodeBasedContent(fileNode));
        result = convertForDisplayIfNecessary(result);
        return result;
    }

    /**
     * Re-scales the image to be the biggest one which fits into a (0,0,maxWidth, maxHeight)
     * rectangle. Preserves the aspect ratio. If the rectangle is bigger than the image does
     * nothing.
     * <p>
     * If the specified image uses grayscale with color depth larger then 8 bits, conversion to 8
     * bits grayscale is done.
     * </p>
     * 
     * @param maxWidth Maximum width of the result image.
     * @param maxHeight Maximum height of the result image.
     */
    public static BufferedImage createThumbnailForDisplay(BufferedImage image, int maxWidth,
            int maxHeight)
    {
        BufferedImage result = rescale(image, maxWidth, maxHeight, true, false);
        result = convertForDisplayIfNecessary(result);
        return result;
    }

    /**
     * If the specified image uses grayscale with color depth larger then 8 bits, conversion to 8
     * bits grayscale is done. Otherwise the original image is returned.
     */
    public static BufferedImage convertForDisplayIfNecessary(BufferedImage image)
    {
        if (isGrayscale(image))
        {
            if (image.getColorModel().getPixelSize() > 8)
            {
                GrayscalePixels pixels = new GrayscalePixels(image);
                Levels intensityRange =
                        IntensityRescaling.computeLevels(pixels,
                                DEFAULT_IMAGE_OPTIMAL_RESCALING_FACTOR);
                BufferedImage result =
                        IntensityRescaling.rescaleIntensityLevelTo8Bits(pixels, intensityRange);
                return result;
            }
        }
        return image;
    }

    private static boolean isGrayscale(BufferedImage image)
    {
        return image.getColorModel().getColorSpace().getNumComponents() == 1;
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

        boolean isTransparent = image.getColorModel().hasAlpha();
        int imageType = image.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM)
        {
            imageType = isTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        }
        BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, imageType);
        Graphics2D graphics2D = thumbnail.createGraphics();
        Object renderingHint =
                highQuality ? RenderingHints.VALUE_INTERPOLATION_BICUBIC
                        : RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderingHint);
        graphics2D.drawImage(image, 0, 0, thumbnailWidth, thumbnailHeight, null);
        graphics2D.dispose();
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
