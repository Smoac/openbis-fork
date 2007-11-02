/*
 * Copyright 2007 ETH Zuerich, CISD.
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

package ch.systemsx.cisd.common.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements the logic of cleaning up a resource even in case of an exception but re-throws an exception
 * of the clean up procedure only when the main procedure didn't throw one. <code>CleanUpRunner</code>s can be
 * stacked.
 * 
 * @author Bernd Rinn
 */
public final class CleanUpCallable
{

    private final List<Runnable> cleanUpList;

    private final ICleanUpRegistry theRegistry = new ICleanUpRegistry()
        {
            public void registerCleanUp(Runnable cleanUp)
            {
                cleanUpList.add(cleanUp);
            }
        };

    public CleanUpCallable()
    {
        this.cleanUpList = new ArrayList<Runnable>();
    }

    /**
     * Runs a {@link ICallableWithCleanUp} and ensures that all registered clean-ups are performed afterwards.
     */
    public <T> T call(ICallableWithCleanUp<T> runnable)
    {
        boolean exceptionThrown = true;
        try
        {
            T result = runnable.call(theRegistry);
            exceptionThrown = false;
            return result;
        } finally
        {
            runCleanUps(exceptionThrown);
        }
    }

    /**
     * Runs all clean-ups in {@link #cleanUpList}.
     * 
     * @param exceptionThrown If <code>true</code>, all exceptions that happen during clean-up will be supressed.
     */
    private void runCleanUps(boolean exceptionThrown)
    {
        RuntimeException exceptionDuringCleanUp = null;
        for (int i = cleanUpList.size() - 1; i >= 0; --i)
        {
            final Runnable runnable = cleanUpList.get(i);
            try
            {
                runnable.run();
            } catch (RuntimeException ex)
            {
                if (exceptionThrown == false && exceptionDuringCleanUp == null)
                {
                    exceptionDuringCleanUp = ex;
                }
            }
        }
        cleanUpList.clear();
        if (exceptionDuringCleanUp != null)
        {
            throw exceptionDuringCleanUp;
        }
    }

}
