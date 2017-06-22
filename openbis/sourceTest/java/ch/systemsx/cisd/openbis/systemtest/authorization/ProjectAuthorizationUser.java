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

import org.testng.annotations.DataProvider;

/**
 * @author pkupczyk
 */
public class ProjectAuthorizationUser
{

    public static final String PROVIDER = "project-authorization-users-provider";

    private String userId;

    private boolean isTestGroupUser;

    private boolean isTestSpaceUser;

    private boolean isTestProjectUser;

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

    public boolean isTestProjectUser()
    {
        return isTestProjectUser;
    }

    private void setTestProjectUser(boolean isTestProjectUser)
    {
        this.isTestProjectUser = isTestProjectUser;
    }

    @DataProvider(name = PROVIDER)
    public static Object[][] providerUsers()
    {
        ProjectAuthorizationUser admin = new ProjectAuthorizationUser("admin");
        admin.setTestGroupUser(true);

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

        return new Object[][] {
                { admin },
                { testSpacePAOff },
                { testSpacePAOn },
                { testProjectPAOff },
                { testProjectPAOn }
        };
    }

    @Override
    public String toString()
    {
        return "userId: " + getUserId() + ", isTestSpaceUser: " + isTestSpaceUser() + ", isTestProjectUser: " + isTestProjectUser()
                + ", hasPAEnabled: " + hasPAEnabled();
    }

}
