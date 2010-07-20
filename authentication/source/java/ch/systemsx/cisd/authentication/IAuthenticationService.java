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

import java.util.List;

import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * Interface for authentication.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAuthenticationService extends ISelfTestable
{

    /**
     * Attempts authentication of the application with credentials passed in the constructor and
     * returns the application token. Implementations should log what is going on, whether the
     * application could register itself successfully or not.
     * <p>
     * The returned application token can then be used to authenticate an user (via
     * {@link #authenticateUser(String, String, String)}) or to retrieve additional details about an
     * user (via {@link #getPrincipal(String, String)})
     * </p>
     * 
     * @return the application token if the application has been successfully authenticated,
     *         <code>null</code> otherwise.
     */
    public String authenticateApplication();

    /**
     * Attempts authentication for the given user credentials.
     * <p>
     * Note that the application must be authenticated (meaning that <var>applicationToken</var> is
     * not <code>null</code>) to perform this lookup.
     * </p>
     * 
     * @return <code>true</code> if the <var>user</var> has been successfully authenticated.
     */
    public boolean authenticateUser(String applicationToken, String user, String password);

    /**
     * For a given user name returns additional details encapsulated in returned
     * <code>Principal</code>.
     * <p>
     * Note that the application must be authenticated (meaning that <var>applicationToken</var> is
     * not <code>null</code>) to perform this lookup.
     * </p>
     * 
     * @return The <code>Principal</code> object for the given <var>user</var>.
     * @throws IllegalArgumentException If either the <var>applicationToken</var> is invalid or the
     *             <var>user</var> cannot be found.
     */
    public Principal getPrincipal(String applicationToken, String user)
            throws IllegalArgumentException;

    /**
     * Returns <code>true</code> if this authentication service supports listing of principals by
     * user id.
     */
    public boolean supportsListingByUserId();

    /**
     * Returns a list of all users that match the <var>userIdQuery</var>.
     * 
     * @param userIdQuery The query for user ids to list. As user ids are unique, it can only
     *            ever return more than one user if it contains one or more wildcard characters (
     *            <code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this
     *             operation.
     */
    public List<Principal> listPrincipalsByUserId(String applicationToken, String userIdQuery);

    /**
     * Returns <code>true</code> if this authentication service supports listing of principals by
     * email address.
     */
    public boolean supportsListingByEmail();

    /**
     * Returns a list of all users that match the <var>emailQuery</var>.
     * 
     * @param emailQuery The query for email addresses to list. May contain one or more wildcard
     *            characters (<code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this
     *             operation.
     */
    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery);

    /**
     * Returns <code>true</code> if this authentication service supports listing of principals by
     * last name.
     */
    public boolean supportsListingByLastName();

    /**
     * Returns a list of all users that match the <var>lastNameQuery</var>.
     * 
     * @param lastNameQuery The query for last names to list. May contain one or more wildcard
     *            characters (<code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this
     *             operation.
     */
    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery);

}