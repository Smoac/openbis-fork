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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization;

import java.lang.reflect.Method;
import java.util.UUID;

import org.apache.log4j.Level;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.TestAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.systemtest.authorization.CommonAuthorizationSystemTestService;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author pkupczyk
 */
public class CommonAuthorizationSystemTest extends SystemTestCase
{

    protected static final String PERSON_OTHER = "test_user_other";

    protected static final String PERSON_WITH_PA_OFF = "test_user_pa_off";

    protected static final String PERSON_WITH_PA_ON = "test_user_pa_on";

    protected static final String PERSON_WITH_OR_WITHOUT_PA_PROVIDER = "personProvider";

    private BufferedAppender logRecorder;

    @BeforeClass
    protected void initData()
    {
        operationLog.info("==== Test data initialized ====");

        getCommonService().recordCreatedObjects();

        PersonPE personOther = new PersonPE();
        personOther.setUserId(PERSON_OTHER);
        getCommonService().createPerson(personOther);

        PersonPE personPaOff = new PersonPE();
        personPaOff.setUserId(PERSON_WITH_PA_OFF);
        getCommonService().createPerson(personPaOff);

        PersonPE personPaOn = new PersonPE();
        personPaOn.setUserId(PERSON_WITH_PA_ON);
        getCommonService().createPerson(personPaOn);

        ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setCode("AUTH-EXPERIMENT-TYPE");
        getCommonService().createType(experimentType, EntityKind.EXPERIMENT);

        for (int s = 1; s <= 2; s++)
        {
            SpacePE space = new SpacePE();
            space.setCode("AUTH-SPACE-" + s);
            space.setRegistrator(personOther);
            getCommonService().createSpace(space);

            for (int p = 1; p <= 2; p++)
            {
                ProjectPE project = new ProjectPE();
                project.setCode("AUTH-PROJECT-" + p);
                project.setPermId(UUID.randomUUID().toString());
                project.setSpace(space);
                project.setRegistrator(personOther);
                getCommonService().createProject(project);

                for (int e = 1; e <= 2; e++)
                {
                    ExperimentPE experiment = new ExperimentPE();
                    experiment.setExperimentType(experimentType);
                    experiment.setCode("AUTH-EXPERIMENT-" + e);
                    experiment.setPermId(UUID.randomUUID().toString());
                    experiment.setProject(project);
                    experiment.setRegistrator(personOther);
                    getCommonService().createExperiment(experiment);
                }
            }
        }
    }

    @AfterClass
    protected void removeData()
    {
        getCommonService().removeCreatedObjects();

        operationLog.info("==== Test data removed ====");
    }

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
        userWithPAOff.setUserId(PERSON_WITH_PA_OFF);

        PersonPE userWithPAOn = new PersonPE();
        userWithPAOn.setUserId(PERSON_WITH_PA_ON);

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

    public CommonAuthorizationSystemTestService getCommonService()
    {
        return applicationContext.getBean(CommonAuthorizationSystemTestService.class);
    }

    public IAuthorizationConfig getAuthorizationConfig()
    {
        return applicationContext.getBean(IAuthorizationConfig.class);
    }

    public SpacePE getSpace1()
    {
        return getCommonService().tryFindSpace("AUTH-SPACE-1");
    }

    public SpacePE getSpace2()
    {
        return getCommonService().tryFindSpace("AUTH-SPACE-2");
    }

    public ProjectPE getProject11()
    {
        return getCommonService().tryFindProject(getSpace1().getCode(), "AUTH-PROJECT-1");
    }

    public ProjectPE getProject12()
    {
        return getCommonService().tryFindProject(getSpace1().getCode(), "AUTH-PROJECT-2");
    }

    public ProjectPE getProject21()
    {
        return getCommonService().tryFindProject(getSpace2().getCode(), "AUTH-PROJECT-1");
    }

    public ProjectPE getProject22()
    {
        return getCommonService().tryFindProject(getSpace2().getCode(), "AUTH-PROJECT-2");
    }

    public ExperimentPE getExperiment111()
    {
        return getCommonService().tryFindExperiment(getProject11(), "AUTH-EXPERIMENT-1");
    }

    public ExperimentPE getExperiment112()
    {
        return getCommonService().tryFindExperiment(getProject11(), "AUTH-EXPERIMENT-2");
    }

    public ExperimentPE getExperiment121()
    {
        return getCommonService().tryFindExperiment(getProject12(), "AUTH-EXPERIMENT-1");
    }

    public ExperimentPE getExperiment122()
    {
        return getCommonService().tryFindExperiment(getProject12(), "AUTH-EXPERIMENT-2");
    }

    public ExperimentPE getExperiment211()
    {
        return getCommonService().tryFindExperiment(getProject21(), "AUTH-EXPERIMENT-1");
    }

    public ExperimentPE getExperiment212()
    {
        return getCommonService().tryFindExperiment(getProject21(), "AUTH-EXPERIMENT-2");
    }

    public ExperimentPE getExperiment221()
    {
        return getCommonService().tryFindExperiment(getProject22(), "AUTH-EXPERIMENT-1");
    }

    public ExperimentPE getExperiment222()
    {
        return getCommonService().tryFindExperiment(getProject22(), "AUTH-EXPERIMENT-2");
    }

    public ExperimentPE getExperiment(SpacePE spacePE, ProjectPE projectPE)
    {
        return getCommonService().tryFindExperiment(projectPE, "AUTH-EXPERIMENT-1");
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
