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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.rinn.restrictions.Private;
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
        Set<ImgChannelStackDTO> channelStacks = map.keySet();
        setChannelStackIds(channelStacks);
        setChannelStackRepresentatives(channelStacks);
        return map;
    }

    private void setChannelStackRepresentatives(Set<ImgChannelStackDTO> channelStacks)
    {
        Set<ImgChannelStackDTO> representatives =
                ChannelStackRepresentativesOracle.calculateRepresentatives(channelStacks);
        for (ImgChannelStackDTO channelStack : channelStacks)
        {
            boolean isRepresentative = representatives.contains(channelStack);
            channelStack.setRepresentative(isRepresentative);
        }
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
                datasetId, spotId, image.tryGetTimePoint(), image.tryGetDepth(),
                image.tryGetSeriesNumber(), false);
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

    @Private
    static final class ChannelStackRepresentativesOracle
    {
        /**
         * For all images of one spot chooses the 'smallest' element as an representative. If there
         * are no spots than the smallest element of all specified images is chosen.
         */
        public static Set<ImgChannelStackDTO> calculateRepresentatives(
                Set<ImgChannelStackDTO> images)
        {
            Map<Long/* spot or null */, List<ImgChannelStackDTO>> mapBySpot =
                    createMapBySpot(images);
            Comparator<? super ImgChannelStackDTO> spotChannelStacksComparator =
                    createChannelStacksComparator();
            for (List<ImgChannelStackDTO> spotChannelStacks : mapBySpot.values())
            {
                Collections.sort(spotChannelStacks, spotChannelStacksComparator);
            }

            Set<ImgChannelStackDTO> representatives = new HashSet<ImgChannelStackDTO>();
            for (List<ImgChannelStackDTO> spotChannelStacks : mapBySpot.values())
            {
                representatives.add(spotChannelStacks.get(0));
            }
            return representatives;
        }

        private static Comparator<? super ImgChannelStackDTO> createChannelStacksComparator()
        {
            return new Comparator<ImgChannelStackDTO>()
                {
                    public int compare(ImgChannelStackDTO o1, ImgChannelStackDTO o2)
                    {
                        int cmp = compareNullable(o1.getRow(), o2.getRow());
                        if (cmp != 0)
                            return cmp;
                        cmp = compareNullable(o1.getColumn(), o2.getColumn());
                        if (cmp != 0)
                            return cmp;
                        cmp = compareNullable(o1.getT(), o2.getT());
                        if (cmp != 0)
                            return cmp;
                        cmp = compareNullable(o1.getZ(), o2.getZ());
                        if (cmp != 0)
                            return cmp;
                        cmp = compareNullable(o1.getSeriesNumber(), o2.getSeriesNumber());
                        return cmp;
                    }

                    private <T extends Comparable<T>> int compareNullable(T v1OrNull, T v2OrNull)
                    {
                        if (v1OrNull == null)
                        {
                            return v2OrNull == null ? 0 : -1;
                        } else
                        {
                            return v2OrNull == null ? 1 : v1OrNull.compareTo(v2OrNull);
                        }
                    }
                };
        }

        private static Map<Long/* spot or null */, List<ImgChannelStackDTO>> createMapBySpot(
                Set<ImgChannelStackDTO> channelStacks)
        {
            Map<Long, List<ImgChannelStackDTO>> mapBySpot =
                    new HashMap<Long, List<ImgChannelStackDTO>>();
            for (ImgChannelStackDTO channelStack : channelStacks)
            {
                Long spotId = channelStack.getSpotId();
                List<ImgChannelStackDTO> spotChannelStacks = mapBySpot.get(spotId);
                if (spotChannelStacks == null)
                {
                    spotChannelStacks = new ArrayList<ImgChannelStackDTO>();
                }
                spotChannelStacks.add(channelStack);
                mapBySpot.put(spotId, spotChannelStacks);
            }
            return mapBySpot;
        }
    }
}
