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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.imagereaders.TiffReadParams;

/**
 * Helper methods and standalone program to calculate the range of brightness of a collection of
 * images in grayscale.
 * 
 * @author Tomasz Pylak
 */
public class ColorRangeCalculator
{
    /** Describes the minimal and maximal brightness of the pixel on the image. */
    public static class ImagePixelsRange
    {
        private final int min, max;

        public ImagePixelsRange(int min, int max)
        {
            this.min = min;
            this.max = max;
        }

        public int getMin()
        {
            return min;
        }

        public int getMax()
        {
            return max;
        }

        public ImagePixelsRange createOverlap(ImagePixelsRange rangeOrNull)
        {
            if (rangeOrNull == null)
            {
                return this;
            }
            int newMin = Math.min(min, rangeOrNull.getMin());
            int newMax = Math.max(max, rangeOrNull.getMax());
            return new ImagePixelsRange(newMin, newMax);
        }

        @Override
        public String toString()
        {
            return "[" + min + ", " + max + "]";
        }
    }

    /**
     * We should have converted 'globalRange' to [0,255] but instead a smaller range 'imageRange'
     * has been converted to [0,255]. We have to squeeze 'imageRange' to a smaller range [a,b] (a >=
     * 0, b <= 255) into which it would be mapped if globalRange would be used at the beginning.
     */
    public static ImagePixelsRange rescaleRange(ImagePixelsRange imageRange,
            ImagePixelsRange globalRange)
    {
        double globalRangeLength = globalRange.getMax() - globalRange.getMin();
        int min = (int) (255 * (imageRange.getMin() - globalRange.getMin()) / globalRangeLength);
        int max = (int) (255 * (imageRange.getMax() - globalRange.getMin()) / globalRangeLength);
        return new ImagePixelsRange(min, max);
    }

    public static ImagePixelsRange calculateOverlapRange(Collection<ImagePixelsRange> ranges)
    {
        ImagePixelsRange globalRange = null;
        for (ImagePixelsRange imageRange : ranges)
        {
            globalRange = imageRange.createOverlap(globalRange);
        }
        return globalRange;
    }

    public static ImagePixelsRange calculatePixelsRange(BufferedImage bufferedImage,
            int minRescaledColor, int maxRescaledColor)
    {
        int minColor = maxRescaledColor;
        int maxColor = minRescaledColor;
        for (int x = 0; x < bufferedImage.getWidth(); x++)
        {
            for (int y = 0; y < bufferedImage.getHeight(); y++)
            {
                int dominantColorComponent = bufferedImage.getRaster().getSample(x, y, 0);
                if (dominantColorComponent >= minRescaledColor
                        && dominantColorComponent <= maxRescaledColor)
                {
                    if (dominantColorComponent > maxColor)
                    {
                        maxColor = dominantColorComponent;
                    } else if (dominantColorComponent < minColor)
                    {
                        minColor = dominantColorComponent;
                    }
                }
            }
        }
        return new ImagePixelsRange(minColor, maxColor);
    }

    public static ImagePixelsRange calculatePixelsRange(List<File> imageFiles) throws IOException
    {
        Collection<ImagePixelsRange> ranges =
                new ArrayList<ColorRangeCalculator.ImagePixelsRange>();
        for (File imageFile : imageFiles)
        {
            BufferedImage image = loadImage(imageFile);
            ImagePixelsRange range = calculatePixelsRange(image, 0, Integer.MAX_VALUE);
            ranges.add(range);
        }
        return calculateOverlapRange(ranges);
    }

    private static BufferedImage tryRead(String libraryName, File file)
    {
        IImageReader reader =
                ImageReaderFactory.tryGetImageReaderForFile(libraryName, file.getPath());
        TiffReadParams params = new TiffReadParams();
        params.setAllow16BitGrayscaleModel(true);
        if (reader != null)
        {
            // System.err.println("Used reader: " + reader.getName());
            return reader.readImage(file, params);
        } else
        {
            return null;
        }
    }

    private static BufferedImage loadImage(File file) throws IOException
    {
        String[] libraries =
                new String[]
                    { ImageReaderConstants.JAI_LIBRARY, ImageReaderConstants.IMAGEIO_LIBRARY,
                            ImageReaderConstants.IMAGEJ_LIBRARY,
                            ImageReaderConstants.BIOFORMATS_LIBRARY };
        for (String libraryName : libraries)
        {
            BufferedImage image = tryRead(libraryName, file);
            if (image != null)
            {
                // System.err.println("Used library: " + libraryName);
                return image;
            }
        }
        throw new IOException("Cannot read image " + file.getPath());
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length == 0)
        {
            System.err.println("No files specified!");
            System.exit(1);
        }
        List<File> imageFiles = new ArrayList<File>();
        for (int i = 0; i < args.length; i++)
        {
            File imageFile = new File(args[i]);
            if (imageFile.isFile() == false)
            {
                System.err.println("File does not exist: " + imageFile.getPath());
                System.exit(1);
            }
            imageFiles.add(imageFile);
        }
        ImagePixelsRange range = calculatePixelsRange(imageFiles);
        System.out.println(range.getMin() + " " + range.getMax());
    }
}
