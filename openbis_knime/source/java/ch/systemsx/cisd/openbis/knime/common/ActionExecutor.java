/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ActionExecutor
{
    public <T> void executeAsync(final ILoadingBuildingAction<T> loadingBuildingAction,
            final IAsyncNodeAction action)
    {
        execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final T data = loadingBuildingAction.load();
                        action.execute(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    loadingBuildingAction.build(data);
                                }
                            });
                    } catch (Throwable ex)
                    {
                        action.handleException(ex);
                    }
                }
            });
    }

    protected void execute(Runnable runnable)
    {
        new Thread(runnable).start();
    }

}
