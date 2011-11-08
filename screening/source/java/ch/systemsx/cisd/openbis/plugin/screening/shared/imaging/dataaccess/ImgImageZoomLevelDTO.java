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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

/**
 * Describes an image dataset with 1. all images resized in the same way or 2. original images.
 * 
 * @author Tomasz Pylak
 */
public class ImgImageZoomLevelDTO extends AbstractImgIdentifiable
{
    @ResultColumn("physical_dataset_perm_id")
    private String physicalDatasetPermId;

    @ResultColumn("is_original")
    private boolean isOriginal;

    @ResultColumn("container_dataset_id")
    private long containerDatasetId;

    @SuppressWarnings("unused")
    private ImgImageZoomLevelDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgImageZoomLevelDTO(String physicalDatasetPermId, boolean isOriginal,
            long containerDatasetId)
    {
        this.physicalDatasetPermId = physicalDatasetPermId;
        this.isOriginal = isOriginal;
        this.containerDatasetId = containerDatasetId;
    }

    public String getPhysicalDatasetPermId()
    {
        return physicalDatasetPermId;
    }

    public void setPhysicalDatasetPermId(String physicalDatasetPermId)
    {
        this.physicalDatasetPermId = physicalDatasetPermId;
    }

    public boolean getIsOriginal()
    {
        return isOriginal;
    }

    public void setOriginal(boolean isOriginal)
    {
        this.isOriginal = isOriginal;
    }

    public long getContainerDatasetId()
    {
        return containerDatasetId;
    }

    public void setContainerDatasetId(long containerDatasetId)
    {
        this.containerDatasetId = containerDatasetId;
    }
}
