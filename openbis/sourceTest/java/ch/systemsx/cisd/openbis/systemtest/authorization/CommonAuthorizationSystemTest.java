/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import java.lang.reflect.Method;

import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.TestAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author pkupczyk
 */
public class CommonAuthorizationSystemTest extends SystemTestCase
{

    protected static final String PERSON_WITH_OR_WITHOUT_PA_PROVIDER = "personProvider";

    @Autowired
    private IDAOFactory daoFactory;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void beforeMethod(Method method)
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        System.out.println(">>>>>>>>> BEFORE METHOD: " + method.getName());
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        logRecorder.reset();
        System.out.println("<<<<<<<<< AFTER METHOD: " + method.getName());
    }

    @DataProvider(name = PERSON_WITH_OR_WITHOUT_PA_PROVIDER)
    public Object[][] providePerson()
    {
        PersonPE userWithPAOff = new PersonPE();
        userWithPAOff.setUserId("test_user_pa_off");

        PersonPE userWithPAOn = new PersonPE();
        userWithPAOn.setUserId("test_user_pa_on");

        return new Object[][] {
                { userWithPAOff },
                { userWithPAOn }
        };
    }

    protected IAuthSessionProvider createSessionProvider(PersonPE person)
    {
        SimpleSession session = new SimpleSession();
        session.setPerson(person);
        return new TestAuthSessionProvider(session);
    }

    protected RoleAssignmentPE createInstanceRole(RoleCode roleCode)
    {
        RoleAssignmentPE ra = new RoleAssignmentPE();
        ra.setRole(roleCode);
        return ra;
    }

    protected RoleAssignmentPE createSpaceRole(RoleCode roleCode, SpacePE space)
    {
        RoleAssignmentPE ra = new RoleAssignmentPE();
        ra.setRole(roleCode);
        ra.setSpace(space);
        return ra;
    }

    protected RoleAssignmentPE createProjectRole(RoleCode roleCode, ProjectPE project)
    {
        RoleAssignmentPE ra = new RoleAssignmentPE();
        ra.setRole(roleCode);
        ra.setProject(project);
        return ra;
    }

    public SpacePE getSpace1()
    {
        return daoFactory.getSpaceDAO().tryFindSpaceByCode("AUTH-SPACE-1");
    }

    public SpacePE getSpace2()
    {
        return daoFactory.getSpaceDAO().tryFindSpaceByCode("AUTH-SPACE-2");
    }

    public ProjectPE getProject11()
    {
        return daoFactory.getProjectDAO().tryFindProject(getSpace1().getCode(), "AUTH-PROJECT-11");
    }

    public ProjectPE getProject12()
    {
        return daoFactory.getProjectDAO().tryFindProject(getSpace1().getCode(), "AUTH-PROJECT-12");
    }

    public ProjectPE getProject21()
    {
        return daoFactory.getProjectDAO().tryFindProject(getSpace2().getCode(), "AUTH-PROJECT-21");
    }

    public ProjectPE getProject22()
    {
        return daoFactory.getProjectDAO().tryFindProject(getSpace2().getCode(), "AUTH-PROJECT-22");
    }

    public ExperimentPE getExperiment111()
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(getProject11(), "AUTH-EXPERIMENT-111");
    }

    public ExperimentPE getExperiment112()
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(getProject11(), "AUTH-EXPERIMENT-112");
    }

    public ExperimentPE getExperiment121()
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(getProject11(), "AUTH-EXPERIMENT-121");
    }

    public ExperimentPE getExperiment122()
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(getProject11(), "AUTH-EXPERIMENT-122");
    }

    public ExperimentPE getExperiment211()
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(getProject11(), "AUTH-EXPERIMENT-211");
    }

    public ExperimentPE getExperiment212()
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(getProject11(), "AUTH-EXPERIMENT-212");
    }

    public ExperimentPE getExperiment221()
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(getProject11(), "AUTH-EXPERIMENT-221");
    }

    public ExperimentPE getExperiment222()
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(getProject11(), "AUTH-EXPERIMENT-222");
    }

    protected static void assertNull(Object object)
    {
        Assert.assertNull(object);
    }

    protected static void assertNotNull(Object object)
    {
        Assert.assertNotNull(object);
    }

    protected static void assertAuthorizationFailureExceptionThatNotEnoughPrivileges(Throwable t)
    {
        assertException(t, AuthorizationFailureException.class, ".*does not have enough privileges.*");
    }

    protected static void assertAuthorizationFailureExceptionThatNoRoles(Throwable t)
    {
        assertException(t, AuthorizationFailureException.class, ".*No role assignments could be found for user.*");
    }

    protected static void assertUserFailureExceptionThatProjectDoesNotExist(Throwable t)
    {
        assertException(t, UserFailureException.class, "Project with ID .* does not exist. Maybe someone has just deleted it.");
    }

    protected static void assertUserFailureExceptionThatExperimentDoesNotExist(Throwable t)
    {
        assertException(t, UserFailureException.class, "Experiment with ID .* does not exist. Maybe someone has just deleted it.");
    }

    protected static void assertNoException(Throwable actualException)
    {
        if (actualException != null)
        {
            actualException.printStackTrace(System.err);
            Assert.fail("Unexpected exception '" + actualException.getClass().getName() + "'.");
        }
    }

    protected static void assertException(Throwable actualException, Class<?> expectedClass, String expectedMessageRegexp)
    {
        if (actualException == null)
        {
            Assert.fail("Expected exception '" + expectedClass.getName() + "' but got null");
        } else
        {
            if (actualException.getClass().equals(expectedClass))
            {
                actualException.printStackTrace(System.out);

                if (expectedMessageRegexp != null)
                {
                    if (actualException.getMessage() == null || false == actualException.getMessage().matches(expectedMessageRegexp))
                    {
                        Assert.fail(
                                "Error message was expected to match '" + expectedMessageRegexp + "' regexp but got '" + actualException.getMessage()
                                        + "'.");
                    }
                }
            } else
            {
                actualException.printStackTrace(System.err);
                Assert.fail("Expected exception '" + expectedClass.getName() + "' but got '" + actualException.getClass().getName() + "'.");
            }
        }
    }

}
