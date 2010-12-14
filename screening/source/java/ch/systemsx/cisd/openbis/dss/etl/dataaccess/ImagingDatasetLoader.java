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

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference.HCSChannelStackByLocationReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference.MicroscopyChannelStackByLocationReference;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.HCSDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;

/**
 * {@link HCSDatasetLoader} extension with code for handling images.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class ImagingDatasetLoader extends HCSDatasetLoader implements IImagingDatasetLoader
{
    private final IContentRepository contentRepository;

    public ImagingDatasetLoader(IImagingReadonlyQueryDAO query, String datasetPermId,
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
        if (StringUtils.isBlank(chosenChannelCode))
        {
            throw new UserFailureException("Unspecified channel.");
        }
        validateChannelStackReference(channelStackReference);

        ImgChannelDTO channel = tryLoadChannel(chosenChannelCode);
        if (channel == null)
        {
            return null;
        }

        long datasetId = getDataset().getId();
        boolean thumbnailPrefered = thumbnailSizeOrNull != null;
        ImgImageDTO imageDTO =
                tryGetImageDTO(channelStackReference, thumbnailPrefered, channel.getId(), datasetId);
        if (imageDTO == null)
        {
            return null;
        }
        AbsoluteImageReference imgRef =
                createAbsoluteImageReference(imageDTO, channel, thumbnailSizeOrNull);

        return imgRef;
    }

    private AbsoluteImageReference createAbsoluteImageReference(ImgImageDTO imageDTO,
            ImgChannelDTO channel, Size thumbnailSizeOrNull)
    {
        String path = imageDTO.getFilePath();
        IContent content = contentRepository.getContent(path);
        ColorComponent colorComponent = imageDTO.getColorComponent();
        AbsoluteImageReference imgRef =
                new AbsoluteImageReference(content, path, imageDTO.getPage(), colorComponent,
                        thumbnailSizeOrNull);
        imgRef.setTransformerFactoryForMergedChannels(tryGetImageTransformerFactoryForMergedChannels());
        imgRef.setTransformerFactory(channel.getImageTransformerFactory());
        return imgRef;
    }

    private IImageTransformerFactory tryGetImageTransformerFactoryForMergedChannels()
    {
        IImageTransformerFactory imageTransformerFactory = dataset.getImageTransformerFactory();
        if (imageTransformerFactory == null && experimentOrNull != null)
        {
            imageTransformerFactory = experimentOrNull.getImageTransformerFactory();
        }
        return imageTransformerFactory;
    }

    private ImgChannelDTO tryLoadChannel(String chosenChannelCode)
    {
        if (containerOrNull != null)
        {
            return query.tryGetChannelForExperiment(containerOrNull.getExperimentId(),
                    chosenChannelCode);
        } else
        {
            return query.tryGetChannelForDataset(dataset.getId(), chosenChannelCode);
        }
    }

    private void validateChannelStackReference(ImageChannelStackReference channelStackReference)
    {
        HCSChannelStackByLocationReference hcsRef = channelStackReference.tryGetHCSChannelStack();
        MicroscopyChannelStackByLocationReference micRef =
                channelStackReference.tryGetMicroscopyChannelStack();
        if (hcsRef != null)
        {
            validateTileReference(hcsRef.getTileLocation());
            ImgContainerDTO container = tryGetContainer();
            if (container != null)
            {
                assert hcsRef.getWellLocation().getX() <= container.getNumberOfColumns();
                assert hcsRef.getWellLocation().getY() <= container.getNumberOfRows();
            }
        } else if (micRef != null)
        {
            validateTileReference(micRef.getTileLocation());
        }
    }

    private void validateTileReference(Location tileLocation)
    {
        assert tileLocation.getX() <= getDataset().getFieldNumberOfColumns();
        assert tileLocation.getY() <= getDataset().getFieldNumberOfRows();
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
        HCSChannelStackByLocationReference hcsRef = channelStackReference.tryGetHCSChannelStack();
        MicroscopyChannelStackByLocationReference micRef =
                channelStackReference.tryGetMicroscopyChannelStack();
        if (hcsRef != null)
        {
            return query.tryGetHCSImage(channelId, datasetId, hcsRef.getTileLocation(),
                    hcsRef.getWellLocation());
        } else if (micRef != null)
        {
            return query.tryGetMicroscopyImage(channelId, datasetId, micRef.getTileLocation());
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
        HCSChannelStackByLocationReference hcsRef = channelStackReference.tryGetHCSChannelStack();
        MicroscopyChannelStackByLocationReference micRef =
                channelStackReference.tryGetMicroscopyChannelStack();
        if (hcsRef != null)
        {
            return query.tryGetHCSThumbnail(channelId, datasetId, hcsRef.getTileLocation(),
                    hcsRef.getWellLocation());
        } else if (micRef != null)
        {
            return query.tryGetMicroscopyThumbnail(channelId, datasetId, micRef.getTileLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            return query.tryGetThumbnail(channelId, channelStackId, datasetId);
        }
    }

    private ImgImageDTO tryGetRepresentativeImageDTO(long channelId, Location wellLocationOrNull,
            boolean thumbnailWanted)
    {
        long datasetId = dataset.getId();
        ImgImageDTO image = null;
        if (wellLocationOrNull == null)
        {
            if (thumbnailWanted)
            {
                image = query.tryGetMicroscopyRepresentativeThumbnail(datasetId, channelId);
            }
            if (image == null)
            {
                image = query.tryGetMicroscopyRepresentativeImage(datasetId, channelId);
            }
        } else
        {
            if (thumbnailWanted)
            {
                image =
                        query.tryGetHCSRepresentativeThumbnail(datasetId, wellLocationOrNull,
                                channelId);
            }
            if (image == null)
            {
                image =
                        query.tryGetHCSRepresentativeImage(datasetId, wellLocationOrNull, channelId);
            }
        }
        return image;
    }

    public AbsoluteImageReference tryGetRepresentativeImage(String channelCode,
            Location wellLocationOrNull, Size thumbnailSizeOrNull)
    {
        ImgChannelDTO channel = tryLoadChannel(channelCode);
        if (channel == null)
        {
            return null;
        }
        ImgImageDTO imageDTO =
                tryGetRepresentativeImageDTO(channel.getId(), wellLocationOrNull,
                        thumbnailSizeOrNull != null);
        if (imageDTO == null)
        {
            return null;
        }
        return createAbsoluteImageReference(imageDTO, channel, thumbnailSizeOrNull);
    }
}