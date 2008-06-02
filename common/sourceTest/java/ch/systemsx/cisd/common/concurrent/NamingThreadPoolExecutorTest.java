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

package ch.systemsx.cisd.common.concurrent;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for the {@link NamingThreadPoolExecutor}.
 *
 * @author Bernd Rinn
 */
public class NamingThreadPoolExecutorTest
{

    private final static String name = "This is the pool name";

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @Test
    public void testNamedPool() throws Throwable
    {
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 2);
        assertEquals(1, eservice.getCorePoolSize());
        assertEquals(2, eservice.getMaximumPoolSize());
        final Future<?> future = eservice.submit(new Runnable()
            {
                public void run()
                {
                    assertEquals(name + "-T1", Thread.currentThread().getName());
                }
            });
        try
        {
            future.get(200L, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex)
        {
            throw ex.getCause();
        }
    }

    @Test(groups = "slow")
    public void testThreadDefaultNames() throws Throwable
    {
        final int max = 10;
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, max, max);
        assertEquals(max, eservice.getCorePoolSize());
        assertEquals(max, eservice.getMaximumPoolSize());
        final Set<String> expectedThreadNameSet = new HashSet<String>();
        for (int i = 1; i <= max; ++i)
        {
            expectedThreadNameSet.add(name + "-T" + i);
        }
        final Set<String> threadNameSet = Collections.synchronizedSet(new HashSet<String>());
        final Set<Future<?>> futureSet = new HashSet<Future<?>>();
        for (int i = 0; i < max; ++i)
        {
            futureSet.add(eservice.submit(new Runnable()
                {
                    public void run()
                    {
                        threadNameSet.add(Thread.currentThread().getName());
                        try
                        {
                            Thread.sleep(20L);
                        } catch (InterruptedException ex)
                        {
                            fail("We got interrupted.");
                        }
                    }
                }));
        }
        for (Future<?> future : futureSet)
        {
            try
            {
                future.get(400L, TimeUnit.MILLISECONDS);
            } catch (ExecutionException ex)
            {
                throw ex.getCause();
            }
        }
        assertEquals(expectedThreadNameSet, threadNameSet);
    }

    @Test(groups = "slow")
    public void testSubmitNamedRunnable() throws Throwable
    {
        final String runnableName = "This is the special runnable name";
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 1);
        assertEquals(1, eservice.getCorePoolSize());
        assertEquals(1, eservice.getMaximumPoolSize());
        final Future<?> future = eservice.submit(new NamedRunnable()
            {
                public void run()
                {
                    assertEquals(name + "-T1::" + runnableName, Thread.currentThread().getName());
                }

                public String getRunnableName()
                {
                    return runnableName;
                }
            });
        try
        {
            future.get(200L, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex)
        {
            throw ex.getCause();
        }
    }

    @Test(groups = "slow")
    public void testExecuteNamedRunnable() throws Throwable
    {
        final String runnableName = "This is the special runnable name";
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 1);
        assertEquals(1, eservice.getCorePoolSize());
        assertEquals(1, eservice.getMaximumPoolSize());
        final Semaphore sem = new Semaphore(0);
        eservice.execute(new NamedRunnable()
            {
                public void run()
                {
                    assertEquals(name + "-T1::" + runnableName, Thread.currentThread().getName());
                    sem.release();
                }

                public String getRunnableName()
                {
                    return runnableName;
                }
            });
        assertTrue(sem.tryAcquire(200L, TimeUnit.MILLISECONDS));
    }

    @Test(groups = "slow")
    public void testSubmitNamedCallable() throws Throwable
    {
        final String callableName = "This is the special callable name";
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 1);
        assertEquals(1, eservice.getCorePoolSize());
        assertEquals(1, eservice.getMaximumPoolSize());
        final Future<?> future = eservice.submit(new NamedCallable<Object>()
            {
                public Object call() throws Exception
                {
                    assertEquals(name + "-T1::" + callableName, Thread.currentThread().getName());
                    return null;
                }

                public String getCallableName()
                {
                    return callableName;
                }
            });
        try
        {
            future.get(200L, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex)
        {
            throw ex.getCause();
        }
    }

    @Test(groups = "slow")
    public void testSubmitNamedCallables() throws Throwable
    {
        final String callableName1 = "This is the first special callable name";
        final ThreadPoolExecutor eservice = new NamingThreadPoolExecutor(name, 1, 1);
        assertEquals(1, eservice.getCorePoolSize());
        assertEquals(1, eservice.getMaximumPoolSize());
        final Future<?> future1 = eservice.submit(new NamedCallable<Object>()
            {
                public Object call() throws Exception
                {
                    assertEquals(name + "-T1::" + callableName1, Thread.currentThread().getName());
                    return null;
                }

                public String getCallableName()
                {
                    return callableName1;
                }
            });
        try
        {
            future1.get(200L, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex)
        {
            throw ex.getCause();
        }
        final String callableName2 = "This is the second special callable name";
        final Future<?> future2 = eservice.submit(new NamedCallable<Object>()
                {
                    public Object call() throws Exception
                    {
                        assertEquals(name + "-T1::" + callableName2, Thread.currentThread().getName());
                        return null;
                    }

                    public String getCallableName()
                    {
                        return callableName2;
                    }
                });
            try
            {
                future2.get(200L, TimeUnit.MILLISECONDS);
            } catch (ExecutionException ex)
            {
                throw ex.getCause();
            }
    }

}
