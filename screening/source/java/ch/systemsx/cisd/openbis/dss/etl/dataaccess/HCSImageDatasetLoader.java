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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.etl.IHCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet.Size;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference.LocationImageChannelStackReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.HCSDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;

/**
 * {@link HCSDatasetLoader} extension with code for handling images.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class HCSImageDatasetLoader extends HCSDatasetLoader implements IHCSImageDatasetLoader
{
    private final IContentRepository contentRepository;

    public HCSImageDatasetLoader(IImagingReadonlyQueryDAO query, String datasetPermId,
            IContentRepository contentRepository)
    {
        super(query, datasetPermId);
        this.contentRepository = contentRepository;
    }

    /**
     * @param chosenChannelCode
     * @return image (with absolute path, page and color)
     */
    public AbsoluteImageReference tryGetImage(String chosenChannelCode,
            ImageChannelStackReference channelStackReference, Size thumbnailSizeOrNull)
    {
        assert StringUtils.isBlank(chosenChannelCode) == false;
        LocationImageChannelStackReference stackLocations =
                channelStackReference.tryGetChannelStackLocations();
        if (stackLocations != null)
        {
            assert stackLocations.getTileLocation().getX() <= getDataset()
                    .getFieldNumberOfColumns();
            assert stackLocations.getTileLocation().getY() <= getDataset().getFieldNumberOfRows();
            assert stackLocations.getWellLocation().getX() <= getContainer().getNumberOfColumns();
            assert stackLocations.getWellLocation().getY() <= getContainer().getNumberOfRows();
        }

        Long chosenChannelId =
                query.tryGetChannelIdByChannelCodeDatasetIdOrExperimentId(getDataset().getId(),
                        getContainer().getExperimentId(), chosenChannelCode);
        if (chosenChannelId == null)
        {
            return null;
        }

        long datasetId = getDataset().getId();
        boolean thumbnailPrefered = thumbnailSizeOrNull != null;
        ImgImageDTO imageDTO =
                tryGetImageDTO(channelStackReference, thumbnailPrefered, chosenChannelId, datasetId);
        if (imageDTO == null)
        {
            return null;
        }
        String path = imageDTO.getFilePath();
        IContent content = contentRepository.getContent(path);
        return new AbsoluteImageReference(content, path, imageDTO.getPage(), imageDTO
                .getColorComponent(), thumbnailSizeOrNull);
    }

    private ImgImageDTO tryGetImageDTO(ImageChannelStackReference channelStackReference,
            boolean thumbnailPrefered, Long chosenChannelId, long datasetId)
    {
        if (thumbnailPrefered)
        {
            ImgImageDTO thumbnailDTO =
                    tryGetThumbnail(chosenChannelId, channelStackReference, datasetId);
            if (thumbnailDTO != null)
            {
                return thumbnailDTO;
            } else
            {
                return tryGetImage(chosenChannelId, channelStackReference, datasetId);
            }
        } else
        {
            // get the image content from the original image
            return tryGetImage(chosenChannelId, channelStackReference, datasetId);
        }
    }

    private ImgImageDTO tryGetImage(long channelId,
            ImageChannelStackReference channelStackReference, long datasetId)
    {
        LocationImageChannelStackReference locations =
                channelStackReference.tryGetChannelStackLocations();
        if (locations != null)
        {
            return query.tryGetImage(channelId, datasetId, locations.getTileLocation(), locations
                    .getWellLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            return query.tryGetImage(channelId, channelStackId, datasetId);
        }
    }

    private ImgImageDTO tryGetThumbnail(long channelId,
            ImageChannelStackReference channelStackReference, long datasetId)
    {
        LocationImageChannelStackReference locations =
                channelStackReference.tryGetChannelStackLocations();
        if (locations != null)
        {
            return query.tryGetThumbnail(channelId, datasetId, locations.getTileLocation(),
                    locations.getWellLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            return query.tryGetThumbnail(channelId, channelStackId, datasetId);
        }
    }
}