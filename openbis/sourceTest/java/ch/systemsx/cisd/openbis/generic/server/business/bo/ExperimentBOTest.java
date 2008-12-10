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
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * Test cases for corresponding {@link ExperimentBO} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = ExperimentPE.class)
public final class ExperimentBOTest
{
    private Mockery context;

    private IDAOFactory daoFactory;

    private IExperimentDAO experimentDAO;

    private IProjectDAO projectDAO;

    private final ExperimentBO createExperimentBO()
    {
        return new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        experimentDAO = context.mock(IExperimentDAO.class);
        projectDAO = context.mock(IProjectDAO.class);
    }

    @AfterMethod
    public final void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
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