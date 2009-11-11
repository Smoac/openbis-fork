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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;

/**
 * Abstract super class of all test commands which are executed if there are no active callbacks
 * (all callbacks were detected by {@link RemoteConsole}).
 * <p>
 * NOTE: Expected callbacks are now completely ignored.
 * 
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
public abstract class AbstractDefaultTestCommand extends Assert implements ITestCommand
{
    // TODO 2009-11-10, Piotr Buczek: expected callbacks are now completely ignored - cleanup
    protected final List<String> expectedCallbackIds = new ArrayList<String>();

    /**
     * Creates an instance with initially no expected callback class.
     */
    public AbstractDefaultTestCommand()
    {
    }

    /**
     * Creates an instance for the specified callback class.
     */
    public AbstractDefaultTestCommand(final Class<? extends AsyncCallback<?>> callbackClass)
    {
        addCallbackClass(callbackClass);
    }

    /**
     * Adds the specified callback classes.
     */
    public void addCallbackClass(final Class<? extends AsyncCallback<?>> callbackClass)
    {
        addCallbackClass(callbackClass.getName());
    }

    /**
     * Adds the callback with the specified id.
     */
    public void addCallbackClass(final String callbackId)
    {
        System.out.println("The command " + getClass().getName() + " is waiting for callback "
                + callbackId);
        // escaping is needed because we use callbackId as a regular expression
        expectedCallbackIds.add(escape(callbackId));
    }

    private String escape(String string)
    {
        return string.replace("$", "\\$");
    }

    public boolean isValidOnSucess(Object result)
    {
        return true; // if previous command succeeded this command should be executed
    }

    public boolean isValidOnFailure(AbstractAsyncCallback<?> callback, String failureMessage,
            Throwable throwable)
    {
        return false; // if previous command failed this command shouldn't be executed
    }

}
