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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;

/**
 * @author pkupczyk
 */
public class TestAuthorizationConfig implements IAuthorizationConfig
{

    private boolean projectLevelEnabled;

    public TestAuthorizationConfig(boolean projectLevelEnabled)
    {
        this.projectLevelEnabled = projectLevelEnabled;
    }

    @Override
    public boolean isProjectLevelEnabled()
    {
        return projectLevelEnabled;
    }

    @Override
    public String toString()
    {
        return "projectAuthorization=" + isProjectLevelEnabled();
    }

}
