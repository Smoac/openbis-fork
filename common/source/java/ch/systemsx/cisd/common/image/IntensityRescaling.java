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

/**
 * Methods for performing an intensity rescaling to 8bits for gray-scale images with a color-depth
 * of more than 8bits.
 * 
 * @author Bernd Rinn
 */
public class IntensityRescaling
{

    /** The largest number a unsigned short can store. */
    private final static int MAX_USHORT = (1 << 16) - 1;

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
        if (image.getColorModel().getColorSpace().getNumComponents() > 1)
        {
            throw new IllegalArgumentException(
                    "computeBitShift() is only applicable to gray scale images.");
        }
        float pixels = image.getWidth() * image.getHeight();
        final int[] b0 = new int[image.getColorModel().getPixelSize() - 8];
        for (int x = 0; x < image.getWidth(); ++x)
        {
            for (int y = 0; y < image.getHeight(); ++y)
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
        if (image.getColorModel().getColorSpace().getNumComponents() > 1)
        {
            throw new IllegalArgumentException(
                    "rescaleIntensityBitShiftTo8Bits() is only applicable to gray scale images.");
        }
        final BufferedImage rescaledImage =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < image.getWidth(); ++x)
        {
            for (int y = 0; y < image.getHeight(); ++y)
            {
                final int intensity = Math.min(255, getGrayIntensity(image, x, y) >>> shiftBits);
                rescaledImage.getRaster().setSample(x, y, 0, intensity);
            }
        }
        return rescaledImage;
    }

    /**
     * Process <var>image</var> and add its pixels to the <var>histogram</var>. Calling this method
     * multiple times with the same <var>histogram</var> accumulates the histogram for all images.
     */
    public static void addToLevelStats(PixelHistogram histogram, BufferedImage image)
    {
        if (image.getColorModel().getColorSpace().getNumComponents() > 1)
        {
            throw new IllegalArgumentException(
                    "addToLevelStats() is only applicable to gray scale images.");
        }
        final int width = image.getWidth();
        final int height = image.getHeight();
        for (int x = 0; x < width; ++x)
        {
            for (int y = 0; y < height; ++y)
            {
                histogram.histogram[getGrayIntensity(image, x, y)]++;
            }
        }
        histogram.pixelCount += width * height;
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
        final int intThreshold = Math.round(threshold * histogram.pixelCount);
        int min = -1;
        int sum = 0;
        while (sum <= intThreshold)
        {
            sum += histogram.histogram[++min];
        }
        if (min < 0)
        {
            min = 0;
        }
        int max = MAX_USHORT + 1;
        sum = 0;
        while (sum <= intThreshold)
        {
            sum += histogram.histogram[--max];
        }
        if (max > MAX_USHORT)
        {
            max = MAX_USHORT;
        }
        return new Levels(min, max);
    }

    /**
     * Computes the levels (black point and white point) of the given <var>image</var> for the given
     * symmetric <var>threshold</var>. The tail of the histogram below the black point will contain
     * the fraction of <var>treshold</var> of all pixels of the <var>image</var>. The same is true
     * for the tail of the histogram above the white point.
     * 
     * @return The levels of the <var>histogram</var> for the <var>treshold</var>.
     */
    public static Levels computeLevels(BufferedImage image, float threshold)
    {
        final PixelHistogram stats = new PixelHistogram();
        addToLevelStats(stats, image);
        return computeLevels(stats, threshold);
    }

    /**
     * Computes an intensity rescaled image from the given <var>image</var>, using the black point
     * and white point as defined by <var>levels</var>. <var>image</var> needs to be a grayscale
     * image with more than 8bit color depth.
     * 
     * @return The rescaled image, a 8 bit grayscale image.
     */
    public static BufferedImage rescaleIntensityLevelTo8Bits(BufferedImage image, Levels levels)
    {
        if (image.getColorModel().getColorSpace().getNumComponents() > 1)
        {
            throw new IllegalArgumentException(
                    "rescaleIntensityLevelTo8Bits() is only applicable to gray scale images.");
        }
        final float dynamicRange = levels.maxLevel - levels.minLevel;
        final BufferedImage rescaledImage =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < image.getWidth(); ++x)
        {
            for (int y = 0; y < image.getHeight(); ++y)
            {
                int originalIntensity = getGrayIntensity(image, x, y);

                // cut all intensities above the white point
                int intensity = Math.min(levels.maxLevel, originalIntensity);
                // cut all intensities below the black point and move the origin to 0
                intensity = Math.max(0, intensity - levels.minLevel);
                // normalize to [0, 1] and rescale to 8 bits
                intensity = Math.round(intensity / dynamicRange * 255);

                rescaledImage.getRaster().setSample(x, y, 0, intensity);
            }
        }
        return rescaledImage;
    }

}