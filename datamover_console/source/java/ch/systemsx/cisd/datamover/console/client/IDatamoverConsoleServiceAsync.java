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

package ch.systemsx.cisd.datamover.console.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.datamover.console.client.dto.DatamoverInfo;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * Asynchronious version of {@link IDatamoverConsoleService}.
 *
 * @author Franz-Josef Elmer
 */
public interface IDatamoverConsoleServiceAsync extends RemoteService
{
    public void tryToGetCurrentUser(AsyncCallback<User> callback);

    public void tryToLogin(final String user, final String password, AsyncCallback<User> callback);

    public void logout(AsyncCallback<Void> callback);
    
    public void listDatamoverInfos(AsyncCallback<List<DatamoverInfo>> callback);
    
    public void getTargets(AsyncCallback<Map<String, String>> callback);
    
    public void startDatamover(String datamoverName, String targetLocation, AsyncCallback<Void> callback);

    public void shutdownDatamover(String datamoverName, AsyncCallback<Void> callback);
}
