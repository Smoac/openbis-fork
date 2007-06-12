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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Interface for authentication.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAuthenticationService
{
    /**
     * Checks whether the service is available or not.
     * 
     * @throws EnvironmentFailureException if the service is not available.
     */
    public void checkAvailability();

    /**
     * Attempts authentication for the given user credentials.
     * 
     * @return a <code>Principal</code> object if the <var>user</var> has been successfully authenticated,
     *         <code>null</code> otherwise.
     */
    public Principal authenticate(String user, String password);
}