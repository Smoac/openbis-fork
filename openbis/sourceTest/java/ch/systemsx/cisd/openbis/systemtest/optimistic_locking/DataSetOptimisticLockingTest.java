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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.base.builder.DataSetUpdateBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetOptimisticLockingTest extends OptimisticLockingTestCase
{
    @Test
    public void testChangeFileFormatTypeViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        assertEquals("XML", ((DataSet) dataSet).getFileFormatType().getCode());
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setFileFormatTypeCode("HDF5");
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setFileFormatUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        ExternalData loadedDataSet = toolBox.loadDataSet(systemSessionToken, dataSet.getCode());
        assertEquals("HDF5", ((DataSet) loadedDataSet).getFileFormatType().getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
    }

    @Test
    public void testChangeFileFormatTypeViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        assertEquals("XML", ((DataSet) dataSet).getFileFormatType().getCode());
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).create();
        dataSetUpdates.setFileFormatTypeCode("HDF5");
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        ExternalData loadedDataSet = toolBox.loadDataSet(systemSessionToken, dataSet.getCode());
        assertEquals("HDF5", ((DataSet) loadedDataSet).getFileFormatType().getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
    }

    @Test
    public void testChangeFileFormatTypeOfAStaleDataSetViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        assertEquals("XML", ((DataSet) dataSet).getFileFormatType().getCode());
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).create();
        dataSetUpdates.setFileFormatTypeCode("HDF5");
        String sessionToken = logIntoCommonClientService().getSessionID();
        etlService.updateDataSet(sessionToken, dataSetUpdates);

        try
        {
            etlService.updateDataSet(sessionToken, dataSetUpdates);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Data set has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testChangePropertyViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewDataSet newDataSet = toolBox.dataSet("DS-1", experiment);
        newDataSet.setDataSetType(new DataSetType("HCS_IMAGE"));
        newDataSet.setDataSetProperties(Arrays.asList(new NewProperty("COMMENT", "1")));
        ExternalData dataSet = toolBox.createAndLoadDataSet(newDataSet);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList(new PropertyBuilder(
                "COMMENT").value("2").getProperty()));
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setPropertiesToUpdate(new HashSet<String>(Arrays.asList("COMMENT")));
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        ExternalData loadedDataSet = toolBox.loadDataSet(systemSessionToken, dataSet.getCode());
        assertEquals("[COMMENT: 2]", loadedDataSet.getProperties().toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
    }

    @Test
    public void testChangePropertyViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewDataSet newDataSet = toolBox.dataSet("DS-1", experiment);
        newDataSet.setDataSetType(new DataSetType("HCS_IMAGE"));
        newDataSet.setDataSetProperties(Arrays.asList(new NewProperty("COMMENT", "1")));
        ExternalData dataSet = toolBox.createAndLoadDataSet(newDataSet);
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).create();
        dataSetUpdates.getProperties().add(new PropertyBuilder("COMMENT").value("2").getProperty());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        ExternalData loadedDataSet = toolBox.loadDataSet(systemSessionToken, dataSet.getCode());
        assertEquals("[COMMENT: 2]", loadedDataSet.getProperties().toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
    }

    @Test
    public void testChangePropertyOfAStaleDataSetViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        NewDataSet newDataSet = toolBox.dataSet("DS-1", experiment);
        newDataSet.setDataSetType(new DataSetType("HCS_IMAGE"));
        newDataSet.setDataSetProperties(Arrays.asList(new NewProperty("COMMENT", "1")));
        ExternalData dataSet = toolBox.createAndLoadDataSet(newDataSet);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList(new PropertyBuilder(
                "COMMENT").value("2").getProperty()));
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setPropertiesToUpdate(new HashSet<String>(Arrays.asList("COMMENT")));
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        try
        {
            etlService.performEntityOperations(systemSessionToken, builder.getDetails());
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Data set DS-1 has been updated since it was retrieved.\n"
                    + "[Current: 1, Retrieved: 0]", ex.getMessage());
        }
    }

    @Test
    public void testAddMetaProjectViaPerformEntityOperation()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        MetaprojectUpdatesDTO metaProjectUpdate = new MetaprojectUpdatesDTO();
        metaProjectUpdate.setAddedEntities(Arrays.asList(new DataSetCodeId(dataSet.getCode())));
        metaProjectUpdate.setRemovedEntities(Arrays.<IObjectId> asList());
        metaProjectUpdate.setMetaprojectId(new TechId(1));
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().metaProjectUpdate(metaProjectUpdate)
                        .user("test");

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        String sessionToken = logIntoCommonClientService().getSessionID();
        ExternalData loadedDataSet = toolBox.loadDataSet(sessionToken, dataSet.getCode());
        assertEquals(dataSet.getModifier(), loadedDataSet.getModifier());
        assertEquals(dataSet.getModificationDate(), loadedDataSet.getModificationDate());
        assertEquals("/test/TEST_METAPROJECTS",
                toolBox.renderMetaProjects(loadedDataSet.getMetaprojects()));
        assertEquals(
                "",
                toolBox.renderMetaProjects(toolBox.loadDataSet(systemSessionToken,
                        dataSet.getCode()).getMetaprojects()));
    }

    @Test
    public void testAddMetaProjectViaUpdateDataSet()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment));
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).create();
        dataSetUpdates.setMetaprojectsOrNull(new String[]
            { "TEST_METAPROJECTS" });
        String sessionToken = logIntoCommonClientService().getSessionID();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        ExternalData loadedDataSet = toolBox.loadDataSet(sessionToken, dataSet.getCode());
        assertEquals(dataSet.getModifier(), loadedDataSet.getModifier());
        assertEquals(dataSet.getModificationDate(), loadedDataSet.getModificationDate());
        assertEquals("/test/TEST_METAPROJECTS",
                toolBox.renderMetaProjects(loadedDataSet.getMetaprojects()));
        assertEquals(
                "",
                toolBox.renderMetaProjects(toolBox.loadDataSet(systemSessionToken,
                        dataSet.getCode()).getMetaprojects()));
    }

    @Test
    public void testChangeExperimentViaPerformEntityOperation()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment1));
        assertEquals(experiment1.getIdentifier(), dataSet.getExperiment().getIdentifier());
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setExperimentIdentifierOrNull(new ExperimentIdentifier(experiment2));
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setExperimentUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        ExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        assertEquals(experiment2.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment2), "test");
        assertEquals(0,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment1))
                        .size());
        assertEquals(1,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment2))
                        .size());
    }

    @Test
    public void testChangeExperimentViaUpdateDataSet()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", experiment1));
        assertEquals(experiment1.getIdentifier(), dataSet.getExperiment().getIdentifier());
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).toExperiment(
                        experiment2).create();
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        ExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        assertEquals(experiment2.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment2), "test");
        assertEquals(0,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment1))
                        .size());
        assertEquals(1,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment2))
                        .size());
    }

    @Test
    public void testChangeSampleViaPerformEntityOperation()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        Sample sample1 = toolBox.createAndLoadSample(1, experiment1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", sample1));
        assertEquals(experiment1.getIdentifier(), dataSet.getExperiment().getIdentifier());
        assertEquals(sample1.getIdentifier(), dataSet.getSampleIdentifier());
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        Sample sample2 = toolBox.createAndLoadSample(2, experiment2);
        DataSetBatchUpdatesDTO dataSetBatchUpdates = new DataSetBatchUpdatesDTO();
        dataSetBatchUpdates.setVersion(dataSet.getVersion());
        dataSetBatchUpdates.setDatasetCode(dataSet.getCode());
        dataSetBatchUpdates.setDatasetId(new TechId(dataSet));
        dataSetBatchUpdates.setProperties(Arrays.<IEntityProperty> asList());
        dataSetBatchUpdates.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample2));
        DataSetBatchUpdateDetails updateDetails = new DataSetBatchUpdateDetails();
        updateDetails.setSampleUpdateRequested(true);
        dataSetBatchUpdates.setDetails(updateDetails);
        AtomicEntityOperationDetailsBuilder builder =
                new AtomicEntityOperationDetailsBuilder().dataSetUpdate(dataSetBatchUpdates).user(
                        "test");
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        ExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        assertEquals(experiment2.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment2), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadSample(sample1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadSample(sample2), "test");
        assertEquals(0,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment1))
                        .size());
        assertEquals(1,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment2))
                        .size());
        assertEquals(0,
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample1), true)
                        .size());
        assertEquals(1,
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample2), true)
                        .size());
    }

    @Test
    public void testChangeSampleViaUpdateDataSet()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        Sample sample1 = toolBox.createAndLoadSample(1, experiment1);
        ExternalData dataSet = toolBox.createAndLoadDataSet(toolBox.dataSet("DS-1", sample1));
        assertEquals(experiment1.getIdentifier(), dataSet.getExperiment().getIdentifier());
        assertEquals(sample1.getIdentifier(), dataSet.getSampleIdentifier());
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        Sample sample2 = toolBox.createAndLoadSample(2, experiment2);
        DataSetUpdatesDTO dataSetUpdates =
                new DataSetUpdateBuilder(commonServer, genericServer, dataSet).toSample(sample2)
                        .create();
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker();

        etlService.updateDataSet(sessionToken, dataSetUpdates);

        ExternalData loadedDataSet = toolBox.loadDataSet(dataSet.getCode());
        assertEquals(experiment2.getIdentifier(), loadedDataSet.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedDataSet, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadExperiment(experiment2), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadSample(sample1), "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker,
                toolBox.loadSample(sample2), "test");
        assertEquals(0,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment1))
                        .size());
        assertEquals(1,
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(experiment2))
                        .size());
        assertEquals(0,
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample1), true)
                        .size());
        assertEquals(1,
                etlService.listDataSetsBySampleID(systemSessionToken, new TechId(sample2), true)
                        .size());
    }
}
