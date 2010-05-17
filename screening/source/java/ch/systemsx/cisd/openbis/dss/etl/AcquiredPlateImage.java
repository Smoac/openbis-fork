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

/**
 * Describes properties extracted for one screening image.
 * 
 * @author Tomasz Pylak
 */
public class AcquiredPlateImage
{
    private final Location wellLocation;

    private final Location tileLocation;

    private final int channel;

    // can be null
    private final Float timePointOrNull, depthOrNull;

    private final RelativeImagePath imageFilePath; // relative to the dataset directory

    public AcquiredPlateImage(Location wellLocation, Location tileLocation, int channel,
            Float timePointOrNull, Float depthOrNull, RelativeImagePath imageFilePath)
    {
        this.wellLocation = wellLocation;
        this.tileLocation = tileLocation;
        this.channel = channel;
        this.timePointOrNull = timePointOrNull;
        this.depthOrNull = depthOrNull;
        this.imageFilePath = imageFilePath;
    }

    public Location getWellLocation()
    {
        return wellLocation;
    }

    public Location getTileLocation()
    {
        return tileLocation;
    }

    public int getChannel()
    {
        return channel;
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
