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

package ch.systemsx.cisd.imagereaders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Immutable value class of an image ID based on series index, time series index, focal plane index, and color channel index.
 * It will be used to identify images in a container image file format like multi-page TIFF.
 *
 * @author Franz-Josef Elmer
 */
public class ImageID
{
    /** ID of the single image in a non-container image file format. */
    public static final ImageID NULL = new ImageID(0, 0, 0, 0);
    
    private static final Pattern ID_PATTERN = Pattern.compile("(\\d+)-(\\d+)-(\\d+)-(\\d+)");
    
    public static ImageID parse(String imageIdAsString)
    {
        Matcher matcher = ID_PATTERN.matcher(imageIdAsString);
        if (matcher.matches() == false)
        {
            throw new IllegalArgumentException("Invalid image ID: " + imageIdAsString);
        }
        return new ImageID(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
    }
    
    private final int seriesIndex;
    private final int timeSeriesIndex;
    private final int focalPlaneIndex;
    private final int colorChannelIndex;
    private final String id;
    
    /**
     * Creates an instance for the specified series index, time series (or T) index, focal plane (or
     * Z) index, color channel index. 
     */
    public ImageID(int seriesIndex, int timeSeriesIndex, int focalPlaneIndex,
            int colorChannelIndex)
    {
        this.seriesIndex = seriesIndex;
        this.timeSeriesIndex = timeSeriesIndex;
        this.focalPlaneIndex = focalPlaneIndex;
        this.colorChannelIndex = colorChannelIndex;
        id = seriesIndex + "-" + timeSeriesIndex + "-" + focalPlaneIndex + "-" + colorChannelIndex;
    }

    public int getSeriesIndex()
    {
        return seriesIndex;
    }

    public int getTimeSeriesIndex()
    {
        return timeSeriesIndex;
    }

    public int getFocalPlaneIndex()
    {
        return focalPlaneIndex;
    }

    public int getColorChannelIndex()
    {
        return colorChannelIndex;
    }

    public String getID()
    {
        return id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ImageID == false)
        {
            return false;
        }
        ImageID that = (ImageID) obj;
        return seriesIndex == that.seriesIndex && timeSeriesIndex == that.timeSeriesIndex
                && focalPlaneIndex == that.focalPlaneIndex
                && colorChannelIndex == that.colorChannelIndex;
    }

    @Override
    public int hashCode()
    {
        return ((((seriesIndex * 37) + timeSeriesIndex) * 37) + focalPlaneIndex) * 37
                + colorChannelIndex;
    }

    @Override
    public String toString()
    {
        return getID();
    }

}
