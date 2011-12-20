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
 * @author Franz-Josef Elmer
 */
public class SizeCriterion implements IImageSelectionCriterion
{
    public static enum Type
    {
        LARGEST_IN_BOUNDING_BOX()
        {
            @Override
            void filter(int width, int height, List<IImageMetaData> imageMetaData,
                    List<IImageMetaData> filteredImageMetaData)
            {
                List<IImageMetaData> smallerMetaData = new ArrayList<IImageMetaData>();
                INSIDE_BOUNDING_BOX.filter(width, height, imageMetaData, smallerMetaData);
                if (smallerMetaData.isEmpty() == false)
                {
                    Collections.sort(smallerMetaData, SITE_COMPARATOR);
                    filteredImageMetaData.add(smallerMetaData.get(smallerMetaData.size() - 1));
                }
            }
        },
        INSIDE_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<IImageMetaData> imageMetaData,
                    List<IImageMetaData> filteredImageMetaData)
            {
                for (IImageMetaData metaData : imageMetaData)
                {
                    Geometry size = metaData.getSize();
                    if (size.getWidth() <= width && size.getHeight() <= height)
                    {
                        filteredImageMetaData.add(metaData);
                    }
                }
            }
        },
        SMALLEST_OUTSIDE_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<IImageMetaData> imageMetaData,
                    List<IImageMetaData> filteredImageMetaData)
            {
                List<IImageMetaData> largerMetaData = new ArrayList<IImageMetaData>();
                LARGER_THEN_BOUNDING_BOX.filter(width, height, imageMetaData, largerMetaData);
                if (largerMetaData.isEmpty() == false)
                {
                    Collections.sort(largerMetaData, SITE_COMPARATOR);
                    filteredImageMetaData.add(largerMetaData.get(0));
                }
            }
        },
        LARGER_THEN_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<IImageMetaData> imageMetaData,
                    List<IImageMetaData> filteredImageMetaData)
            {
                for (IImageMetaData metaData : imageMetaData)
                {
                    Geometry size = metaData.getSize();
                    if (size.getWidth() >= width && size.getHeight() >= height)
                    {
                        filteredImageMetaData.add(metaData);
                    }
                }
            }
        },
        EXACTLY
        {
            @Override
            void filter(int width, int height, List<IImageMetaData> imageMetaData,
                    List<IImageMetaData> filteredImageMetaData)
            {
                for (IImageMetaData metaData : imageMetaData)
                {
                    Geometry size = metaData.getSize();
                    if (size.getWidth() == width && size.getHeight() == height)
                    {
                        filteredImageMetaData.add(metaData);
                    }
                }
            }
        };

        void filter(int width, int height, List<IImageMetaData> imageMetaData,
                List<IImageMetaData> filteredImageMetaData)
        {
        }
    }

    private static final Comparator<IImageMetaData> SITE_COMPARATOR =
            new Comparator<IImageMetaData>()
                {
                    public int compare(IImageMetaData i1, IImageMetaData i2)
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

    public List<IImageMetaData> getMatching(List<IImageMetaData> imageMetaData)
    {
        List<IImageMetaData> filteredMetaData = new ArrayList<IImageMetaData>();
        type.filter(width, height, imageMetaData, filteredMetaData);
        return filteredMetaData;
    }

}
