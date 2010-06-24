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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.SQLException;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * Tests for {@link IImagingQueryDAO} methods that deal with feature vectors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups =
    { "db", "screening" })
public class FeatureVectorDAOTest extends AbstractDBTest
{
    private IImagingQueryDAO dao;

    private ImgDatasetDTO dataset;

    private static final String EXP_PERM_ID = "expFvId";

    private static final String CONTAINER_PERM_ID = "cFvId";

    private static final String DS_PERM_ID = "dsFvId";

    private static final String TEST_FEATURE_NAME = "test";

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        dao = DBUtils.getQuery(datasource, IImagingQueryDAO.class);
    }

    private ImgDatasetDTO createDataSet()
    {
        IImagingQueryDAO imagingDao = dao;

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

        ImgFeatureDefDTO featureDef = featureDefs.get(0);
        assertEquals(TEST_FEATURE_NAME, featureDef.getName());

        createFeatureValues(featureDef);
        List<ImgFeatureValuesDTO> featureValuesList = dao.getFeatureValues(featureDef);
        assertEquals(1, featureValuesList.size());

        ImgFeatureValuesDTO featureValues = featureValuesList.get(0);
        assertEquals(0.0, featureValues.getT());
        assertEquals(0.0, featureValues.getZ());

        MDDoubleArray spreadsheet = featureValues.getValuesDoubleArray();
        int[] dims =
            { 2, 3 };
        assertEquals(spreadsheet.dimensions().length, dims.length);
        assertEquals(spreadsheet.dimensions()[0], dims[0]);
        assertEquals(spreadsheet.dimensions()[1], dims[1]);

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                assertEquals((double) (i + j), spreadsheet.get(i, j));
            }
        }
    }

    private long createFeatureValues(ImgFeatureDefDTO featureDef)
    {
        int[] dims =
            { 2, 3 };
        MDDoubleArray array = new MDDoubleArray(dims);
        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                array.set(i + j, i, j);
            }
        }
        ImgFeatureValuesDTO featureValues =
                new ImgFeatureValuesDTO(0.0, 0.0, array, featureDef.getId());
        return dao.addFeatureValues(featureValues);
    }

    private long createFeatureDef(ImgDatasetDTO dataSet)
    {
        // Attach a feature def to it
        ImgFeatureDefDTO featureDef =
                new ImgFeatureDefDTO(TEST_FEATURE_NAME, "Test", dataSet.getId());
        return dao.addFeatureDef(featureDef);
    }
}
