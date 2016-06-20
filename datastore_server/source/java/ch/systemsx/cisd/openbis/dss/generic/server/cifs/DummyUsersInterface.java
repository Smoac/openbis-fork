/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.cifs;

import org.alfresco.jlan.server.auth.UserAccount;
import org.alfresco.jlan.server.auth.UsersInterface;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.springframework.extensions.config.ConfigElement;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DummyUsersInterface implements UsersInterface
{

    @Override
    public void initializeUsers(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
    }

    @Override
    public UserAccount getUserAccount(String userName)
    {
        return new UserAccount(userName, null);
    }

}
