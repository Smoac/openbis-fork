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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link ExperimentBO} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses =
    { ExperimentPE.class, ExperimentBO.class })
public final class ExperimentBOTest extends AbstractBOTest
{

    private static final String EXP_TYPE_UNEXISTENT = "EXP-TYPE-UNEXISTENT";

    private static final String PROJECT_UNEXISTENT = "PROJECT-UNEXISTENT";

    private static final String DB = "DB";

    private static final String GROUP = "GROUP";

    private static final String PROJECT = "PROJECT";

    private static final String EXP_TYPE_CODE = "EXP-TYPE-CODE";

    private static final String EXP_CODE = "EXP-CODE";

    private final ExperimentBO createExperimentBO()
    {
        return new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @Test
    public void testLoadByExperimentIdentifier() throws Exception
    {
        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        prepareLoadExperimentByIdentifier(identifier, exp);
        loadExperiment(identifier, exp);
        context.assertIsSatisfied();
    }

    private void prepareUpdateProperties(final Set<ExperimentPropertyPE> oldProperties,
            final List<IEntityProperty> newProperties, final EntityTypePE entityType,
            final PersonPE registrator, final List<ExperimentPropertyPE> updated)
    {
        context.checking(new Expectations()
            {
                {
                    one(propertiesConverter).updateProperties(oldProperties, entityType,
                            newProperties, registrator, new HashSet<String>());
                    will(returnValue(new HashSet<ExperimentPropertyPE>(updated)));

                }
            });
    }

    private void prepareLoadExperimentByIdentifier(final ExperimentIdentifier identifier,
            final ExperimentPE exp)
    {
        prepareAnyDaoCreation();
        final ProjectPE project = exp.getProject();
        prepareTryFindProject(identifier, project);
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryFindByCodeAndProject(project,
                            identifier.getExperimentCode());
                    will(returnValue(exp));

                }
            });
    }

    private void prepareTryFindProject(final ProjectIdentifier identifier,
            final ProjectPE foundProject)
    {
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(identifier.getDatabaseInstanceCode(),
                            identifier.getSpaceCode(), identifier.getProjectCode());
                    will(Expectations.returnValue(foundProject));
                }
            });

    }

    @Test
    public void testGetExperimentFileAttachment() throws Exception
    {
        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        final AttachmentPE attachment1 = CommonTestUtils.createAttachment();
        final AttachmentPE attachment2 = CommonTestUtils.createAttachment();
        attachment2.setVersion(attachment1.getVersion() + 1);
        setExperimentAttachments(exp, attachment1, attachment2);
        final ProjectPE project = exp.getProject();
        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(identifier.getDatabaseInstanceCode(),
                            identifier.getSpaceCode(), identifier.getProjectCode());
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project,
                            identifier.getExperimentCode());
                    will(returnValue(exp));

                }
            });
        final ExperimentBO expBO = createExperimentBO();
        expBO.loadByExperimentIdentifier(identifier);

        // Get first attachment
        AssertJUnit.assertEquals(
                attachment1,
                expBO.getExperimentFileAttachment(attachment1.getFileName(),
                        attachment1.getVersion()));

        // Get another version of attachment
        AssertJUnit.assertEquals(
                attachment2,
                expBO.getExperimentFileAttachment(attachment2.getFileName(),
                        attachment2.getVersion()));

        // Try find not existing version of attachment
        testThrowingExceptionOnUnknownFileVersion(attachment2, expBO);

        // Try find not existing attachment (incorrect file name)
        testThrowingExceptionOnUnknownFilename(attachment2, expBO);
    }

    @Test
    public void testDefineAndSave()
    {
        final String expCode = EXP_CODE;
        final String expTypeCode = EXP_TYPE_CODE;
        final String projectCode = PROJECT;
        final String groupCode = GROUP;
        final String dbCode = DB;

        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(createIdentifier(dbCode, groupCode, projectCode, expCode));
        newExperiment.setExperimentTypeCode(expTypeCode);

        final ProjectPE project = createProject(dbCode, groupCode, projectCode);
        final ExperimentTypePE type = createExperimentType(expTypeCode);
        final ExperimentPE experiment = createExperiment(project, expCode, type);

        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(entityTypeDAO).listEntityTypes();
                    will(Expectations.returnValue(Collections.singletonList(type)));

                    atLeast(1).of(entityPropertyTypeDAO).listEntityPropertyTypes(type);
                    will(Expectations.returnValue(new ArrayList<ExperimentTypePropertyTypePE>(type
                            .getExperimentTypePropertyTypes())));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));

                    one(projectDAO).tryFindProject(dbCode, groupCode, projectCode);
                    will(returnValue(project));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010112341234-1"));

                    one(experimentDAO).createExperiment(experiment);
                }
            });
        final ExperimentBO experimentBO = createExperimentBO();
        experimentBO.define(newExperiment);
        experimentBO.save();

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithUnexistentExperimentType()
    {
        final String expCode = EXP_CODE;
        final String expTypeCode = EXP_TYPE_UNEXISTENT;
        final String projectCode = PROJECT;
        final String groupCode = GROUP;
        final String dbCode = DB;

        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(createIdentifier(dbCode, groupCode, projectCode, expCode));
        newExperiment.setExperimentTypeCode(expTypeCode);

        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(null));
                }
            });
        final ExperimentBO experimentBO = createExperimentBO();
        boolean exceptionThrown = false;
        try
        {
            experimentBO.define(newExperiment);
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
            assertTrue(e.getMessage().indexOf(
                    String.format(ExperimentBO.ERR_EXPERIMENT_TYPE_NOT_FOUND, expTypeCode)) > -1);
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithUnexistentProject()
    {
        final String expCode = EXP_CODE;
        final String expTypeCode = EXP_TYPE_CODE;
        final String projectCode = PROJECT_UNEXISTENT;
        final String groupCode = GROUP;
        final String dbCode = DB;

        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(createIdentifier(dbCode, groupCode, projectCode, expCode));
        newExperiment.setExperimentTypeCode(expTypeCode);

        final ExperimentTypePE type = createExperimentType(expTypeCode);

        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(entityTypeDAO).listEntityTypes();
                    will(Expectations.returnValue(Collections.singletonList(type)));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));

                    one(projectDAO).tryFindProject(dbCode, groupCode, projectCode);
                    will(returnValue(null));

                    one(entityPropertyTypeDAO).listEntityPropertyTypes(type);
                    will(returnValue(new ArrayList<EntityTypePropertyTypePE>()));

                }
            });
        final ExperimentBO experimentBO = createExperimentBO();
        boolean exceptionThrown = false;
        try
        {
            experimentBO.define(newExperiment);
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
            assertTrue(e.getMessage().indexOf(
                    String.format(ExperimentBO.ERR_PROJECT_NOT_FOUND,
                            createIdentifier(dbCode, groupCode, projectCode, expCode))) > -1);
        }
        assertTrue(exceptionThrown);

        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineAndSaveAlreadyExistingExperiment()
    {
        final String expCode = EXP_CODE;
        final String expTypeCode = EXP_TYPE_CODE;
        final String projectCode = PROJECT;
        final String groupCode = GROUP;
        final String dbCode = DB;

        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(createIdentifier(dbCode, groupCode, projectCode, expCode));
        newExperiment.setExperimentTypeCode(expTypeCode);

        final ProjectPE project = createProject(dbCode, groupCode, projectCode);
        final ExperimentTypePE type = createExperimentType(expTypeCode);
        final ExperimentPE experiment = createExperiment(project, expCode, type);

        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    allowing(entityTypeDAO).listEntityTypes();
                    will(Expectations.returnValue(Collections.singletonList(type)));

                    allowing(entityPropertyTypeDAO).listEntityPropertyTypes(type);
                    will(Expectations.returnValue(new ArrayList<ExperimentTypePropertyTypePE>(type
                            .getExperimentTypePropertyTypes())));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));

                    one(projectDAO).tryFindProject(dbCode, groupCode, projectCode);
                    will(returnValue(project));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010101011111-1"));

                    one(experimentDAO).createExperiment(experiment);
                    will(throwException(new DataIntegrityViolationException(
                            "exception description...")));
                }
            });
        final ExperimentBO experimentBO = createExperimentBO();
        experimentBO.define(newExperiment);
        boolean exceptionThrown = false;
        try
        {
            experimentBO.save();
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    private void prepareAnyDaoCreation()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.EXPERIMENT);
                    will(Expectations.returnValue(entityPropertyTypeDAO));

                    allowing(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(Expectations.returnValue(entityTypeDAO));

                    allowing(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    allowing(daoFactory).getExternalDataDAO();
                    will(returnValue(externalDataDAO));

                    allowing(daoFactory).getPermIdDAO();
                    will(returnValue(permIdDAO));
                }
            });
    }

    @Test
    public final void testEditProperties()
    {
        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        ExperimentTypePE experimentType = CommonTestUtils.createExperimentType();
        exp.setExperimentType(experimentType);

        ExperimentPropertyPE changedProperty = CommonTestUtils.createOrganProperty(experimentType);
        ExperimentPropertyPE deletedProperty = CommonTestUtils.createNotesProperty(experimentType);
        ExperimentPropertyPE addedProperty = CommonTestUtils.createCategoryProperty(experimentType);
        exp.setProperties(new HashSet<ExperimentPropertyPE>(Arrays.asList(changedProperty,
                deletedProperty)));

        prepareLoadExperimentByIdentifier(identifier, exp);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter);
        bo.loadByExperimentIdentifier(identifier);

        assertTrue(bo.getExperiment().getProperties().contains(changedProperty));
        assertTrue(bo.getExperiment().getProperties().contains(deletedProperty));
        assertFalse(bo.getExperiment().getProperties().contains(addedProperty));

        final List<IEntityProperty> newProperties = createDummyProperties();
        prepareUpdateProperties(exp.getProperties(), newProperties, experimentType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(),
                Arrays.asList(changedProperty, addedProperty));
        bo.updateProperties(newProperties);

        assertTrue(bo.getExperiment().getProperties().contains(changedProperty));
        assertFalse(bo.getExperiment().getProperties().contains(deletedProperty));
        assertTrue(bo.getExperiment().getProperties().contains(addedProperty));

        context.assertIsSatisfied();
    }

    private List<IEntityProperty> createDummyProperties()
    {
        return new ArrayList<IEntityProperty>();
    }

    @Test
    public final void testEditProject()
    {

        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        ExperimentPE exp = CommonTestUtils.createExperiment(identifier);

        SpacePE group = CommonTestUtils.createGroup(identifier);
        SamplePE assignedSample = createSampleWithCode("assignedSample");
        assignedSample.setSpace(group);
        exp.setSamples(Arrays.asList(assignedSample));

        prepareLoadExperimentByIdentifier(identifier, exp);
        ExperimentBO expBO = loadExperiment(identifier, exp);

        final ProjectIdentifier newProjectIdentifier =
                new ProjectIdentifier(identifier.getDatabaseInstanceCode(), "anotherGroup",
                        "anotherProject");
        final ProjectPE newProject = CommonTestUtils.createProject(newProjectIdentifier);
        prepareTryFindProject(newProjectIdentifier, newProject);

        assertFalse(newProject.equals(exp.getProject()));
        assertFalse(newProject.getSpace().equals(assignedSample.getSpace()));

        expBO.updateProject(newProjectIdentifier);

        assertEquals(newProject, exp.getProject());
        assertEquals(newProject.getSpace(), assignedSample.getSpace());
    }

    @Test
    public final void testEditSamples()
    {
        // we test if this sample will stay assigned to the experiment if it was assigned before
        SamplePE untouchedSample = createSampleWithCode("untouchedSample");
        // we test unasignment of this sample from the experiment
        SamplePE unassignedSample = createSampleWithCode("unassignedSample");
        // we test if this sample will be assigned to the experiment
        SamplePE assignedSample = createSampleWithCode("assignedSample");

        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        exp.setSamples(Arrays.asList(untouchedSample, unassignedSample));

        prepareLoadExperimentByIdentifier(identifier, exp);
        prepareTryFindSample(exp.getProject().getSpace(), assignedSample.getCode(), assignedSample);
        prepareNoDatasetsFound();
        final ExperimentBO expBO = loadExperiment(identifier, exp);

        String[] editedSamples = new String[]
            { untouchedSample.getCode(), assignedSample.getCode() };
        expBO.setExperimentSamples(editedSamples);
        assertEquals(exp, untouchedSample.getExperiment());
        assertEquals(exp, assignedSample.getExperiment());
        assertNull(unassignedSample.getExperiment());
    }

    private void prepareNoDatasetsFound()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(externalDataDAO).hasExternalData((with(any(SamplePE.class))));
                    will(returnValue(false));
                }
            });
    }

    @Test
    public final void testEditSamplesAddingAssignedSampleFails()
    {
        SamplePE assignedSample = createSampleWithCode("assignedSample");
        assignedSample.setExperiment(createExperiment("anotherExp"));

        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        assert exp.getSamples().size() == 0 : "no samples expected";

        prepareLoadExperimentByIdentifier(identifier, exp);
        prepareTryFindSample(exp.getProject().getSpace(), assignedSample.getCode(), assignedSample);

        final ExperimentBO expBO = loadExperiment(identifier, exp);

        String[] editedSamples = new String[]
            { assignedSample.getCode() };

        String errorMsg = "Sample 'assignedSample' is already assigned to the experiment";
        try
        {
            expBO.setExperimentSamples(editedSamples);
        } catch (UserFailureException e)
        {

            AssertionUtil.assertContains(errorMsg, e.getMessage());
            return;
        }
        fail("exception expected with the error msg: " + errorMsg);
    }

    @Test
    public final void testEditSamplesAssigningUnexistingSampleFails()
    {
        String unknownSampleCode = "unknownSampleCode";

        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);

        prepareLoadExperimentByIdentifier(identifier, exp);
        final ExperimentBO expBO = loadExperiment(identifier, exp);

        prepareTryFindSample(exp.getProject().getSpace(), unknownSampleCode, null);
        String errorMsg =
                "Samples with following codes do not exist in the space 'HOME_GROUP': '[unknownSampleCode]'.";
        try
        {
            expBO.setExperimentSamples(new String[]
                { unknownSampleCode });
        } catch (UserFailureException e)
        {

            assertEquals(errorMsg, e.getMessage());
            return;
        }
        fail("exception expected with the error msg: " + errorMsg);
    }

    @Test
    public final void testEditSamplesUnassigningSampleWithDatasetsFails()
    {
        final SamplePE assignedSample = createSampleWithCode("assignedSample");

        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        exp.setSamples(Arrays.asList(assignedSample));

        prepareLoadExperimentByIdentifier(identifier, exp);
        final ExperimentBO expBO = loadExperiment(identifier, exp);
        context.checking(new Expectations()
            {
                {
                    allowing(externalDataDAO).hasExternalData(with(assignedSample));
                    will(returnValue(true));
                }
            });

        String errorMsg =
                "Operation cannot be performed, because some datasets have been already produced for the sample 'assignedSample'.";
        try
        {
            expBO.setExperimentSamples(new String[] {}); // remove all samples
        } catch (UserFailureException e)
        {

            assertEquals(errorMsg, e.getMessage());
            return;
        }
        fail("exception expected with the error msg: " + errorMsg);
    }

    @Test
    public void testDelete()
    {
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final ExperimentPE experiment = createExperiment(EXP_CODE);
        experiment.setId(experimentId.getId());
        final String reason = "reason";

        prepareAnyDaoCreation();
        prepareTryToLoadOfExperimentWithId(experiment);
        context.checking(new Expectations()
            {
                {
                    PersonPE person = EXAMPLE_SESSION.tryGetPerson();
                    EventPE event = ExperimentBO.createDeletionEvent(experiment, person, reason);
                    one(eventDAO).persist(event);
                    one(experimentDAO).delete(experiment);
                }
            });

        final ExperimentBO expBO = createExperimentBO();
        expBO.deleteByTechId(experimentId, reason);
        context.assertIsSatisfied();
    }

    private void prepareTryToLoadOfExperimentWithId(final ExperimentPE experiment)
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryGetByTechId(with(new TechId(experiment.getId())),
                            with(any(String[].class)));
                    will(returnValue(experiment));
                }
            });
    }

    private static ExperimentPE createExperiment(String code)
    {
        ExperimentIdentifier ident =
                new ExperimentIdentifier(CommonTestUtils.createProjectIdentifier(), code);
        return CommonTestUtils.createExperiment(ident);
    }

    private ExperimentBO loadExperiment(final ExperimentIdentifier identifier,
            final ExperimentPE exp)
    {
        final ExperimentBO expBO = createExperimentBO();
        expBO.loadByExperimentIdentifier(identifier);
        AssertJUnit.assertEquals(exp, expBO.getExperiment());
        return expBO;
    }

    private void prepareTryFindSample(final SpacePE group, final String sampleCode,
            final SamplePE foundSample)
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryFindByCodeAndSpace(sampleCode, group);
                    will(returnValue(foundSample));
                }
            });
    }

    private static SamplePE createSampleWithCode(String code)
    {
        SamplePE s = CommonTestUtils.createSample();
        s.setCode(code);
        return s;
    }

    private static String createIdentifier(final String dbCode, final String groupCode,
            final String projectCode, final String expCode)
    {
        return dbCode + ":/" + groupCode + "/" + projectCode + "/" + expCode;
    }

    private static ExperimentPE createExperiment(ProjectPE project, final String expCode,
            ExperimentTypePE type)
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(expCode);
        experiment.setExperimentType(type);
        experiment.setProject(project);
        return experiment;
    }

    private static ProjectPE createProject(final String dbCode, final String groupCode,
            final String projectCode)
    {
        ProjectPE project = new ProjectPE();
        project.setCode(projectCode);
        final SpacePE group = new SpacePE();
        group.setCode(groupCode);
        final DatabaseInstancePE db = new DatabaseInstancePE();
        db.setCode(dbCode);
        group.setDatabaseInstance(db);
        project.setSpace(group);
        return project;
    }

    private static ExperimentTypePE createExperimentType(final String expTypeCode)
    {
        ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setDatabaseInstance(new DatabaseInstancePE());
        experimentType.setCode(expTypeCode);
        return experimentType;
    }

    private static void setExperimentAttachments(final ExperimentPE exp,
            final AttachmentPE attachment1, final AttachmentPE attachment2)
    {
        final HashSet<AttachmentPE> set = new HashSet<AttachmentPE>();
        set.add(attachment1);
        set.add(attachment2);
        exp.setInternalAttachments(set);
    }

    private static void testThrowingExceptionOnUnknownFileVersion(final AttachmentPE attachment,
            final ExperimentBO expBO)
    {
        boolean exceptionThrown = false;
        try
        {
            expBO.getExperimentFileAttachment(attachment.getFileName(),
                    attachment.getVersion() + 100);
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
        } finally
        {
            AssertJUnit.assertTrue(exceptionThrown);
        }
    }

    private static void testThrowingExceptionOnUnknownFilename(final AttachmentPE attachment2,
            final ExperimentBO expBO)
    {
        boolean exceptionThrown;
        exceptionThrown = false;
        try
        {
            expBO.getExperimentFileAttachment("nonexistentAttachment.txt", attachment2.getVersion());
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
        } finally
        {
            AssertJUnit.assertTrue(exceptionThrown);
        }
    }

}