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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A command which will be executed after a successful invocation of
 * {@link AsyncCallback#onSuccess(Object)}.
 * 
 * @author Franz-Josef Elmer
 */
public interface ITestCommand
{
    /**
     * Returns <code>true</code> if the specified callback objects, failureMessage, and throwable
     * should trigger this command in case if an invocation of
     * {@link AsyncCallback#onFailure(Throwable)}.
     * 
     * @param callbackObjects List of callback objects since the last successful match of a test
     *            command. Contains at least one element.
     */
    public boolean validOnFailure(List<AsyncCallback<Object>> callbackObjects,
            String failureMessage, Throwable throwable);
    
    /**
     * Returns <code>true</code> if the specified callback objects and result should trigger this
     * command in case if an invocation of {@link AsyncCallback#onSuccess(Object)}.
     * 
     * @param callbackObjects List of callback objects since the last successful match of a test
     *            command. Contains at least one element.
     */
    public boolean validOnSucess(List<AsyncCallback<Object>> callbackObjects, Object result);

    /**
     * Executes this command.
     */
    public void execute();
}
