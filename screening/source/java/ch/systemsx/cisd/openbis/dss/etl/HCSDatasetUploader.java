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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgSpotDTO;

/**
 * @author Tomasz Pylak
 */
class HCSDatasetUploader
{
    public static void upload(IImagingUploadDAO dao, ScreeningContainerDatasetInfo info,
            List<AcquiredPlateImage> images)
    {
        new HCSDatasetUploader(dao).upload(info, images);
    }

    private final IImagingUploadDAO dao;

    private HCSDatasetUploader(IImagingUploadDAO dao)
    {
        this.dao = dao;
    }

    private void upload(ScreeningContainerDatasetInfo info, List<AcquiredPlateImage> images)
    {
        long expId = getOrCreateExperiment(info);
        long contId = getOrCreateContainer(expId, info);
        Long[][] spotIds = getOrCreateSpots(contId, info);
        long datasetId = createDataset(contId, info);
        createImages(images, spotIds, datasetId);
    }

    private void createImages(List<AcquiredPlateImage> images, Long[][] spotIds, long datasetId)
    {
        // TODO 2010-05-18, Tomasz Pylak: create immages and channels
    }

    // returns a matrix of spot tech ids. The matrix[row][col] contains null is spot at (row,col)
    // does not exist. Spot coordinates are 0-based in the matrix.
    private Long[][] getOrCreateSpots(long contId, ScreeningContainerDatasetInfo info)
    {
        String[][] wantedSpotPermIds = info.getSpotPermIds();
        List<ImgSpotDTO> spots = dao.listSpots(asList(wantedSpotPermIds));
        List<ImgSpotDTO> newSpots = findSpotsToCreate(spots, wantedSpotPermIds, contId);
        addSpotsAndSetIds(newSpots);
        spots.addAll(newSpots);
        return createPermIdMatrix(spots, info.getContainerWidth(), info.getContainerHeight());
    }

    private static Long[][] createPermIdMatrix(List<ImgSpotDTO> spots, int width, int height)
    {
        Long[][] matrix = new Long[width][height];
        for (ImgSpotDTO spot : spots)
        {
            matrix[spot.getY() - 1][spot.getX() - 1] = spot.getId();
        }
        return matrix;
    }

    private void addSpotsAndSetIds(List<ImgSpotDTO> spots)
    {
        for (ImgSpotDTO spot : spots)
        {
            long id = dao.addSpot(spot);
            spot.setId(id);
        }
    }

    private static List<ImgSpotDTO> findSpotsToCreate(List<ImgSpotDTO> existingSpots,
            String[][] wantedSpotPermIds, long contId)
    {
        List<ImgSpotDTO> newSpots = new ArrayList<ImgSpotDTO>();
        Set<String> existingPermIds = asPermIdSet(existingSpots);
        for (int row = 0; row < wantedSpotPermIds.length; row++)
        {
            String[] spotRow = wantedSpotPermIds[row];
            for (int col = 0; col < spotRow.length; col++)
            {
                String spotPermId = spotRow[col];
                if (spotPermId != null && existingPermIds.contains(spotPermId) == false)
                {
                    newSpots.add(new ImgSpotDTO(spotPermId, col + 1, row + 1, contId));
                }
            }
        }
        return newSpots;
    }

    private static Set<String> asPermIdSet(List<ImgSpotDTO> spots)
    {
        Set<String> set = new HashSet<String>();
        for (ImgSpotDTO spot : spots)
        {
            set.add(spot.getPermId());
        }
        return set;
    }

    private static List<String> asList(String[][] spotPermIds)
    {
        List<String> result = new ArrayList<String>();
        for (int row = 0; row < spotPermIds.length; row++)
        {
            String[] spotRow = spotPermIds[row];
            for (int col = 0; col < spotRow.length; col++)
            {
                result.add(spotRow[col]);
            }
        }
        return result;
    }

    private long createDataset(long contId, ScreeningContainerDatasetInfo info)
    {
        ImgDatasetDTO dataset =
                new ImgDatasetDTO(info.getDatasetPermId(), info.getTileWidth(), info
                        .getTileHeight(), contId);
        return dao.addDataset(dataset);
    }

    private long getOrCreateContainer(long expId, ScreeningContainerDatasetInfo info)
    {
        String containerPermId = info.getContainerPermId();
        Long containerId = dao.tryGetContainerIdPermId(containerPermId);
        if (containerId != null)
        {
            return containerId;
        } else
        {
            ImgContainerDTO container =
                    new ImgContainerDTO(containerPermId, info.getContainerWidth(), info
                            .getContainerHeight(), expId);
            return dao.addContainer(container);
        }
    }

    private long getOrCreateExperiment(ScreeningContainerDatasetInfo info)
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
