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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ICallbackListener;

/**
 * A class which allows to execute a sequence of {@link ITestCommand} instances. The commands are
 * executed in the order they have been added by the various prepare methods. A
 * {@link ICallbackCondition} has to be fulfilled before the next command is executed. This
 * condition is checked after each successful invocation of
 * {@link AsyncCallback#onSuccess(Object)}. With the method {@link #finish(int)} a timeout
 * will be specified after which the test will be terminated independent whether all commands have
 * been executed or not. In the later case the test fails.
 * 
 * @author Franz-Josef Elmer
 */
public class RemoteConsole
{
    private static final class CommandEntry 
    {
        private final ICallbackCondition<Object> condition;
        private final ITestCommand command;
        CommandEntry(ICallbackCondition<Object> condition, ITestCommand command)
        {
            this.condition = condition;
            this.command = command;
        }
    }
    
    private class EqualsCallback implements ICallbackCondition<Object>
    {
        private final AsyncCallback<?> callbackToCheck;
        
        public EqualsCallback(AsyncCallback<?> callback)
        {
            this.callbackToCheck = callback;
        }
        
        public boolean valid(AsyncCallback<Object> callback, Object result)
        {
            return callbackToCheck.equals(callback);
        }
    }
    
    private final AbstractGWTTestCase testCase;
    private final List<CommandEntry> entries;
    
    private int entryIndex;
    
    /**
     * Creates an instance for the specified test.
     */
    public RemoteConsole(final AbstractGWTTestCase testCase)
    {
        this.testCase = testCase;
        entries = new ArrayList<CommandEntry>();
        AbstractAsyncCallback.setCallbackListener(new ICallbackListener()
            {
                public void onFailureOf(AsyncCallback<Object> callback, Throwable throwable)
                {
                    Assert.fail("Failed condition " + callback + ": " + throwable);
                }

                public void startOnSuccessOf(AsyncCallback<Object> callback, Object result)
                {
                }

                public void finishOnSuccessOf(AsyncCallback<Object> callback, Object result)
                {
                    if (entryIndex < entries.size()
                            && entries.get(entryIndex).condition.valid(callback, result))
                    {
                        CommandEntry commandEntry = entries.get(entryIndex++);
                        commandEntry.command.execute();
                        if (entryIndex == entries.size())
                        {
                            testCase.terminateTest();
                        }
                    }
                }

            });
    }

    /**
     * Prepares the console with the specified command.
     */
    public RemoteConsole prepare(ITestCommandWithCondition<Object> command)
    {
        return prepare(command, command);
    }
    
    /**
     * Prepares the console with the specified command which will be executed if the
     * class of the callback object is as specified. 
     */
    public RemoteConsole prepare(Class<? extends AsyncCallback<?>> clazz, ITestCommand command)
    {
        return prepare(new CallbackClassCondition(clazz), command);
    }
    
    /**
     * Prepares the console with the specified command which will be executed if the
     * callback object equals the specified one. 
     */
    public RemoteConsole prepare(AsyncCallback<?> callback, ITestCommand command)
    {
        return prepare(new EqualsCallback(callback), command);
    }

    /**
     * Prepares the console with the specified command which will be executed if the
     * specified condition is fulfilled. 
     */
    public RemoteConsole prepare(ICallbackCondition<Object> condition, ITestCommand command)
    {
        entries.add(new CommandEntry(condition, command));
        return this;
    }
    
    /**
     * Sets the timeout after which the test is terminated.
     * 
     * @throws AssertionError if not all commands have been executed.
     */
    public void finish(int delayInMilliseconds)
    {
        new Timer()
            {
                @Override
                public void run()
                {
                    AbstractAsyncCallback.setCallbackListener(null);
                    int numberOfUnexcutedCommands = entries.size() - entryIndex;
                    if (numberOfUnexcutedCommands > 0)
                    {
                        Assert.fail("Console not finished. Last "
                                + (numberOfUnexcutedCommands == 1 ? "command has"
                                        : numberOfUnexcutedCommands + " commands have")
                                + " not been executed.");
                    }
                }
            }.schedule(delayInMilliseconds);
       testCase.delayTestTermination(delayInMilliseconds + 1000);
    }
}
