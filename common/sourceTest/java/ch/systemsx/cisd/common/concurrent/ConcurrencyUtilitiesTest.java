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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities.ILogSettings;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.test.Retry10;

/**
 * Test cases for {@link ConcurrencyUtilities}.
 * 
 * @author Bernd Rinn
 */
public class ConcurrencyUtilitiesTest
{

    private final static String name = "This is the pool name";

    private static class LogRecord
    {
        final LogLevel level;

        final String message;

        LogRecord(final LogLevel level, final String message)
        {
            this.level = level;
            this.message = message;
        }
    }

    private class AssertingLogger implements ISimpleLogger
    {
        private final List<LogRecord> records = new ArrayList<LogRecord>();

        public void log(final LogLevel level, final String message)
        {
            records.add(new LogRecord(level, message));
        }

        public void assertNumberOfMessage(final int expectedNumberOfMessages)
        {
            assertEquals(expectedNumberOfMessages, records.size());
        }

        public void assertEq(final int i, final LogLevel expectedLevel, final String expectedMessage)
        {
            assertEquals(expectedLevel, records.get(i).level);
            assertEquals(expectedMessage, records.get(i).message);
        }

    }

    private ILogSettings logSettings;

    private AssertingLogger logger;

    @BeforeTest
    public void init()
    {
        LogInitializer.init();
    }

    @BeforeMethod
    public void beforeMethod()
    {
        logger = new AssertingLogger();
        createLogSettings(LogLevel.WARN);
    }

    @AfterClass
    public void clearThreadInterruptionState()
    {
        Thread.interrupted();
    }

    private void createLogSettings(final LogLevel level)
    {
        logSettings = new ILogSettings()
            {
                public LogLevel getLogLevelForError()
                {
                    return level;
                }

                public AssertingLogger getLogger()
                {
                    return logger;
                }

                public String getOperationName()
                {
                    return name;
                }
            };
    }

    @Test
    public void testTryGetFutureOK()
    {
        final String valueProvided = "This is the execution return value";
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    return valueProvided;
                }
            });
        final String valueObtained =
                ConcurrencyUtilities.tryGetResult(future, 200L, logSettings, true);
        assertEquals(valueProvided, valueObtained);
        assertTrue(future.isDone());
        logger.assertNumberOfMessage(0);
    }

    @Test
    public void testGetExecutionResultOK()
    {
        final String valueProvided = "This is the execution return value";
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    return valueProvided;
                }
            });
        final ExecutionResult<String> result =
                ConcurrencyUtilities.getResult(future, 200L, logSettings);
        assertEquals(ExecutionStatus.COMPLETE, result.getStatus());
        assertNull(result.tryGetException());
        assertEquals(valueProvided, result.tryGetResult());
        assertTrue(future.isDone());
        logger.assertNumberOfMessage(0);
    }

    @Test
    public void testGetExecutionResultOKWithoutLogging()
    {
        final String valueProvided = "This is the execution return value";
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
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
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
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
        final String shouldBeNull =
                ConcurrencyUtilities.tryGetResult(future, 20L, logSettings, true);
        assertNull(shouldBeNull);
        assertTrue(future.isDone());
        logger.assertNumberOfMessage(1);
        logger.assertEq(0, LogLevel.WARN, name + ": timeout of 0.02 s exceeded, cancelled.");
    }

    @Test(groups = "slow")
    public void testGetExecutionResultTimeout()
    {
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
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
                ConcurrencyUtilities.getResult(future, 20L, logSettings);
        assertEquals(ExecutionStatus.TIMED_OUT, result.getStatus());
        assertNull(result.tryGetResult());
        assertNull(result.tryGetException());
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        logger.assertNumberOfMessage(1);
        logger.assertEq(0, LogLevel.WARN, name + ": timeout of 0.02 s exceeded, cancelled.");
    }

    @Test(groups = "slow", retryAnalyzer = Retry10.class)
    public void testGetExecutionResultNoTimeoutDueToSensor()
    {
        final RecordingActivityObserverSensor sensor = new RecordingActivityObserverSensor();
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
        final Timer updatingTimer = new Timer();
        final String msg = "success";
        try
        {
            updatingTimer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        sensor.update();
                    }
                }, 0L, 10L);
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
                        return msg;
                    }
                });
            final ExecutionResult<String> result =
                    ConcurrencyUtilities.getResult(future, 50L, true, logSettings, null, sensor);
            assertEquals(ExecutionStatus.COMPLETE, result.getStatus());
            assertEquals(msg, result.tryGetResult());
            assertNull(result.tryGetException());
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());
            logger.assertNumberOfMessage(0);
        } finally
        {
            updatingTimer.cancel();
        }
    }

    @Test(groups = "slow")
    public void testGetExecutionResultTimeoutWithoutCancelation()
    {
        createLogSettings(LogLevel.INFO);
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
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
                ConcurrencyUtilities.getResult(future, 20L, false, logSettings);
        assertEquals(ExecutionStatus.TIMED_OUT, result.getStatus());
        assertNull(result.tryGetResult());
        assertNull(result.tryGetException());
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        logger.assertNumberOfMessage(1);
        logger.assertEq(0, LogLevel.INFO, name + ": timeout of 0.02 s exceeded.");
    }

    @Test
    public void testTryGetFutureInterrupted()
    {
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
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
        final String shouldBeNull =
                ConcurrencyUtilities.tryGetResult(future, 200L, logSettings, false);
        t.cancel();
        assertNull(shouldBeNull);
        assertTrue(future.isCancelled());
        assertFalse(Thread.interrupted());
        logger.assertNumberOfMessage(1);
        logger.assertEq(0, LogLevel.WARN, name + ": interrupted.");
    }

    @Test(expectedExceptions =
        { InterruptedExceptionUnchecked.class })
    public void testTryGetFutureStop()
    {
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
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
        createLogSettings(LogLevel.DEBUG);
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
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
        final ExecutionResult<String> result =
                ConcurrencyUtilities.getResult(future, 200L, logSettings);
        t.cancel();
        assertEquals(ExecutionStatus.INTERRUPTED, result.getStatus());
        assertNull(result.tryGetResult());
        assertNull(result.tryGetException());
        assertTrue(future.isCancelled());
        assertFalse(Thread.interrupted());
        logger.assertNumberOfMessage(1);
        logger.assertEq(0, LogLevel.DEBUG, name + ": interrupted.");
    }

    private static class TaggedException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public TaggedException()
        {
        }

        public TaggedException(final String msg)
        {
            super(msg);
        }
    }

    @Test
    public void testGetExecutionResultException()
    {
        createLogSettings(LogLevel.ERROR);
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    throw new TaggedException();
                }
            });
        final ExecutionResult<String> result =
                ConcurrencyUtilities.getResult(future, 100L, logSettings);
        assertEquals(ExecutionStatus.EXCEPTION, result.getStatus());
        assertTrue(result.tryGetException() instanceof TaggedException);
        assertNull(result.tryGetResult());
        assertTrue(future.isDone());
        logger.assertNumberOfMessage(1);
        logger.assertEq(0, LogLevel.ERROR, name + ": exception: <no message> [TaggedException].");
    }

    @Test
    public void testTryGetFutureException()
    {
        final String msg = "This is some sort of error message";
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    throw new TaggedException(msg);
                }
            });
        try
        {
            ConcurrencyUtilities.tryGetResult(future, 100L, logSettings, true);
            fail("Should have been a TaggedException");
        } catch (final TaggedException ex)
        {
            // Good
        }
        assertTrue(future.isDone());
        logger.assertNumberOfMessage(1);
        logger.assertEq(0, LogLevel.WARN, name
                + ": exception: This is some sort of error message [TaggedException].");
    }

    @Test
    public void testTryGetFutureExceptionWithoutLogging()
    {
        final String msg = "This is some sort of error message";
        final ThreadPoolExecutor eservice =
                new NamingThreadPoolExecutor(name).corePoolSize(1).maximumPoolSize(2);
        final Future<String> future = eservice.submit(new Callable<String>()
            {
                public String call() throws Exception
                {
                    throw new TaggedException(msg);
                }
            });
        try
        {
            ConcurrencyUtilities.tryGetResult(future, 100L);
            fail("Should have been a TaggedException");
        } catch (final TaggedException ex)
        {
            // Good
        }
        assertTrue(future.isDone());
    }
}
