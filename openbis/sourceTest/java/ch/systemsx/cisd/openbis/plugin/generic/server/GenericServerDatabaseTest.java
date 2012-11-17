/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * @author pkupczyk
 */
public class GenericServerDatabaseTest extends AbstractDAOTest
{
    private static final String REUSE_EXPERIMENT_PERMID = "200811050940555-1032";

    private static final String TEST_EXPERIMENT_PERMID = "200902091255058-1035";

    private static final String REUSE_EXPERIMENT_SAMPLE_PERMID = "200811050929940-1018";

    private static final String TEST_EXPERIMENT_SAMPLE_PERMID = "200902091225616-1027";

    private static final String REUSE_EXPERIMENT_CONTAINER_DATA_SET_CODE = "20110509092359990-10";

    private static final String REUSE_EXPERIMENT_CONTAINED_DATA_SET_CODE = "20110509092359990-11";

    private static final String TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE = "20110805092359990-17";

    @Resource(name = ResourceNames.GENERIC_PLUGIN_SERVER)
    IGenericServer server;

    private SessionContextDTO session;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        session = server.tryAuthenticate("test", "password");
    }

    @Test
    public void testChangingSampleShouldSetSampleAndExperiment()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        Assert.assertEquals(TEST_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());

        SamplePE newSample = findSample(REUSE_EXPERIMENT_SAMPLE_PERMID);

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setSampleIdentifierOrNull(newSample.getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setSampleUpdateRequested(true);

        update(dataset, newDataset, updateDetails);

        Assert.assertEquals(REUSE_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(REUSE_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());
    }

    @Test
    public void testChangingExperimentShouldClearSampleAndSetExperiment()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        Assert.assertEquals(TEST_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());

        ExperimentPE newExperiment = findExperiment(REUSE_EXPERIMENT_PERMID);

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setExperimentIdentifier(newExperiment.getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setExperimentUpdateRequested(true);

        update(dataset, newDataset, updateDetails);

        Assert.assertNull(dataset.tryGetSample());
        Assert.assertEquals(REUSE_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());
    }

    @Test
    public void testChangingContainerToDataSetThatIsContainerShouldBeAllowed()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        Assert.assertNull(dataset.getContainer());

        DataPE newContainer = findData(REUSE_EXPERIMENT_CONTAINER_DATA_SET_CODE);

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setContainerIdentifierOrNull(newContainer.getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setContainerUpdateRequested(true);

        update(dataset, newDataset, updateDetails);

        Assert.assertEquals(REUSE_EXPERIMENT_CONTAINER_DATA_SET_CODE, dataset.getContainer()
                .getPermId());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testChangingContainerToDataSetThatIsNotContainerShouldNotBeAllowed()
    {
        DataPE dataset = findData(REUSE_EXPERIMENT_CONTAINER_DATA_SET_CODE);

        Assert.assertNull(dataset.getContainer());

        DataPE newContainer = findData(REUSE_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setContainerIdentifierOrNull(newContainer.getIdentifier());

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setContainerUpdateRequested(true);

        update(dataset, newDataset, updateDetails);
    }

    @Test
    public void testClearingSampleShouldClearSampleAndLeaveExperiment()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        Assert.assertEquals(TEST_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setSampleIdentifierOrNull(null);

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setSampleUpdateRequested(true);

        update(dataset, newDataset, updateDetails);

        Assert.assertNull(dataset.tryGetSample());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testClearingExperimentShouldNotBeAllowed()
    {
        DataPE dataset = findData(TEST_EXPERIMENT_CONTAINED_DATA_SET_CODE);

        Assert.assertEquals(TEST_EXPERIMENT_SAMPLE_PERMID, dataset.tryGetSample().getPermId());
        Assert.assertEquals(TEST_EXPERIMENT_PERMID, dataset.getExperiment().getPermId());

        NewDataSet newDataset = new NewDataSet();
        newDataset.setCode(dataset.getCode());
        newDataset.setExperimentIdentifier(null);

        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setExperimentUpdateRequested(true);

        update(dataset, newDataset, updateDetails);
    }

    private void update(DataPE data, NewDataSet newDataset, DataSetBatchUpdateDetails updateDetails)
    {
        List<NewDataSet> newDatasets = new ArrayList<NewDataSet>();
        newDatasets.add(new UpdatedDataSet(newDataset, updateDetails));

        NewDataSetsWithTypes newDatasetsWithType = new NewDataSetsWithTypes();
        newDatasetsWithType.setDataSetType(new DataSetType(data.getDataSetType().getCode()));
        newDatasetsWithType.setNewDataSets(newDatasets);

        server.updateDataSets(session.getSessionToken(), newDatasetsWithType);
        sessionFactory.getCurrentSession().update(data);
    }

}
