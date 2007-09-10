/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication;

import org.apache.commons.lang.StringUtils;

/**
 * Dummy authentication services.
 * 
 * @author Franz-Josef Elmer
 */
public final class DummyAuthenticationService implements IAuthenticationService
{

    //
    // IAuthenticationService
    //

    public final String authenticateApplication()
    {
        // Up to the contract, if it returns <code>null</code> here, it assumes that the application
        // did not authenticate successfully.
        return StringUtils.EMPTY;
    }

    /**
     * Always returns <code>true</code>, meaning that the login was successfull.
     */
    public final boolean authenticateUser(String applicationToken, String user, String password)
    {
        return true;
    }

    public final Principal getPrincipal(String applicationToken, String user)
    {
        return new Principal(user, "John", "Doe", "franz-josef.elmer@systemsx.ch");
    }

    public final void check()
    {
        // Always available.
    }
}