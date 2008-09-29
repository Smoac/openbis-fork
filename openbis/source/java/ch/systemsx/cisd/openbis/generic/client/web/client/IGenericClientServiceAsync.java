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

package ch.systemsx.cisd.openbis.generic.client.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * Asynchronous version of {@link IGenericClientService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericClientServiceAsync
{
    /** @see IGenericClientService#getApplicationInfo() */
    public void getApplicationInfo(AsyncCallback<ApplicationInfo> callback);

    /** @see IGenericClientService#tryToGetCurrentSessionContext() */
    public void tryToGetCurrentSessionContext(AsyncCallback<SessionContext> callback);

    /** @see IGenericClientService#tryToLogin(String, String) */
    public void tryToLogin(String userID, String password, AsyncCallback<SessionContext> callback);

    /** @see IGenericClientService#logout() */
    public void logout(AsyncCallback<Void> callback);

    /** @see IGenericClientService#listGroups(String) */
    public void listGroups(String databaseInstanceCode, AsyncCallback<List<Group>> callback);

    /** @see IGenericClientService#registerGroup(String, String, String) */
    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull,
            AsyncCallback<Void> callback);

    /** @see IGenericClientService#listPersons() */
    public void listPersons(AsyncCallback<List<Person>> asyncCallback);

    /** @see IGenericClientService#registerPerson(String) */
    public void registerPerson(String code, AsyncCallback<Void> asyncCallback);

    /** @see IGenericClientService#listRoles() */
    public void listRoles(AsyncCallback<List<RoleAssignment>> asyncCallback);

    /** @see IGenericClientService#registerGroupRole(String, String, String) */
    public void registerGroupRole(String roleSetCode, String group, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see IGenericClientService#deleteGroupRole(String, String, String) */
    public void deleteGroupRole(String roleSetCode, String group, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see IGenericClientService#registerInstanceRole(String, String) */
    public void registerInstanceRole(String roleSetCode, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see IGenericClientService#deleteInstanceRole(String, String) */
    public void deleteInstanceRole(String roleSetCode, String person,
            AsyncCallback<Void> asyncCallback);

}
