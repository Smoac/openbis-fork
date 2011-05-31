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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelColor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractDBTest;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAcquiredImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgSpotDTO;

/**
 * Tests for {@link IImagingQueryDAO}.
 * 
 * @author Piotr Buczek
 */
@Test(groups =
    { "db", "screening" })
public class ImagingQueryDAOTest extends AbstractDBTest
{

    private static final String CHANNEL_LABEL = "Channel Label";

    private static final ImageChannelColor CHANNEL_COLOR = ImageChannelColor.RED;

    private static final String PAGE = "1-2-3-4";

    private static final int Y_TILE_ROW = 2;

    private static final int X_TILE_COLUMN = 1;

    private static final int WAVELENGTH = 1;

    private static final int Y_WELL_ROW = 2;

    private static final int X_WELL_COLUMN = 1;

    private static final String PATH1 = "path1";

    private static final String PATH2 = "path2";

    private static final String MICROSCOPY_IMAGE_PATH1 = "path_MICROSCOPY_IMAGE_PATH1";

    private static final String MICROSCOPY_IMAGE_PATH2 = "path_MICROSCOPY_IMAGE_PATH2";

    private static final String EXP_PERM_ID = "expId";

    private static final String CONTAINER_PERM_ID = "cId";

    private static final String DS_PERM_ID = "dsId";

    private static final String DS_CHANNEL = "dsChannel";

    private static final String CHANNEL_DESCRIPTION = "channel desc";

    private static final String EXP_CHANNEL = "expChannel";

    private static final Float TIMEPOINT = 1.3F;

    private static final Float DEPTH = null;

    private IImagingQueryDAO dao;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        dao = QueryTool.getQuery(datasource, IImagingQueryDAO.class);
    }

    @Test
    public void testInit()
    {
        // tests that parameter bindings in all queries are correct
    }

    // adding rows to tables

    @Test
    public void testCreateMicroscopyDataset()
    {
        long datasetId = addDataset(DS_PERM_ID + "2", null);
        long channelId = addDatasetChannel(datasetId);
        long channelStackId = addChannelStack(datasetId, null, null, null);
        long imageId1 = addImage(MICROSCOPY_IMAGE_PATH1, ColorComponent.BLUE);
        addAcquiredImage(imageId1, channelStackId, channelId);

        List<ImgChannelStackDTO> stack = dao.listSpotlessChannelStacks(datasetId);
        assertEquals(1, stack.size());
        ImgChannelStackDTO stackDTO = stack.get(0);
        assertEquals(channelStackId, stackDTO.getId());

        testGetMicroscopyImage(datasetId, channelStackId, channelId);
    }

    private void testGetMicroscopyImage(final long datasetId, final long channelStackId,
            final long channelId)
    {
        Location tileLocation = new Location(X_TILE_COLUMN, Y_TILE_ROW);
        ImgImageDTO image1 = dao.tryGetMicroscopyImage(channelId, datasetId, tileLocation);
        assertEquals(MICROSCOPY_IMAGE_PATH1, image1.getFilePath());
        assertEquals(ColorComponent.BLUE, image1.getColorComponent());

        ImgImageDTO representativeImage =
                dao.tryGetMicroscopyRepresentativeImage(datasetId, channelId);
        assertEquals(image1, representativeImage);

        ImgImageDTO image1bis = dao.tryGetImage(channelId, channelStackId, datasetId);
        assertEquals(image1, image1bis);

        ImgImageDTO thumbnail1 = dao.tryGetMicroscopyThumbnail(channelId, datasetId, tileLocation);
        assertEquals(image1, thumbnail1);

        ImgImageDTO representativeThumbnail =
                dao.tryGetMicroscopyRepresentativeThumbnail(datasetId, channelId);
        assertEquals(image1, representativeThumbnail);

        ImgImageDTO thumbnail1bis = dao.tryGetThumbnail(channelId, channelStackId, datasetId);
        assertEquals(thumbnail1, thumbnail1bis);
    }

    // FIXME 2010-12-10, Tomasz Pylak: uncomment when representative image functionality will be
    // implemented
    // @Test
    // public void testCreateMicroscopyDatasetWithSeries()
    // {
    // long datasetId = addDataset(DS_PERM_ID + "3", null);
    // long channelId = addDatasetChannel(datasetId);
    // long channelStackId = addChannelStack(datasetId, null, TIMEPOINT, DEPTH);
    // long imageId1 = addImage(MICROSCOPY_IMAGE_PATH2, ColorComponent.BLUE);
    // addAcquiredImage(imageId1, channelStackId, channelId);
    //
    // Location tileLocation = new Location(X_TILE_COLUMN, Y_TILE_ROW);
    // // if there are any time/depth series, this method should return null
    // ImgImageDTO image1 = dao.tryGetMicroscopyImage(channelId, datasetId, tileLocation);
    // assertNull(image1);
    //
    // // but this one should not!
    // ImgImageDTO image1bis = dao.tryGetImage(channelId, channelStackId, datasetId);
    // assertEquals(MICROSCOPY_IMAGE_PATH2, image1bis.getFilePath());
    //
    // // the same applies for thumbnails
    // ImgImageDTO thumbnail1 = dao.tryGetMicroscopyThumbnail(channelId, datasetId, tileLocation);
    // assertNull(thumbnail1);
    //
    // ImgImageDTO thumbnail1bis = dao.tryGetThumbnail(channelId, channelStackId, datasetId);
    // assertEquals(image1bis, thumbnail1bis);
    // }

    @Test
    public void testCreateMicroscopyDatasetWithSeries()
    {
        long datasetId = addDataset(DS_PERM_ID + "3", null);
        long channelId = addDatasetChannel(datasetId);
        long channelStackId = addChannelStack(datasetId, null, TIMEPOINT, DEPTH);
        long imageId1 = addImage(MICROSCOPY_IMAGE_PATH2, ColorComponent.BLUE);
        addAcquiredImage(imageId1, channelStackId, channelId);

        Location tileLocation = new Location(X_TILE_COLUMN, Y_TILE_ROW);
        // if there are any time/depth series, this method should return null
        ImgImageDTO image1 = dao.tryGetMicroscopyImage(channelId, datasetId, tileLocation);

        // but this one should not!
        ImgImageDTO image1bis = dao.tryGetImage(channelId, channelStackId, datasetId);
        assertEquals(MICROSCOPY_IMAGE_PATH2, image1bis.getFilePath());
        assertEquals(image1, image1bis);

        // the same applies for thumbnails
        ImgImageDTO thumbnail1 = dao.tryGetMicroscopyThumbnail(channelId, datasetId, tileLocation);

        ImgImageDTO thumbnail1bis = dao.tryGetThumbnail(channelId, channelStackId, datasetId);
        assertEquals(image1bis, thumbnail1bis);
        assertEquals(thumbnail1, thumbnail1bis);

    }

    @Test
    public void testCreateFullExperimentAndGetImages()
    {
        // create experiment, container, dataset, spot, ds channel and exp channel
        final long experimentId = addExperiment();
        final long containerId = addContainer(experimentId);
        final long datasetId = addDataset(DS_PERM_ID, containerId);
        final long spotId = addSpot(containerId);
        final long datasetChannelId1 = addDatasetChannel(datasetId);
        assertTrue(dao.hasDatasetChannels(DS_PERM_ID));
        final long experimentChannelId2 = addExperimentChannel(experimentId);

        testChannelMethods(experimentId, datasetId, datasetChannelId1, experimentChannelId2);

        // create channel stack, images and acquired images
        final long channelStackId = addChannelStack(datasetId, spotId, null, null);
        final long imageId1 = addImage(PATH1, ColorComponent.BLUE);
        final long imageId2 = addImage(PATH2, ColorComponent.RED);
        addAcquiredImage(imageId1, channelStackId, datasetChannelId1);
        addAcquiredImage(imageId2, channelStackId, experimentChannelId2);

        testGetHCSImage(datasetId, channelStackId, datasetChannelId1, experimentChannelId2, spotId);
        testListChannelStacksAndSpots(datasetId, channelStackId, spotId);
    }

    private void testListChannelStacksAndSpots(long datasetId, long channelStackId, long spotId)
    {
        ImgChannelStackDTO stackDTO = testListChannelStacks(datasetId, channelStackId);

        assertEquals(spotId, stackDTO.getSpotId().longValue());
        assertEquals(datasetId, stackDTO.getDatasetId());
        assertNull(stackDTO.getT());
        assertNull(stackDTO.getZ());
        assertEquals(Y_WELL_ROW, stackDTO.getRow().intValue());
        assertEquals(X_WELL_COLUMN, stackDTO.getColumn().intValue());
    }

    private ImgChannelStackDTO testListChannelStacks(long datasetId, long channelStackId)
    {
        List<ImgChannelStackDTO> stack =
                dao.listChannelStacks(datasetId, X_WELL_COLUMN, Y_WELL_ROW);
        assertEquals(1, stack.size());
        ImgChannelStackDTO stackDTO = stack.get(0);
        assertEquals(channelStackId, stackDTO.getId());
        return stackDTO;
    }

    private void testGetHCSImage(final long datasetId, final long channelStackId,
            final long channelId1, final long channelId2, long spotId)
    {
        Location tileLocation = new Location(X_TILE_COLUMN, Y_TILE_ROW);
        Location wellLocation = new Location(X_WELL_COLUMN, Y_WELL_ROW);
        ImgImageDTO image1 = dao.tryGetHCSImage(channelId1, datasetId, tileLocation, wellLocation);
        assertEquals(PATH1, image1.getFilePath());
        assertEquals(ColorComponent.BLUE, image1.getColorComponent());

        ImgImageDTO representativeImage =
                dao.tryGetHCSRepresentativeImage(datasetId, wellLocation, channelId1);
        assertEquals(image1, representativeImage);

        ImgImageDTO image1bis = dao.tryGetImage(channelId1, channelStackId, datasetId);
        assertEquals(image1, image1bis);

        ImgImageDTO thumbnail1 =
                dao.tryGetHCSThumbnail(channelId1, datasetId, tileLocation, wellLocation);
        assertEquals(image1, thumbnail1);

        ImgImageDTO representativeThumbnail =
                dao.tryGetHCSRepresentativeThumbnail(datasetId, wellLocation, channelId1);
        assertEquals(thumbnail1, representativeThumbnail);

        ImgImageDTO thumbnail1bis = dao.tryGetThumbnail(channelId1, channelStackId, datasetId);
        assertEquals(thumbnail1, thumbnail1bis);

        ImgImageDTO image2 = dao.tryGetHCSImage(channelId2, datasetId, wellLocation, tileLocation);
        assertEquals(PATH2, image2.getFilePath());
        assertEquals(ColorComponent.RED, image2.getColorComponent());

        ImgImageDTO image2bis = dao.tryGetImage(channelId2, channelStackId, datasetId);
        assertEquals(image2, image2bis);

        List<ImgImageEnrichedDTO> allImages = dao.listHCSImages(DS_PERM_ID, DS_CHANNEL);
        assertEquals(1, allImages.size());
        assertEquals(spotId, allImages.get(0).getSpotId());
    }

    private void testChannelMethods(final long experimentId, final long datasetId,
            final long datasetChannelId, final long experimentChannelId)
    {
        List<ImgChannelDTO> datasetChannels = dao.getChannelsByDatasetId(datasetId);
        assertEquals(1, datasetChannels.size());
        ImgChannelDTO datasetChannel = datasetChannels.get(0);

        assertEquals("DSCHANNEL", datasetChannel.getCode());
        assertEquals(CHANNEL_LABEL, datasetChannel.getLabel());
        assertEquals(CHANNEL_COLOR.name(), datasetChannel.getDbChannelColor());
        assertEquals(datasetChannelId, datasetChannel.getId());

        assertEquals(datasetChannel, dao.tryGetChannelForDataset(datasetId, "dsChannel"));

        List<ImgChannelDTO> experimentChannels = dao.getChannelsByExperimentId(experimentId);
        assertEquals(1, experimentChannels.size());
        ImgChannelDTO experimentChannel = experimentChannels.get(0);
        assertEquals("EXPCHANNEL", experimentChannel.getCode());
        assertEquals(CHANNEL_LABEL, experimentChannel.getLabel());
        assertEquals(CHANNEL_COLOR.name(), experimentChannel.getDbChannelColor());
        assertEquals(experimentChannelId, experimentChannel.getId());

        assertEquals(experimentChannel, dao.tryGetChannelForExperiment(experimentId, "expChannel"));

        // test update
        ImgChannelDTO channel = experimentChannels.get(0);
        channel.setDescription("new " + CHANNEL_DESCRIPTION);
        channel.setWavelength(WAVELENGTH + 100);
        dao.updateChannel(channel);
        assertEquals(channel, dao.getChannelsByExperimentId(experimentId).get(0));
    }

    private long addImage(String path, ColorComponent colorComponent)
    {
        final ImgImageDTO image = new ImgImageDTO(dao.createImageId(), path, PAGE, colorComponent);
        dao.addImages(Arrays.asList(image));
        return image.getId();
    }

    private long addExperiment()
    {
        final String permId = EXP_PERM_ID;
        final long experimentId = dao.addExperiment(permId);

        assertEquals(experimentId, dao.tryGetExperimentByPermId(permId).getId());

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

        List<ImgContainerDTO> containers = dao.listContainersByIds(containerId);
        assertEquals(1, containers.size());
        assertEquals(loadedContainer, containers.get(0));

        return containerId;
    }

    private long addDataset(String permId, Long containerIdOrNull)
    {
        final Integer fieldsWidth = 1;
        final Integer fieldsHeight = 2;
        String libraryName = "BIOFORMATS";
        String libraryReader = "TIFF-READER";
        final ImgDatasetDTO dataset =
                new ImgDatasetDTO(permId, fieldsHeight, fieldsWidth, containerIdOrNull, false,
                        libraryName, libraryReader);
        final long datasetId = dao.addDataset(dataset);

        final ImgDatasetDTO loadedDataset = dao.tryGetDatasetByPermId(permId);
        assertNotNull(loadedDataset);
        assertEquals(permId, loadedDataset.getPermId());
        assertEquals(fieldsWidth, loadedDataset.getFieldNumberOfColumns());
        assertEquals(fieldsHeight, loadedDataset.getFieldNumberOfRows());
        assertEquals(containerIdOrNull, loadedDataset.getContainerId());
        assertEquals(libraryName, loadedDataset.getImageLibraryName());
        assertEquals(libraryReader, loadedDataset.getImageReaderName());

        // test listDatasetsByPermId
        final List<ImgDatasetDTO> datasets = dao.listDatasetsByPermId(new String[]
            { permId, "not existing" });
        assertEquals(1, datasets.size());
        assertEquals(loadedDataset, datasets.get(0));

        return datasetId;
    }

    private long addSpot(long containerId)
    {
        final ImgSpotDTO spot = new ImgSpotDTO(Y_WELL_ROW, X_WELL_COLUMN, containerId);
        return dao.addSpot(spot);
    }

    private long addDatasetChannel(long datasetId)
    {
        final ImgChannelDTO channel =
                new ImgChannelDTO(DS_CHANNEL, CHANNEL_DESCRIPTION, WAVELENGTH, datasetId, null,
                        CHANNEL_LABEL, CHANNEL_COLOR);
        return dao.addChannel(channel);
    }

    private long addExperimentChannel(long experimentId)
    {
        final ImgChannelDTO channel =
                new ImgChannelDTO(EXP_CHANNEL, CHANNEL_DESCRIPTION, WAVELENGTH, null, experimentId,
                        CHANNEL_LABEL, CHANNEL_COLOR);
        return dao.addChannel(channel);
    }

    private long addChannelStack(long datasetId, Long spotIdOrNull, Float timeOrNull,
            Float depthOrNull)
    {
        final ImgChannelStackDTO channelStack =
                new ImgChannelStackDTO(dao.createChannelStackId(), Y_TILE_ROW, X_TILE_COLUMN,
                        datasetId, spotIdOrNull, timeOrNull, depthOrNull, null, true);
        dao.addChannelStacks(Arrays.asList(channelStack));
        return channelStack.getId();
    }

    private void addAcquiredImage(long imageId, long channelStackId, long channelId)
    {
        final ImgAcquiredImageDTO acquiredImage = new ImgAcquiredImageDTO();
        acquiredImage.setImageId(imageId);
        // we set the same image to be its thumbnail to simplify tests
        acquiredImage.setThumbnailId(imageId);

        acquiredImage.setChannelStackId(channelStackId);
        acquiredImage.setChannelId(channelId);

        dao.addAcquiredImages(Arrays.asList(acquiredImage));
    }

}
