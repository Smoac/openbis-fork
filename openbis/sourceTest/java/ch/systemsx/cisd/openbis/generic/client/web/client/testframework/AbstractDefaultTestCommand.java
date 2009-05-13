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
 * Abstract super class of all test commands which are executed if the set of classes of recent
 * callback objects includes all classes specified in the constructor.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDefaultTestCommand extends Assert implements ITestCommand
{
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
        expectedCallbackIds.add(callbackId);
    }

    public List<AbstractAsyncCallback<Object>> tryValidOnFailure(
            final List<AbstractAsyncCallback<Object>> callbackObjects, final String failureMessage,
            final Throwable throwable)
    {
        return null;
    }

    public List<AbstractAsyncCallback<Object>> tryValidOnSucess(
            final List<AbstractAsyncCallback<Object>> callbackObjects, final Object result)
    {
        return tryGetUnmatchedCallbacks(callbackObjects);
    }

    /**
     * If all expected callbacks can be found among specified callbacks, returns the list of
     * callbacks which were not expected. Otherwise returns null;
     */
    protected List<AbstractAsyncCallback<Object>> tryGetUnmatchedCallbacks(
            final List<AbstractAsyncCallback<Object>> callbackObjects)
    {
        List<String> expectedIds = new ArrayList<String>(expectedCallbackIds);
        List<AbstractAsyncCallback<Object>> unmatched =
                new ArrayList<AbstractAsyncCallback<Object>>();
        for (final AbstractAsyncCallback<Object> asyncCallback : callbackObjects)
        {
            String id = asyncCallback.getCallbackId();
            if (expectedIds.contains(id))
            {
                expectedIds.remove(id);
            } else
            {
                unmatched.add(asyncCallback);
            }
        }
        if (expectedIds.size() == 0)
        {
            return unmatched; // all expected callbacks are present
        } else
        {
            return null;
        }
    }
}
