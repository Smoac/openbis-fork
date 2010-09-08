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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;

/**
 * Based on the list of available plate locations and geometries tries to figure the correct plate
 * geometry.
 * 
 * @author Izabela Adamczyk
 */
public class PlateGeometryOracle
{

    public static String figureGeometry(List<Location> plateLocations, List<String> plateGeometries)
    {
        return getMatchingGeometry(getMaxLocation(plateLocations), plateGeometries);
    }

    private static Location getMaxLocation(List<Location> locations)
    {
        int maxX = -1;
        int maxY = -1;
        for (Location l : locations)
        {
            if (maxX < l.getX())
            {
                maxX = l.getX();
            }
            if (maxY < l.getY())
            {
                maxY = l.getY();
            }
        }
        return new Location(maxX, maxY);
    }

    private static String getMatchingGeometry(Location location, List<String> plateGeometries)
    {
        Map<Geometry, String> map = new HashMap<Geometry, String>();
        List<Geometry> geometries = new ArrayList<Geometry>();
        for (String geometryString : plateGeometries)
        {
            Geometry geometry = Geometry.createFromPlateGeometryString(geometryString);
            geometries.add(geometry);
            map.put(geometry, geometryString);
        }
        Collections.sort(geometries, new Comparator<Geometry>()
            {

                public int compare(Geometry a, Geometry b)
                {
                    return a.getHeight() * a.getWidth() - b.getHeight() * b.getWidth();
                }
            });
        for (Geometry g : geometries)
        {
            if (isEnough(g, location))
            {
                return map.get(g);
            }
        }
        throw new UserFailureException(String.format(
                "Matching geometry not found (max location: %s, geometries: %s)", location,
                StringUtils.joinList(plateGeometries)));
    }

    static private boolean isEnough(Geometry geometry, Location location)
    {
        int dimX = geometry.getDimX();
        int x = location.getX();
        int dimY = geometry.getDimY();
        int y = location.getY();
        return dimX >= x && dimY >= y;
    }
}
