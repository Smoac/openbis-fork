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

package ch.systemsx.cisd.openbis.dss.screening.server.logic;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageSetMetaData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageSetSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSetMetaData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;

/**
 * Find {@link ImgImageZoomLevelDTO} instances which fulfill some criteria.
 *
 * @author Franz-Josef Elmer
 */
public class ZoomLevelFinder
{
    private final IImageSetSelectionCriterion[] criteria;

    public ZoomLevelFinder(IImageSetSelectionCriterion... criteria)
    {
        this.criteria = criteria;
    }
        
    public List<ImageSetMetaData> find(List<ImageSetMetaData> zoomLevels)
    {
        List<ImageSetMetaData> filteredLevels = zoomLevels;
        for (IImageSetSelectionCriterion criterion : criteria)
        {
            if (filteredLevels.size() == 1)
            {
                break;
            }
            List<IImageSetMetaData> adaptedFilteredLevels = new ArrayList<IImageSetMetaData>();
            for (ImageSetMetaData level : filteredLevels)
            {
                adaptedFilteredLevels.add(new SimpleImageSetMetaData(level));
            }
            adaptedFilteredLevels = criterion.getMatching(adaptedFilteredLevels);
            filteredLevels = new ArrayList<ImageSetMetaData>();
            for (IImageSetMetaData adaptedFilteredLevel : adaptedFilteredLevels)
            {
                if (adaptedFilteredLevel instanceof SimpleImageSetMetaData)
                {
                    filteredLevels.add(((SimpleImageSetMetaData) adaptedFilteredLevel)
                            .getMetaData());
                }
            }
        }
        return filteredLevels;
    }
}
