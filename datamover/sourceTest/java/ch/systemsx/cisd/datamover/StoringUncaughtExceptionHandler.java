/*
 * Copyright 2007 ETH Zuerich, CISD
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
package ch.systemsx.cisd.datamover;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * An exception handler that stores the first occurring exception for later investigation. Needs to be activated by
 * 
 * <pre>
 * StoringUncaughtExceptionHandler exceptionHandler = new StoringUncaughtExceptionHandler();
 * Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
 * </pre>
 * 
 * @author Bernd Rinn
 */
public class StoringUncaughtExceptionHandler implements UncaughtExceptionHandler
{

    private Throwable throwable;

    private String threadName;

    public void uncaughtException(Thread t, Throwable e)
    {
        if (throwable == null) // Only store the first throwable
        {
            throwable = e;
            threadName = t.getName();
        }
    }

    /** Resets the handler. Any stored exception will be lost. */
    public void reset()
    {
        throwable = null;
        threadName = null;
    }

    /**
     * @return <code>true</code> if an exception or error has occurred, <code>false</code> otherwise.
     */
    public boolean hasExceptionOccurred()
    {
        return (throwable != null);
    }

    /**
     * @return The throwable, if any has been occurred, or <code>null</code> otherwise.
     */
    public Throwable getThrowable()
    {
        return throwable;
    }

    /**
     * @return The name of the thread where the exception or error has occurred, or <code>null</code>, if no
     *         exception or error has occurred.
     */
    public String getThreadName()
    {
        return threadName;
    }

    /**
     * Checks whether an exception or error has occurred and, if yes, throws a new {@link RuntimeException} with the
     * caught exception as cause in the current thread.
     */
    public void checkAndRethrowException()
    {
        if (hasExceptionOccurred())
        {
            throw new RuntimeException(String.format("An exception occurred in thread %s.", getThreadName()),
                    getThrowable());
        }
    }

}