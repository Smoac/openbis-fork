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

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageMetaData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;

/**
 * Implementation of {@link IImageMetaData} which wraps a {@link ImgImageZoomLevelDTO}.
 *
 * @author Franz-Josef Elmer
 */
public class ZoomLevelBasedImageMetaData implements IImageMetaData
{
    private final Geometry size;
    private final ImgImageZoomLevelDTO zoomLevel;

    public ZoomLevelBasedImageMetaData(ImgImageZoomLevelDTO zoomLevel)
    {
        this.zoomLevel = zoomLevel;
        size = Geometry.createFromCartesianDimensions(zoomLevel.getWidth(), zoomLevel.getHeight());
    }

    public ImgImageZoomLevelDTO getZoomLevel()
    {
        return zoomLevel;
    }

    public long getId()
    {
        return zoomLevel.getId();
    }

    public boolean isOriginal()
    {
        return zoomLevel.getIsOriginal();
    }

    public Geometry getSize()
    {
        return size;
    }

    public Integer getColorDepth()
    {
        return zoomLevel.getColorDepth();
    }

    public String getFileType()
    {
        return zoomLevel.getFileType();
    }

}
