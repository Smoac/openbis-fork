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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private ThreadPoolExecutor eservice;

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        eservice = (ThreadPoolExecutor) ConcurrencyUtilities.newNamedPool(name, 1, 2);
    }
    
    @Test 
    public void testNewNamedPool() throws Throwable
    {
        assertEquals(1, eservice.getCorePoolSize());
        assertEquals(2, eservice.getMaximumPoolSize());
        final Future future = eservice.submit(new Runnable()
        {
            public void run()
            {
                assertEquals(name + " 1", Thread.currentThread().getName());
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
    
    
    @Test
    public void testTryGetFutureTimeout()
    {
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

    @Test
    public void testTryGetFutureInterrupted()
    {
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
        t.schedule(new TimerTask() {
            @Override
            public void run()
            {
                thread.interrupt();
            }
        }, 20L);
        final String shouldBeNull = ConcurrencyUtilities.tryGetResult(future, 200L);
        assertNull(shouldBeNull);
        assertTrue(future.isCancelled());
        assertFalse(Thread.interrupted());
    }

    private static class TaggedException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }
    
    @Test
    public void testTryGetFutureException()
    {
        final Thread thread = Thread.currentThread();
        final Future<String> future = eservice.submit(new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        throw new TaggedException(); 
                    }
                });
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run()
            {
                thread.interrupt();
            }
        }, 20L);
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
