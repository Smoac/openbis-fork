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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;

/**
 * System tests for
 * {@link IETLLIMSService#performEntityOperations(String, AtomicEntityOperationDetails)}
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class EntityOperationTest extends SystemTestCase
{
    private static final String PREFIX = "EO_";

    private static final String SPACE_ETL_SERVER_FOR_A = PREFIX + "S_ETL_A";

    private static final String SPACE_ETL_SERVER_FOR_B = PREFIX + "S_ETL_B";

    private static final String INSTANCE_ETL_SERVER = PREFIX + "I_ETL";

    private static final String INSTANCE_ADMIN = PREFIX + "I_A";

    private static final SpaceIdentifier SPACE_A = new SpaceIdentifier("CISD", "CISD");

    private static final SpaceIdentifier SPACE_B = new SpaceIdentifier("CISD", "TESTGROUP");

    private static final class EntityOperationBuilder
    {
        private static long counter = 1000;

        private final List<NewSpace> spaces = new ArrayList<NewSpace>();

        private final List<NewProject> projects = new ArrayList<NewProject>();

        private final List<NewExperiment> experiments = new ArrayList<NewExperiment>();

        private final List<ExperimentUpdatesDTO> experimentUpdates =
                new ArrayList<ExperimentUpdatesDTO>();

        private final List<NewSample> samples = new ArrayList<NewSample>();

        private final List<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

        private final List<NewExternalData> dataSets = new ArrayList<NewExternalData>();

        private final List<DataSetBatchUpdatesDTO> dataSetUpdates =
                new ArrayList<DataSetBatchUpdatesDTO>();

        private final Map<String, List<NewMaterial>> materials =
                new HashMap<String, List<NewMaterial>>();

        private final List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();

        private final List<NewMetaproject> metaprojectRegistrations =
                new ArrayList<NewMetaproject>();

        private final List<MetaprojectUpdatesDTO> metaprojectUpdates =
                new ArrayList<MetaprojectUpdatesDTO>();

        private final List<VocabularyUpdatesDTO> vocabularyUpdates =
                new ArrayList<VocabularyUpdatesDTO>();

        private TechId registrationID = new TechId(counter++);

        private String userID;

        EntityOperationBuilder user(String user)
        {
            this.userID = user;
            return this;
        }

        EntityOperationBuilder space(String code)
        {
            return space(new NewSpace(code, null, null));
        }

        EntityOperationBuilder space(NewSpace space)
        {
            spaces.add(space);
            return this;
        }

        EntityOperationBuilder material(String materialTypeCode, Material material)
        {
            List<NewMaterial> list = materials.get(materialTypeCode);
            if (list == null)
            {
                list = new ArrayList<NewMaterial>();
                materials.put(materialTypeCode, list);
            }
            list.add(MaterialTranslator.translateToNewMaterial(material));
            return this;
        }

        EntityOperationBuilder project(SpaceIdentifier spaceIdentifier, String projectCode)
        {
            String projectIdentifier =
                    new ProjectIdentifier(spaceIdentifier, projectCode).toString();
            return project(new NewProject(projectIdentifier, null));
        }

        EntityOperationBuilder project(NewProject project)
        {
            projects.add(project);
            return this;
        }

        EntityOperationBuilder experiment(Experiment experiment)
        {
            NewExperiment newExperiment =
                    new NewExperiment(experiment.getIdentifier(), experiment.getEntityType()
                            .getCode());
            newExperiment.setPermID(experiment.getPermId());
            newExperiment.setProperties(experiment.getProperties().toArray(new IEntityProperty[0]));
            experiments.add(newExperiment);
            return this;
        }

        EntityOperationBuilder sample(Sample sample)
        {
            NewSample newSample = new NewSample();
            newSample.setIdentifier(sample.getIdentifier());
            newSample.setSampleType(sample.getSampleType());
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                newSample.setExperimentIdentifier(experiment.getIdentifier());
            }
            newSample.setProperties(sample.getProperties().toArray(new IEntityProperty[0]));
            samples.add(newSample);
            return this;
        }

        EntityOperationBuilder sampleUpdate(Sample sample)
        {
            sampleUpdates.add(new SampleUpdatesDTO(new TechId(sample), sample.getProperties(),
                    null, null, sample.getVersion(), SampleIdentifierFactory.parse(sample
                            .getIdentifier()), null, null));
            return this;
        }

        EntityOperationBuilder dataSet(ExternalData dataSet)
        {
            NewExternalData newExternalData = new NewExternalData();
            newExternalData.setCode(dataSet.getCode());
            newExternalData.setDataSetType(dataSet.getDataSetType());
            newExternalData.setDataStoreCode(dataSet.getDataStore().getCode());
            if (dataSet instanceof DataSet)
            {
                DataSet realDataSet = (DataSet) dataSet;
                newExternalData.setFileFormatType(realDataSet.getFileFormatType());
                newExternalData.setLocation(realDataSet.getLocation());
                newExternalData.setLocatorType(realDataSet.getLocatorType());
            }
            newExternalData.setStorageFormat(StorageFormat.PROPRIETARY);
            List<IEntityProperty> properties = dataSet.getProperties();
            List<NewProperty> newProperties = new ArrayList<NewProperty>();
            for (IEntityProperty property : properties)
            {
                newProperties.add(new NewProperty(property.getPropertyType().getCode(), property
                        .tryGetAsString()));
            }
            newExternalData.setDataSetProperties(newProperties);
            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                newExternalData.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample
                        .getIdentifier()));
            }
            Experiment experiment = dataSet.getExperiment();
            if (experiment != null)
            {
                newExternalData.setExperimentIdentifierOrNull(ExperimentIdentifierFactory
                        .parse(experiment.getIdentifier()));
            }
            dataSets.add(newExternalData);
            return this;
        }

        EntityOperationBuilder dataSetUpdate(ExternalData dataSet)
        {
            DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
            dataSetUpdate.setDetails(new DataSetBatchUpdateDetails());
            dataSetUpdate.setDatasetId(new TechId(dataSet));
            dataSetUpdate.setDatasetCode(dataSet.getCode());
            dataSetUpdate.setVersion(dataSet.getModificationDate());
            if (dataSet instanceof DataSet)
            {
                DataSet realDataSet = (DataSet) dataSet;
                dataSetUpdate.setFileFormatTypeCode(realDataSet.getFileFormatType().getCode());
            }
            dataSetUpdate.setProperties(dataSet.getProperties());

            // Request an update of all properties
            HashSet<String> propertiesToUpdate = new HashSet<String>();
            for (IEntityProperty property : dataSet.getProperties())
            {
                propertiesToUpdate.add(property.getPropertyType().getCode());
            }
            dataSetUpdate.getDetails().setPropertiesToUpdate(propertiesToUpdate);

            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                dataSetUpdate.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample
                        .getIdentifier()));
            }
            Experiment experiment = dataSet.getExperiment();
            if (experiment != null)
            {
                dataSetUpdate.setExperimentIdentifierOrNull(ExperimentIdentifierFactory
                        .parse(experiment.getIdentifier()));
            }
            dataSetUpdates.add(dataSetUpdate);
            return this;
        }

        AtomicEntityOperationDetails create()
        {
            return new AtomicEntityOperationDetails(registrationID, userID, spaces, projects,
                    experiments, experimentUpdates, sampleUpdates, samples, materials,
                    materialUpdates, dataSets, dataSetUpdates, metaprojectRegistrations,
                    metaprojectUpdates, vocabularyUpdates);
        }

    }

    @BeforeClass
    public void createTestUsers()
    {
        assignSpaceRole(registerPerson(SPACE_ETL_SERVER_FOR_A), RoleCode.ETL_SERVER, SPACE_A);
        assignSpaceRole(registerPerson(SPACE_ETL_SERVER_FOR_B), RoleCode.ETL_SERVER, SPACE_B);
        assignInstanceRole(registerPerson(INSTANCE_ADMIN), RoleCode.ADMIN);
        assignInstanceRole(registerPerson(INSTANCE_ETL_SERVER), RoleCode.ETL_SERVER);
    }

    @Test
    public void testCreateSpaceAsInstanceAdmin()
    {
        String sessionToken = authenticateAs(INSTANCE_ADMIN);
        AtomicEntityOperationDetails eo = new EntityOperationBuilder().space("TEST_SPACE").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSpacesCreatedCount());

        Space space = etlService.tryGetSpace(sessionToken, new SpaceIdentifier("TEST_SPACE"));
        assertEquals("CISD/TEST_SPACE", space.toString());
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void testCreateSpaceAsInstanceAdminButLoginAsSpaceETLServerFails()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).space("TEST_SPACE").create();

        etlService.performEntityOperations(sessionToken, eo);
    }

    @Test
    public void testCreateSpaceAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo = new EntityOperationBuilder().space("TEST_SPACE").create();

        performFailungEntityOperations(sessionToken, eo,
                "Authorization failure: ERROR: \"None of method roles "
                        + "'[INSTANCE_ETL_SERVER, INSTANCE_ADMIN]' "
                        + "could be found in roles of user '" + SPACE_ETL_SERVER_FOR_A + "'.\".");
    }

    @Test
    public void testCreateMaterialAsInstanceETLServer()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().material(
                        "GENE",
                        new MaterialBuilder().code("ALPHA").property("GENE_SYMBOL", "42")
                                .getMaterial()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getMaterialsCreatedCount());

        Material material =
                etlService.tryGetMaterial(sessionToken, new MaterialIdentifier("ALPHA", "GENE"));
        assertEquals("ALPHA (GENE)", material.toString());
        assertEquals("[GENE_SYMBOL: 42]", material.getProperties().toString());
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void testCreateMaterialAsInstanceAdminButLoginAsSpaceETLServerFails()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .material(
                                "GENE",
                                new MaterialBuilder().code("ALPHA").property("GENE_SYMBOL", "42")
                                        .getMaterial()).create();

        etlService.performEntityOperations(sessionToken, eo);
    }

    @Test
    public void testCreateMaterialAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().material("GENE",
                        new MaterialBuilder().code("ALPHA").getMaterial()).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: "
                + "ERROR: \"None of method roles '[INSTANCE_ETL_SERVER, INSTANCE_ADMIN]' "
                + "could be found in roles of user '" + SPACE_ETL_SERVER_FOR_A + "'.\".");
    }

    @Test
    public void testCreateProjectAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().project(SPACE_A, "P1").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getProjectsCreatedCount());

        Project project =
                etlService.tryGetProject(sessionToken, new ProjectIdentifier(SPACE_A, "P1"));
        assertEquals("/" + SPACE_A.getSpaceCode() + "/P1", project.toString());
    }

    @Test
    public void testCreateProjectAsInstanceAdminButLoginAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).project(SPACE_A, "P1").create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getProjectsCreatedCount());

        Project project =
                etlService.tryGetProject(sessionToken, new ProjectIdentifier(SPACE_A, "P1"));
        assertEquals("/" + SPACE_A.getSpaceCode() + "/P1", project.toString());
    }

    @Test
    public void testCreateProjectAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().project(SPACE_B, "P1").create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_A + "' does not have enough privileges.\".");
    }

    @Test
    public void testCreateExperimentAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String experimentIdentifier = "/CISD/NEMO/E1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().experiment(
                        new ExperimentBuilder().identifier(experimentIdentifier).type("SIRNA_HCS")
                                .property("DESCRIPTION", "hello").getExperiment()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getExperimentsCreatedCount());

        // Need to make an additional call to get the experiment from the DB
        Experiment experiment =
                etlService.tryToGetExperiment(sessionToken,
                        ExperimentIdentifierFactory.parse(experimentIdentifier));

        assertEquals("/CISD/NEMO/E1", experiment.getIdentifier());
        assertEquals("SIRNA_HCS", experiment.getExperimentType().getCode());
        assertEquals("[DESCRIPTION: hello]", experiment.getProperties().toString());
    }

    @Test
    public void testCreateExperimentAsInstanceAdminButLoginAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String experimentIdentifier = "/CISD/NEMO/E1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .experiment(
                                new ExperimentBuilder().identifier(experimentIdentifier)
                                        .type("SIRNA_HCS").property("DESCRIPTION", "hello")
                                        .getExperiment()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getExperimentsCreatedCount());

        // Need to make an additional call to get the experiment from the DB
        Experiment experiment =
                etlService.tryToGetExperiment(sessionToken,
                        ExperimentIdentifierFactory.parse(experimentIdentifier));

        assertEquals("/CISD/NEMO/E1", experiment.getIdentifier());
        assertEquals("SIRNA_HCS", experiment.getExperimentType().getCode());
        assertEquals("[DESCRIPTION: hello]", experiment.getProperties().toString());
    }

    @Test
    public void testCreateExperimentAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_B);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().experiment(
                        new ExperimentBuilder().identifier("/CISD/NEMO/E1").type("SIRNA_HCS")
                                .getExperiment()).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testCreateInstanceSampleAsInstanceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        String sampleIdentifier = "/S1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sample(
                        new SampleBuilder().identifier(sampleIdentifier).type("MASTER_PLATE")
                                .property("$PLATE_GEOMETRY", "96_WELLS_8X12").getSample()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesCreatedCount());

        Sample sample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdentifier));
        assertEquals(sampleIdentifier, sample.getIdentifier());
        assertEquals("MASTER_PLATE", sample.getSampleType().getCode());
        assertEquals("[$PLATE_GEOMETRY: 96_WELLS_8X12]", sample.getProperties().toString());
    }

    @Test
    public void testCreateInstanceSampleAsInstanceAdminButLoginAsInstanceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        String sampleIdentifier = "/S1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .sample(new SampleBuilder().identifier(sampleIdentifier)
                                .type("MASTER_PLATE").property("$PLATE_GEOMETRY", "96_WELLS_8X12")
                                .getSample()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesCreatedCount());

        Sample sample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdentifier));
        assertEquals(sampleIdentifier, sample.getIdentifier());
        assertEquals("MASTER_PLATE", sample.getSampleType().getCode());
        assertEquals("[$PLATE_GEOMETRY: 96_WELLS_8X12]", sample.getProperties().toString());
    }

    @Test
    public void testCreateInstanceSampleAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sample(
                        new SampleBuilder().identifier("/S1").type("MASTER_PLATE")
                                .property("$PLATE_GEOMETRY", "96_WELLS_8X12").getSample()).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_A + "' does not have enough privileges "
                + "to modify database instance 'CISD'.\".");
    }

    @Test
    public void testCreateSpaceSampleAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String sampleIdentifier = "/CISD/S1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sample(
                        new SampleBuilder()
                                .identifier(sampleIdentifier)
                                .type("CELL_PLATE")
                                .property("COMMENT", "hello")
                                .experiment(
                                        new ExperimentBuilder().identifier("/CISD/NEMO/EXP1")
                                                .getExperiment()).getSample()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesCreatedCount());

        Sample sample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdentifier));
        assertEquals(sampleIdentifier, sample.getIdentifier());
        assertEquals("CELL_PLATE", sample.getSampleType().getCode());
        assertEquals("[COMMENT: hello]", sample.getProperties().toString());
        assertEquals("/CISD/NEMO/EXP1", sample.getExperiment().getIdentifier());
    }

    @Test
    public void testCreateSpaceSampleAsSpaceETLServerButLoginAsInstanceAdminSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ADMIN);
        String sampleIdentifier = "/CISD/S1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(SPACE_ETL_SERVER_FOR_A)
                        .sample(new SampleBuilder()
                                .identifier(sampleIdentifier)
                                .type("CELL_PLATE")
                                .property("COMMENT", "hello")
                                .experiment(
                                        new ExperimentBuilder().identifier("/CISD/NEMO/EXP1")
                                                .getExperiment()).getSample()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesCreatedCount());

        Sample sample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdentifier));
        assertEquals(sampleIdentifier, sample.getIdentifier());
        assertEquals("CELL_PLATE", sample.getSampleType().getCode());
        assertEquals("[COMMENT: hello]", sample.getProperties().toString());
        assertEquals("/CISD/NEMO/EXP1", sample.getExperiment().getIdentifier());
    }

    @Test
    public void testCreateSpaceSampleAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_B);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sample(
                        new SampleBuilder().identifier("/CISD/S1").type("CELL_PLATE").getSample())
                        .create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testUpdateInstanceSampleAsInstanceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(646)).getParent();
        List<IEntityProperty> properties = sample.getProperties();
        assertEquals("[$PLATE_GEOMETRY: 384_WELLS_16X24]", properties.toString());
        sample.setProperties(new SampleBuilder().property("$PLATE_GEOMETRY", "96_WELLS_8X12")
                .getSample().getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sampleUpdate(sample).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesUpdatedCount());

        Sample updatedSample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sample.getIdentifier()));

        assertEquals(new Long(646), updatedSample.getId());
        assertEquals("/MP", updatedSample.getIdentifier());
        assertEquals("MASTER_PLATE", updatedSample.getSampleType().getCode());
        assertEquals("[$PLATE_GEOMETRY: 96_WELLS_8X12]", updatedSample.getProperties().toString());
    }

    @Test
    public void testUpdateInstanceSampleAsInstanceAdminButLoginAsInstanceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ETL_SERVER);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(646)).getParent();
        List<IEntityProperty> properties = sample.getProperties();
        assertEquals("[$PLATE_GEOMETRY: 384_WELLS_16X24]", properties.toString());
        sample.setProperties(new SampleBuilder().property("$PLATE_GEOMETRY", "96_WELLS_8X12")
                .getSample().getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).sampleUpdate(sample).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesUpdatedCount());

        Sample updatedSample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sample.getIdentifier()));

        assertEquals(new Long(646), updatedSample.getId());
        assertEquals("/MP", updatedSample.getIdentifier());
        assertEquals("MASTER_PLATE", updatedSample.getSampleType().getCode());
        assertEquals("[$PLATE_GEOMETRY: 96_WELLS_8X12]", updatedSample.getProperties().toString());
    }

    @Test
    public void testUpdateInstanceSampleAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(646)).getParent();
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sampleUpdate(sample).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: "
                + "\"None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' "
                + "could be found in roles of user '" + SPACE_ETL_SERVER_FOR_A + "'.\".");
    }

    @Test
    public void testUpdateSpaceSampleAsSpaceETLServerButLoginAsInstanceAdminSuccessfully()
    {
        String sessionToken = authenticateAs(INSTANCE_ADMIN);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(986)).getParent();
        List<IEntityProperty> properties = sample.getProperties();
        assertEquals("[]", properties.toString());
        sample.setProperties(new SampleBuilder().property("COMMENT", "hello").getSample()
                .getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(SPACE_ETL_SERVER_FOR_A).sampleUpdate(sample)
                        .create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesUpdatedCount());

        Sample updatedSample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sample.getIdentifier()));
        assertEquals(new Long(986), updatedSample.getId());
        assertEquals("/CISD/3VCP5", updatedSample.getIdentifier());
        assertEquals("CELL_PLATE", updatedSample.getSampleType().getCode());
        assertEquals("[COMMENT: hello]", updatedSample.getProperties().toString());
    }

    @Test
    public void testUpdateSpaceSampleAsSpaceETLServerSuccessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(986)).getParent();
        List<IEntityProperty> properties = sample.getProperties();
        assertEquals("[]", properties.toString());
        sample.setProperties(new SampleBuilder().property("COMMENT", "hello").getSample()
                .getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sampleUpdate(sample).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getSamplesUpdatedCount());

        Sample updatedSample =
                etlService.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sample.getIdentifier()));
        assertEquals(new Long(986), updatedSample.getId());
        assertEquals("/CISD/3VCP5", updatedSample.getIdentifier());
        assertEquals("CELL_PLATE", updatedSample.getSampleType().getCode());
        assertEquals("[COMMENT: hello]", updatedSample.getProperties().toString());
    }

    @Test
    public void testUpdateSpaceSampleAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_B);
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(986)).getParent();
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().sampleUpdate(sample).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testCreateDataSetAsSpaceETLServerSucessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String dataSetCode = "DS-1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().dataSet(
                        new DataSetBuilder().code(dataSetCode).type("HCS_IMAGE")
                                .store(new DataStoreBuilder("STANDARD").getStore())
                                .fileFormat("XML").location("a/b/c").property("COMMENT", "my data")
                                .sample(new SampleBuilder().identifier("/CISD/CP1-A1").getSample())
                                .getDataSet()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getDataSetsCreatedCount());
        ExternalData dataSet = etlService.tryGetDataSet(sessionToken, dataSetCode);

        assertEquals(dataSetCode, dataSet.getCode());
        assertEquals("HCS_IMAGE", dataSet.getDataSetType().getCode());
        assertEquals("[COMMENT: my data]", dataSet.getProperties().toString());
    }

    @Test
    public void testCreateDataSetAsInstanceAdminButLoginAsSpaceETLServerSucessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        String dataSetCode = "DS-1";
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(INSTANCE_ADMIN)
                        .dataSet(
                                new DataSetBuilder()
                                        .code(dataSetCode)
                                        .type("HCS_IMAGE")
                                        .store(new DataStoreBuilder("STANDARD").getStore())
                                        .fileFormat("XML")
                                        .location("a/b/c")
                                        .property("COMMENT", "my data")
                                        .sample(new SampleBuilder().identifier("/CISD/CP1-A1")
                                                .getSample()).getDataSet()).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getDataSetsCreatedCount());
        ExternalData dataSet = etlService.tryGetDataSet(sessionToken, dataSetCode);

        assertEquals(dataSetCode, dataSet.getCode());
        assertEquals("HCS_IMAGE", dataSet.getDataSetType().getCode());
        assertEquals("[COMMENT: my data]", dataSet.getProperties().toString());
    }

    @Test
    public void testCreateDataSetAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder()
                        .user(SPACE_ETL_SERVER_FOR_B)
                        .dataSet(
                                new DataSetBuilder()
                                        .code("DS-1")
                                        .type("UNKNOWN")
                                        .store(new DataStoreBuilder("STANDARD").getStore())
                                        .fileFormat("XML")
                                        .location("a/b/c")
                                        .experiment(
                                                new ExperimentBuilder().identifier(
                                                        "/CISD/NEMO/EXP1").getExperiment())
                                        .getDataSet()).create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    @Test
    public void testUpdateDataSetAsSpaceETLServerSucessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        ExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(4));
        dataSet.setDataSetProperties(new DataSetBuilder().property("COMMENT", "hello").getDataSet()
                .getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().dataSetUpdate(dataSet).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getDataSetsUpdatedCount());

        ExternalData updatedDataSet = etlService.tryGetDataSet(sessionToken, dataSet.getCode());
        assertEquals(new Long(4), updatedDataSet.getId());
        assertEquals("[COMMENT: hello]", updatedDataSet.getProperties().toString());
    }

    @Test
    public void testUpdateDataSetAsInstanceAdminButLoginAsSpaceETLServerSucessfully()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        ExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(4));
        dataSet.setDataSetProperties(new DataSetBuilder().property("COMMENT", "hello").getDataSet()
                .getProperties());
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(INSTANCE_ADMIN).dataSetUpdate(dataSet).create();

        AtomicEntityOperationResult result = etlService.performEntityOperations(sessionToken, eo);
        assertEquals(1, result.getDataSetsUpdatedCount());

        ExternalData updatedDataSet = etlService.tryGetDataSet(sessionToken, dataSet.getCode());
        assertEquals(new Long(4), updatedDataSet.getId());
        assertEquals("[COMMENT: hello]", updatedDataSet.getProperties().toString());
    }

    @Test
    public void testUpdateDataSetAsSpaceETLServerThrowsAuthorizationFailure()
    {
        String sessionToken = authenticateAs(SPACE_ETL_SERVER_FOR_A);
        ExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(4));
        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(SPACE_ETL_SERVER_FOR_B).dataSetUpdate(dataSet)
                        .create();

        performFailungEntityOperations(sessionToken, eo, "Authorization failure: ERROR: \"User '"
                + SPACE_ETL_SERVER_FOR_B + "' does not have enough privileges.\".");
    }

    private void performFailungEntityOperations(String sessionToken,
            AtomicEntityOperationDetails eo, String expectedMessage)
    {
        try
        {
            etlService.performEntityOperations(sessionToken, eo);
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(expectedMessage, ex.getMessage());
        }
    }
}
