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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Criterion based on the image size.
 * 
 * @author Franz-Josef Elmer
 */
public class SizeCriterion implements IImageSetSelectionCriterion
{
    private static final long serialVersionUID = 1L;

    public static enum Type
    {
        /**
         * Picks that image set where the image size is the largest one which just fits into
         * a bounding box specified by width and height.
         */
        LARGEST_IN_BOUNDING_BOX()
        {
            @Override
            void filter(int width, int height, List<IImageSetMetaData> imageMetaData,
                    List<IImageSetMetaData> filteredImageMetaData)
            {
                List<IImageSetMetaData> smallerMetaData = new ArrayList<IImageSetMetaData>();
                INSIDE_BOUNDING_BOX.filter(width, height, imageMetaData, smallerMetaData);
                if (smallerMetaData.isEmpty() == false)
                {
                    Collections.sort(smallerMetaData, SIZE_COMPARATOR);
                    filteredImageMetaData.add(smallerMetaData.get(smallerMetaData.size() - 1));
                }
            }
        },
        /**
         * Picks all image sets where the image size is inside a bounding box specified by width and
         * height.
         */
        INSIDE_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<IImageSetMetaData> imageMetaData,
                    List<IImageSetMetaData> filteredImageMetaData)
            {
                for (IImageSetMetaData metaData : imageMetaData)
                {
                    Geometry size = metaData.getSize();
                    if (size.getWidth() <= width && size.getHeight() <= height)
                    {
                        filteredImageMetaData.add(metaData);
                    }
                }
            }
        },
        /**
         * Picks that image set where the image size is the smallest one covering a bounding box
         * specified by width and height.
         */
        SMALLEST_COVERING_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<IImageSetMetaData> imageMetaData,
                    List<IImageSetMetaData> filteredImageMetaData)
            {
                List<IImageSetMetaData> largerMetaData = new ArrayList<IImageSetMetaData>();
                COVERING_BOUNDING_BOX.filter(width, height, imageMetaData, largerMetaData);
                if (largerMetaData.isEmpty() == false)
                {
                    Collections.sort(largerMetaData, SIZE_COMPARATOR);
                    filteredImageMetaData.add(largerMetaData.get(0));
                }
            }
        },
        /**
         * Picks all image sets where the image size covers a bounding box specified by width and
         * height.
         */
        COVERING_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<IImageSetMetaData> imageMetaData,
                    List<IImageSetMetaData> filteredImageMetaData)
            {
                for (IImageSetMetaData metaData : imageMetaData)
                {
                    Geometry size = metaData.getSize();
                    if (size.getWidth() >= width && size.getHeight() >= height)
                    {
                        filteredImageMetaData.add(metaData);
                    }
                }
            }
        },
        /**
         * Picks all image sets where the image size is exactly as specified by width and height.
         */
        EXACTLY
        {
            @Override
            void filter(int width, int height, List<IImageSetMetaData> imageMetaData,
                    List<IImageSetMetaData> filteredImageMetaData)
            {
                for (IImageSetMetaData metaData : imageMetaData)
                {
                    Geometry size = metaData.getSize();
                    if (size.getWidth() == width && size.getHeight() == height)
                    {
                        filteredImageMetaData.add(metaData);
                    }
                }
            }
        };

        void filter(int width, int height, List<IImageSetMetaData> imageMetaData,
                List<IImageSetMetaData> filteredImageMetaData)
        {
        }
    }

    private static final Comparator<IImageSetMetaData> SIZE_COMPARATOR =
            new Comparator<IImageSetMetaData>()
                {
                    public int compare(IImageSetMetaData i1, IImageSetMetaData i2)
                    {
                        return area(i1.getSize()) - area(i2.getSize());
                    }

                    private int area(Geometry geometry)
                    {
                        return geometry.getWidth() * geometry.getHeight();
                    }
                };

    private final int width;

    private final int height;

    private final Type type;

    /**
     * Creates an instance for the specified image size and criterion type.
     */
    public SizeCriterion(int width, int height, Type type)
    {
        this.width = width;
        this.height = height;
        if (type == null)
        {
            throw new IllegalArgumentException("Type not specified.");
        }
        this.type = type;
    }

    public List<IImageSetMetaData> getMatching(List<IImageSetMetaData> imageMetaData)
    {
        List<IImageSetMetaData> filteredMetaData = new ArrayList<IImageSetMetaData>();
        type.filter(width, height, imageMetaData, filteredMetaData);
        return filteredMetaData;
    }

}
