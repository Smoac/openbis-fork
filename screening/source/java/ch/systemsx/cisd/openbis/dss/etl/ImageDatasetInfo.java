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

/**
 * Describes one image dataset from imaging database.
 * 
 * @author Tomasz Pylak
 */
public class ImageDatasetInfo extends ScreeningContainerDatasetInfo
{
    private final int tileRows, tileColumns;

    // has any well timepoints or depth stack images?
    private final boolean hasImageSeries;

    public ImageDatasetInfo(ScreeningContainerDatasetInfo info, int tileRows, int tileColumns,
            boolean hasImageSeries)
    {
        super.setContainerRows(info.getContainerRows());
        super.setContainerColumns(info.getContainerColumns());
        super.setContainerPermId(info.getContainerPermId());
        super.setDatasetPermId(info.getDatasetPermId());
        super.setExperimentPermId(info.getExperimentPermId());
        this.tileRows = tileRows;
        this.tileColumns = tileColumns;
        this.hasImageSeries = hasImageSeries;
    }

    public int getTileRows()
    {
        return tileRows;
    }

    public int getTileColumns()
    {
        return tileColumns;
    }

    public boolean hasImageSeries()
    {
        return hasImageSeries;
    }
}
