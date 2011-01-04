/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ExperimentTypeCode;

/**
 * Test cases for {@link ExperimentDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "experiment" })
public class ExperimentDAOTest extends AbstractDAOTest
{
    private static final String MODIFIED = "_MODIFIED";

    //
    // Experiments existing in the test database
    //
    private static final String CISD_CISD_DEFAULT_EXP_REUSE = "/CISD/DEFAULT/EXP-REUSE";

    private static final String CISD_CISD_DEFAULT_EXP_X = "/CISD/DEFAULT/EXP-X";

    private static final String CISD_CISD_NEMO_EXP1 = "/CISD/NEMO/EXP1";

    private static final String CISD_CISD_NEMO_EXP10 = "/CISD/NEMO/EXP10";

    private static final String CISD_CISD_NEMO_EXP11 = "/CISD/NEMO/EXP11";

    @Test
    public void testListExperiments() throws Exception
    {
        final List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(8, experiments.size());
        assertExperimentIdentifierPresent(CISD_CISD_DEFAULT_EXP_REUSE, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_DEFAULT_EXP_X, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP1, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP10, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);
    }

    @Test
    public void testListExperimentsWithPropertiesForEmptySet() throws Exception
    {
        List<ExperimentPE> list = daoFactory.getExperimentDAO().listExperimentsWithProperties(Collections.<Long>emptySet());
        assertEquals(0, list.size());
    }
    
    @Test
    public void testListByPermIDForEmptySet() throws Exception
    {
        List<ExperimentPE> list = daoFactory.getExperimentDAO().listByPermID(Collections.<String>emptySet());
        assertEquals(0, list.size());
    }
    
    @Test
    public void testListExperimentsFromProject() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(8, experiments.size());
        final ExperimentPE expInNemo =
                assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);

        final ProjectPE projectNemo = expInNemo.getProject();
        assertEquals(ProjectDAOTest.NEMO, projectNemo.getCode());

        final ExperimentTypePE expType = expInNemo.getExperimentType();
        assertEquals(ExperimentTypeCode.SIRNA_HCS.getCode(), expType.getCode());

        experiments =
                daoFactory.getExperimentDAO().listExperimentsWithProperties(expType, projectNemo);
        Collections.sort(experiments);
        assertEquals(4, experiments.size());
        ExperimentPE exp1 = assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP1, experiments);
        List<DataPE> dataSets = exp1.getDataSets();
        assertEquals(2, dataSets.size());
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP10, experiments);
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);
    }

    @Test
    public void testListExperimentsFromAnotherProject() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(8, experiments.size());
        final ExperimentPE expInDefault = experiments.get(0);
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, expInDefault.getIdentifier());

        final ProjectPE projectDefault = expInDefault.getProject();
        assertEquals(ProjectDAOTest.DEFAULT, projectDefault.getCode());

        final ExperimentTypePE expType = expInDefault.getExperimentType();
        assertEquals(ExperimentTypeCode.SIRNA_HCS.getCode(), expType.getCode());

        experiments =
                daoFactory.getExperimentDAO()
                        .listExperimentsWithProperties(expType, projectDefault);
        Collections.sort(experiments);
        assertEquals(2, experiments.size());
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, experiments.get(0).getIdentifier());
        assertEquals(CISD_CISD_DEFAULT_EXP_X, experiments.get(1).getIdentifier());
    }

    @Test
    public void testListExperimentsOfAnotherType() throws Exception
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(8, experiments.size());
        final ExperimentPE expInDefault = experiments.get(0);
        assertEquals(CISD_CISD_DEFAULT_EXP_REUSE, expInDefault.getIdentifier());

        final ProjectPE projectDefault = expInDefault.getProject();
        assertEquals(ProjectDAOTest.DEFAULT, projectDefault.getCode());

        final List<EntityTypePE> types =
                daoFactory.getEntityTypeDAO(EntityKind.EXPERIMENT).listEntityTypes();
        Collections.sort(types);
        assertEquals(2, types.size());
        final ExperimentTypePE expType = (ExperimentTypePE) types.get(0);
        assertEquals(ExperimentTypeCode.COMPOUND_HCS.getCode(), expType.getCode());

        experiments =
                daoFactory.getExperimentDAO()
                        .listExperimentsWithProperties(expType, projectDefault);
        Collections.sort(experiments);
        assertEquals(0, experiments.size());
    }

    @Test
    public void testTryFindByCodeAndProject()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(8, experiments.size());
        final ExperimentPE templateExp =
                assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP1, experiments);

        ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryFindByCodeAndProject(templateExp.getProject(),
                        templateExp.getCode());

        assertEquals(CISD_CISD_NEMO_EXP1, experiment.getIdentifier());
        assertEquals(2, experiment.getDataSets().size());
    }

    private final ExperimentPE findExperiment(String identifier)
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        List<ExperimentPE> experiments = experimentDAO.listExperiments();

        final ExperimentPE experiment = assertExperimentIdentifierPresent(identifier, experiments);

        return experiment;
    }

    @Test
    public final void testDeleteWithProperties()
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        final ExperimentPE deletedExperiment = findExperiment("/CISD/DEFAULT/EXP-X");

        // Deleted experiment should have all collections which prevent it from deletion empty.
        assertTrue(deletedExperiment.getAttachments().isEmpty());
        assertTrue(deletedExperiment.getDataSets().isEmpty());
        assertTrue(deletedExperiment.getSamples().isEmpty());

        // delete
        experimentDAO.delete(deletedExperiment);

        // test successful deletion of experiment
        assertNull(experimentDAO.tryGetByTechId(TechId.create(deletedExperiment)));

        // test successful deletion of sample properties
        assertFalse(deletedExperiment.getProperties().isEmpty());
        List<EntityTypePropertyTypePE> retrievedPropertyTypes =
                daoFactory.getEntityPropertyTypeDAO(EntityKind.EXPERIMENT).listEntityPropertyTypes(
                        deletedExperiment.getEntityType());
        for (ExperimentPropertyPE property : deletedExperiment.getProperties())
        {
            int index = retrievedPropertyTypes.indexOf(property.getEntityTypePropertyType());
            EntityTypePropertyTypePE retrievedPropertyType = retrievedPropertyTypes.get(index);
            assertFalse(retrievedPropertyType.getPropertyValues().contains(property));
        }
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithAttachments()
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        final ExperimentPE deletedExperiment = findExperiment("/CISD/DEFAULT/EXP-X");

        // Deleted experiment should have attachments which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.

        // Currently there is no such experiment in test DB so we first add an attachment
        // to an experiment empty experiment (with no connections).
        AttachmentPE attachment = CommonTestUtils.createAttachment();
        daoFactory.getAttachmentDAO().createAttachment(attachment, deletedExperiment);

        assertFalse(deletedExperiment.getAttachments().isEmpty());
        assertTrue(deletedExperiment.getDataSets().isEmpty());
        assertTrue(deletedExperiment.getSamples().isEmpty());

        // delete
        experimentDAO.delete(deletedExperiment);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithDataSets()
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        final ExperimentPE deletedExperiment = findExperiment("/CISD/DEFAULT/EXP-X");

        // Deleted experiment should have data sets which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.

        // Currently there is no such experiment in test DB so we first add a data set
        // to an empty experiment (with no connections).
        final ExternalDataPE dataSet = findExternalData("20081105092158673-1");
        dataSet.setExperiment(deletedExperiment);
        daoFactory.getExternalDataDAO().validateAndSaveUpdatedEntity(dataSet);

        assertTrue(deletedExperiment.getAttachments().isEmpty());
        assertFalse(deletedExperiment.getDataSets().isEmpty());
        assertTrue(deletedExperiment.getSamples().isEmpty());

        // delete
        experimentDAO.delete(deletedExperiment);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithSamples()
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        final ExperimentPE deletedExperiment = findExperiment("/CISD/NEMO/EXP10");

        // Deleted experiment should have samples which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertTrue(deletedExperiment.getAttachments().isEmpty());
        assertTrue(deletedExperiment.getDataSets().isEmpty());
        assertFalse(deletedExperiment.getSamples().isEmpty());

        // delete
        experimentDAO.delete(deletedExperiment);
    }

    @Test
    public void testTryFindByCodeAndProjectNonexistent()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        assertEqualsOrGreater(8, experiments.size());
        final ExperimentPE templateExp =
                assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experiments);

        AssertJUnit.assertNull(daoFactory.getExperimentDAO().tryFindByCodeAndProject(
                templateExp.getProject(), "nonexistent"));
    }

    @Test
    public void testCreateExperiment() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        int sizeBefore = experimentsBefore.size();
        assertEqualsOrGreater(8, sizeBefore);

        ExperimentPE experiment = createExperiment("CISD", "CISD", "NEMO", "EXP12", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createExperiment(experiment);

        List<ExperimentPE> experimentsAfter = daoFactory.getExperimentDAO().listExperiments();
        assertEquals(sizeBefore + 1, experimentsAfter.size());
        Collections.sort(experimentsAfter);
        assertExperimentsEqual(experiment, experimentsAfter.get(sizeBefore));
    }

    @Test
    public void testUpdateExperiment() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        int sizeBefore = experimentsBefore.size();
        Collections.sort(experimentsBefore);
        assertEqualsOrGreater(8, sizeBefore);

        ExperimentPE experiment = experimentsBefore.get(0);
        String codeBefore = experiment.getCode();
        String codeModified = codeBefore + MODIFIED;
        experiment.setCode(codeModified);
        experiment.setPermId(daoFactory.getPermIdDAO().createPermId());
        final Date modificationTimestamp = experiment.getModificationDate();
        daoFactory.getExperimentDAO().createExperiment(experiment);

        List<ExperimentPE> experimentsAfter = daoFactory.getExperimentDAO().listExperiments();
        assertEquals(sizeBefore, experimentsAfter.size());
        Collections.sort(experimentsAfter);
        final ExperimentPE experimentFound = experimentsAfter.get(0);
        assertEquals(codeModified, experimentFound.getCode());
        Assert.assertFalse(modificationTimestamp.equals(experimentFound.getModificationDate()));
    }

    @Test
    public void testCreateExperimentsOfDifferentTypes() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        int sizeBefore = experimentsBefore.size();
        assertEqualsOrGreater(8, sizeBefore);

        ExperimentPE experiment = createExperiment("CISD", "CISD", "NEMO", "EXP13", "SIRNA_HCS");
        daoFactory.getExperimentDAO().createExperiment(experiment);

        ExperimentPE experiment2 =
                createExperiment("CISD", "CISD", "NEMO", "EXP12", "COMPOUND_HCS");
        daoFactory.getExperimentDAO().createExperiment(experiment2);

        List<ExperimentPE> experimentsAfter = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experimentsAfter);
        assertEquals(sizeBefore + 2, experimentsAfter.size());
        assertExperimentsEqual(experiment, experimentsAfter.get(sizeBefore + 1));
        assertEquals(experiment.getExperimentType().getCode(), experimentsAfter.get(sizeBefore + 1)
                .getExperimentType().getCode());
        assertExperimentsEqual(experiment2, experimentsAfter.get(sizeBefore));
        assertEquals(experiment2.getExperimentType().getCode(), experimentsAfter.get(sizeBefore)
                .getExperimentType().getCode());
    }

    @Test
    public void testTryCreateExperimentWithExistingIdentifier() throws Exception
    {
        List<ExperimentPE> experimentsBefore = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experimentsBefore);
        assertEqualsOrGreater(8, experimentsBefore.size());
        assertExperimentIdentifierPresent(CISD_CISD_NEMO_EXP11, experimentsBefore);

        ExperimentPE experiment = createExperiment("CISD", "CISD", "NEMO", "EXP11", "SIRNA_HCS");
        boolean exceptionThrown = false;
        try
        {
            daoFactory.getExperimentDAO().createExperiment(experiment);
        } catch (DataIntegrityViolationException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    private static ExperimentPE assertExperimentIdentifierPresent(String experimentIdentifier,
            List<ExperimentPE> experiments)
    {
        for (ExperimentPE experiment : experiments)
        {
            if (experiment.getIdentifier().equals(experimentIdentifier))
            {
                return experiment;
            }
        }
        fail("Experiment with the identifier '" + experimentIdentifier
                + "' expected, but not found.");
        return null; // never reached
    }

    @Test(dataProvider = "illegalCodesProvider")
    public final void testCreateExperimentWithIllegalCode(String code)
    {
        final ExperimentPE experiment = createExperiment("CISD", "CISD", "NEMO", code, "SIRNA_HCS");
        boolean exceptionThrown = false;
        try
        {
            daoFactory.getExperimentDAO().createExperiment(experiment);
        } catch (final DataIntegrityViolationException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public final void testListExperimentsBySimpleProperty()
    {
        final IExperimentDAO dao = daoFactory.getExperimentDAO();
        ProjectPE project =
                findProject(daoFactory.getHomeDatabaseInstance().getCode(), "CISD", "DEFAULT");

        List<ExperimentPE> entities =
                dao.listExperimentsByProjectAndProperty("DESCRIPTION", "A simple experiment",
                        project);

        assertEquals(1, entities.size());
        assertEquals("EXP-X", entities.get(0).getCode());
    }

    @Test
    public final void testListExperimentsByVocabularyTermProperty()
    {
        final IExperimentDAO dao = daoFactory.getExperimentDAO();
        ProjectPE project =
                findProject(daoFactory.getHomeDatabaseInstance().getCode(), "CISD", "NEMO");

        List<ExperimentPE> entities =
                dao.listExperimentsByProjectAndProperty("GENDER", "MALE", project);

        assertEquals(3, entities.size());
    }

    private void assertExperimentsEqual(ExperimentPE e1, ExperimentPE e2)
    {
        assertEquals(e1.getCode(), e2.getCode());
        assertEquals(e1.getExperimentType(), e2.getExperimentType());
        assertEquals(e1.getProject(), e2.getProject());
        assertEquals(e1.getRegistrator(), e2.getRegistrator());
        assertEquals(e1.getRegistrationDate(), e2.getRegistrationDate());
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] illegalCodesProvider()
    {
        return new Object[][]
            {
                { EXCEED_CODE_LENGTH_CHARACTERS },
                { "" },
                { null },
                { "@XPERIMENT" }, };
    }

    @Test
    public void testLoadByPermId() throws Exception
    {
        ExperimentPE exp = daoFactory.getExperimentDAO().listAllEntities().get(0);
        HashSet<String> keys = new HashSet<String>();
        keys.add(exp.getPermId());
        keys.add("nonexistent");
        List<ExperimentPE> result = daoFactory.getExperimentDAO().listByPermID(keys);
        AssertJUnit.assertEquals(1, result.size());
        AssertJUnit.assertEquals(exp, result.get(0));
    }

    @Test
    public void testLoadByPermIdNoEntries() throws Exception
    {
        HashSet<String> keys = new HashSet<String>();
        List<ExperimentPE> result = daoFactory.getExperimentDAO().listByPermID(keys);
        AssertJUnit.assertTrue(result.isEmpty());
    }

}
