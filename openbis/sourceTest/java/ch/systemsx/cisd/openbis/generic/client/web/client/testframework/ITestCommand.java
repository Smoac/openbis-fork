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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;

/**
 * A command which will be executed after a successful invocation of
 * {@link AsyncCallback#onSuccess(Object)}.
 * 
 * @author Franz-Josef Elmer
 */
public interface ITestCommand
{
    /**
     * If the specified callback objects, failureMessage, and throwable should trigger this command
     * in case of an invocation of {@link AsyncCallback#onFailure(Throwable)} then the result is the
     * list of callbacks which were not expected by the command. Otherwise <code>null</code> is
     * returned.
     * 
     * @param callbackObjects List of callback objects since the last successful match of a test
     *            command. Contains at least one element.
     */
    public List<AbstractAsyncCallback<Object>> tryValidOnFailure(
            List<AbstractAsyncCallback<Object>> callbackObjects, String failureMessage,
            Throwable throwable);

    /**
     * If the specified callback objects and result should trigger this command in case of an
     * invocation of {@link AsyncCallback#onSuccess(Object)} then the result is the list of
     * callbacks which were not expected by the command. Otherwise <code>null</code> is returned.
     * 
     * @param callbackObjects List of callback objects since the last successful match of a test
     *            command. Contains at least one element.
     */
    public List<AbstractAsyncCallback<Object>> tryValidOnSucess(
            List<AbstractAsyncCallback<Object>> callbackObjects, Object result);

    /**
     * Executes this command.
     */
    public void execute();
}
