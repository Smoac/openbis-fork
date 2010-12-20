/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageSeriesPoint;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimension;

/**
 * Helper class to set the <code>is_complete</code> flag in the <i>BDS</i> library.
 * <p>
 * All the possible combinations are computed in the constructor. This class is also able to spot
 * images which have already been handled.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class HCSImageCheckList
{
    private final Map<FullLocation, Check> imageMap;

    public HCSImageCheckList(final List<String> channelCodes, final PlateDimension plateGeometry,
            final Geometry wellGeometry)
    {
        if (channelCodes.size() < 1)
        {
            throw new IllegalArgumentException("Number of channels smaller than one.");
        }
        if (plateGeometry == null)
        {
            throw new IllegalArgumentException("Unspecified plate geometry.");
        }
        if (wellGeometry == null)
        {
            throw new IllegalArgumentException("Unspecified well geometry.");
        }
        imageMap = new HashMap<FullLocation, Check>();
        for (String channelCode : channelCodes)
        {
            for (int wellCol = 1; wellCol <= plateGeometry.getColsNum(); wellCol++)
            {
                for (int wellRow = 1; wellRow <= plateGeometry.getRowsNum(); wellRow++)
                {
                    for (int tileCol = 1; tileCol <= wellGeometry.getColumns(); tileCol++)
                    {
                        for (int tileRow = 1; tileRow <= wellGeometry.getRows(); tileRow++)
                        {
                            imageMap.put(new FullLocation(wellRow, wellCol, tileRow, tileCol,
                                    channelCode), new Check());
                        }
                    }
                }
            }
        }
        assert imageMap.size() == channelCodes.size() * plateGeometry.getColsNum()
                * plateGeometry.getRowsNum() * wellGeometry.getColumns() * wellGeometry.getRows() : "Wrong map size";
    }

    public final void checkOff(AcquiredSingleImage image)
    {
        assert image != null : "Unspecified image.";
        FullLocation location = createLocation(image);
        final Check check = imageMap.get(location);
        if (check == null)
        {
            throw new IllegalArgumentException("Invalid channel/well/tile: " + image);
        }
        Float timepointOrNull = image.tryGetTimePoint();
        Float depthOrNull = image.tryGetDepth();
        Integer seriesNumberOrNull = image.tryGetSeriesNumber();
        if (check.isCheckedOff(timepointOrNull, depthOrNull, seriesNumberOrNull))
        {
            throw new IllegalArgumentException("Image already handled: " + image);
        }
        check.checkOff(timepointOrNull, depthOrNull, seriesNumberOrNull);
    }

    private static FullLocation createLocation(AcquiredSingleImage image)
    {
        return new FullLocation(image.getWellRow(), image.getWellColumn(), image.getTileRow(),
                image.getTileColumn(), image.getChannelCode());
    }

    public final List<FullLocation> getCheckedOnFullLocations()
    {
        final List<FullLocation> fullLocations = new ArrayList<FullLocation>();
        for (final Map.Entry<FullLocation, Check> entry : imageMap.entrySet())
        {
            if (entry.getValue().isCheckedOff(null, null, null) == false)
            {
                fullLocations.add(entry.getKey());
            }
        }
        return fullLocations;
    }

    //
    // Helper classes
    //

    private static final class Check
    {
        private boolean checkedOff;

        private final Set<ImageSeriesPoint> dimensions = new HashSet<ImageSeriesPoint>();

        final void checkOff(Float timepointOrNull, Float depthOrNull, Integer seriesNumberOrNull)
        {
            dimensions.add(new ImageSeriesPoint(timepointOrNull, depthOrNull, seriesNumberOrNull));
            checkedOff = true;
        }

        final boolean isCheckedOff(Float timepointOrNull, Float depthOrNull,
                Integer seriesNumberOrNull)
        {
            ImageSeriesPoint dim = null;
            if (timepointOrNull != null || depthOrNull != null || seriesNumberOrNull != null)
            {
                dim = new ImageSeriesPoint(timepointOrNull, depthOrNull, seriesNumberOrNull);
            }
            return checkedOff && (dim == null || dimensions.contains(dim));
        }
    }

    public final static class FullLocation extends AbstractHashable
    {
        final int wellRow, wellCol;

        final int tileRow, tileCol;

        final String channelCode;

        public FullLocation(int wellRow, int wellCol, int tileRow, int tileCol, String channelCode)
        {
            this.wellRow = wellRow;
            this.wellCol = wellCol;
            this.tileRow = tileRow;
            this.tileCol = tileCol;
            this.channelCode = channelCode.toUpperCase();
        }

        private final static String toString(final int row, final int col, final String type)
        {
            return type + "=(" + row + "," + col + ")";
        }

        //
        // AbstractHashable
        //

        @Override
        public final String toString()
        {
            return "[channel=" + channelCode + ", " + toString(wellRow, wellCol, "well") + ", "
                    + toString(tileRow, tileCol, "tile") + "]";
        }
    }
}
