/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
 * @author Pawel Glyzewski
 */
public class ImgAnalysisDatasetDTO extends AbstractImgIdentifiable
{
    @ResultColumn("PERM_ID")
    private String permId;

    @ResultColumn("CONT_ID")
    private Long containerId;

    // The field that is not (yet) in database, but is useful to have here.
    private String dataSetContainerId;

    @SuppressWarnings("unused")
    private ImgAnalysisDatasetDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    // feature vector dataset
    public ImgAnalysisDatasetDTO(String permId, Long plateId)
    {
        this.permId = permId;
        this.containerId = plateId;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    /** can be null */
    public Long getContainerId()
    {
        return containerId;
    }

    public void setContainerId(Long plateId)
    {
        this.containerId = plateId;
    }

    public String getDataSetContainerId()
    {
        return dataSetContainerId;
    }

    public void setDataSetContainerId(String dataSetContainerId)
    {
        this.dataSetContainerId = dataSetContainerId;
    }

}
