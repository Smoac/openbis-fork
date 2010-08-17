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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfoHelper.ExperimentWithChannelsAndContainer;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAcquiredImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgSpotDTO;

/**
 * @author Tomasz Pylak
 */
public class HCSDatasetUploader
{
    public static void upload(IImagingQueryDAO dao, ImageDatasetInfo info,
            List<AcquiredPlateImage> images, Set<HCSImageFileExtractionResult.Channel> channels)
    {
        new HCSDatasetUploader(dao).upload(info, images, channels);
    }

    private final IImagingQueryDAO dao;

    private HCSDatasetUploader(IImagingQueryDAO dao)
    {
        this.dao = dao;
    }

    private void upload(ImageDatasetInfo info, List<AcquiredPlateImage> images,
            Set<HCSImageFileExtractionResult.Channel> channels)
    {
        ExperimentWithChannelsAndContainer basicStruct =
                ScreeningContainerDatasetInfoHelper.getOrCreateExperimentWithChannelsAndContainer(
                        dao, info, channels);
        long contId = basicStruct.getContainerId();
        Map<String, Long/* (tech id */> channelsMap = basicStruct.getChannelsMap();

        Long[][] spotIds = getOrCreateSpots(contId, info, images);
        long datasetId = createDataset(contId, info);

        createImages(images, spotIds, channelsMap, datasetId);
    }

    private static class AcquiredImageInStack
    {
        private final String channelName;

        private final RelativeImageReference imageFilePath;

        private final RelativeImageReference thumbnailPathOrNull;

        public AcquiredImageInStack(String channelName, RelativeImageReference imageFilePath,
                RelativeImageReference thumbnailPathOrNull)
        {
            this.channelName = channelName.toUpperCase();
            this.imageFilePath = imageFilePath;
            this.thumbnailPathOrNull = thumbnailPathOrNull;
        }

        public String getChannelName()
        {
            return channelName;
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

    private void createImages(List<AcquiredPlateImage> images, Long[][] spotIds,
            Map<String, Long> channelsMap, long datasetId)
    {
        Map<ImgChannelStackDTO, List<AcquiredImageInStack>> stackImagesMap =
                makeStackImagesMap(images, spotIds, datasetId);
        dao.addChannelStacks(new ArrayList<ImgChannelStackDTO>(stackImagesMap.keySet()));
        createImages(stackImagesMap, channelsMap);
    }

    private Map<ImgChannelStackDTO, List<AcquiredImageInStack>> makeStackImagesMap(
            List<AcquiredPlateImage> images, Long[][] spotIds, long datasetId)
    {
        Map<ImgChannelStackDTO, List<AcquiredImageInStack>> map =
                new HashMap<ImgChannelStackDTO, List<AcquiredImageInStack>>();
        for (AcquiredPlateImage image : images)
        {
            ImgChannelStackDTO stackDTO = makeStackDtoWithouId(image, spotIds, datasetId);
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

    private static AcquiredImageInStack makeAcquiredImageInStack(AcquiredPlateImage image)
    {
        return new AcquiredImageInStack(image.getChannelName(), image.getImageReference(), image
                .getThumbnailFilePathOrNull());
    }

    private ImgChannelStackDTO makeStackDtoWithouId(AcquiredPlateImage image, Long[][] spotIds,
            long datasetId)
    {
        long spotId = getSpotId(image, spotIds);
        int dummyId = 0;
        return new ImgChannelStackDTO(dummyId, image.getTileRow(), image.getTileColumn(),
                datasetId, spotId, image.tryGetTimePoint(), image.tryGetDepth());
    }

    private static long getSpotId(AcquiredPlateImage image, Long[][] spotIds)
    {
        int wellRow = image.getWellRow();
        int wellColumn = image.getWellColumn();
        Long spotId = spotIds[wellRow - 1][wellColumn - 1];
        assert spotId != null : "no spot for " + image;
        return spotId;
    }

    private void createImages(Map<ImgChannelStackDTO, List<AcquiredImageInStack>> stackImagesMap,
            Map<String, Long> channelsMap)
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
            Map<String, Long> channelsMap, List<AcquiredImageInStack> images)
    {
        List<ImgImageDTO> imageDTOs = imagesToCreate.getImages();
        List<ImgAcquiredImageDTO> acquiredImageDTOs = imagesToCreate.getAcquiredImages();
        for (AcquiredImageInStack image : images)
        {
            long channelTechId = channelsMap.get(image.getChannelName());

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
                        imageReferenceOrNull.tryGetPage(), imageReferenceOrNull
                                .tryGetColorComponent());
        return dto;
    }

    // returns a matrix of spot tech ids. The matrix[row][col] contains null is
    // spot at (row,col)
    // does not exist. Spot coordinates are 0-based in the matrix.
    private Long[][] getOrCreateSpots(long contId, ScreeningContainerDatasetInfo info,
            List<AcquiredPlateImage> images)
    {
        List<ImgSpotDTO> oldSpots = dao.listSpots(contId);
        List<ImgSpotDTO> newSpots =
                createNewSpots(contId, images, oldSpots, info.getContainerRows(), info
                        .getContainerColumns(), info.getContainerPermId());
        newSpots.addAll(oldSpots);
        return makeTechIdMatrix(newSpots, info.getContainerRows(), info.getContainerColumns());
    }

    private List<ImgSpotDTO> createNewSpots(long contId, List<AcquiredPlateImage> images,
            List<ImgSpotDTO> existingSpots, int rows, int columns, String containerPermId)
    {
        Boolean[][] newSpotMatrix = extractNewSpots(rows, columns, images, existingSpots);
        List<ImgSpotDTO> newSpots = makeSpotDTOs(newSpotMatrix, contId);
        enrichWithPermIds(newSpots, containerPermId);
        for (ImgSpotDTO spot : newSpots)
        {
            long id = dao.addSpot(spot);
            spot.setId(id);
        }
        return newSpots;
    }

    private void enrichWithPermIds(List<ImgSpotDTO> newSpots, String containerPermId)
    {
        Map<String, String> permIds = getOrCreateWells(newSpots, containerPermId);
        for (ImgSpotDTO spot : newSpots)
        {
            spot.setPermId(permIds.get(createCoordinate(spot)));
        }
    }

    private Map<String, String> getOrCreateWells(List<ImgSpotDTO> newSpots, String containerPermId)
    {
        IEncapsulatedOpenBISService server = ServiceProvider.getOpenBISService();
        Set<String> codes = new HashSet<String>();
        for (ImgSpotDTO spot : newSpots)
        {
            codes.add(createCoordinate(spot));
        }
        return server.listOrRegisterComponents(containerPermId, codes,
                ScreeningConstants.OLIGO_WELL_TYPE_CODE);
    }

    private static String createCoordinate(ImgSpotDTO spot)
    {
        return Location.tryCreateMatrixCoordinateFromLocation(new Location(spot.getColumn(), spot
                .getRow()));
    }

    private static Boolean[][] extractNewSpots(int rows, int columns,
            List<AcquiredPlateImage> images, List<ImgSpotDTO> existingSpots)
    {
        Boolean[][] spots = extractExistingSpots(rows, columns, images);
        unmarkSpots(existingSpots, spots);
        return spots;
    }

    private static Boolean[][] extractExistingSpots(int rows, int columns,
            List<AcquiredPlateImage> images)
    {
        Boolean[][] spots = new Boolean[rows][columns];
        for (AcquiredPlateImage image : images)
        {
            spots[image.getWellRow() - 1][image.getWellColumn() - 1] = true;
        }
        return spots;
    }

    private static Long[][] makeTechIdMatrix(List<ImgSpotDTO> existingSpots, int rows, int columns)
    {
        Long[][] matrix = new Long[rows][columns];
        for (ImgSpotDTO spot : existingSpots)
        {
            matrix[spot.getRow() - 1][spot.getColumn() - 1] = spot.getId();
        }
        return matrix;
    }

    private static List<ImgSpotDTO> makeSpotDTOs(Boolean[][] spots, long contId)
    {

        List<ImgSpotDTO> newSpots = new ArrayList<ImgSpotDTO>();
        for (int row = 0; row < spots.length; row++)
        {
            Boolean[] spotRow = spots[row];
            for (int col = 0; col < spotRow.length; col++)
            {
                Boolean wanted = spotRow[col];
                if (wanted != null && wanted)
                {
                    newSpots.add(new ImgSpotDTO(row + 1, col + 1, contId));
                }
            }
        }
        return newSpots;
    }

    private static void unmarkSpots(List<ImgSpotDTO> existingSpots, Boolean[][] spotMatrix)
    {
        for (ImgSpotDTO existingSpot : existingSpots)
        {
            spotMatrix[existingSpot.getRow() - 1][existingSpot.getColumn() - 1] = false;
        }
    }

    private long createDataset(long contId, ImageDatasetInfo info)
    {
        return ScreeningContainerDatasetInfoHelper.createImageDataset(dao, info, contId);
    }
}
