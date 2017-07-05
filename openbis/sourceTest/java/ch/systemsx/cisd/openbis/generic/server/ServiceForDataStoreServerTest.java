/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class ServiceForDataStoreServerTest extends SystemTestCase
{

    @Test()
    public void testListPhysicalDataSetsWithUnknownSize()
    {
        String sessionToken = authenticateAs("test");
        List<SimpleDataSetInformationDTO> dataSetsWithUnknownSize = etlService.listPhysicalDataSetsWithUnknownSize(sessionToken, "STANDARD", 3, null);

        Assert.assertEquals(3, dataSetsWithUnknownSize.size());
        Assert.assertEquals("20081105092159188-3", dataSetsWithUnknownSize.get(0).getDataSetCode());
        Assert.assertEquals("20081105092159222-2", dataSetsWithUnknownSize.get(1).getDataSetCode());
        Assert.assertEquals("20081105092159333-3", dataSetsWithUnknownSize.get(2).getDataSetCode());
    }

    @Test()
    public void testListPhysicalDataSetsWithUnknownSizeAndDataSetCodeLimit()
    {
        String sessionToken = authenticateAs("test");
        List<SimpleDataSetInformationDTO> dataSetsWithUnknownSize =
                etlService.listPhysicalDataSetsWithUnknownSize(sessionToken, "STANDARD", 3, "20081105092159188-3");

        Assert.assertEquals(3, dataSetsWithUnknownSize.size());
        Assert.assertEquals("20081105092159222-2", dataSetsWithUnknownSize.get(0).getDataSetCode());
        Assert.assertEquals("20081105092159333-3", dataSetsWithUnknownSize.get(1).getDataSetCode());
        Assert.assertEquals("20081105092259000-18", dataSetsWithUnknownSize.get(2).getDataSetCode());
    }

    @Test(dependsOnMethods = "testListPhysicalDataSetsWithUnknownSize")
    public void testUpdatePhysicalDataSetsWithUnknownSize()
    {
        String sessionToken = authenticateAs("test");

        Map<String, Long> sizeMap = new HashMap<String, Long>();
        sizeMap.put("20081105092159188-3", 123L);

        etlService.updatePhysicalDataSetsSize(sessionToken, sizeMap);

        List<SimpleDataSetInformationDTO> dataSetsWithUnknownSize =
                etlService.listPhysicalDataSetsWithUnknownSize(sessionToken, "STANDARD", 100, null);
        List<AbstractExternalData> updatedDataSets = etlService.listDataSetsByCode(sessionToken, Arrays.asList("20081105092159188-3"));

        Assert.assertEquals(23, dataSetsWithUnknownSize.size());
        Assert.assertEquals("20081105092159222-2", dataSetsWithUnknownSize.get(0).getDataSetCode());
        Assert.assertEquals("VALIDATIONS_PARENT-28", dataSetsWithUnknownSize.get(dataSetsWithUnknownSize.size() - 1).getDataSetCode());

        Assert.assertEquals(1, updatedDataSets.size());
        Assert.assertEquals("20081105092159188-3", updatedDataSets.get(0).getCode());
        Assert.assertEquals(Long.valueOf(123L), updatedDataSets.get(0).getSize());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListExperimentsForProjectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        ProjectIdentifier projectIdentifier = new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT");
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();

        if (user.isTestSpaceUser() || (user.isTestProjectUser() && user.hasPAEnabled()))
        {
            List<Experiment> experiments =
                    etlService.listExperimentsForProjects(session.getSessionToken(), Arrays.asList(projectIdentifier), fetchOptions);
            assertEquals(experiments.size(), 1);
            assertEquals(experiments.get(0).getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                etlService.listExperimentsForProjects(session.getSessionToken(), Arrays.asList(projectIdentifier), fetchOptions);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListProjectsWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        List<Project> projects = etlService.listProjects(session.getSessionToken());

        if (user.isTestSpaceUser())
        {
            assertEntities("[/TEST-SPACE/NOE, /TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
        } else if (user.isTestGroupUser())
        {
            assertEntities("[/TESTGROUP/TESTPROJ]", projects);
        } else if (user.isTestProjectUser() && user.hasPAEnabled())
        {
            assertEntities("[/TEST-SPACE/PROJECT-TO-DELETE, /TEST-SPACE/TEST-PROJECT]", projects);
        } else
        {
            assertEntities("[]", projects);
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testTryGetProjectWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        ProjectIdentifier projectIdentifier = new ProjectIdentifier("TEST-SPACE", "TEST-PROJECT");

        if (user.isTestSpaceUser() || (user.isTestProjectUser() && user.hasPAEnabled()))
        {
            Project project = etlService.tryGetProject(session.getSessionToken(), projectIdentifier);
            assertEquals(project.getIdentifier(), "/TEST-SPACE/TEST-PROJECT");
        } else
        {
            try
            {
                etlService.tryGetProject(session.getSessionToken(), projectIdentifier);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testTryGetProjectByPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        PermId projectPermId = new PermId("20120814110011738-105"); // /TEST-SPACE/TEST-PROJECT

        if (user.isTestSpaceUser() || (user.isTestProjectUser() && user.hasPAEnabled()))
        {
            Project project = etlService.tryGetProjectByPermId(session.getSessionToken(), projectPermId);
            assertEquals(project.getIdentifier(), "/TEST-SPACE/TEST-PROJECT");
        } else
        {
            try
            {
                etlService.tryGetProjectByPermId(session.getSessionToken(), projectPermId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testTryGetExperimentByPermIdWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        PermId experimentPermId = new PermId("201206190940555-1032"); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isTestSpaceUser() || (user.isTestProjectUser() && user.hasPAEnabled()))
        {
            Experiment experiment = etlService.tryGetExperimentByPermId(session.getSessionToken(), experimentPermId);
            assertEquals(experiment.getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        } else
        {
            try
            {
                etlService.tryGetExperimentByPermId(session.getSessionToken(), experimentPermId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testListDataSetsByExperimentIDWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SessionContextDTO session = etlService.tryAuthenticate(user.getUserId(), PASSWORD);

        TechId experimentId = new TechId(23L); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST

        if (user.isTestSpaceUser() || (user.isTestProjectUser() && user.hasPAEnabled()))
        {
            List<AbstractExternalData> dataSets = etlService.listDataSetsByExperimentID(session.getSessionToken(), experimentId);
            assertEquals(dataSets.size(), 9);
        } else
        {
            try
            {
                etlService.listDataSetsByExperimentID(session.getSessionToken(), experimentId);
                fail();
            } catch (AuthorizationFailureException e)
            {
                // expected
            }
        }
    }

}
