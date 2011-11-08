/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.lemnik.eodsql.QueryTool;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractDBTest;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;

/**
 * @author Kaloyan Enimanev
 */
public class ExperimentMetadaLoaderTest extends AbstractDBTest
{

    private IImagingQueryDAO dao;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        dao = QueryTool.getQuery(datasource, IImagingQueryDAO.class);
    }

    @Test
    public void testTwoEqualContainers()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        createContainerWithDataSets(container(plateGeometry, experimentId), dataset(tileGeometry),
                dataset(tileGeometry));
        createContainerWithDataSets(container(plateGeometry, experimentId), dataset(tileGeometry),
                dataset(tileGeometry));

        ExperimentMetadaLoader loader = createLoader(experimentId);

        assertEquals(plateGeometry, loader.tryGetPlateGeometry());
        assertEquals(tileGeometry, loader.tryGetTileGeometry());

    }

    @Test
    public void testNoPlateGeometry()
    {
        Geometry plateGeometry1 = Geometry.createFromRowColDimensions(15, 10);
        Geometry plateGeometry2 = Geometry.createFromRowColDimensions(16, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        createContainerWithDataSets(container(plateGeometry1, experimentId), dataset(tileGeometry),
                dataset(tileGeometry));
        createContainerWithDataSets(container(plateGeometry2, experimentId), dataset(tileGeometry),
                dataset(tileGeometry));

        ExperimentMetadaLoader loader = createLoader(experimentId);

        assertEquals(null, loader.tryGetPlateGeometry());
        assertEquals(tileGeometry, loader.tryGetTileGeometry());

    }

    @Test
    public void testNoTileGeometry()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry1 = Geometry.createFromRowColDimensions(2, 3);
        Geometry tileGeometry2 = Geometry.createFromRowColDimensions(3, 2);
        long experimentId = createExperiment();
        createContainerWithDataSets(container(plateGeometry, experimentId), dataset(tileGeometry1),
                dataset(tileGeometry1));
        createContainerWithDataSets(container(plateGeometry, experimentId), dataset(tileGeometry2),
                dataset(tileGeometry2));

        ExperimentMetadaLoader loader = createLoader(experimentId);

        assertEquals(plateGeometry, loader.tryGetPlateGeometry());
        assertEquals(null, loader.tryGetTileGeometry());
    }

    @Test
    public void testNonUniqueChannelsFiltered()
    {
        Mockery context = new Mockery();
        final IImagingReadonlyQueryDAO dao1 = context.mock(IImagingReadonlyQueryDAO.class, "dao1");
        final IImagingReadonlyQueryDAO dao2 = context.mock(IImagingReadonlyQueryDAO.class, "dao2");

        final Long experimentId = 0L;
        final ImgChannelDTO channel1 = channel("C1");
        final ImgChannelDTO channel2 = channel("C2");
        context.checking(new Expectations()
            {
                {
                    one(dao1).getChannelsByExperimentId(experimentId);
                    will(returnValue(Arrays.asList(channel1)));

                    one(dao2).getChannelsByExperimentId(experimentId);
                    will(returnValue(Arrays.asList(channel1, channel2)));
                }
            });
        ExperimentMetadaLoader loader = createLoader(0L, dao1, dao2);

        List<ImageChannel> channels = loader.getImageChannels();
        assertEquals(2, channels.size());
        assertEquals("C1", channels.get(0).getCode());
        assertEquals("C2", channels.get(1).getCode());

        context.assertIsSatisfied();
    }

    private ImgChannelDTO channel(String code)
    {
        return new ImgChannelDTO(code, null, null, null, 0L, null, 0, 1, 2);
    }

    private long createExperiment()
    {
        return dao.addExperiment(generatePermId());
    }

    private void createContainerWithDataSets(ImgContainerDTO container,
            ImgImageDatasetDTO... datasets)
    {
        long containerId = dao.addContainer(container);
        for (ImgImageDatasetDTO dataset : datasets)
        {
            dataset.setContainerId(containerId);
            dao.addImageDataset(dataset);
        }
    }

    private ImgContainerDTO container(Geometry plateGeometry, long experimentId)
    {
        return new ImgContainerDTO(generatePermId(), plateGeometry.getNumberOfRows(),
                plateGeometry.getNumberOfColumns(), experimentId);
    }

    private ImgImageDatasetDTO dataset(Geometry tileGeometry)
    {
        return new ImgImageDatasetDTO(generatePermId(), tileGeometry.getNumberOfRows(),
                tileGeometry.getNumberOfColumns(), 0L, true, null, null);
    }

    private ExperimentMetadaLoader createLoader(long experimentId)
    {
        return createLoader(experimentId, dao);
    }

    private ExperimentMetadaLoader createLoader(long experimentId, IImagingReadonlyQueryDAO... daos)
    {
        return new ExperimentMetadaLoader(experimentId,
                Arrays.<IImagingReadonlyQueryDAO> asList(daos));
    }

    private String generatePermId()
    {
        return UUID.randomUUID().toString();
    }
}
