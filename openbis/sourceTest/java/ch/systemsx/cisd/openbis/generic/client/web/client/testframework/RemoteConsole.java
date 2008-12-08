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
 * executed in the order they have been added by {@link #prepare(ITestCommand)}. In order to be
 * executed a command has to be valid for the kind of {@link AsyncCallback} invocation. With the
 * method {@link #finish(int)} a timeout will be specified after which the test will be terminated
 * independent whether all commands have been executed or not. In the later case the test fails.
 * 
 * @author Franz-Josef Elmer
 */
public class RemoteConsole
{

    private final AbstractGWTTestCase testCase;

    private final List<ITestCommand> commands;

    private final List<AsyncCallback<Object>> lastCallbackObjects =
            new ArrayList<AsyncCallback<Object>>();

    private int entryIndex;

    private Timer timer;

    /**
     * Creates an instance for the specified test.
     */
    public RemoteConsole(final AbstractGWTTestCase testCase)
    {
        this.testCase = testCase;
        commands = new ArrayList<ITestCommand>();
        AbstractAsyncCallback.setStaticCallbackListener(new RemoteConsoleCallbackListener());
    }

    /**
     * Prepares the console with the specified command which will be executed if the specified
     * condition is fulfilled.
     */
    public RemoteConsole prepare(final ITestCommand command)
    {
        commands.add(command);
        return this;
    }

    /**
     * Sets the timeout after which the test is terminated.
     * 
     * @throws AssertionError if not all commands have been executed.
     */
    public void finish(final int delayInMilliseconds)
    {
        timer = new Timer()
            {
                @Override
                public void run()
                {
                    AbstractAsyncCallback
                            .setStaticCallbackListener(AbstractAsyncCallback.DEFAULT_CALLBACK_LISTENER);
                    final int numberOfUnexcutedCommands = commands.size() - entryIndex;
                    if (numberOfUnexcutedCommands > 0)
                    {
                        final StringBuffer buffer = new StringBuffer("Console not finished. Last ");
                        buffer.append(numberOfUnexcutedCommands == 1 ? "command has"
                                : numberOfUnexcutedCommands + " commands have");
                        buffer.append(" not been executed. ");
                        if (lastCallbackObjects.size() == 0)
                        {
                            buffer.append("No unmatched callback objects.");
                        } else
                        {
                            buffer.append("Unmatched callback objects:");
                            for (final AsyncCallback<?> callback : lastCallbackObjects)
                            {
                                buffer.append('\n').append(callback);
                            }
                        }
                        Assert.fail(buffer.toString());
                    }
                }
            };
        timer.schedule(delayInMilliseconds);
        testCase.delayTestTermination(delayInMilliseconds + 1000);
    }

    void cancelTimer()
    {
        if (timer != null)
        {
            timer.cancel();
        } else
        {
            Assert.fail("Missing preparation of the remote console with method finish().");
        }
    }

    //
    // Helper classes
    //

    private final class RemoteConsoleCallbackListener implements ICallbackListener
    {
        RemoteConsoleCallbackListener()
        {
        }

        private final void executeCommand()
        {
            lastCallbackObjects.clear();
            final ITestCommand testCommand = commands.get(entryIndex++);
            System.out.println("EXECUTE: " + testCommand);
            testCommand.execute();
            if (entryIndex == commands.size())
            {
                testCase.terminateTest();
            }
        }

        //
        // ICallbackListener
        //

        public final void onFailureOf(final AsyncCallback<Object> callback,
                final String failureMessage, final Throwable throwable)
        {
            lastCallbackObjects.add(callback);
            if (entryIndex < commands.size()
                    && commands.get(entryIndex).validOnFailure(lastCallbackObjects, failureMessage,
                            throwable))
            {
                executeCommand();
            } else
            {
                Assert.fail("Failed callback " + callback + ": " + failureMessage + "["
                        + throwable.getClass() + "]");
            }
        }

        public final void finishOnSuccessOf(final AsyncCallback<Object> callback,
                final Object result)
        {
            lastCallbackObjects.add(callback);
            if (entryIndex < commands.size()
                    && commands.get(entryIndex).validOnSucess(lastCallbackObjects, result))
            {
                executeCommand();
            }
        }
    }
}
