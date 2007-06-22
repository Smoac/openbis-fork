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

    public String authenticateApplication()
    {
        // We do not care about the returned application token.
        return null;
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
        return new Principal(user, "John", "Doe", "jdoe@somewhere.org");
    }
    
    public final void check()
    {
        // Always available.
    }
}