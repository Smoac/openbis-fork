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

import java.io.Serializable;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageSetMetaData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSetMetaData;

/**
 * Implementation of {@link IImageSetMetaData} which wraps a {@link ImageSetMetaData}.
 *
 * @author Franz-Josef Elmer
 */
public class SimpleImageSetMetaData implements IImageSetMetaData, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final Geometry size;
    private final ImageSetMetaData metaData;

    public SimpleImageSetMetaData(ImageSetMetaData zoomLevel)
    {
        this.metaData = zoomLevel;
        size = Geometry.createFromCartesianDimensions(zoomLevel.getWidth(), zoomLevel.getHeight());
    }

    public ImageSetMetaData getMetaData()
    {
        return metaData;
    }

    public long getId()
    {
        return metaData.getId();
    }

    public boolean isOriginal()
    {
        return metaData.isOriginal();
    }

    public Geometry getSize()
    {
        return size;
    }

    public Integer getColorDepth()
    {
        return metaData.getColorDepth();
    }

    public String getFileType()
    {
        return metaData.getFileType();
    }

}
