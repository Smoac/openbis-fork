/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * Describes properties extracted for one screening image.
 * 
 * @author Tomasz Pylak
 */
public class AcquiredPlateImage extends AbstractHashable
{
    private final Location wellLocation;

    private final Location tileLocation;

    private final String channelName;

    // can be null
    private final Float timePointOrNull, depthOrNull;

    private final RelativeImagePath imageFilePath; // relative to the dataset directory

    public AcquiredPlateImage(Location wellLocation, Location tileLocation, String channelName,
            Float timePointOrNull, Float depthOrNull, RelativeImagePath imageFilePath)
    {
        this.wellLocation = wellLocation;
        this.tileLocation = tileLocation;
        this.channelName = channelName;
        this.timePointOrNull = timePointOrNull;
        this.depthOrNull = depthOrNull;
        this.imageFilePath = imageFilePath;
    }

    public int getWellRow()
    {
        return wellLocation.getY();
    }

    public int getWellColumn()
    {
        return wellLocation.getX();
    }

    public int getTileRow()
    {
        return tileLocation.getY();
    }

    public int getTileColumn()
    {
        return tileLocation.getX();
    }

    public String getChannelName()
    {
        return channelName;
    }

    public Float tryGetTimePoint()
    {
        return timePointOrNull;
    }

    public Float tryGetDepth()
    {
        return depthOrNull;
    }

    public RelativeImagePath getImageFilePath()
    {
        return imageFilePath;
    }

}
