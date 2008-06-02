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

package ch.systemsx.cisd.common.concurrent;

import static org.testng.AssertJUnit.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for {@link ConcurrencyUtilities}.
 * 
 * @author Bernd Rinn
 */
public class ConcurrencyUtilitiesTest
{

    private final static String name = "This is the pool name";

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @Test
    public void testTryGetFutureOK()
    {
        final String valueProvided = "This is the execution return value";
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    return valueProvided;
                }
            });
        final String valueObtained = ConcurrencyUtilities.tryGetResult(future, 200L);
        assertEquals(valueProvided, valueObtained);
        assertTrue(future.isDone());
    }

    @Test
    public void testGetExecutionResultOK()
    {
        final String valueProvided = "This is the execution return value";
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    return valueProvided;
                }
            });
        final ExecutionResult<String> result = ConcurrencyUtilities.getResult(future, 200L);
        assertEquals(ExecutionStatus.COMPLETE, result.getStatus());
        assertNull(result.tryGetException());
        assertEquals(valueProvided, result.tryGetResult());
        assertTrue(future.isDone());
    }

    @Test(groups = "slow")
    public void testTryGetFutureTimeout()
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    try
                    {
                        Thread.sleep(200L);
                    } catch (InterruptedException ex)
                    {
                        throw new CheckedExceptionTunnel(ex);
                    }
                    return null;
                }
            });
        final String shouldBeNull = ConcurrencyUtilities.tryGetResult(future, 20L);
        assertNull(shouldBeNull);
        assertTrue(future.isDone());
    }

    @Test(groups = "slow")
    public void testGetExecutionResultTimeout()
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    try
                    {
                        Thread.sleep(200L);
                    } catch (InterruptedException ex)
                    {
                        throw new CheckedExceptionTunnel(ex);
                    }
                    return null;
                }
            });
        final ExecutionResult<String> result = ConcurrencyUtilities.getResult(future, 20L);
        assertEquals(ExecutionStatus.TIMED_OUT, result.getStatus());
        assertNull(result.tryGetResult());
        assertNull(result.tryGetException());
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
    }

    @Test(groups = "slow")
    public void testGetExecutionResultTimeoutWithoutCancelation()
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    try
                    {
                        Thread.sleep(200L);
                    } catch (InterruptedException ex)
                    {
                        throw new CheckedExceptionTunnel(ex);
                    }
                    return null;
                }
            });
        final ExecutionResult<String> result =
                ConcurrencyUtilities.getResult(future, 20L, false, null, null);
        assertEquals(ExecutionStatus.TIMED_OUT, result.getStatus());
        assertNull(result.tryGetResult());
        assertNull(result.tryGetException());
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
    }

    @Test
    public void testTryGetFutureInterrupted()
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Thread thread = Thread.currentThread();
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    try
                    {
                        Thread.sleep(200L);
                    } catch (InterruptedException ex)
                    {
                        throw new CheckedExceptionTunnel(ex);
                    }
                    return null;
                }
            });
        final Timer t = new Timer();
        t.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    thread.interrupt();
                }
            }, 20L);
        final String shouldBeNull = ConcurrencyUtilities.tryGetResult(future, 200L, false);
        t.cancel();
        assertNull(shouldBeNull);
        assertTrue(future.isCancelled());
        assertFalse(Thread.interrupted());
    }

    @Test(expectedExceptions = { StopException.class })
    public void testTryGetFutureStop()
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Thread thread = Thread.currentThread();
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    try
                    {
                        Thread.sleep(200L);
                    } catch (InterruptedException ex)
                    {
                        throw new CheckedExceptionTunnel(ex);
                    }
                    return null;
                }
            });
        final Timer t = new Timer();
        t.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    thread.interrupt();
                }
            }, 20L);
        // Supposed to throw a StopException
        ConcurrencyUtilities.tryGetResult(future, 200L);
    }

    @Test
    public void testGetExecutionResultInterrupted()
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Thread thread = Thread.currentThread();
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    try
                    {
                        Thread.sleep(200L);
                    } catch (InterruptedException ex)
                    {
                        throw new CheckedExceptionTunnel(ex);
                    }
                    return null;
                }
            });
        final Timer t = new Timer();
        t.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    thread.interrupt();
                }
            }, 20L);
        final ExecutionResult<String> result = ConcurrencyUtilities.getResult(future, 200L);
        t.cancel();
        assertEquals(ExecutionStatus.INTERRUPTED, result.getStatus());
        assertNull(result.tryGetResult());
        assertNull(result.tryGetException());
        assertTrue(future.isCancelled());
        assertFalse(Thread.interrupted());
    }

    private static class TaggedException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }

    @Test
    public void testGetExecutionResultException()
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    throw new TaggedException();
                }
            });
        final ExecutionResult<String> result = ConcurrencyUtilities.getResult(future, 100L);
        assertEquals(ExecutionStatus.EXCEPTION, result.getStatus());
        assertTrue(result.tryGetException() instanceof TaggedException);
        assertNull(result.tryGetResult());
        assertTrue(future.isDone());
    }

    @Test
    public void testTryGetFutureException()
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    throw new TaggedException();
                }
            });
        try
        {
            ConcurrencyUtilities.tryGetResult(future, 100L);
            fail("Should have been a TaggedException");
        } catch (TaggedException ex)
        {
            // Good
        }
        assertTrue(future.isDone());
    }
}
