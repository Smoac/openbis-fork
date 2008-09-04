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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GenericClientService implements IGenericClientService
{
    void setConfigParameters(GenericConfigParameters configParameters)
    {
    }
    
    public ApplicationInfo getApplicationInfo()
    {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setVersion(BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        return applicationInfo;
    }

    public SessionContext tryToGetCurrentSessionContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public SessionContext tryToLogin(String userID, String password)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
