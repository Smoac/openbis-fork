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

package ch.systemsx.cisd.common.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.EnumSet;

/**
 * Methods for performing an intensity rescaling of images.
 * 
 * @author Bernd Rinn
 */
public class IntensityRescaling
{

    /** The largest number a unsigned short can store. */
    private final static int MAX_USHORT = (1 << 16) - 1;

    public static enum Channel
    {
        RED(0, 16), GREEN(1, 8), BLUE(2, 0);

        private int band;

        private int shift;

        private Channel(int band, int shift)
        {
            this.band = band;
            this.shift = shift;
        }

        public int getShift()
        {
            return shift;
        }

        public int getBand()
        {
            return band;
        }
    }

    /**
     * The levels in a distribution of pixel values for a given symmetric quantile value.
     */
    public static class Levels
    {
        final int minLevel;

        final int maxLevel;

        public Levels(int minLevel, int maxLevel)
        {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        /**
         * The minimal level (black point).
         */
        public int getMinLevel()
        {
            return minLevel;
        }

        /**
         * The maximal level (white point).
         */
        public int getMaxLevel()
        {
            return maxLevel;
        }

        @Override
        public String toString()
        {
            return "MinMax [minLevel=" + minLevel + ", maxLevel=" + maxLevel + "]";
        }

    }

    /**
     * A class to store the histogram of pixel values.
     */
    public static class PixelHistogram
    {
        final int[] histogram = new int[MAX_USHORT + 1];

        int pixelCount = 0;

        /**
         * Returns the total number of pixels in the histogram.
         */
        public int getPixelCount()
        {
            return pixelCount;
        }

        /**
         * Returns the histogram. Changing the returned object changes the pixel histogram.
         */
        public int[] getHistogram()
        {
            return histogram;
        }
    }

    private static int getGrayIntensity(BufferedImage image, int x, int y)
    {
        return image.getRaster().getSample(x, y, 0);
    }

    private static int getBitShiftLowerThanThreshold(int[] b0, float pixels, float threshold)
    {
        int shift = b0.length - 1;
        while (shift >= 0 && (b0[shift] / pixels) < threshold)
        {
            --shift;
        }
        return shift + 1;
    }

    /**
     * Computes the number of significant bits in an image, minus 8. A bit position is considered
     * significant if only a small fraction (given by <var>threshold</var> of all pixels has a value
     * of 1 in this bit position.
     * <p>
     * For example, if the image is 16-bit and only uses 10-bits, this method will return 2.
     * 
     * @param image The image to compute the bits for.
     * @param threshold The threshold of pixels (divided by the total number of pixels) that can be
     *            '1' in bit position so that the bit-position is still consider insignificant. A
     *            typical value will be 0.001f (one per-mill).
     * @return The number of significant bits of the intensity minus 8.
     */
    public static int computeBitShift(BufferedImage image, float threshold)
    {
        if (isNotGrayscale(image))
        {
            throw new IllegalArgumentException(
                    "computeBitShift() is only applicable to gray scale images.");
        }
        float pixels = image.getWidth() * image.getHeight();
        final int[] b0 = new int[image.getColorModel().getPixelSize() - 8];
        for (int y = 0; y < image.getHeight(); ++y)
        {
            for (int x = 0; x < image.getWidth(); ++x)
            {
                final int intensity = getGrayIntensity(image, x, y);
                for (int b = 0; b < b0.length; ++b)
                {
                    if (((intensity >>> (b + 8)) & 1) == 1)
                    {
                        ++b0[b];
                    }
                }
            }
        }
        return getBitShiftLowerThanThreshold(b0, pixels, threshold);
    }

    /** @return true if the specified image in not in grayscale */
    public static boolean isNotGrayscale(BufferedImage image)
    {
        return image.getColorModel().getColorSpace().getNumComponents() > 1;
    }

    /** @return Used rgb channels in an image. Returns empty set if image type not RGB */
    public static EnumSet<Channel> getUsedRgbChannels(BufferedImage image)
    {
        EnumSet<Channel> channels = EnumSet.noneOf(Channel.class);

        if (image.getType() != BufferedImage.TYPE_INT_RGB
                && image.getType() != BufferedImage.TYPE_INT_ARGB)
        {
            return channels;
        }

        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                int rgb = image.getRGB(x, y);
                if (((rgb >> 16) & 0xff) > 0)
                {
                    channels.add(Channel.RED);
                }
                if (((rgb >> 8) & 0xff) > 0)
                {
                    channels.add(Channel.GREEN);
                }
                if ((rgb & 0xff) > 0)
                {
                    channels.add(Channel.BLUE);
                }
            }
        }
        return channels;
    }

    /**
     * Performs an intensity rescaling on a gray-scale image by shifting all intensities so that
     * only significant bits are kept. A bit position is considered significant if only a small
     * fraction (given by <var>threshold</var> of all pixels has a value of 1 in this bit position.
     * 
     * @param image The original n-bit gray-scale image (n>8).
     * @param threshold The threshold of pixels (divided by the total number of pixels) that can be
     *            '1' in bit position so that the bit-position is still consider insignificant. A
     *            typical value will be 0.001f (one per-mill).
     * @return The rescaled 8-bit gray-scale image.
     */
    public static BufferedImage rescaleIntensityBitShiftTo8Bits(BufferedImage image, float threshold)
    {
        return rescaleIntensityBitShiftTo8Bits(image, computeBitShift(image, threshold));
    }

    /**
     * Performs an intensity rescaling on a gray-scale image by shifting all intensities by
     * <var>shiftBits</var> bits.
     * 
     * @param image The original n-bit gray-scale image (n>8).
     * @param shiftBits The number of bits to shift the image by.
     * @return The rescaled 8-bit gray-scale image.
     */
    public static BufferedImage rescaleIntensityBitShiftTo8Bits(BufferedImage image, int shiftBits)
    {
        return rescaleIntensityBitShiftTo8Bits(new Pixels(image), shiftBits);
    }

    /** See {@link #rescaleIntensityBitShiftTo8Bits(BufferedImage, int)}. */
    private static BufferedImage rescaleIntensityBitShiftTo8Bits(Pixels image, int shiftBits)
    {
        final BufferedImage rescaledImage =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = rescaledImage.getRaster();

        int[] pixelData = image.getPixelData()[0];
        int offset = 0;
        for (int y = 0; y < image.getHeight(); ++y)
        {
            for (int x = 0; x < image.getWidth(); ++x)
            {
                final int intensity = Math.min(255, pixelData[offset++] >>> shiftBits);
                raster.setSample(x, y, 0, intensity);
            }
        }
        return rescaledImage;
    }

    /**
     * Process <var>image</var> and add its pixels to the <var>histogram</var>. Calling this method
     * multiple times with the same <var>histogram</var> accumulates the histogram for all images.
     * 
     * @param image a gray scale image (will not be checked)
     */
    public static void addToLevelStats(PixelHistogram histogram, BufferedImage image)
    {
        Pixels pixels = new Pixels(image);
        addToLevelStats(histogram, pixels);
    }

    /**
     * Process <var>image</var> and add its pixels to the <var>histogram</var>. Calling this method
     * multiple times with the same <var>histogram</var> accumulates the histogram for all images.
     */
    private static void addToLevelStats(PixelHistogram histogram, Pixels pixels)
    {
        int[] histogramArray = histogram.getHistogram();
        int[][] pixelData = pixels.getPixelData();
        for (int[] channelPixelData : pixelData)
        {
            for (int i = 0; i < channelPixelData.length; i++)
            {
                histogramArray[channelPixelData[i]]++;
            }
            histogram.pixelCount += channelPixelData.length;
        }
    }

    /**
     * Extracts and stores all pixels of an image. It is usually more efficient to fetch
     * all the pixels from {@link BufferedImage} because then accessing all the pixels one by one is
     * much faster due to compiler optimizations.
     */
    public static class Pixels
    {
        private final int width;

        private final int height;

        private final int[][] pixelData;

        public Pixels(BufferedImage image)
        {
            width = image.getWidth();
            height = image.getHeight();
            ColorModel colorModel = image.getColorModel();
            pixelData = new int[colorModel.getNumColorComponents()][width * height];
            WritableRaster raster = image.getRaster();
            int numberOfBands = raster.getNumBands();
            if (colorModel instanceof IndexColorModel == false && numberOfBands >= pixelData.length)
            {
                for (int band = 0; band < pixelData.length; band++)
                {
                    raster.getSamples(0, 0, width, height, band, pixelData[band]);
                }
            } else
            {
                // In case of an IndexColorModel or
                // number of bands is less then number of color components
                // we can not use the fast method 
                for (int y = 0; y < height; y++)
                {
                    int offset = y * width;
                    for (int x = 0; x < width; x++)
                    {
                        int rgb = image.getRGB(x, y);
                        for (Channel channel : Channel.values())
                        {
                            pixelData[channel.getBand()][offset + x] = (rgb >> channel.getShift()) & 0xff;
                        }
                    }
                }
            }
        }
        
        /** @return all the pixels of the image */
        public int[][] getPixelData()
        {
            return pixelData;
        }

        /** @return width of the image */
        public int getWidth()
        {
            return width;
        }

        /** @return height of the image */
        public int getHeight()
        {
            return height;
        }

    }

    /**
     * Computes the levels (black point and white point) of the given <var>histogram</var> for the
     * given symmetric <var>threshold</var>. The tail of the histogram below the black point will
     * contain the fraction of <var>treshold</var> of all pixels of the histogram. The same is true
     * for the tail of the <var>histogram</var> above the white point.
     * 
     * @return The levels of the <var>histogram</var> for the <var>treshold</var>.
     */
    public static Levels computeLevels(PixelHistogram histogram, float threshold)
    {
        final int intThreshold = Math.round(threshold * histogram.getPixelCount());
        int[] histogramArray = histogram.getHistogram();

        return computeLevels(intThreshold, histogramArray);
    }

    public static Levels computeLevels(final int intThreshold, int[] histogramArray)
    {
        int min = -1;
        int max = histogramArray.length;

        int minSum = 0;
        int maxSum = 0;

        while (minSum <= intThreshold || maxSum <= intThreshold)
        {
            if (minSum <= intThreshold)
            {
                minSum += histogramArray[++min];
            }
            if (min >= max - 1)
            {
                break;
            }
            if (maxSum <= intThreshold)
            {
                maxSum += histogramArray[--max];
            }
        }

        if (max >= histogramArray.length)
        {
            max = histogramArray.length - 1;
        }
        return new Levels(min, max);
    }
    
    /**
     * Calculates levels for the specified image. It starts with a threshold 0.01 (i.e. cut-off of points
     * to be too light or dark). If the number of levels is less than the specified minimum the threshold
     * is reduced by a factor 10 and the levels are calculated again. This iteration is continue until
     * either the number of levels is large enough or the threshold is below 10<sup>-5</sup>.
     */
    public static Levels computeLevels(BufferedImage image, int minimumNumberOfLevels)
    {
        Pixels pixels = new Pixels(image);
        Levels levels = null;
        for (float threshold = 0.01f; threshold > 1e-5; threshold /= 10)
        {
            levels = computeLevels(pixels, threshold);
            if (levels.getMaxLevel() - levels.getMinLevel() > minimumNumberOfLevels)
            {
                break;
            }
        }
        return levels;
    }

    /**
     * Computes the levels (black point and white point) of the given <var>image</var> for the given
     * symmetric <var>threshold</var>. The tail of the histogram below the black point will contain
     * the fraction of <var>treshold</var> of all pixels of the <var>image</var>. The same is true
     * for the tail of the histogram above the white point.
     * 
     * @param image a gray scale image (will not be checked).
     * @return The levels of the <var>histogram</var> for the <var>treshold</var>.
     */
    public static Levels computeLevels(Pixels image, float threshold)
    {
        final PixelHistogram stats = new PixelHistogram();
        addToLevelStats(stats, image);
        return computeLevels(stats, threshold);
    }

    /**
     * Computes an intensity rescaled image from the given <var>image</var>, using the black point
     * and white point as defined by <var>levels</var>. 
     */
    public static BufferedImage rescaleIntensityLevelTo8Bits(BufferedImage image, Levels levels)
    {
        return rescaleIntensityLevelTo8Bits(new Pixels(image), levels, Channel.values());
    }

    public static BufferedImage rescaleIntensityLevelTo8Bits(Pixels pixels, Levels levels,
            Channel... channels)
    {
        final int width = pixels.getWidth();
        final int height = pixels.getHeight();
        int[][] pixelData = pixels.getPixelData();
        int numberOfColorComponents = pixelData.length;
        int type = numberOfColorComponents == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB;
        final BufferedImage rescaledImage = new BufferedImage(width, height, type);
        WritableRaster rescaledRaster = rescaledImage.getRaster();

        for (Channel channel : channels)
        {
            int band = channel.getBand();
            if (band >= numberOfColorComponents)
            {
                break;
            }
            for (int y = 0; y < height; ++y)
            {
                int offset = y * width;
                for (int x = 0; x < width; ++x)
                {
                    int originalIntensity = pixelData[band][offset + x];
                    int rescaledIntensity = rescaleIntensity(originalIntensity, levels);
                    rescaledRaster.setSample(x, y, band, rescaledIntensity);
                }
            }
        }
        return rescaledImage;
    }

    private static int rescaleIntensity(int originalIntensity, Levels levels)
    {
        // cut all intensities above the white point
        int intensity = Math.min(levels.maxLevel, originalIntensity);
        // cut all intensities below the black point and move the origin to 0
        intensity = Math.max(0, intensity - levels.minLevel);
        // normalize to [0, 1] and rescale to 8 bits
        int range = levels.maxLevel - levels.minLevel;
        return (255 * intensity + range / 2) / range;
    }
}