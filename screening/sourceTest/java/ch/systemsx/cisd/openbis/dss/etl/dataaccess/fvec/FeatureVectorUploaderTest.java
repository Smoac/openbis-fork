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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess.fvec;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.convert.NativeTaggedArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.AbstractDBTest;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.DBUtils;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CanonicalFeatureVector;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.FeatureVectorUploader;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FeatureVectorUploaderTest extends AbstractDBTest
{
    private static final String EXP_PERM_ID = "expFvuId";

    private static final String CONTAINER_PERM_ID = "cFvuId";

    private static final String DS_PERM_ID = "dsFvuId";

    private static final String TEST_FEATURE_NAME = "test";

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

    @Test
    public void testCreateFeatureValues()
    {
        ScreeningContainerDatasetInfo info = new ScreeningContainerDatasetInfo();
        info.setExperimentPermId(EXP_PERM_ID);
        info.setContainerPermId(CONTAINER_PERM_ID);
        info.setDatasetPermId(DS_PERM_ID);
        FeatureVectorUploader uploader = new FeatureVectorUploader(dao, info);
        ArrayList<CanonicalFeatureVector> fvecs = new ArrayList<CanonicalFeatureVector>();
        new FeatureVectorProducer(fvecs).produce();

        uploader.uploadFeatureVectors(fvecs);

        new FeatureVectorVerifier(fvecs.get(0).getFeatureDef().getDataSetId()).verify();
    }

    // Class should be non-static to call the assert methods
    private class FeatureVectorVerifier
    {
        private final long datasetId;

        // Execution state
        private ImgFeatureDefDTO featureDef;

        private ImgFeatureValuesDTO featureValues;

        private int count;

        private FeatureVectorVerifier(long datasetId)
        {
            this.datasetId = datasetId;
        }

        private void verify()
        {
            List<ImgFeatureDefDTO> featureDefs = dao.listFeatureDefsByDataSetId(datasetId);
            assertEquals(2, featureDefs.size());

            count = 0;
            featureDef = featureDefs.get(count);
            verifyFeatureDef();

            featureDef = featureDefs.get(++count);
            verifyFeatureDef();
        }

        private void verifyFeatureDef()
        {
            assertEquals(TEST_FEATURE_NAME + count, featureDef.getName());
            List<ImgFeatureValuesDTO> featureValuesList = dao.getFeatureValues(featureDef);
            assertEquals(1, featureValuesList.size());
            featureValues = featureValuesList.get(0);
            verifyFeatureValues();
        }

        private void verifyFeatureValues()
        {
            assertEquals(0.0, featureValues.getT());
            assertEquals(0.0, featureValues.getZ());

            MDDoubleArray spreadsheet =
                    NativeTaggedArray.tryToDoubleArray(featureValues.getValues());
            int[] dims =
                { 3, 5 };
            int[] spreadsheetDims = spreadsheet.dimensions();
            assertEquals(spreadsheetDims.length, dims.length);
            assertEquals(spreadsheetDims[0], dims[0]);
            assertEquals(spreadsheetDims[1], dims[1]);

            for (int i = 0; i < dims[0]; ++i)
            {
                for (int j = 0; j < dims[1]; ++j)
                {
                    assertEquals((double) (i + j), spreadsheet.get(i, j));
                }
            }
        }
    }

    private static class FeatureVectorProducer
    {
        private final ArrayList<CanonicalFeatureVector> fvecs;

        private FeatureVectorProducer(ArrayList<CanonicalFeatureVector> fvecs)
        {
            this.fvecs = fvecs;
        }

        private void produce()
        {

            fvecs.add(createFeatureVector(0, 3, 5));
            fvecs.add(createFeatureVector(1, 3, 5));
        }

        private CanonicalFeatureVector createFeatureVector(int i, int rowCount, int columnCount)
        {
            CanonicalFeatureVector fvec = new CanonicalFeatureVector();

            String featureName = TEST_FEATURE_NAME + i;
            String featureDesc = featureName + " desc";
            ImgFeatureDefDTO featureDef = new ImgFeatureDefDTO(featureName, featureDesc, 0);
            fvec.setFeatureDef(featureDef);
            byte[] values = createValues(rowCount, columnCount);
            ImgFeatureValuesDTO featureValues = new ImgFeatureValuesDTO(0.0, 0.0, values, 0);

            fvec.setValues(Collections.singletonList(featureValues));

            return fvec;
        }

        private byte[] createValues(int rowCount, int columnCount)
        {
            int[] dims =
                { rowCount, columnCount };
            MDDoubleArray array = new MDDoubleArray(dims);
            for (int i = 0; i < dims[0]; ++i)
            {
                for (int j = 0; j < dims[1]; ++j)
                {
                    array.set(i + j, i, j);
                }
            }
            return NativeTaggedArray.toByteArray(array);
        }
    }
}
