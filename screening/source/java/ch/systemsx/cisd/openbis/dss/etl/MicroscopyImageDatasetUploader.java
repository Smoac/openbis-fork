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

import java.util.List;

import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseHelper.ImagingChannelsMap;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Channel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;

/**
 * Uploads microscopy imagaes (no spots, no container) into imaging database.
 * 
 * @author Tomasz Pylak
 */
public class MicroscopyImageDatasetUploader extends AbstractImageDatasetUploader
{
    public static void upload(IImagingQueryDAO dao, MicroscopyImageDatasetInfo dataset,
            List<AcquiredSingleImage> images, List<Channel> channels)
    {
        new MicroscopyImageDatasetUploader(dao).upload(dataset, images, channels);
    }

    private MicroscopyImageDatasetUploader(IImagingQueryDAO dao)
    {
        super(dao);
    }

    private void upload(MicroscopyImageDatasetInfo dataset, List<AcquiredSingleImage> images,
            List<Channel> channels)
    {
        long datasetId = createMicroscopyDataset(dataset);
        ImagingChannelsMap channelsMap =
                ImagingDatabaseHelper.createDatasetChannels(dao, datasetId, channels);
        createImages(images, createDummySpotProvider(), channelsMap, datasetId);
    }

    private static ISpotProvider createDummySpotProvider()
    {
        return new ISpotProvider()
            {
                public Long tryGetSpotId(AcquiredSingleImage image)
                {
                    return null;
                }
            };
    }

    private long createMicroscopyDataset(MicroscopyImageDatasetInfo dataset)
    {
        ImgDatasetDTO datasetDTO =
                new ImgDatasetDTO(dataset.getDatasetPermId(), dataset.getTileRows(),
                        dataset.getTileColumns(), null, dataset.hasImageSeries());
        return dao.addDataset(datasetDTO);
    }
}
