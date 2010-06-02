/*
 * Copyright 2009 ETH Zuerich, CISD
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.sql.SQLException;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgImageDTO.ColorComponent;

/**
 * Tests for {@link IImagingUploadDAO}.
 * 
 * @author Piotr Buczek
 */
@Test(groups =
    { "db", "screening" })
public class ImagingUploadDAOTest extends AbstractDBTest
{

    private static final String PERM_ID = "PERM_ID";

    private static final int PAGE = 1;

    private static final int Y_TILE_ROW = 2;

    private static final int X_TILE_COLUMN = 1;

    private static final int WAVELENGTH = 1;

    private static final int Y_WELL_ROW = 2;

    private static final int X_WELL_COLUMN = 1;

    private static final String PATH1 = "path1";

    private static final String PATH2 = "path2";

    private static final String EXP_PERM_ID = "expId";

    private static final String CONTAINER_PERM_ID = "cId";

    private static final String DS_PERM_ID = "dsId";

    private static final String DS_CHANNEL = "dsChannel";

    private static final String CHANNEL_DESCRIPTION = "channel desc";

    private static final String EXP_CHANNEL = "expChannel";

    private IImagingUploadDAO dao;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        dao = DBUtils.getQuery(datasource, IImagingUploadDAO.class);
    }

    @Test
    public void testInit()
    {
        // tests that parameter bindings in all queries are correct
    }

    // adding rows to tables

    @Test
    public void testCreateFullExperimentAndGetImages()
    {
        // create experiment, container, dataset, spot, ds channel and exp channel
        final long experimentId = addExperiment();
        final long containerId = addContainer(experimentId);
        final long datasetId = addDataset(containerId);
        final long spotId = addSpot(containerId);
        final long channelId1 = addDatasetChannel(datasetId);
        final long channelId2 = addExperimentChannel(experimentId);

        testChannelMethods(experimentId, datasetId, channelId1, channelId2);

        // create channel stack, images and acquired images
        final long channelStackId = addChannelStack(datasetId, spotId);
        final long imageId1 = addImage(PATH1, ColorComponent.BLUE);
        final long imageId2 = addImage(PATH2, ColorComponent.RED);
        addAcquiredImage(imageId1, channelStackId, channelId1);
        addAcquiredImage(imageId2, channelStackId, channelId2);

        testGetImage(datasetId, channelId1, channelId2);
    }

    private void testGetImage(final long datasetId, final long channelId1, final long channelId2)
    {
        ImgImageDTO image1 =
                dao.tryGetImage(channelId1, datasetId, new Location(X_WELL_COLUMN, Y_WELL_ROW),
                        new Location(X_TILE_COLUMN, Y_TILE_ROW));
        assertEquals(PATH1, image1.getFilePath());
        assertEquals(ColorComponent.BLUE, image1.getColorComponent());

        ImgImageDTO image2 =
                dao.tryGetImage(channelId2, datasetId, new Location(X_WELL_COLUMN, Y_WELL_ROW),
                        new Location(X_TILE_COLUMN, Y_TILE_ROW));
        assertEquals(PATH2, image2.getFilePath());
        assertEquals(ColorComponent.RED, image2.getColorComponent());
    }

    private void testChannelMethods(final long experimentId, final long datasetId,
            final long channelId1, final long channelId2)
    {
        // test countChannelByDatasetIdOrExperimentId
        assertEquals(2, dao.countChannelByDatasetIdOrExperimentId(datasetId, experimentId));

        // test getChannelIdsByDatasetIdOrExperimentId
        long[] channels = dao.getChannelIdsByDatasetIdOrExperimentId(datasetId, experimentId);
        assertEquals(2, channels.length);
        AssertJUnit.assertTrue(channels[0] == channelId1 && channels[1] == channelId2
                || channels[1] == channelId1 && channels[0] == channelId2);

        List<ImgChannelDTO> experimentChannels = dao.getChannelsByExperimentId(experimentId);
        assertEquals(1, experimentChannels.size());

        // test update
        ImgChannelDTO channel = experimentChannels.get(0);
        channel.setDescription("new " + CHANNEL_DESCRIPTION);
        channel.setWavelength(WAVELENGTH + 100);
        dao.updateChannel(channel);
        assertEquals(channel, dao.getChannelsByExperimentId(experimentId).get(0));
    }

    private long addImage(String path, ColorComponent colorComponent)
    {
        final ImgImageDTO image = new ImgImageDTO(path, PAGE, colorComponent);
        return dao.addImage(image);
    }

    private long addExperiment()
    {
        final String permId = EXP_PERM_ID;
        final long experimentId = dao.addExperiment(permId);

        assertEquals(Long.valueOf(experimentId), dao.tryGetExperimentIdByPermId(permId));

        return experimentId;
    }

    private long addContainer(long experimentId)
    {
        final String permId = CONTAINER_PERM_ID;
        final Integer spotWidth = 1;
        final Integer spotHeight = 2;
        final ImgContainerDTO container =
                new ImgContainerDTO(permId, spotHeight, spotWidth, experimentId);
        final Long containerId = dao.addContainer(container);
        // test tryGetContainerIdPermId
        assertEquals(containerId, dao.tryGetContainerIdPermId(permId));
        final ImgContainerDTO loadedContainer = dao.getContainerById(containerId);
        assertNotNull(loadedContainer);
        assertEquals(permId, loadedContainer.getPermId());
        assertEquals(spotWidth, loadedContainer.getNumberOfColumns());
        assertEquals(spotHeight, loadedContainer.getNumberOfRows());
        assertEquals(experimentId, loadedContainer.getExperimentId());

        return containerId;
    }

    private long addDataset(long containerId)
    {
        final String permId = DS_PERM_ID;
        final Integer fieldsWidth = 1;
        final Integer fieldsHeight = 2;
        final ImgDatasetDTO dataset =
                new ImgDatasetDTO(permId, ".", fieldsHeight, fieldsWidth, containerId);
        final long datasetId = dao.addDataset(dataset);

        final ImgDatasetDTO loadedDataset = dao.tryGetDatasetByPermId(DS_PERM_ID);
        assertNotNull(loadedDataset);
        assertEquals(permId, loadedDataset.getPermId());
        assertEquals(fieldsWidth, loadedDataset.getFieldNumberOfColumns());
        assertEquals(fieldsHeight, loadedDataset.getFieldNumberOfRows());
        assertEquals(containerId, loadedDataset.getContainerId());

        return datasetId;
    }

    private long addSpot(long containerId)
    {
        final ImgSpotDTO spot = new ImgSpotDTO(Y_WELL_ROW, X_WELL_COLUMN, containerId);
        spot.setPermId(PERM_ID);
        return dao.addSpot(spot);
    }

    private long addDatasetChannel(long datasetId)
    {
        final ImgChannelDTO channel =
                ImgChannelDTO.createDatasetChannel(DS_CHANNEL, CHANNEL_DESCRIPTION, WAVELENGTH,
                        datasetId);
        return dao.addChannel(channel);
    }

    private long addExperimentChannel(long experimentId)
    {
        final ImgChannelDTO channel =
                ImgChannelDTO.createExperimentChannel(EXP_CHANNEL, CHANNEL_DESCRIPTION, WAVELENGTH,
                        experimentId);
        return dao.addChannel(channel);
    }

    private long addChannelStack(long datasetId, long spotId)
    {
        final ImgChannelStackDTO channelStack =
                new ImgChannelStackDTO(Y_TILE_ROW, X_TILE_COLUMN, datasetId, spotId);
        return dao.addChannelStack(channelStack);
    }

    private long addAcquiredImage(long imageId, long channelStackId, long channelId)
    {
        final ImgAcquiredImageDTO acquiredImage =
                new ImgAcquiredImageDTO(imageId, channelStackId, channelId);
        return dao.addAcquiredImage(acquiredImage);
    }

}
