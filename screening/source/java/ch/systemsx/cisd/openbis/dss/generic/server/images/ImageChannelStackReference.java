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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * Identifies one channel stack, by well and tile location or by the technical id.
 * 
 * @author Tomasz Pylak
 */
public class ImageChannelStackReference extends AbstractHashable
{
    public static final class LocationImageChannelStackReference extends AbstractHashable
    {
        protected Location wellLocation;

        protected Location tileLocation;

        public LocationImageChannelStackReference(Location wellLocation, Location tileLocation)
        {
            this.wellLocation = wellLocation;
            this.tileLocation = tileLocation;
        }

        public Location getWellLocation()
        {
            return wellLocation;
        }

        public Location getTileLocation()
        {
            return tileLocation;
        }
    }

    public static final ImageChannelStackReference createFromLocations(Location wellLocation,
            Location tileLocation)
    {
        return new ImageChannelStackReference(new LocationImageChannelStackReference(wellLocation,
                tileLocation), null);
    }

    public static final ImageChannelStackReference createFromId(long channelStackId)
    {
        return new ImageChannelStackReference(null, channelStackId);
    }

    private final LocationImageChannelStackReference locationRefOrNull;

    private final Long idRefOrNull;

    private ImageChannelStackReference(LocationImageChannelStackReference locationRefOrNull,
            Long idRefOrNull)
    {
        assert locationRefOrNull == null || idRefOrNull == null;
        assert locationRefOrNull != null || idRefOrNull != null;
        this.locationRefOrNull = locationRefOrNull;
        this.idRefOrNull = idRefOrNull;
    }

    public Long tryGetChannelStackId()
    {
        return idRefOrNull;
    }

    public LocationImageChannelStackReference tryGetChannelStackLocations()
    {
        return locationRefOrNull;
    }

}
