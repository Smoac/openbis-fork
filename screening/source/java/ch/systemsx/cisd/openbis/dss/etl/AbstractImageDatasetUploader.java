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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseHelper.ImagingChannelsMap;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAcquiredImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;

/**
 * Abstract superclass for uploaders of image datasets into the imaging database.
 * 
 * @author Tomasz Pylak
 */
abstract class AbstractImageDatasetUploader
{
    protected final IImagingQueryDAO dao;

    protected AbstractImageDatasetUploader(IImagingQueryDAO dao)
    {
        this.dao = dao;
    }

    protected static class AcquiredImageInStack
    {
        private final String channelCode;

        private final RelativeImageReference imageFilePath;

        private final RelativeImageReference thumbnailPathOrNull;

        public AcquiredImageInStack(String channelCode, RelativeImageReference imageFilePath,
                RelativeImageReference thumbnailPathOrNull)
        {
            this.channelCode = channelCode.toUpperCase();
            this.imageFilePath = imageFilePath;
            this.thumbnailPathOrNull = thumbnailPathOrNull;
        }

        public String getChannelCode()
        {
            return channelCode;
        }

        public RelativeImageReference getImageFilePath()
        {
            return imageFilePath;
        }

        public final RelativeImageReference getThumbnailPathOrNull()
        {
            return thumbnailPathOrNull;
        }
    }

    protected static interface ISpotProvider
    {
        Long tryGetSpotId(AcquiredSingleImage image);
    }

    protected final void createImages(List<AcquiredSingleImage> images, ISpotProvider spotProvider,
            ImagingChannelsMap channelsMap, long datasetId)
    {
        Map<ImgChannelStackDTO, List<AcquiredImageInStack>> stackImagesMap =
                makeStackImagesMap(images, spotProvider, datasetId);
        dao.addChannelStacks(new ArrayList<ImgChannelStackDTO>(stackImagesMap.keySet()));
        createImages(stackImagesMap, channelsMap);
    }

    private Map<ImgChannelStackDTO, List<AcquiredImageInStack>> makeStackImagesMap(
            List<AcquiredSingleImage> images, ISpotProvider spotProvider, long datasetId)
    {
        Map<ImgChannelStackDTO, List<AcquiredImageInStack>> map =
                new HashMap<ImgChannelStackDTO, List<AcquiredImageInStack>>();
        for (AcquiredSingleImage image : images)
        {
            ImgChannelStackDTO stackDTO = makeStackDtoWithouId(image, spotProvider, datasetId);
            List<AcquiredImageInStack> stackImages = map.get(stackDTO);
            if (stackImages == null)
            {
                stackImages = new ArrayList<AcquiredImageInStack>();
            }
            stackImages.add(makeAcquiredImageInStack(image));
            map.put(stackDTO, stackImages);
        }
        setChannelStackIds(map.keySet());
        return map;
    }

    private void setChannelStackIds(Set<ImgChannelStackDTO> channelStacks)
    {
        for (ImgChannelStackDTO channelStack : channelStacks)
        {
            channelStack.setId(dao.createChannelStackId());
        }
    }

    private static AcquiredImageInStack makeAcquiredImageInStack(AcquiredSingleImage image)
    {
        return new AcquiredImageInStack(image.getChannelCode(), image.getImageReference(),
                image.getThumbnailFilePathOrNull());
    }

    private ImgChannelStackDTO makeStackDtoWithouId(AcquiredSingleImage image,
            ISpotProvider spotProvider, long datasetId)
    {
        Long spotId = spotProvider.tryGetSpotId(image);
        int dummyId = 0;
        return new ImgChannelStackDTO(dummyId, image.getTileRow(), image.getTileColumn(),
                datasetId, spotId, image.tryGetTimePoint(), image.tryGetDepth());
    }

    private void createImages(Map<ImgChannelStackDTO, List<AcquiredImageInStack>> stackImagesMap,
            ImagingChannelsMap channelsMap)
    {
        ImagesToCreate imagesToCreate =
                new ImagesToCreate(new ArrayList<ImgImageDTO>(),
                        new ArrayList<ImgAcquiredImageDTO>());
        for (Entry<ImgChannelStackDTO, List<AcquiredImageInStack>> entry : stackImagesMap
                .entrySet())
        {
            long stackId = entry.getKey().getId();
            addImagesToCreate(imagesToCreate, stackId, channelsMap, entry.getValue());
        }
        dao.addImages(imagesToCreate.getImages());
        dao.addAcquiredImages(imagesToCreate.getAcquiredImages());
    }

    /**
     * Because we can have millions of images, we have to create them in batches. That is why we
     * create all the DTOs first and generate ids for them before they are created in the database.
     * Then we can save everything in one go.
     */
    private void addImagesToCreate(ImagesToCreate imagesToCreate, long stackId,
            ImagingChannelsMap channelsMap, List<AcquiredImageInStack> images)
    {
        List<ImgImageDTO> imageDTOs = imagesToCreate.getImages();
        List<ImgAcquiredImageDTO> acquiredImageDTOs = imagesToCreate.getAcquiredImages();
        for (AcquiredImageInStack image : images)
        {
            long channelTechId = channelsMap.getChannelId(image.getChannelCode());

            ImgImageDTO imageDTO = mkImageWithIdDTO(image.getImageFilePath());
            ImgImageDTO thumbnailDTO = tryMkImageWithIdDTO(image.getThumbnailPathOrNull());
            Long thumbnailId = thumbnailDTO == null ? null : thumbnailDTO.getId();
            ImgAcquiredImageDTO acquiredImage =
                    mkAcquiredImage(stackId, channelTechId, imageDTO.getId(), thumbnailId);

            imageDTOs.add(imageDTO);
            if (thumbnailDTO != null)
            {
                imageDTOs.add(thumbnailDTO);
            }
            acquiredImageDTOs.add(acquiredImage);
        }
    }

    private static class ImagesToCreate
    {
        private final List<ImgImageDTO> images;

        private final List<ImgAcquiredImageDTO> acquiredImages;

        public ImagesToCreate(List<ImgImageDTO> images, List<ImgAcquiredImageDTO> acquiredImages)
        {
            super();
            this.images = images;
            this.acquiredImages = acquiredImages;
        }

        public List<ImgImageDTO> getImages()
        {
            return images;
        }

        public List<ImgAcquiredImageDTO> getAcquiredImages()
        {
            return acquiredImages;
        }
    }

    private ImgAcquiredImageDTO mkAcquiredImage(long stackId, long channelTechId, long imageId,
            Long thumbnailId)
    {
        ImgAcquiredImageDTO acquiredImage = new ImgAcquiredImageDTO();
        acquiredImage.setImageId(imageId);
        acquiredImage.setThumbnailId(thumbnailId);
        acquiredImage.setChannelStackId(stackId);
        acquiredImage.setChannelId(channelTechId);
        return acquiredImage;
    }

    private ImgImageDTO tryMkImageWithIdDTO(RelativeImageReference imageReferenceOrNull)
    {
        if (imageReferenceOrNull == null)
        {
            return null;
        }
        return mkImageWithIdDTO(imageReferenceOrNull);
    }

    private ImgImageDTO mkImageWithIdDTO(RelativeImageReference imageReferenceOrNull)
    {
        ImgImageDTO dto =
                new ImgImageDTO(dao.createImageId(), imageReferenceOrNull.getRelativeImagePath(),
                        imageReferenceOrNull.tryGetPage(),
                        imageReferenceOrNull.tryGetColorComponent());
        return dto;
    }
}
