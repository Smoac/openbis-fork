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

import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgDatasetDTO;

/**
 * Helper class for retrieving and/or creating entities associated with the screening container data
 * set info in the DB.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ScreeningContainerDatasetInfoHelper
{
    private final ScreeningContainerDatasetInfo info;

    private final IImagingUploadDAO dao;

    public ScreeningContainerDatasetInfoHelper(IImagingUploadDAO dao,
            ScreeningContainerDatasetInfo info)
    {
        this.dao = dao;
        this.info = info;
    }

    public long getOrCreateExperiment()
    {
        return getOrCreateExperiment(dao, info);
    }

    public long getOrCreateContainer(long expId)
    {
        return getOrCreateContainer(dao, info, expId);
    }

    public long getOrCreateDataset(long contId)
    {
        ImgDatasetDTO dataset = dao.tryGetDatasetByPermId(info.getDatasetPermId());
        if (null != dataset)
        {
            return dataset.getId();
        } else
        {
            return createDataset(dao, info, contId);
        }

    }

    // Package-visible static methods
    static long createDataset(IImagingUploadDAO dao, ScreeningContainerDatasetInfo info, long contId)
    {
        ImgDatasetDTO dataset =
                new ImgDatasetDTO(info.getDatasetPermId(), info.getTileRows(), info
                        .getTileColumns(), contId);
        return dao.addDataset(dataset);
    }

    static long getOrCreateContainer(IImagingUploadDAO dao, ScreeningContainerDatasetInfo info,
            long expId)
    {
        String containerPermId = info.getContainerPermId();
        Long containerId = dao.tryGetContainerIdPermId(containerPermId);
        if (containerId != null)
        {
            return containerId;
        } else
        {
            ImgContainerDTO container =
                    new ImgContainerDTO(containerPermId, info.getContainerRows(), info
                            .getContainerColumns(), expId);
            return dao.addContainer(container);
        }
    }

    static long getOrCreateExperiment(IImagingUploadDAO dao, ScreeningContainerDatasetInfo info)
    {
        String experimentPermId = info.getExperimentPermId();
        Long expId = dao.tryGetExperimentIdByPermId(experimentPermId);
        if (expId != null)
        {
            return expId;
        } else
        {
            return dao.addExperiment(experimentPermId);
        }
    }
}
