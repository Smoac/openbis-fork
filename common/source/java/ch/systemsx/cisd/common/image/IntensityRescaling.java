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
     * significant if only a small fraction (given by <var>threshold</var> of all pixels has a
     * value of 1 in this bit position.
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
}
