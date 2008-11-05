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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Abstract super class of all test commands which are executed if the set of classes of recent
 * callback objects includes all classes specified in the constructor.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDefaultTestCommand extends Assert implements ITestCommand
{
    protected final Set<Class<? extends AsyncCallback<?>>> callbackClasses =
            new HashSet<Class<? extends AsyncCallback<?>>>();

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
        callbackClasses.add(callbackClass);
    }

    /**
     * Adds the specified callback classes.
     */
    public void addCallbackClass(final Class<? extends AsyncCallback<?>> callbackClass)
    {
        this.callbackClasses.add(callbackClass);
    }

    public boolean validOnFailure(final List<AsyncCallback<Object>> callbackObjects,
            final String failureMessage, final Throwable throwable)
    {
        return false;
    }

    public boolean validOnSucess(final List<AsyncCallback<Object>> callbackObjects,
            final Object result)
    {
        return containsExpectedCallbacks(callbackObjects);
    }

    /**
     * Returns <code>true</code> if the specified list of callback objects contain all of expected
     * types.
     */
    protected boolean containsExpectedCallbacks(final List<AsyncCallback<Object>> callbackObjects)
    {
        final Set<Class<?>> classesOfCallbackObjects = new HashSet<Class<?>>();
        for (final AsyncCallback<Object> asyncCallback : callbackObjects)
        {
            classesOfCallbackObjects.add(asyncCallback.getClass());
        }
        for (final Class<?> clazz : callbackClasses)
        {
            if (classesOfCallbackObjects.contains(clazz) == false)
            {
                return false;
            }
        }
        return true;
    }

}
