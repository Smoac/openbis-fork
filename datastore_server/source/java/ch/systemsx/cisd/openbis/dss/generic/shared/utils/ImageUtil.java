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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.exception.EnvironmentFailureException;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.GrayscalePixels;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Utility function on images.
 * 
 * @author Franz-Josef Elmer
 */
public class ImageUtil
{
    public static final String TIFF_FILE = "tif";

    public static final String PNG_FILE = "png";

    public static final String JPEG_FILE = "jpg";

    public static final String GIF_FILE = "gif";

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

        public Dimension readDimension(IRandomAccessFile handle, ImageID imageID);
    }

    private static final class MagicNumber
    {
        private final String fileType;

        private final String[] magicHexNumbers;

        private final int maxLength;

        MagicNumber(String fileType, String... magicHexNumbers)
        {
            this.fileType = fileType;
            this.magicHexNumbers = magicHexNumbers;
            int length = 0;
            for (String magicNumber : magicHexNumbers)
            {
                length = Math.max(length, magicNumber.length());
            }
            maxLength = length / 2;
        }

        public String getFileType()
        {
            return fileType;
        }

        int getMaxLength()
        {
            return maxLength;
        }

        public boolean matches(byte[] bytes)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < maxLength; i++)
            {
                byte b = bytes[i];
                builder.append(Integer.toHexString((b >> 4) & 0xf));
                builder.append(Integer.toHexString(b & 0xf));
            }
            String initialBytes = builder.toString().toLowerCase();
            for (String magicNumber : magicHexNumbers)
            {
                if (initialBytes.startsWith(magicNumber))
                {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class MagicNumbersManager
    {
        private final MagicNumber[] magicNumbers;

        MagicNumbersManager(MagicNumber... magicNumbers)
        {
            this.magicNumbers = magicNumbers;
        }

        int getMaxLength()
        {
            int max = 0;
            for (MagicNumber magicNumber : magicNumbers)
            {
                max = Math.max(max, magicNumber.getMaxLength());
            }
            return max;
        }

        String tryToFigureOutFileTypeOf(byte[] initialBytes)
        {
            for (MagicNumber magicNumber : magicNumbers)
            {
                if (magicNumber.matches(initialBytes))
                {
                    return magicNumber.getFileType();
                }
            }
            return null;
        }
    }

    private static final MagicNumbersManager MAGIC_NUMBERS_MANAGER =
            new MagicNumbersManager(new MagicNumber(GIF_FILE, "474946383961", "474946383761"),
                    new MagicNumber(JPEG_FILE, "ffd8ff"), new MagicNumber(PNG_FILE,
                            "89504e470d0a1a0a"), new MagicNumber(TIFF_FILE, "49492a00", "4d4d002a"));

    private static final class TiffImageLoader implements ImageLoader
    {
        private final static int MAX_READ_AHEAD = 30000000;

        @Override
        public BufferedImage load(IRandomAccessFile handle, ImageID imageID)
        {
            handle.mark(MAX_READ_AHEAD);
            try
            {
                return loadWithBioFormats(handle, imageID);
            } catch (RuntimeException ex1)
            {
                try
                {
                    return loadJavaAdvancedImagingTiff(handle, imageID);
                } catch (RuntimeException ex2)
                {
                    if (imageID.equals(ImageID.NULL))
                    {
                        handle.reset();
                        // There are some TIFF files which cannot be opened by JAI, try ImageJ
                        // instead...
                        return loadWithImageJ(handle);
                    } else
                    {
                        throw ex2;
                    }
                }
            }
        }

        @Override
        public Dimension readDimension(IRandomAccessFile handle, ImageID imageID)
        {
            handle.mark(MAX_READ_AHEAD);
            try
            {
                return loadDimensionWithBioFormats(handle, imageID);
            } catch (RuntimeException ex1)
            {
                try
                {
                    return loadDimensionJavaAdvancedImagingTiff(handle, imageID);
                } catch (RuntimeException ex2)
                {
                    if (imageID.equals(ImageID.NULL))
                    {
                        handle.reset();
                        // There are some TIFF files which cannot be opened by JAI, try ImageJ
                        // instead...
                        return loadDimensionWithImageJ(handle);
                    } else
                    {
                        throw ex2;
                    }
                }
            }
        }
    }

    private static BufferedImage loadWithImageJ(IRandomAccessFile handle)
    {
        return loadWithLibrary(handle, ImageID.NULL, ImageReaderConstants.IMAGEJ_LIBRARY, "tiff");
    }

    private static Dimension loadDimensionWithImageJ(IRandomAccessFile handle)
    {
        return loadDimensionWithLibrary(handle, ImageID.NULL, ImageReaderConstants.IMAGEJ_LIBRARY,
                "tiff");
    }

    private static BufferedImage loadWithBioFormats(IRandomAccessFile handle, ImageID imageID)
    {
        return loadWithLibrary(handle, imageID, ImageReaderConstants.BIOFORMATS_LIBRARY,
                "TiffDelegateReader");
    }

    private static Dimension loadDimensionWithBioFormats(IRandomAccessFile handle, ImageID imageID)
    {
        return loadDimensionWithLibrary(handle, imageID, ImageReaderConstants.BIOFORMATS_LIBRARY,
                "TiffDelegateReader");
    }

    /**
     * For experts only! Loads some kinds of TIFF images handled by JAI library.
     */
    public static BufferedImage loadJavaAdvancedImagingTiff(IRandomAccessFile handle,
            ImageID imageID) throws EnvironmentFailureException
    {
        return loadWithLibrary(handle, imageID, ImageReaderConstants.JAI_LIBRARY, "tiff");
    }

    /**
     * For experts only! Loads some kinds of TIFF images handled by JAI library.
     */
    public static Dimension loadDimensionJavaAdvancedImagingTiff(IRandomAccessFile handle,
            ImageID imageID) throws EnvironmentFailureException
    {
        return loadDimensionWithLibrary(handle, imageID, ImageReaderConstants.JAI_LIBRARY, "tiff");
    }

    private static BufferedImage loadWithLibrary(IRandomAccessFile handle, ImageID imageIDOrNull,
            String libraryName, String readerName)
    {
        operationLog.debug("Load tiff image using " + libraryName);
        IImageReader imageReader = ImageReaderFactory.tryGetReader(libraryName, readerName);
        if (imageReader == null)
        {
            throw new IllegalStateException(String.format(
                    "There is no reader '%s' in image library '%s'.", readerName, libraryName));
        }
        try
        {
            return imageReader.readImage(handle, imageIDOrNull, null);
        } catch (Exception ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot decode image.", ex);
        }
    }

    private static Dimension loadDimensionWithLibrary(IRandomAccessFile handle,
            ImageID imageIDOrNull, String libraryName, String readerName)
    {
        operationLog.debug("Load tiff image using " + libraryName);
        IImageReader imageReader = ImageReaderFactory.tryGetReader(libraryName, readerName);
        if (imageReader == null)
        {
            throw new IllegalStateException(String.format(
                    "There is no reader '%s' in image library '%s'.", readerName, libraryName));
        }
        try
        {
            return imageReader.readDimensions(handle, imageIDOrNull);
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

        @Override
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

        @Override
        public Dimension readDimension(IRandomAccessFile handle, ImageID imageID)
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
                return imageReader.readDimensions(handle, imageID);
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
    public static BufferedImage loadUnchangedImage(IHierarchicalContentNode contentNode,
            String imageIdOrNull, String imageLibraryNameOrNull,
            String imageLibraryReaderNameOrNull, IReadParams params)
    {
        assert (imageLibraryReaderNameOrNull == null || imageLibraryNameOrNull != null) : "if image reader "
                + "is specified then library name should be specified as well";
        ImageID imageID = parseImageID(imageIdOrNull, contentNode);

        if (imageLibraryNameOrNull != null && imageLibraryReaderNameOrNull != null)
        {
            IImageReader reader =
                    ImageReaderFactory.tryGetReader(imageLibraryNameOrNull,
                            imageLibraryReaderNameOrNull);
            if (reader != null)
            {
                IRandomAccessFile handle = contentNode.getFileContent();
                try
                {
                    return reader.readImage(handle, imageID, params);
                } finally
                {
                    closeQuietly(handle);
                }
            }
        }
        return loadImageGuessingLibrary(contentNode, imageID);
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
    public static Dimension loadUnchangedImageDimension(IHierarchicalContentNode contentNode,
            String imageIdOrNull, String imageLibraryNameOrNull, String imageLibraryReaderNameOrNull)
    {
        assert (imageLibraryReaderNameOrNull == null || imageLibraryNameOrNull != null) : "if image reader "
                + "is specified then library name should be specified as well";
        ImageID imageID = parseImageID(imageIdOrNull, contentNode);

        if (imageLibraryNameOrNull != null && imageLibraryReaderNameOrNull != null)
        {
            IImageReader reader =
                    ImageReaderFactory.tryGetReader(imageLibraryNameOrNull,
                            imageLibraryReaderNameOrNull);
            if (reader != null)
            {
                IRandomAccessFile handle = contentNode.getFileContent();
                try
                {
                    return reader.readDimensions(handle, imageID);
                } finally
                {
                    closeQuietly(handle);
                }
            }
        }
        return loadImageDimensionGuessingLibrary(contentNode, imageID);
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
        writeImageToPng(image, out, PngFilterType.FILTER_DEFAULT, 0);
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

    public static void writeImageUsingImageIO(BufferedImage image, OutputStream out, String format)
            throws IOExceptionUnchecked
    {
        try
        {
            ImageIO.write(image, format, out);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
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
    public static ImageID parseImageID(String imageIdOrNull, IHierarchicalContentNode contentNode)
    {
        ImageID id = imageIdOrNull == null ? ImageID.NULL : ImageID.parse(imageIdOrNull);
        try
        {
            final File fileOrNull = contentNode.tryGetFile();
            if (fileOrNull != null)
            {
                id.setFileName(fileOrNull.getCanonicalPath());
            }
        } catch (Exception ex)
        {
            operationLog.warn("Unable to set file name on image id. ", ex);
        }
        return id;
    }

    private static BufferedImage loadImageGuessingLibrary(IHierarchicalContentNode contentNode,
            ImageID imageID)
    {
        IRandomAccessFile handle = contentNode.getFileContent();
        String fileType = tryToFigureOutFileTypeOf(handle);
        return loadImageGuessingLibrary(handle, fileType, imageID);
    }

    private static Dimension loadImageDimensionGuessingLibrary(
            IHierarchicalContentNode contentNode, ImageID imageID)
    {
        IRandomAccessFile handle = contentNode.getFileContent();
        String fileType = tryToFigureOutFileTypeOf(handle);
        return loadImageDimensionGuessingLibrary(handle, fileType, imageID);
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

    private static Dimension loadImageDimensionGuessingLibrary(IRandomAccessFile handle,
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
            return imageLoader.readDimension(handle, imageID);
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
        return loadImage(new FileBasedContentNode(file));
    }

    /**
     * Only for tests.
     */
    @Private
    static BufferedImage loadImage(IHierarchicalContentNode contentNode)
    {
        return loadUnchangedImage(contentNode, null, null, null, null);
    }

    /**
     * Loads an image from specified file node. Supported file formats are GIF, JPG, PNG, and TIFF.
     * 
     * @throws IllegalArgumentException if the file isn't a valid image file.
     */
    public static BufferedImage loadImageForDisplay(IHierarchicalContentNode contentNode)
    {
        if (contentNode.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: "
                    + contentNode.getRelativePath());
        }
        BufferedImage result = loadImage(contentNode);
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
        return convertForDisplayIfNecessary(image, DEFAULT_IMAGE_OPTIMAL_RESCALING_FACTOR);
    }

    /**
     * If the specified image uses grayscale with color depth larger then 8 bits, conversion to 8
     * bits grayscale is done. Otherwise the original image is returned.
     */
    public static BufferedImage convertForDisplayIfNecessary(BufferedImage image, float threshold)
    {
        if (isGrayscale(image))
        {
            if (image.getColorModel().getPixelSize() > 8)
            {
                GrayscalePixels pixels = new GrayscalePixels(image);
                Levels intensityRange = IntensityRescaling.computeLevels(pixels, threshold);
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
     * @param highQuality8Bit if true thumbnails will be of higher quality, but rescaling will take
     *            longer and the image will be converted to 8 bit.
     */
    public static BufferedImage rescale(BufferedImage image, int maxWidth, int maxHeight,
            boolean enlargeIfNecessary, boolean highQuality8Bit)
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

        BufferedImage thumbnail =
                createNewEmptyImage(image, highQuality8Bit, thumbnailWidth, thumbnailHeight);

        Graphics2D graphics2D = thumbnail.createGraphics();
        BufferedImage imageToRescale = image;
        if (highQuality8Bit)
        {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            // WORKAROUND: non-default interpolations do not work well with 16 bit grayscale images.
            // We have to rescale colors to 8 bit here, otherwise the result will contain only few
            // colors.
            imageToRescale = convertForDisplayIfNecessary(imageToRescale, 0);
        }
        graphics2D.drawImage(imageToRescale, 0, 0, thumbnailWidth, thumbnailHeight, null);
        graphics2D.dispose();
        return thumbnail;
    }

    private static BufferedImage createNewEmptyImage(BufferedImage image, boolean highQuality8Bit,
            int thumbnailWidth, int thumbnailHeight)
    {
        boolean isTransparent = image.getColorModel().hasAlpha();
        int imageType = image.getType();
        if (highQuality8Bit)
        {
            imageType =
                    imageType == BufferedImage.TYPE_USHORT_GRAY ? BufferedImage.TYPE_BYTE_GRAY
                            : BufferedImage.TYPE_INT_RGB;
        } else if (imageType == BufferedImage.TYPE_CUSTOM)
        {
            imageType = isTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        } else if (imageType == BufferedImage.TYPE_BYTE_INDEXED)
        {
            imageType = BufferedImage.TYPE_INT_RGB;
        }
        BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, imageType);
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

    /**
     * Tries to figure out the file type of the specified binary content. It uses the first few
     * bytes as a finger print (so-called 'magic numbers') as a heuristic to get the type of
     * content. Currently only the following types are recognized: <code>gif, jpg, png, tif</code>.
     * 
     * @param handle {@link IRandomAccessFile} which supports marking.
     * @return <code>null</code> if file type couldn't be figured out.
     */
    public static String tryToFigureOutFileTypeOf(IRandomAccessFile handle)
    {
        if (handle.markSupported() == false)
        {
            throw new IllegalArgumentException("Input stream does not support marking. "
                    + "Wrap input stream with a BufferedInputStream to solve the problem.");
        }
        int maxLength = MAGIC_NUMBERS_MANAGER.getMaxLength();
        handle.mark(maxLength);
        byte[] initialBytes = new byte[maxLength];
        handle.read(initialBytes);
        handle.reset();
        return MAGIC_NUMBERS_MANAGER.tryToFigureOutFileTypeOf(initialBytes);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a tiff file.
     */
    public static boolean isTiff(String fileTypeOrNull)
    {
        return TIFF_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a jpeg file.
     */
    public static boolean isJpeg(String fileTypeOrNull)
    {
        return JPEG_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a png file.
     */
    public static boolean isPng(String fileTypeOrNull)
    {
        return PNG_FILE.equals(fileTypeOrNull);
    }

    /**
     * Returns <code>true</code> if the <var>fileTypeOrNull</var> is a gif file.
     */
    public static boolean isGif(String fileTypeOrNull)
    {
        return GIF_FILE.equals(fileTypeOrNull);
    }

}
