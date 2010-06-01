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

    private int containerRows, containerColumns;

    private int tileRows, tileColumns;

    // All images paths are relative to the IMAGES_DIRECTORY folder.
    // The folder path itself is relative to the dataset root directory.
    private String relativeImagesDirectory;

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

    public int getContainerRows()
    {
        return containerRows;
    }

    public void setContainerRows(int containerRows)
    {
        this.containerRows = containerRows;
    }

    public int getContainerColumns()
    {
        return containerColumns;
    }

    public void setContainerColumns(int containerColumns)
    {
        this.containerColumns = containerColumns;
    }

    public int getTileRows()
    {
        return tileRows;
    }

    public void setTileRows(int tileRows)
    {
        this.tileRows = tileRows;
    }

    public int getTileColumns()
    {
        return tileColumns;
    }

    public void setTileColumns(int tileColumns)
    {
        this.tileColumns = tileColumns;
    }

    public String getRelativeImagesDirectory()
    {
        return relativeImagesDirectory;
    }

    public void setRelativeImagesDirectory(String relativeImagesDirectory)
    {
        this.relativeImagesDirectory = relativeImagesDirectory;
    }
}