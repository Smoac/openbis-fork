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

package ch.systemsx.cisd.base.namedthread;

import java.util.concurrent.FutureTask;

/**
 * A {@link FutureTask} with a name.
 * 
 * @author Bernd Rinn
 */
class NamedFutureTask<V> extends FutureTask<V> implements NamedRunnable
{

    private final String name;

    NamedFutureTask(NamedCallable<V> callable)
    {
        super(callable);
        this.name = callable.getCallableName();
    }

    NamedFutureTask(NamedRunnable runnable, V result)
    {
        super(runnable, result);
        this.name = runnable.getRunnableName();
    }

    public String getRunnableName()
    {
        return name;
    }

}
