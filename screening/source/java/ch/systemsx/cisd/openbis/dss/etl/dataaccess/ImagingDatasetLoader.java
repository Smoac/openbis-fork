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

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference.HCSChannelStackByLocationReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference.MicroscopyChannelStackByLocationReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.HCSDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgSpotDTO;

/**
 * {@link HCSDatasetLoader} extension with code for handling images.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class ImagingDatasetLoader extends HCSDatasetLoader implements IImagingDatasetLoader
{
    private final IHierarchicalContent content;

    public ImagingDatasetLoader(IImagingReadonlyQueryDAO query, String datasetPermId,
            IHierarchicalContent content)
    {
        super(query, datasetPermId);
        this.content = content;
    }

    /**
     * @param chosenChannelCode
     * @return image (with absolute path, page and color)
     */
    public AbsoluteImageReference tryGetImage(String chosenChannelCode,
            ImageChannelStackReference channelStackReference, RequestedImageSize imageSize)
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
        long chosenChannelId = channel.getId();
        ImgImageDTO imageDTO = null;
        if (imageSize.isThumbnailRequired())
        {
            imageDTO = tryGetThumbnail(chosenChannelId, channelStackReference, datasetId);
        }
        boolean thumbnailFetched = (imageDTO != null);
        if (imageDTO == null)
        {
            // get the image content from the original image
            imageDTO = tryGetOriginalImage(chosenChannelId, channelStackReference, datasetId);
            // Thumbnail is not required, but original image is not there.
            // We have to fetch the thumbnail anyway.
            if (imageDTO == null && imageSize.isThumbnailRequired() == false)
            {
                imageDTO = tryGetThumbnail(chosenChannelId, channelStackReference, datasetId);
            }
        }
        if (imageDTO == null)
        {
            return null;
        }
        AbsoluteImageReference imgRef =
                createAbsoluteImageReference(imageDTO, channel, imageSize, thumbnailFetched);
        if (thumbnailFetched && isThumbnailsTooSmall(imageSize, imgRef.getUnchangedImage()))
        {
            imageDTO = tryGetOriginalImage(channel.getId(), channelStackReference, datasetId);
            if (imageDTO != null)
            {
                thumbnailFetched = false;
                imgRef =
                        createAbsoluteImageReference(imageDTO, channel, imageSize, thumbnailFetched);
            }
        }

        return imgRef;
    }

    private boolean isThumbnailsTooSmall(RequestedImageSize imageSize, BufferedImage image)
    {
        Size requestedThumbnailSize = imageSize.tryGetThumbnailSize();
        double width = 1.5 * image.getWidth();
        double height = 1.5 * image.getHeight();
        boolean thumbnailTooSmall =
                requestedThumbnailSize.getWidth() > width
                        || requestedThumbnailSize.getHeight() > height;
        return thumbnailTooSmall;
    }

    private AbsoluteImageReference createAbsoluteImageReference(ImgImageDTO imageDTO,
            ImgChannelDTO channel, RequestedImageSize imageSize, boolean useNativeImageLibrary)
    {
        String path = imageDTO.getFilePath();
        IHierarchicalContentNode contentNode = content.getNode(path);
        ColorComponent colorComponent = imageDTO.getColorComponent();
        ImageTransfomationFactories imageTransfomationFactories =
                createImageTransfomationFactories(imageDTO, channel);
        ImageLibraryInfo imageLibrary = tryGetImageLibrary(dataset, useNativeImageLibrary);
        return new AbsoluteImageReference(contentNode, path, imageDTO.getImageID(), colorComponent,
                imageSize, getColor(channel), imageTransfomationFactories, imageLibrary);
    }

    private static ChannelColorRGB getColor(ImgChannelDTO channel)
    {
        return new ChannelColorRGB(channel.getRedColorComponent(),
                channel.getGreenColorComponent(), channel.getBlueColorComponent());
    }

    private ImageTransfomationFactories createImageTransfomationFactories(ImgImageDTO imageDTO,
            ImgChannelDTO channel)
    {
        ImageTransfomationFactories imageTransfomationFactories = new ImageTransfomationFactories();
        imageTransfomationFactories
                .setForMergedChannels(tryGetImageTransformerFactoryForMergedChannels());

        Map<String, IImageTransformerFactory> singleChannelMap =
                getAvailableImageTransformationsForChannel(channel);
        imageTransfomationFactories.setForChannel(singleChannelMap);

        imageTransfomationFactories.setForImage(imageDTO.tryGetImageTransformerFactory());
        return imageTransfomationFactories;
    }

    private Map<String, IImageTransformerFactory> getAvailableImageTransformationsForChannel(
            ImgChannelDTO channel)
    {
        List<ImgImageTransformationDTO> availableImageTransformations =
                availableImageTransformationsMap.get(channel.getId());
        Map<String, IImageTransformerFactory> singleChannelMap =
                new HashMap<String, IImageTransformerFactory>();
        if (availableImageTransformations != null)
        {
            for (ImgImageTransformationDTO transformation : availableImageTransformations)
            {
                singleChannelMap.put(transformation.getCode(),
                        transformation.getImageTransformerFactory());
            }
        }
        return singleChannelMap;
    }

    private IImageTransformerFactory tryGetImageTransformerFactoryForMergedChannels()
    {
        IImageTransformerFactory imageTransformerFactory = dataset.tryGetImageTransformerFactory();
        if (imageTransformerFactory == null && experimentOrNull != null)
        {
            imageTransformerFactory = experimentOrNull.tryGetImageTransformerFactory();
        }
        return imageTransformerFactory;
    }

    private ImgChannelDTO tryLoadChannel(String chosenChannelCode)
    {
        // first we check if there are some channels defined at the dataset level (even for HCS one
        // can decide in configuration about that)
        ImgChannelDTO channel = query.tryGetChannelForDataset(dataset.getId(), chosenChannelCode);
        // if not, we check at the experiment level
        if (channel == null && containerOrNull != null)
        {
            channel =
                    query.tryGetChannelForExperiment(containerOrNull.getExperimentId(),
                            chosenChannelCode);
        }
        return channel;
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
                Location wellLocation = hcsRef.getWellLocation();
                if (wellLocation.getX() > container.getNumberOfColumns())
                {
                    throw new IllegalArgumentException("Well column coordinate "
                            + wellLocation.getX()
                            + " is bigger then the number of plate's columns "
                            + container.getNumberOfColumns());
                }
                if (wellLocation.getY() > container.getNumberOfRows())
                {
                    throw new IllegalArgumentException("Well row coordinate " + wellLocation.getY()
                            + " is bigger then the number of plate's rows "
                            + container.getNumberOfRows());
                }
            }
        } else if (micRef != null)
        {
            validateTileReference(micRef.getTileLocation());
        }
    }

    private void validateTileReference(Location tileLocation)
    {
        if (tileLocation.getX() > getDataset().getFieldNumberOfColumns())
        {
            throw new IllegalArgumentException("Tile x coordinate " + tileLocation.getX()
                    + " is bigger then number of well's columns "
                    + getDataset().getFieldNumberOfColumns());
        }
        if (tileLocation.getY() > getDataset().getFieldNumberOfRows())
        {
            throw new IllegalArgumentException("Tile y coordinate " + tileLocation.getY()
                    + " is bigger then number of well's rows "
                    + getDataset().getFieldNumberOfRows());
        }
    }

    private ImgImageDTO tryGetOriginalImage(long channelId,
            ImageChannelStackReference channelStackReference, long datasetId)
    {
        HCSChannelStackByLocationReference hcsRef = channelStackReference.tryGetHCSChannelStack();
        MicroscopyChannelStackByLocationReference micRef =
                channelStackReference.tryGetMicroscopyChannelStack();
        ImgImageDTO image;
        if (hcsRef != null)
        {
            image =
                    query.tryGetHCSImage(channelId, datasetId, hcsRef.getTileLocation(),
                            hcsRef.getWellLocation());
        } else if (micRef != null)
        {
            image = query.tryGetMicroscopyImage(channelId, datasetId, micRef.getTileLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            image = query.tryGetImage(channelId, channelStackId, datasetId);
        }
        return checkAccessability(image);
    }

    private ImgImageDTO tryGetThumbnail(long channelId,
            ImageChannelStackReference channelStackReference, long datasetId)
    {
        HCSChannelStackByLocationReference hcsRef = channelStackReference.tryGetHCSChannelStack();
        MicroscopyChannelStackByLocationReference micRef =
                channelStackReference.tryGetMicroscopyChannelStack();
        ImgImageDTO image;
        if (hcsRef != null)
        {
            image =
                    query.tryGetHCSThumbnail(channelId, datasetId, hcsRef.getTileLocation(),
                            hcsRef.getWellLocation());
        } else if (micRef != null)
        {
            image = query.tryGetMicroscopyThumbnail(channelId, datasetId, micRef.getTileLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            image = query.tryGetThumbnail(channelId, channelStackId, datasetId);
        }
        return checkAccessability(image);
    }

    private ImgImageDTO checkAccessability(ImgImageDTO image)
    {
        return isFileAccessible(image) ? image : null;
    }

    private boolean isFileAccessible(ImgImageDTO image)
    {
        String filePath = image.getFilePath();
        try
        {
            content.getNode(filePath);
            return true;
        } catch (IllegalArgumentException ex)
        {
            operationLog
                    .warn(String
                            .format("Path '%s' is unaccessible, probably the dataset has been deleted and the imaging db is not yet synchronized.",
                                    filePath));
            return false;
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
        return checkAccessability(image);
    }

    public AbsoluteImageReference tryFindAnyOriginalImage()
    {
        List<ImgSpotDTO> wells = query.listWellsWithAnyImages(dataset.getId());
        List<String> channelCodes = getImageParameters().getChannelsCodes();
        RequestedImageSize originalOrThumbnail = RequestedImageSize.createOriginal();

        for (ImgSpotDTO well : wells)
        {
            for (String channelCode : channelCodes)
            {
                AbsoluteImageReference image =
                        tryGetRepresentativeImage(channelCode,
                                new Location(well.getRow(), well.getColumn()), originalOrThumbnail);
                if (image != null)
                {
                    return image;
                }
            }
        }
        return null;
    }

    public AbsoluteImageReference tryGetRepresentativeImage(String channelCode,
            Location wellLocationOrNull, RequestedImageSize imageSize)
    {
        ImgChannelDTO channel = tryLoadChannel(channelCode);
        if (channel == null)
        {
            return null;
        }
        ImgImageDTO imageDTO =
                tryGetRepresentativeImageDTO(channel.getId(), wellLocationOrNull,
                        imageSize.isThumbnailRequired());
        if (imageDTO == null)
        {
            return null;
        }
        boolean useNativeImageLibrary = false;
        return createAbsoluteImageReference(imageDTO, channel, imageSize, useNativeImageLibrary);
    }

    private ImgImageDTO tryGetRepresentativeThumbnailImageDTO(long channelId,
            Location wellLocationOrNull)
    {
        ImgImageDTO image;
        if (wellLocationOrNull == null)
        {
            image = query.tryGetMicroscopyRepresentativeThumbnail(dataset.getId(), channelId);
        } else
        {
            image =
                    query.tryGetHCSRepresentativeThumbnail(dataset.getId(), wellLocationOrNull,
                            channelId);
        }
        return checkAccessability(image);
    }

    public AbsoluteImageReference tryFindAnyThumbnail()
    {
        List<ImgSpotDTO> wells = query.listWellsWithAnyThumbnails(dataset.getId());
        List<String> channelCodes = getImageParameters().getChannelsCodes();
        for (ImgSpotDTO well : wells)
        {
            for (String channelCode : channelCodes)
            {
                AbsoluteImageReference image =
                        tryGetRepresentativeThumbnail(channelCode,
                                new Location(well.getRow(), well.getColumn()));
                if (image != null)
                {
                    return image;
                }
            }
        }
        return null;
    }

    public AbsoluteImageReference tryGetRepresentativeThumbnail(String channelCode,
            Location wellLocationOrNull)
    {
        ImgChannelDTO channel = tryLoadChannel(channelCode);
        if (channel == null)
        {
            return null;
        }
        ImgImageDTO imageDTO =
                tryGetRepresentativeThumbnailImageDTO(channel.getId(), wellLocationOrNull);
        if (imageDTO == null)
        {
            return null;
        }
        return createAbsoluteImageReference(imageDTO, channel, new RequestedImageSize(
                Size.NULL_SIZE, false), true);
    }

    public AbsoluteImageReference tryGetThumbnail(String channelCode,
            ImageChannelStackReference channelStackReference)
    {
        if (StringUtils.isBlank(channelCode))
        {
            throw new UserFailureException("Unspecified channel.");
        }
        validateChannelStackReference(channelStackReference);

        final ImgChannelDTO channel = tryLoadChannel(channelCode);
        if (channel == null)
        {
            return null;
        }

        long datasetId = getDataset().getId();
        final ImgImageDTO thumbnailDTO =
                tryGetThumbnail(channel.getId(), channelStackReference, datasetId);
        if (thumbnailDTO == null)
        {
            return null;
        }

        return createAbsoluteImageReference(thumbnailDTO, channel,
                RequestedImageSize.createOriginal(), true);
    }

    private static ImageLibraryInfo tryGetImageLibrary(ImgImageDatasetDTO dataset,
            boolean isThumbnail)
    {
        if (isThumbnail)
        {
            // we do not use special libraries for thumbnails, they are always in the PNG format
            return null;
        }
        String imageLibraryName = dataset.getImageLibraryName();
        if (imageLibraryName != null)
        {
            return new ImageLibraryInfo(imageLibraryName, dataset.getImageReaderName());
        } else
        {
            return null;
        }
    }

}