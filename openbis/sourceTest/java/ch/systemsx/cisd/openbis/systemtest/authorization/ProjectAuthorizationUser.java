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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.DataProvider;

/**
 * @author pkupczyk
 */
public class ProjectAuthorizationUser
{

    public static final String PROVIDER = "project-authorization-users-provider";

    public static final String PROVIDER_WITH_ETL = "project-authorization-users-provider-with-etl";

    private String userId;

    private boolean isInstanceUser;

    private boolean isTestGroupUser;

    private boolean isTestSpaceUser;

    private boolean isTestProjectUser;

    private boolean isETLServerUser;

    private boolean paEnabled;

    public ProjectAuthorizationUser(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public boolean hasPAEnabled()
    {
        return paEnabled;
    }

    private void setPAEnabled(boolean paEnabled)
    {
        this.paEnabled = paEnabled;
    }

    public boolean isInstanceUser()
    {
        return isInstanceUser;
    }

    public void setInstanceUser(boolean isInstanceUser)
    {
        this.isInstanceUser = isInstanceUser;
    }

    public boolean isTestGroupUser()
    {
        return isTestGroupUser;
    }

    private void setTestGroupUser(boolean isTestGroupUser)
    {
        this.isTestGroupUser = isTestGroupUser;
    }

    public boolean isTestSpaceUser()
    {
        return isTestSpaceUser;
    }

    private void setTestSpaceUser(boolean isTestSpaceUser)
    {
        this.isTestSpaceUser = isTestSpaceUser;
    }

    public boolean isETLServerUser()
    {
        return isETLServerUser;
    }

    public void setETLServerUser(boolean isETLServerUser)
    {
        this.isETLServerUser = isETLServerUser;
    }

    public boolean isTestProjectUser()
    {
        return isTestProjectUser;
    }

    private void setTestProjectUser(boolean isTestProjectUser)
    {
        this.isTestProjectUser = isTestProjectUser;
    }
    
    public boolean isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser()
    {
        return isInstanceUser() || isTestSpaceUser() || (isTestProjectUser() && hasPAEnabled());
    }

    @DataProvider(name = PROVIDER)
    public static Object[][] providerUsers()
    {
        ProjectAuthorizationUser instanceAdmin = new ProjectAuthorizationUser("instance_admin");
        instanceAdmin.setInstanceUser(true);

        ProjectAuthorizationUser testSpacePAOff = new ProjectAuthorizationUser("test_space_pa_off");
        testSpacePAOff.setTestSpaceUser(true);

        ProjectAuthorizationUser testSpacePAOn = new ProjectAuthorizationUser("test_space_pa_on");
        testSpacePAOn.setTestSpaceUser(true);
        testSpacePAOn.setPAEnabled(true);

        ProjectAuthorizationUser testProjectPAOff = new ProjectAuthorizationUser("test_project_pa_off");
        testProjectPAOff.setTestProjectUser(true);

        ProjectAuthorizationUser testProjectPAOn = new ProjectAuthorizationUser("test_project_pa_on");
        testProjectPAOn.setTestProjectUser(true);
        testProjectPAOn.setPAEnabled(true);

        ProjectAuthorizationUser testGroup = new ProjectAuthorizationUser("admin");
        testGroup.setTestGroupUser(true);

        return new Object[][] {
                { instanceAdmin },
                { testSpacePAOff },
                { testSpacePAOn },
                { testProjectPAOff },
                { testProjectPAOn },
                { testGroup }
        };
    }

    @DataProvider(name = PROVIDER_WITH_ETL)
    public static Object[][] provideUsersWithETL()
    {
        ProjectAuthorizationUser instanceETLServer = new ProjectAuthorizationUser("etlserver");
        instanceETLServer.setInstanceUser(true);
        instanceETLServer.setETLServerUser(true);

        ProjectAuthorizationUser testSpaceETLServer = new ProjectAuthorizationUser("test_space_etl_server");
        testSpaceETLServer.setTestSpaceUser(true);
        testSpaceETLServer.setETLServerUser(true);

        ProjectAuthorizationUser testGroupETLServer = new ProjectAuthorizationUser("test_group_etl_server");
        testGroupETLServer.setTestGroupUser(true);
        testGroupETLServer.setETLServerUser(true);

        List<Object[]> users = new ArrayList<Object[]>(Arrays.asList(providerUsers()));
        users.add(new Object[] { instanceETLServer });
        users.add(new Object[] { testSpaceETLServer });
        users.add(new Object[] { testGroupETLServer });

        return users.<Object[]> toArray(new Object[][] {});
    }

    @Override
    public String toString()
    {
        return "userId: " + getUserId() + ", isInstanceUser: " + isInstanceUser() + ", isTestSpaceUser: " + isTestSpaceUser()
                + ", isTestProjectUser: " + isTestProjectUser()
                + ", isETLServer: " + isETLServerUser() + ", hasPAEnabled: " + hasPAEnabled();
    }

}
