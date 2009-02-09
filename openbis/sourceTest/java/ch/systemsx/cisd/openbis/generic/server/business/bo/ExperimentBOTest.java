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

import java.util.HashSet;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
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
        final ProjectPE project = exp.getProject();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    one(projectDAO).tryFindProject(identifier.getDatabaseInstanceCode(),
                            identifier.getGroupCode(), identifier.getProjectCode());
                    will(returnValue(project));

                    one(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    one(experimentDAO).tryFindByCodeAndProject(project,
                            identifier.getExperimentCode());
                    will(returnValue(exp));

                }
            });
        final ExperimentBO expBO = createExperimentBO();
        expBO.loadByExperimentIdentifier(identifier);
        AssertJUnit.assertEquals(exp, expBO.getExperiment());
        context.assertIsSatisfied();
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
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    one(projectDAO).tryFindProject(identifier.getDatabaseInstanceCode(),
                            identifier.getGroupCode(), identifier.getProjectCode());
                    will(returnValue(project));

                    one(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    one(experimentDAO).tryFindByCodeAndProject(project,
                            identifier.getExperimentCode());
                    will(returnValue(exp));

                }
            });
        final ExperimentBO expBO = createExperimentBO();
        expBO.loadByExperimentIdentifier(identifier);

        // Get first attachment
        AssertJUnit.assertEquals(attachment1, expBO.getExperimentFileAttachment(attachment1
                .getFileName(), attachment1.getVersion()));

        // Get another version of attachment
        AssertJUnit.assertEquals(attachment2, expBO.getExperimentFileAttachment(attachment2
                .getFileName(), attachment2.getVersion()));

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

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));
                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));
                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));
                    one(projectDAO).tryFindProject(dbCode, groupCode, projectCode);
                    will(returnValue(project));
                    one(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));
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

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));
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

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));
                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));
                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));
                    one(projectDAO).tryFindProject(dbCode, groupCode, projectCode);
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
                    String.format(ExperimentBO.ERR_PROJECT_NOT_FOUND, createIdentifier(dbCode,
                            groupCode, projectCode, expCode))) > -1);
        }
        assertTrue(exceptionThrown);

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineAndSaveAlreadyExistingExperiment()
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

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));
                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));
                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));
                    one(projectDAO).tryFindProject(dbCode, groupCode, projectCode);
                    will(returnValue(project));
                    one(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));
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

    private String createIdentifier(final String dbCode, final String groupCode,
            final String projectCode, final String expCode)
    {
        return dbCode + ":/" + groupCode + "/" + projectCode + "/" + expCode;
    }

    private ExperimentPE createExperiment(ProjectPE project, final String expCode,
            ExperimentTypePE type)
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(expCode);
        experiment.setExperimentType(type);
        experiment.setProject(project);
        return experiment;
    }

    private ProjectPE createProject(final String dbCode, final String groupCode,
            final String projectCode)
    {
        ProjectPE project = new ProjectPE();
        project.setCode(projectCode);
        final GroupPE group = new GroupPE();
        group.setCode(groupCode);
        final DatabaseInstancePE db = new DatabaseInstancePE();
        db.setCode(dbCode);
        group.setDatabaseInstance(db);
        project.setGroup(group);
        return project;
    }

    private ExperimentTypePE createExperimentType(final String expTypeCode)
    {
        ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setCode(expTypeCode);
        return experimentType;
    }

    private void setExperimentAttachments(final ExperimentPE exp, final AttachmentPE attachment1,
            final AttachmentPE attachment2)
    {
        final HashSet<AttachmentPE> set = new HashSet<AttachmentPE>();
        set.add(attachment1);
        set.add(attachment2);
        exp.setInternalAttachments(set);
    }

    private void testThrowingExceptionOnUnknownFileVersion(final AttachmentPE attachment,
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

    private void testThrowingExceptionOnUnknownFilename(final AttachmentPE attachment2,
            final ExperimentBO expBO)
    {
        boolean exceptionThrown;
        exceptionThrown = false;
        try
        {
            expBO
                    .getExperimentFileAttachment("nonexistentAttachment.txt", attachment2
                            .getVersion());
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
        } finally
        {
            AssertJUnit.assertTrue(exceptionThrown);
        }
    }

}