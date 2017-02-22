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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.BdsDirectoryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IFileFormatTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ILocatorTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IStorageFormatId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.LocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.StorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.ReindexingState;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;

/**
 * @author pkupczyk
 */
public class CreateDataSetTest extends AbstractDataSetTest
{
    @Test
    public void testCreateDSWithAdminUserInAnotherSpace()
    {
        final DataSetPermId permId = new DataSetPermId("NO_SHALL_CREATE");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
                    physicalCreation.setLocation("test/location/" + permId.getPermId());
                    physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
                    physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
                    physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

                    DataSetCreation creation = new DataSetCreation();
                    creation.setCode(permId.getPermId());
                    creation.setTypeId(new EntityTypePermId("UNKNOWN"));
                    creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP_SPACE_TEST"));
                    creation.setDataStoreId(new DataStorePermId("STANDARD"));
                    creation.setPhysicalData(physicalCreation);
                    creation.setCreationId(new CreationId(permId.getPermId()));

                    v3api.createDataSets(sessionToken, Collections.singletonList(creation));
                }
            }, "Data set creation can be only executed by a user with ETL_SERVER role");
    }

    @Test
    public void testArchiveWithAdminUserInAnotherSpace()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    List<DataSetPermId> permIds = testCreateWithIndexCheck();
                    String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
                    v3api.archiveDataSets(sessionToken, permIds, new DataSetArchiveOptions());
                }
            });
    }

    @Test
    public void testUnArchiveWithAdminUserInAnotherSpace()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    List<DataSetPermId> permIds = testCreateWithIndexCheck();
                    String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
                    v3api.unarchiveDataSets(sessionToken, permIds, new DataSetUnarchiveOptions());
                }
            });
    }

    @Test
    public List<DataSetPermId> testCreateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation dataSet = physicalDataSetCreation();
        ReindexingState state = new ReindexingState();

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(dataSet));

        assertDataSetsReindexed(state, permIds.get(0).getPermId());
        return permIds;
    }

    @Test
    public void testCreateWithNonAutogeneratedCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet = physicalDataSetCreation();
        dataSet.setCode(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Code cannot be empty for a non auto generated code");
    }

    @Test
    public void testCreateWithAutogeneratedCodeNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet = physicalDataSetCreation();
        dataSet.setCode("DATASET_WITH_USER_GIVEN_CODE");
        dataSet.setAutoGeneratedCode(true);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Code should be empty when auto generated code is selected");
    }

    @Test
    public void testCreateWithAutogeneratedCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet1 = physicalDataSetCreation();
        dataSet1.setCode(null);
        dataSet1.setAutoGeneratedCode(true);
        final DataSetCreation dataSet2 = physicalDataSetCreation();
        dataSet2.setCode(null);
        dataSet2.setAutoGeneratedCode(true);

        List<DataSetPermId> datasetWithAutogeneratedCode = v3api.createDataSets(sessionToken, Arrays.asList(dataSet1, dataSet2));
        AssertionUtil.assertCollectionSize(datasetWithAutogeneratedCode, 2);
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation dataSet = physicalDataSetCreation();
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
        final DataSetCreation dataSet = physicalDataSetCreation();
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

        final DataSetCreation dataSet = physicalDataSetCreation();
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
        final DataSetCreation dataSet = physicalDataSetCreation();
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

        final DataSetCreation creation = physicalDataSetCreation();
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

        final DataSetCreation creation = physicalDataSetCreation();
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

        final DataSetCreation creation = physicalDataSetCreation();
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
    public void testCreateWithDataStoreNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setDataStoreId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data store id cannot be null.");
    }

    @Test
    public void testCreateWithDataStoreNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IDataStoreId dataStoreId = new DataStorePermId("IDONTEXIST");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setDataStoreId(dataStoreId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, dataStoreId);
    }

    @Test
    public void testCreateWithMeasuredTrue()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setMeasured(true);

        DataSet dataSet = createDataSet(sessionToken, creation, new DataSetFetchOptions());
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.isMeasured(), Boolean.TRUE);
    }

    @Test
    public void testCreateWithMeasuredFalse()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setMeasured(false);

        DataSet dataSet = createDataSet(sessionToken, creation, new DataSetFetchOptions());
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.isMeasured(), Boolean.FALSE);
    }

    @Test
    public void testCreateWithTagsExisting()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setTagIds(Arrays.asList(new TagCode("TEST_METAPROJECTS")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withTags();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertTags(dataSet.getTags(), "/test_space/TEST_METAPROJECTS");
    }

    @Test
    public void testCreateWithTagsNonexistent()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setTagIds(Arrays.asList(new TagCode("IDONTEXIST")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withTags();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertTags(dataSet.getTags(), "/test_space/IDONTEXIST");
    }

    @Test
    public void testCreateWithSystemProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setPropertyTypeCode("$PLATE_GEOMETRY");
        assignment.setEntityTypeCode("UNKNOWN");
        assignment.setEntityKind(EntityKind.DATA_SET);
        assignment.setOrdinal(1000L);
        commonServer.assignPropertyType(sessionToken, assignment);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withProperties();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getProperty("$PLATE_GEOMETRY"), "384_WELLS_16X24");
    }

    @Test
    public void testCreateWithDataProducer()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setDataProducer("TEST_DATA_PRODUCER");
        creation.setDataProductionDate(new Date());

        DataSet dataSet = createDataSet(sessionToken, creation, new DataSetFetchOptions());

        assertEquals(dataSet.getDataProducer(), creation.getDataProducer());
        assertEqualsDate(dataSet.getDataProductionDate(), creation.getDataProductionDate());
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
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

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setSampleId(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP1");
        assertEquals(dataSet.getSample(), null);
    }

    @Test
    public void testCreateWithExperimentNullAndSampleInExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(new SampleIdentifier("/CISD/CP-TEST-1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/CP-TEST-1");
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNotInExperimentWhenTypeAllows()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(null);
        creation.setSampleId(new SampleIdentifier("/CISD/3V-125"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment(), null);
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/3V-125");
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNotInExperimentWhenTypeDoesNotAllow()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
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

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        creation.setSampleId(new SampleIdentifier("/CISD/3V-125"));

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set can not be registered because it connected to a different experiment than its sample.");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNotNullInSameExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        creation.setSampleId(new SampleIdentifier("/CISD/CP-TEST-1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/CP-TEST-1");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNotNullInDifferentExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setSampleId(new SampleIdentifier("/CISD/CP-TEST-1"));

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set can not be registered because it connected to a different experiment than its sample.");
    }

    @Test
    public void testCreateWithExperimentInTrash()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation()));
        ExperimentDeletionOptions deletion = new ExperimentDeletionOptions();
        deletion.setReason("testing");
        v3api.deleteExperiments(sessionToken, experimentIds, deletion);

        final DataSetCreation creation = physicalDataSetCreation();
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
    public void testCreateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IExperimentId experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(experimentId);
        creation.setSampleId(null);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, experimentId);
    }

    @Test
    public void testCreateWithSampleInTrash()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation()));
        SampleDeletionOptions deletion = new SampleDeletionOptions();
        deletion.setReason("testing");
        v3api.deleteSamples(sessionToken, sampleIds, deletion);

        final DataSetCreation creation = physicalDataSetCreation();
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
    public void testCreateWithSampleUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final ISampleId sampleId = new SampleIdentifier("/CISD/CP-TEST-1");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(sampleId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, sampleId);
    }

    @Test
    public void testCreateWithSampleShared()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
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
    public void testCreateWithContainersThatAreNonContainerDataSets()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setContainerIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set 20081105092159111-1 is not of a container type therefore cannot be set as a container of data set "
                    + creation.getCode().toUpperCase() + ".");
    }

    @Test
    public void testCreateWithContainersThatAreContainerDataSets()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setContainerIds(Collections.singletonList(new DataSetPermId("20110509092359990-10")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withContainers();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertDataSetCodes(dataSet.getContainers(), "20110509092359990-10");
    }

    @Test
    public void testCreateWithContainersCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation1 = containerDataSetCreation();
        final DataSetCreation creation2 = containerDataSetCreation();
        final DataSetCreation creation3 = containerDataSetCreation();

        creation2.setContainerIds(Collections.singletonList(creation1.getCreationId()));
        creation3.setContainerIds(Collections.singletonList(creation2.getCreationId()));
        creation1.setContainerIds(Collections.singletonList(creation3.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2, creation3));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithContainersUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IDataSetId containerId = new DataSetPermId("20110509092359990-10");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setContainerIds(Collections.singletonList(containerId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, containerId);
    }

    @Test
    public void testCreateWithComponentsForContainerDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = containerDataSetCreation();
        creation.setComponentIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withComponents();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertDataSetCodes(dataSet.getComponents(), "20081105092159111-1");
    }

    @Test
    public void testCreateWithComponentsForNonContainerDataSet()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setComponentIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Data set " + creation.getCode().toUpperCase() + " is not of a container type therefore cannot have component data sets.");
    }

    @Test
    public void testCreateWithComponentsCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation1 = containerDataSetCreation();
        final DataSetCreation creation2 = containerDataSetCreation();
        final DataSetCreation creation3 = containerDataSetCreation();

        creation2.setComponentIds(Collections.singletonList(creation1.getCreationId()));
        creation3.setComponentIds(Collections.singletonList(creation2.getCreationId()));
        creation1.setComponentIds(Collections.singletonList(creation3.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2, creation3));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithComponentsUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IDataSetId componentId = new DataSetPermId("20081105092159111-1");
        final DataSetCreation creation = containerDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setComponentIds(Collections.singletonList(componentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, componentId);
    }

    @Test
    public void testCreateWithParents()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setParentIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withParents();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertDataSetCodes(dataSet.getParents(), "20081105092159111-1");
    }

    @Test
    public void testCreateWithParentsCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation1 = physicalDataSetCreation();
        final DataSetCreation creation2 = physicalDataSetCreation();
        final DataSetCreation creation3 = physicalDataSetCreation();

        creation2.setParentIds(Collections.singletonList(creation1.getCreationId()));
        creation3.setParentIds(Collections.singletonList(creation2.getCreationId()));
        creation1.setParentIds(Collections.singletonList(creation3.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2, creation3));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithParentsUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IDataSetId parentId = new DataSetPermId("20081105092159111-1");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setParentIds(Collections.singletonList(parentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, parentId);
    }

    @Test
    public void testCreateWithChildren()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setChildIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withChildren();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertDataSetCodes(dataSet.getChildren(), "20081105092159111-1");
    }

    @Test
    public void testCreateWithChildrenCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation1 = physicalDataSetCreation();
        final DataSetCreation creation2 = physicalDataSetCreation();
        final DataSetCreation creation3 = physicalDataSetCreation();

        creation2.setChildIds(Collections.singletonList(creation1.getCreationId()));
        creation3.setChildIds(Collections.singletonList(creation2.getCreationId()));
        creation1.setChildIds(Collections.singletonList(creation3.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2, creation3));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithChildrenUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IDataSetId childId = new DataSetPermId("20081105092159111-1");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setChildIds(Collections.singletonList(childId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, childId);
    }

    @Test
    public void testCreateWithUserNonEtlServer()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, physicalDataSetCreation(), new DataSetFetchOptions());
                }
            }, "Data set creation can be only executed by a system user or a user with at least SPACE_ETL_SERVER role");
    }

    @Test
    public void testCreateWithUserEtlServer()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();
        fo.withRegistrator();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(dataSet.getSample(), null);
        assertEquals(dataSet.getRegistrator().getUserId(), TEST_USER);
    }

    @Test
    public void testCreateWithUserEtlServerOnBehalfOtherUser()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();
        fo.withRegistrator();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(dataSet.getSample(), null);
        assertEquals(dataSet.getRegistrator().getUserId(), TEST_SPACE_USER);
    }

    @Test
    public void testCreatePhysicalDataSetWithPhysicalDataNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setLocation("a/b/c");
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_PHYSICAL_DATASET");
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setPhysicalData(physicalCreation);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData().withFileFormatType();
        fetchOptions.withPhysicalData().withLocatorType();
        fetchOptions.withPhysicalData().withStorageFormat();
        fetchOptions.withLinkedData();

        DataSet dataSet = createDataSet(sessionToken, creation, fetchOptions);
        assertEquals(dataSet.getCode(), "TEST_PHYSICAL_DATASET");
        assertEquals(dataSet.getType().getCode(), "UNKNOWN");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        assertEquals(dataSet.getDataStore().getCode(), "STANDARD");
        assertEquals(dataSet.getPhysicalData().getLocation(), "a/b/c");
        assertEquals(dataSet.getPhysicalData().getFileFormatType().getCode(), "TIFF");
        assertEquals(dataSet.getPhysicalData().getLocatorType().getCode(), "RELATIVE_LOCATION");
        assertEquals(dataSet.getPhysicalData().getStorageFormat().getCode(), "PROPRIETARY");
        assertNull(dataSet.getLinkedData());
    }

    @Test
    public void testCreatePhysicalDataSetWithPhysicalDataNotNullAndLinkedDataNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setLinkedData(new LinkedDataCreation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Linked data cannot be set for a non-link data set.", patternContains("setting relation dataset-linkeddata (1/1)"));
    }

    @Test
    public void testCreatePhysicalDataSetWithPhysicalDataNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setPhysicalData(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Physical data cannot be null for a physical data set.", patternContains("setting relation dataset-physicaldata (1/1)"));
    }

    @Test
    public void testCreatePhysicalDataSetWithShareIdNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setShareId(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getShareId(), null);
    }

    @Test
    public void testCreatePhysicalDataSetWithShareIdNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setShareId("SOME_SHARE");

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getShareId(), "SOME_SHARE");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocationNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocation(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Location can not be null.");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocationAbsolute()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocation("/cannot_be_absolute_path/sorry");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Location is not relative");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocationRelative()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocation("relative_path_should_be/fine");

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getLocation(), creation.getPhysicalData().getLocation());
    }

    @Test
    public void testCreatePhysicalDataSetWithLocationExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final String location = "duplicated_location/should_fail";

        final DataSetCreation creation1 = physicalDataSetCreation();
        creation1.getPhysicalData().setLocation(location);

        final DataSetCreation creation2 = physicalDataSetCreation();
        creation2.getPhysicalData().setLocation(location);

        final DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet1 = createDataSet(sessionToken, creation1, fo);
        assertEquals(dataSet1.getPhysicalData().getLocation(), creation1.getPhysicalData().getLocation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation2, fo);
                }
            }, "DataSet already exists in the database and needs to be unique.");
    }

    @Test
    public void testCreatePhysicalDataSetWithSizeNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSize(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getSize(), null);
    }

    @Test
    public void testCreatePhysicalDataSetWithSizeNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSize(12345L);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getSize(), Long.valueOf(12345));
    }

    @Test
    public void testCreatePhysicalDataSetWithSizeNegative()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSize(-12345L);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Physical data set size cannot be < 0.");
    }

    @Test
    public void testCreatePhysicalDataSetWithStorageFormatNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setStorageFormatId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Storage format id cannot be null for a physical data set.");
    }

    @Test
    public void testCreatePhysicalDataSetWithStorageFormatNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setStorageFormatId(new BdsDirectoryStorageFormatPermId());

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData().withStorageFormat();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getStorageFormat().getCode(), "BDS_DIRECTORY");
    }

    @Test
    public void testCreatePhysicalDataSetWithStorageFormatNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IStorageFormatId storageFormatId = new StorageFormatPermId("IDONTEXIST");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setStorageFormatId(storageFormatId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, storageFormatId);
    }

    @Test
    public void testCreatePhysicalDataSetWithFileFormatTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setFileFormatTypeId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "File format type id cannot be null for a physical data set.");
    }

    @Test
    public void testCreatePhysicalDataSetWithFileFormatTypeNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setFileFormatTypeId(new FileFormatTypePermId("XML"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData().withFileFormatType();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getFileFormatType().getCode(), "XML");
    }

    @Test
    public void testCreatePhysicalDataSetWithFileFormatTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IFileFormatTypeId formatId = new FileFormatTypePermId("IDONTEXIST");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setFileFormatTypeId(formatId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, formatId);
    }

    @Test
    public void testCreatePhysicalDataSetWithLocatorTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocatorTypeId(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData().withLocatorType();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        // got default value
        assertEquals(dataSet.getPhysicalData().getLocatorType().getCode(), "RELATIVE_LOCATION");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocatorTypeNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocatorTypeId(new RelativeLocationLocatorTypePermId());

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData().withLocatorType();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getLocatorType().getCode(), "RELATIVE_LOCATION");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocatorTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ILocatorTypeId locatorTypeId = new LocatorTypePermId("IDONTEXIST");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocatorTypeId(locatorTypeId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, locatorTypeId);
    }

    @Test
    public void testCreatePhysicalDataSetWithCompleteNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setComplete(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        // got default value
        assertEquals(dataSet.getPhysicalData().getComplete(), Complete.UNKNOWN);
    }

    @Test
    public void testCreatePhysicalDataSetWithCompleteNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setComplete(Complete.YES);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getComplete(), Complete.YES);
    }

    @Test
    public void testCreatePhysicalDataSetWithSpeedHintNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSpeedHint(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        // got default value
        assertEquals(dataSet.getPhysicalData().getSpeedHint(), Integer.valueOf(-50));
    }

    @Test
    public void testCreatePhysicalDataSetWithSpeedHintNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSpeedHint(123);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getSpeedHint(), Integer.valueOf(123));
    }

    @Test
    public void testCreateContainerDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_CONTAINER_DATASET");
        creation.setTypeId(new EntityTypePermId("CONTAINER_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setComponentIds(Arrays.asList(new DataSetPermId("20081105092159188-3")));

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        fetchOptions.withLinkedData();
        fetchOptions.withComponents();

        DataSet dataSet = createDataSet(sessionToken, creation, fetchOptions);
        assertEquals(dataSet.getCode(), "TEST_CONTAINER_DATASET");
        assertEquals(dataSet.getType().getCode(), "CONTAINER_TYPE");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        assertEquals(dataSet.getDataStore().getCode(), "STANDARD");
        assertNull(dataSet.getPhysicalData());
        assertNull(dataSet.getLinkedData());
        assertEquals(dataSet.getComponents().size(), 1);
        assertEquals(dataSet.getComponents().iterator().next().getCode(), "20081105092159188-3");
    }

    @Test
    public void testCreateContainerDataSetWithPhysicalDataNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = containerDataSetCreation();
        creation.setPhysicalData(new PhysicalDataCreation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Physical data cannot be set for a non-physical data set.");
    }

    @Test
    public void testCreateContainerDataSetWithLinkedDataNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = containerDataSetCreation();
        creation.setLinkedData(new LinkedDataCreation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Linked data cannot be set for a non-link data set.");
    }

    @Test
    public void testCreateLinkDataSetWithLinkedDataNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        LinkedDataCreation linkedCreation = new LinkedDataCreation();
        linkedCreation.setExternalCode("TEST_EXTERNAL_CODE");
        linkedCreation.setExternalDmsId(new ExternalDmsPermId("DMS_1"));

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_LINK_DATASET");
        creation.setTypeId(new EntityTypePermId("LINK_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setLinkedData(linkedCreation);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        fetchOptions.withLinkedData().withExternalDms();

        DataSet dataSet = createDataSet(sessionToken, creation, fetchOptions);
        assertEquals(dataSet.getCode(), "TEST_LINK_DATASET");
        assertEquals(dataSet.getType().getCode(), "LINK_TYPE");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        assertEquals(dataSet.getDataStore().getCode(), "STANDARD");
        assertEquals(dataSet.getLinkedData().getExternalCode(), "TEST_EXTERNAL_CODE");
        assertEquals(dataSet.getLinkedData().getExternalDms().getCode(), "DMS_1");
        assertNull(dataSet.getPhysicalData());
    }

    @Test
    public void testCreateLinkDataSetWithLinkedDataNotNullAndPhyscialDataNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = linkDataSetCreation();
        creation.setPhysicalData(new PhysicalDataCreation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Physical data cannot be set for a non-physical data set.");
    }

    @Test
    public void testCreateLinkDataSetWithLinkedDataNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = linkDataSetCreation();
        creation.setLinkedData(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Linked data cannot be null for a link data set.");
    }

    @Test
    public void testCreateLinkDataSetWithExternalCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = linkDataSetCreation();
        creation.getLinkedData().setExternalCode(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "External code can not be null.");
    }

    @Test
    public void testCreateLinkDataSetWithExternalDmsNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IExternalDmsId externalDmsId = new ExternalDmsPermId("IDONTEXIST");
        final DataSetCreation creation = linkDataSetCreation();
        creation.getLinkedData().setExternalDmsId(externalDmsId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, externalDmsId);
    }

    @Test
    public void testCreateLinkDataSetWithExternalDmsNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = linkDataSetCreation();
        creation.getLinkedData().setExternalDmsId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "External data management system id cannot be null for a link data set.");
    }

    private DataSetCreation physicalDataSetCreation()
    {
        String code = UUID.randomUUID().toString();

        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setLocation("test/location/" + code);
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

        DataSetCreation creation = new DataSetCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setPhysicalData(physicalCreation);
        creation.setCreationId(new CreationId(code));

        return creation;
    }

    private DataSetCreation containerDataSetCreation()
    {
        String code = UUID.randomUUID().toString();

        DataSetCreation creation = new DataSetCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("CONTAINER_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setCreationId(new CreationId(code));
        return creation;
    }

    private DataSetCreation linkDataSetCreation()
    {
        String code = UUID.randomUUID().toString();

        LinkedDataCreation linkedCreation = new LinkedDataCreation();
        linkedCreation.setExternalCode("TEST_EXTERNAL_CODE");
        linkedCreation.setExternalDmsId(new ExternalDmsPermId("DMS_1"));

        DataSetCreation creation = new DataSetCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("LINK_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setLinkedData(linkedCreation);
        creation.setCreationId(new CreationId(code));

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

    private DataSet createDataSet(String sessionToken, DataSetCreation creation, DataSetFetchOptions fo)
    {
        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        Map<IDataSetId, DataSet> dataSets = v3api.getDataSets(sessionToken, permIds, fo);
        return dataSets.values().iterator().next();
    }

}
