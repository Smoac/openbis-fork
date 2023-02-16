/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.authorization;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * @author pkupczyk
 */
public class AuthorizationConfigFacade
{

    private IAuthorizationConfig authorizationConfig;

    public AuthorizationConfigFacade(IAuthorizationConfig authorizationConfig)
    {
        this.authorizationConfig = authorizationConfig;
    }

    public boolean isProjectLevelEnabled(String userId)
    {
        return authorizationConfig.isProjectLevelEnabled() && authorizationConfig.isProjectLevelUser(userId);
    }

    public boolean isRoleEnabled(RoleWithHierarchy role)
    {
        if (role.isProjectLevel())
        {
            return authorizationConfig.isProjectLevelEnabled();
        } else
        {
            return true;
        }
    }

}
