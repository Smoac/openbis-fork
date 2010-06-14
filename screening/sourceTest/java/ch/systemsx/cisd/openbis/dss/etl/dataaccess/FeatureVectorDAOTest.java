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

import java.sql.SQLException;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link IFeatureVectorDAO}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups =
    { "db", "screening" })
public class FeatureVectorDAOTest extends AbstractDBTest
{
    private IFeatureVectorDAO dao;

    private ImgDatasetDTO dataset;

    private static final String EXP_PERM_ID = "expFvId";

    private static final String CONTAINER_PERM_ID = "cFvId";

    private static final String DS_PERM_ID = "dsFvId";

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        dao = DBUtils.getQuery(datasource, IFeatureVectorDAO.class);
    }

    private ImgDatasetDTO createDataSet()
    {
        IImagingUploadDAO imagingDao = DBUtils.getQuery(datasource, IImagingUploadDAO.class);

        // Create an Experiment
        final long experimentId = imagingDao.addExperiment(EXP_PERM_ID);

        // Create a container
        final Integer spotWidth = 1;
        final Integer spotHeight = 2;
        final ImgContainerDTO container =
                new ImgContainerDTO(CONTAINER_PERM_ID, spotHeight, spotWidth, experimentId);
        final Long containerId = imagingDao.addContainer(container);

        final Integer fieldsWidth = 1;
        final Integer fieldsHeight = 2;
        final ImgDatasetDTO ds =
                new ImgDatasetDTO(DS_PERM_ID, fieldsHeight, fieldsWidth, containerId);
        final long datasetId = imagingDao.addDataset(ds);

        imagingDao.commit();
        imagingDao.close();

        ds.setId(datasetId);
        return ds;
    }

    @Test
    public void testInit()
    {
        // tests that parameter bindings in all queries are correct
    }

    // adding rows to tables

    @Test
    public void testCreateFeatureValues()
    {
        // Initialize the data set
        dataset = createDataSet();

        createFeatureDef(dataset);
        List<ImgFeatureDefDTO> featureDefs = dao.listFeatureDefsByDataSetId(dataset.getId());
        assertEquals(1, featureDefs.size());

    }

    private long createFeatureDef(ImgDatasetDTO dataSet)
    {
        // Attach a feature def to it
        ImgFeatureDefDTO featureDef = new ImgFeatureDefDTO("test", "Test", dataSet.getId());
        return dao.addFeatureDef(featureDef);
    }
}
