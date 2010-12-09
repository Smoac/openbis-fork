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
 * Describes properties extracted for one logical screening image (note that one file can contain
 * many logical images).
 * 
 * @author Tomasz Pylak
 */
public class AcquiredSingleImage extends AbstractHashable
{
    // null for non-HCS images
    private final Location wellLocationOrNull;

    private final Location tileLocation;

    private final String channelCode;

    // can be null
    private final Float timePointOrNull, depthOrNull;

    private final RelativeImageReference imageFilePath; // relative to the original dataset
                                                        // directory

    private RelativeImageReference thumbnailFilePathOrNull;

    public AcquiredSingleImage(Location wellLocationOrNull, Location tileLocation,
            String channelCode, Float timePointOrNull, Float depthOrNull,
            RelativeImageReference imageFilePath)
    {
        this.wellLocationOrNull = wellLocationOrNull;
        this.tileLocation = tileLocation;
        this.channelCode = channelCode.toUpperCase();
        this.timePointOrNull = timePointOrNull;
        this.depthOrNull = depthOrNull;
        this.imageFilePath = imageFilePath;
    }

    /** Valid only in HCS case, do not call this method for microscopy images. */
    public int getWellRow()
    {
        assert wellLocationOrNull != null : "wellLocationOrNull is null";
        return wellLocationOrNull.getY();
    }

    /** Valid only in HCS case, do not call this method for microscopy images. */
    public int getWellColumn()
    {
        assert wellLocationOrNull != null : "wellLocationOrNull is null";
        return wellLocationOrNull.getX();
    }

    public int getTileRow()
    {
        return tileLocation.getY();
    }

    public int getTileColumn()
    {
        return tileLocation.getX();
    }

    public String getChannelCode()
    {
        return channelCode;
    }

    public Float tryGetTimePoint()
    {
        return timePointOrNull;
    }

    public Float tryGetDepth()
    {
        return depthOrNull;
    }

    public RelativeImageReference getImageReference()
    {
        return imageFilePath;
    }

    public RelativeImageReference getThumbnailFilePathOrNull()
    {
        return thumbnailFilePathOrNull;
    }

    public final void setThumbnailFilePathOrNull(RelativeImageReference thumbnailFilePathOrNull)
    {
        this.thumbnailFilePathOrNull = thumbnailFilePathOrNull;
    }

}
