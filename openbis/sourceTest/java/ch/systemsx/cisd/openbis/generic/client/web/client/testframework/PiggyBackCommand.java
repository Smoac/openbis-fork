/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;

/**
 * A {@link ITestCommand} which wraps two other test command. All validation calls are delegated to
 * the first command. The method {@link #execute()} executes the second command right after the
 * first one. Thus, a piggy-back command behaves like the first command with an additional execution
 * of a carried-on command (i.e. the second command).
 * 
 * @author Franz-Josef Elmer
 */
public class PiggyBackCommand extends AbstractDefaultTestCommand
{

    private final ITestCommand firstCommand;

    private final ITestCommand secondCommand;

    public PiggyBackCommand(AbstractDefaultTestCommand firstCommand, ITestCommand secondCommand)
    {
        this.firstCommand = firstCommand;
        this.secondCommand = secondCommand;
    }

    @Override
    public List<AbstractAsyncCallback<Object>> tryValidOnFailure(
            List<AbstractAsyncCallback<Object>> callbackObjects, String failureMessage,
            Throwable throwable)
    {
        return firstCommand.tryValidOnFailure(callbackObjects, failureMessage, throwable);
    }

    @Override
    public List<AbstractAsyncCallback<Object>> tryValidOnSucess(
            List<AbstractAsyncCallback<Object>> callbackObjects, Object result)
    {
        return firstCommand.tryValidOnSucess(callbackObjects, result);
    }

    public void execute()
    {
        System.out.println("EXECUTE: " + firstCommand);
        firstCommand.execute();
        System.out.println("EXECUTE: " + secondCommand);
        secondCommand.execute();
    }

}
