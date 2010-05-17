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
 * Describes one dataset container (e.g. plate) with images.
 * 
 * @author Tomasz Pylak
 */
class ScreeningContainerDatasetInfo
{
    private String experimentPermId;

    private String containerPermId;

    private String datasetPermId;

    private String[][] spotPermIds;

    private int containerWidth, containerHeight;

    private int tileWidth, tileHeight;

    public String getExperimentPermId()
    {
        return experimentPermId;
    }

    public void setExperimentPermId(String experimentPermId)
    {
        this.experimentPermId = experimentPermId;
    }

    public String getContainerPermId()
    {
        return containerPermId;
    }

    public void setContainerPermId(String containerPermId)
    {
        this.containerPermId = containerPermId;
    }

    public String getDatasetPermId()
    {
        return datasetPermId;
    }

    public void setDatasetPermId(String datasetPermId)
    {
        this.datasetPermId = datasetPermId;
    }

    public String[][] getSpotPermIds()
    {
        return spotPermIds;
    }

    public void setSpotPermIds(String[][] spotPermIds)
    {
        this.spotPermIds = spotPermIds;
    }

    public int getContainerWidth()
    {
        return containerWidth;
    }

    public void setContainerWidth(int containerWidth)
    {
        this.containerWidth = containerWidth;
    }

    public int getContainerHeight()
    {
        return containerHeight;
    }

    public void setContainerHeight(int containerHeight)
    {
        this.containerHeight = containerHeight;
    }

    public int getTileWidth()
    {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth)
    {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight()
    {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight)
    {
        this.tileHeight = tileHeight;
    }

}