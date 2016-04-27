/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.client.util.v1;

import ch.systemsx.cisd.cina.client.util.v1.impl.CinaUtilitiesFacade;

/**
 * Factory for creating ICinaUtilities implementations.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaUtilitiesFacadeFactory
{
    /**
     * Public factory method for creating an ICinaUtilities with a username and password.
     * 
     * @param user The user name
     * @param password The user's password
     * @param openBISUrl The URL to openBIS
     * @param timeoutInMillis timeout in milliseconds. Specify 0 for no timeout.
     */
    public static ICinaUtilities tryCreate(String user, String password, String openBISUrl,
            long timeoutInMillis)
    {
        return CinaUtilitiesFacade.tryCreate(user, password, openBISUrl, timeoutInMillis);
    }

    /**
     * Public factory method for creating an ICinaUtilities for a user that has already been authenticated.
     * 
     * @param sessionToken The session token provided by authentication
     * @param openBISUrl The URL to openBIS
     * @param timeoutInMillis timeout in milliseconds. Specify 0 for no timeout.
     */
    public static ICinaUtilities tryCreate(String sessionToken, String openBISUrl,
            long timeoutInMillis)
    {
        return CinaUtilitiesFacade.tryCreate(sessionToken, openBISUrl, timeoutInMillis);
    }
}
