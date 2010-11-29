/*
 * Copyright 2007 ETH Zuerich, CISD
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

import org.jmock.Expectations;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Test cases for corresponding {@link ProjectBO} class.
 * 
 * @author Christian Ribeaud
 */
public final class ProjectBOTest extends AbstractBOTest
{

    private final ProjectBO createProjectBO()
    {
        return new ProjectBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @Test
    public final void testSaveWithNullProject()
    {
        boolean exceptionThrown = false;
        try
        {
            createProjectBO().save();
        } catch (final AssertionError ex)
        {
            exceptionThrown = true;
        }
        AssertJUnit.assertTrue("Can not save a null project", exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineWithNullCode()
    {
        final ProjectBO projectBO = createProjectBO();
        boolean fail = true;
        try
        {
            projectBO.define(null, null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testSave()
    {
        final ProjectBO projectBO = createProjectBO();
        prepareDefineProject();
        projectBO.define(createProjectIdent(), null, null);
        context.checking(new Expectations()
            {
                {

                    one(projectDAO).createProject(projectBO.getProject());
                }
            });
        projectBO.save();
        context.assertIsSatisfied();
    }

    private void prepareDefineProject()
    {
        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this, ManagerTestTool.EXAMPLE_SESSION
                            .tryGetHomeGroupCode(), daoFactory, groupDAO);

                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));
                }
            });
    }

    @Test
    public final void testSaveWithAlreadyExistingProject()
    {
        final ProjectBO projectBO = createProjectBO();
        final ProjectIdentifier projIdent = createProjectIdent();

        prepareDefineProject();
        projectBO.define(projIdent, null, null);
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).createProject(projectBO.getProject());
                    will(throwException(ManagerTestTool.createUniqueViolationException()));
                }
            });

        try
        {
            // System.err.print(projectBO.getProject());
            projectBO.save();
            fail("An UserFailureException must be thrown here.");
        } catch (final UserFailureException ex)
        {
            assertEquals(String.format(DataAccessExceptionTranslator.UNIQUE_VIOLATION_FORMAT,
                    String.format("Project '%s'", projIdent.getProjectCode())), ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    private ProjectIdentifier createProjectIdent()
    {
        final ProjectPE projectDTO = ManagerTestTool.EXAMPLE_PROJECT;
        return new ProjectIdentifier(projectDTO.getSpace().getCode(), projectDTO.getCode());
    }
}
