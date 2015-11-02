/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.deletion.sample.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.LocatorTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.datastore.DataStorePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.vocabulary.VocabularyTermCode;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class CreateDataSetTest extends AbstractDataSetTest
{

    @Test
    public void testCreateWithCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet = dataSetCreation();
        dataSet.setCode(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation dataSet = dataSetCreation();
        dataSet.setCode("DATA_SET_WITH_EXISTING_CODE");
        v3api.createDataSets(sessionToken, Arrays.asList(dataSet));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "DataSet already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet = dataSetCreation();
        dataSet.setCode("?!*");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Given code '?!*' contains illegal characters (allowed: A-Z, a-z, 0-9 and _, -, .)");
    }

    @Test
    public void testCreateWithTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation dataSet = dataSetCreation();
        dataSet.setTypeId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Type id cannot be null");
    }

    @Test
    public void testCreateWithTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IEntityTypeId typeId = new EntityTypePermId("IDONTEXIST");
        final DataSetCreation dataSet = dataSetCreation();
        dataSet.setTypeId(typeId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, typeId);
    }

    @Test
    public void testCreateWithPropertyCodeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist");
    }

    @Test
    public void testCreateWithPropertyValueIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setTypeId(new EntityTypePermId("HCS_IMAGE"));
        creation.setProperty("GENDER", "NON_EXISTENT_GENDER");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Vocabulary value 'NON_EXISTENT_GENDER' is not valid. It must exist in 'GENDER' controlled vocabulary");
    }

    @Test
    public void testCreateWithPropertyValueMandatoryButNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setTypeId(new EntityTypePermId("HCS_IMAGE"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Value of mandatory property 'COMMENT' not specified");
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Experiment id and sample id cannot be both null.");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setSampleId(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        Map<IDataSetId, DataSet> dataSets = v3api.mapDataSets(sessionToken, permIds, fo);
        DataSet dataSet = dataSets.values().iterator().next();

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP1");
        assertEquals(dataSet.getSample(), null);
    }

    @Test
    public void testCreateWithExperimentNullAndSampleInExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(new SampleIdentifier("/CISD/CP-TEST-1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        Map<IDataSetId, DataSet> dataSets = v3api.mapDataSets(sessionToken, permIds, fo);
        DataSet dataSet = dataSets.values().iterator().next();

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/CP-TEST-1");
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNotInExperimentWhenTypeAllows()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(null);
        creation.setSampleId(new SampleIdentifier("/CISD/3V-125"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        Map<IDataSetId, DataSet> dataSets = v3api.mapDataSets(sessionToken, permIds, fo);
        DataSet dataSet = dataSets.values().iterator().next();

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment(), null);
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/3V-125");
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNotInExperimentWhenTypeDoesNotAllow()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setTypeId(new EntityTypePermId("REQUIRES_EXPERIMENT"));
        creation.setExperimentId(null);
        creation.setSampleId(new SampleIdentifier("/CISD/3V-125"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set can not be registered because it is not connected to an experiment.");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNotNullNotInExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        creation.setSampleId(new SampleIdentifier("/CISD/3V-125"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        Map<IDataSetId, DataSet> dataSets = v3api.mapDataSets(sessionToken, permIds, fo);
        DataSet dataSet = dataSets.values().iterator().next();

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment(), null);
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/3V-125");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNotNullInSameExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        creation.setSampleId(new SampleIdentifier("/CISD/CP-TEST-1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        Map<IDataSetId, DataSet> dataSets = v3api.mapDataSets(sessionToken, permIds, fo);
        DataSet dataSet = dataSets.values().iterator().next();

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/CP-TEST-1");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNotNullInDifferentExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setSampleId(new SampleIdentifier("/CISD/CP-TEST-1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        Map<IDataSetId, DataSet> dataSets = v3api.mapDataSets(sessionToken, permIds, fo);
        DataSet dataSet = dataSets.values().iterator().next();

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/CP-TEST-1");
    }

    @Test
    public void testCreateWithExperimentInTrash()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation()));
        ExperimentDeletionOptions deletion = new ExperimentDeletionOptions();
        deletion.setReason("testing");
        v3api.deleteExperiments(sessionToken, experimentIds, deletion);

        final DataSetCreation creation = dataSetCreation();
        creation.setExperimentId(experimentIds.get(0));
        creation.setSampleId(null);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, experimentIds.get(0));
    }

    @Test
    public void testCreateWithSampleInTrash()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation()));
        SampleDeletionOptions deletion = new SampleDeletionOptions();
        deletion.setReason("testing");
        v3api.deleteSamples(sessionToken, sampleIds, deletion);

        final DataSetCreation creation = dataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(sampleIds.get(0));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, sampleIds.get(0));
    }

    @Test
    public void testCreateWithSampleShared()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = dataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(new SamplePermId("200811050947161-652"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set can not be registered because sample '/MP' is a shared sample.");
    }

    @Test
    public void testCreatePhysicalData()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setLocation("a/b/c");
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        physicalCreation.setLocatorTypeId(new LocatorTypePermId("RELATIVE_LOCATION"));
        physicalCreation.setStorageFormatId(new VocabularyTermCode("PROPRIETARY"));

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_PHYSICAL_DATASET");
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setPhysicalData(physicalCreation);

        List<DataSetPermId> ids = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData().withFileFormatType();
        fetchOptions.withPhysicalData().withLocatorType();
        fetchOptions.withPhysicalData().withStorageFormat();

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, ids, fetchOptions);

        DataSet dataSet = map.get(ids.get(0));
        assertEquals(dataSet.getCode(), "TEST_PHYSICAL_DATASET");
        assertEquals(dataSet.getType().getCode(), "UNKNOWN");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        assertEquals(dataSet.getDataStore().getCode(), "STANDARD");
        assertEquals(dataSet.getPhysicalData().getLocation(), "a/b/c");
        assertEquals(dataSet.getPhysicalData().getFileFormatType().getCode(), "TIFF");
        assertEquals(dataSet.getPhysicalData().getLocatorType().getCode(), "RELATIVE_LOCATION");
        assertEquals(dataSet.getPhysicalData().getStorageFormat().getCode(), "PROPRIETARY");
    }

    @Test
    public void testCreateContainerData()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_CONTAINER_DATASET");
        creation.setTypeId(new EntityTypePermId("CONTAINER_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setContainedIds(Arrays.asList(new DataSetPermId("20081105092159188-3")));

        List<DataSetPermId> ids = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        fetchOptions.withContained();

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, ids, fetchOptions);

        DataSet dataSet = map.get(ids.get(0));
        assertEquals(dataSet.getCode(), "TEST_CONTAINER_DATASET");
        assertEquals(dataSet.getType().getCode(), "CONTAINER_TYPE");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        assertEquals(dataSet.getDataStore().getCode(), "STANDARD");
        assertNull(dataSet.getPhysicalData());
        assertEquals(dataSet.getContained().size(), 1);
        assertEquals(dataSet.getContained().iterator().next().getCode(), "20081105092159188-3");
    }

    private DataSetCreation dataSetCreation()
    {
        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setLocation("a/b/c");
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        physicalCreation.setLocatorTypeId(new LocatorTypePermId("RELATIVE_LOCATION"));
        physicalCreation.setStorageFormatId(new VocabularyTermCode("PROPRIETARY"));

        DataSetCreation creation = new DataSetCreation();
        creation.setCode(UUID.randomUUID().toString());
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setPhysicalData(physicalCreation);

        return creation;
    }

    private ExperimentCreation experimentCreation()
    {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode(UUID.randomUUID().toString());
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        return creation;
    }

    private SampleCreation sampleCreation()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(UUID.randomUUID().toString());
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        return creation;
    }

    public static void main(String[] args)
    {
        System.out.println("REQUIRES_EXPERIMENT".matches("(?!REQUIRES\\_EXPERIMENT).*"));
    }

}
