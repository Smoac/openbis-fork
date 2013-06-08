/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import java.awt.image.BufferedImage;

/**
 * Utilities for working with segmentation images.
 * 
 * @author Bernd Rinn
 */
public class SegmentationImageUtilities
{

    private static int getGreyIntensity(BufferedImage image, int x, int y)
    {
        return image.getRaster().getSample(x, y, 0);
    }

    private static int getNumberOfObjects(BufferedImage image)
    {
        int numberOfObjects = 0;
        for (int x = 0; x < image.getWidth(); ++x)
        {
            for (int y = 0; y < image.getHeight(); ++y)
            {
                numberOfObjects = Math.max(numberOfObjects, getGreyIntensity(image, x, y));
            }
        }
        return numberOfObjects;
    }

    /**
     * Returns the segmented objects encoded in the given <var>image</var>.
     * <p>
     * This methods assumes that <var>image</var> is a grey-scale image that encodes the points
     * belonging to object <var>id</var> with gray-scale intensity value <var>id + 1</var> and
     * points that do not belong to any object with value 0 (black).
     * 
     * @param image The image to extract the segmented objects from.
     * @return The segmented objects.
     */
    public static SegmentedObject[] getSegmentedObjects(BufferedImage image)
    {
        final int numberOfObjects = getNumberOfObjects(image);
        final SegmentedObject[] result = new SegmentedObject[numberOfObjects];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = new SegmentedObject();
        }
        for (int x = 0; x < image.getWidth(); ++x)
        {
            for (int y = 0; y < image.getHeight(); ++y)
            {
                int id = getGreyIntensity(image, x, y);
                if (id > 0)
                {
                    final SegmentedObject info = result[id - 1];
                    info.updateLeftUpperX(x);
                    info.updateRightLowerX(x);
                    info.updateLeftUpperY(y);
                    info.updateRightLowerY(y);
                }
            }
        }
        for (int id = 0; id < result.length; ++id)
        {
            final SegmentedObject info = result[id];
            final int idval = id + 1;
            for (int x = info.getLeftUpperX(); x <= info.getRightLowerX(); ++x)
            {
                for (int y = info.getLeftUpperY(); y <= info.getRightLowerY(); ++y)
                {
                    int val = getGreyIntensity(image, x, y);
                    if (val == idval)
                    {
                        info.setMaskPoint(x, y);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a binary image of the edges of all segmented objects.
     * 
     * @param imageGeometry The geometry of the image to create.
     * @param objects The segmented objects.
     */
    public static BufferedImage createBinarySegmentationEdgesImage(ImageGeometry imageGeometry,
            SegmentedObject[] objects)
    {
        final BufferedImage image =
                new BufferedImage(imageGeometry.getWidth(), imageGeometry.getHeight(),
                        BufferedImage.TYPE_BYTE_BINARY);
        for (SegmentedObject obj : objects)
        {
            for (int x = obj.getLeftUpperX(); x <= obj.getRightLowerX(); ++x)
            {
                for (int y = obj.getLeftUpperY(); y <= obj.getRightLowerY(); ++y)
                {
                    if (obj.getEdgeMaskPoint(x, y))
                    {
                        image.getRaster().setDataElements(x, y, new byte[]
                            { 1 });
                    }
                }
            }
        }
        return image;
    }

    /**
     * Returns a 16-bit grayscale image of all segmented objects where the color value represents
     * the object index.
     * 
     * @param imageGeometry The geometry of the image to create.
     * @param objects The segmented objects.
     */
    public static BufferedImage createGrayscaleSegmentationImage(ImageGeometry imageGeometry,
            SegmentedObject[] objects)
    {
        final BufferedImage image =
                new BufferedImage(imageGeometry.getWidth(), imageGeometry.getHeight(),
                        BufferedImage.TYPE_USHORT_GRAY);
        for (SegmentedObject obj : objects)
        {
            for (int x = obj.getLeftUpperX(); x <= obj.getRightLowerX(); ++x)
            {
                for (int y = obj.getLeftUpperY(); y <= obj.getRightLowerY(); ++y)
                {
                    if (obj.getMaskPoint(x, y))
                    {
                        image.getRaster().setDataElements(x, y, new short[]
                            { (short) (obj.getObjectIndex() + 1) });
                    }
                }
            }
        }
        return image;
    }
}
