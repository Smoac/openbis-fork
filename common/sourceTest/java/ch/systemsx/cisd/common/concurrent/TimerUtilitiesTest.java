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

import static org.testng.AssertJUnit.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.InterruptedExceptionUnchecked;

/**
 * Test cases for the {@link TimerUtilities}.
 * <p>
 * Note that the semaphores are dual-use as synchronization barrier (ensure the tasks are running
 * when we try to interact with its thread) and as probes for success or failure.
 * 
 * @author Bernd Rinn
 */
public class TimerUtilitiesTest
{
    @Test
    public void testOperational()
    {
        assertTrue(TimerUtilities.isOperational());
    }

    @Test
    public void testInterrupt() throws InterruptedException
    {
        final Semaphore sem = new Semaphore(0);
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    sem.release();
                    try
                    {
                        Thread.sleep(200L);
                        fail("should have been interrupted.");
                    } catch (InterruptedException ex)
                    {
                        // That is expected.
                        sem.release();
                    }
                }
            };
        timer.schedule(task, 0L);
        sem.acquire(); // Ensure we don't cancel() before the task is running.
        assertTrue(TimerUtilities.tryInterruptTimerThread(timer));
        assertTrue(sem.tryAcquire(100L, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testJoin() throws InterruptedException
    {
        final Semaphore sem = new Semaphore(0);
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    sem.release();
                    // We immediately return.
                }
            };
        timer.schedule(task, 50L);
        sem.acquire(); // Ensure we don't cancel() before the task is running.
        timer.cancel();
        assertTrue(TimerUtilities.tryJoinTimerThread(timer, 200L));
    }

    @Test
    public void testJoinFailed() throws InterruptedException
    {
        final Semaphore sem = new Semaphore(0);
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    sem.release();
                    try
                    {
                        Thread.sleep(50L);
                    } catch (InterruptedException ex)
                    {
                        throw new AssertionError("Unexpected interrupt.");
                    }
                }
            };
        timer.schedule(task, 0L);
        sem.acquire(); // Ensure we don't cancel() before the task is running.
        assertFalse(TimerUtilities.tryJoinTimerThread(timer, 100L));
        timer.cancel(); // Ensure the timer doesn't get called again.
    }

    @Test
    public void testInterruptAndJoin() throws InterruptedException
    {
        final Semaphore sem = new Semaphore(0);
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    sem.release();
                    try
                    {
                        Thread.sleep(200L);
                        fail("should have been interrupted.");
                    } catch (InterruptedException ex)
                    {
                        // That is expected, signal success.
                        sem.release();
                    }
                }
            };
        timer.schedule(task, 0L);
        sem.acquire(); // Ensure we don't cancel() before the task is running.
        timer.cancel();
        assertTrue(TimerUtilities.tryInterruptTimerThread(timer));
        assertTrue(TimerUtilities.tryJoinTimerThread(timer, 100L));
        assertTrue(sem.tryAcquire());
    }

    @Test
    public void testInterruptAndJoinFailed() throws InterruptedException
    {
        final Semaphore sem = new Semaphore(0);
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    sem.release();
                    try
                    {
                        Thread.sleep(200L);
                        fail("should have been interrupted.");
                    } catch (InterruptedException ex)
                    {
                        // That is expected, signal success.
                        sem.release();
                    }
                }
            };
        timer.schedule(task, 0L, 1000L);
        sem.acquire(); // Ensure we don't cancel() before the task is running.
        // Here we would need a timer.cancel() to make the join succeed
        assertTrue(TimerUtilities.tryInterruptTimerThread(timer));
        assertFalse(TimerUtilities.tryJoinTimerThread(timer, 100L));
        assertTrue(sem.tryAcquire());
        timer.cancel(); // Ensure the timer doesn't get called again.
    }

    @Test
    public void testShutdown() throws InterruptedException
    {
        final Semaphore sem = new Semaphore(0);
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    sem.release();
                    try
                    {
                        Thread.sleep(200L);
                        fail("should have been interrupted.");
                    } catch (InterruptedException ex)
                    {
                        // That is expected, signal success.
                        sem.release();
                    }
                }
            };
        timer.schedule(task, 0L);
        sem.acquire(); // Ensure we don't cancel() before the task is running.
        assertTrue(TimerUtilities.tryShutdownTimer(timer, 100L));
        assertTrue(sem.tryAcquire());
        timer.cancel(); // Just to be sure.
    }

    @Test
    public void testShutdownWithStop() throws InterruptedException
    {
        final Semaphore sem = new Semaphore(0);
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask()
            {
                private void keepWheelsSpinning()
                {
                    do
                    {
                        // Nothing.
                    } while (true);
                }

                @Override
                public void run()
                {
                    sem.release();
                    try
                    {
                        keepWheelsSpinning();
                        fail("should have been stopped.");
                    } catch (InterruptedExceptionUnchecked ex)
                    {
                        // That is expected, signal success.
                        sem.release();
                    }
                }
            };
        timer.schedule(task, 0L);
        sem.acquire(); // Ensure we don't cancel() before the task is running.
        assertTrue(TimerUtilities.tryShutdownTimer(timer, 50L));
        assertTrue(sem.tryAcquire());
        timer.cancel(); // Just to be sure.
    }
}
