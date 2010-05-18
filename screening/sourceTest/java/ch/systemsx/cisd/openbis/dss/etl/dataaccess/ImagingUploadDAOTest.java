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

import java.sql.SQLException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    public void testAddAll()
    {
        // it just tests if sql is correct
        final long experimentId = addExperiment();
        final long containerId = addContainer(experimentId);
        final long datasetId = addDataset(containerId);
        final long spotId = addSpot(containerId);
        final long channelId1 = addDatasetChannel(datasetId);
        final long channelId2 = addExperimentChannel(experimentId);
        final long channelStackId = addChannelStack(datasetId, spotId);
        final long imageId1 = addImage();
        final long imageId2 = addImage();
        addAcquiredImage(imageId1, channelStackId, channelId1);
        addAcquiredImage(imageId2, channelStackId, channelId2);
    }

    private long addImage()
    {
        final ImgImageDTO image = new ImgImageDTO("path", 1, ColorComponent.RED);
        return dao.addImage(image);
    }

    private long addExperiment()
    {
        final long result = dao.addExperiment("exp");
        return result;
    }

    private long addContainer(long experimentId)
    {
        final ImgContainerDTO container = new ImgContainerDTO("cId", 1, 1, experimentId);
        return dao.addContainer(container);
    }

    private long addDataset(long containerId)
    {
        final ImgDatasetDTO dataset = new ImgDatasetDTO("dsId", 1, 1, containerId);
        return dao.addDataset(dataset);
    }

    private long addSpot(long containerId)
    {
        final ImgSpotDTO spot = new ImgSpotDTO("sId", 1, 1, containerId);
        return dao.addSpot(spot);
    }

    private long addDatasetChannel(long datasetId)
    {
        final ImgChannelDTO channel =
                ImgChannelDTO.createDatasetChannel("name", "desc", 1, datasetId);
        return dao.addChannel(channel);
    }

    private long addExperimentChannel(long experimentId)
    {
        final ImgChannelDTO channel =
                ImgChannelDTO.createExperimentChannel("name", "desc", 1, experimentId);
        return dao.addChannel(channel);
    }

    private long addChannelStack(long datasetId, long spotId)
    {
        final ImgChannelStackDTO channelStack = new ImgChannelStackDTO(1, 1, datasetId, spotId);
        return dao.addChannelStack(channelStack);
    }

    private long addAcquiredImage(long imageId, long channelStackId, long channelId)
    {
        final ImgAcquiredImageDTO acquiredImage =
                new ImgAcquiredImageDTO(imageId, channelStackId, channelId);
        return dao.addAcquiredImage(acquiredImage);
    }

}
