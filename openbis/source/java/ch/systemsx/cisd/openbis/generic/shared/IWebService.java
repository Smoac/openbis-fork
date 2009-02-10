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

package ch.systemsx.cisd.openbis.generic.shared;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * An abstract set of methods that should be implemented by each <b>LIMS</b> <i>Web Service</i>.
 * <p>
 * It includes versioning method and session management ones.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IWebService
{

    /**
     * Everytime the public <i>Web Service</i> interface changes, we should increment this.
     * <p>
     * Kind of versioning of the <i>Web Service</i>.
     * </p>
     */
    public static final int VERSION = 28; // for release S40

    /**
     * Returns the version of the web service. Does not require any authentication.
     */
    public int getVersion();

    /**
     * Authenticates given <code>user</code> with given <code>password</code>.
     * 
     * @return A session token for the user if the user has been successfully authenticated or
     *         <code>null</code> otherwise.
     */
    @Transactional
    public String authenticate(String user, String password) throws UserFailureException;

    /**
     * Closes session by removing given <code>sessionToken</code> from active sessions.
     */
    public void closeSession(String sessionToken) throws UserFailureException;

}