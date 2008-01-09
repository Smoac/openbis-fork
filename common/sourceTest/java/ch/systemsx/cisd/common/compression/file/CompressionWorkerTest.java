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

package ch.systemsx.cisd.common.compression.file;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * Test cases for the {@link CompressionWorker}.
 * 
 * @author Bernd Rinn
 */
public class CompressionWorkerTest
{

    private Mockery context;

    private Queue<File> queue;

    private Collection<FailureRecord> failed;

    private ICompressionMethod compressionMethod;

    private CompressionWorker worker;

    private BufferedAppender logRecorder;

    @SuppressWarnings("unchecked")
    private Queue<File> createFileQueue()
    {
        return context.mock(Queue.class);
    }

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        queue = createFileQueue();
        failed = new ArrayList<FailureRecord>();
        compressionMethod = context.mock(ICompressionMethod.class);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        worker = new CompressionWorker(queue, failed, compressionMethod);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCompressionWorkerImmediateExit()
    {
        context.checking(new Expectations()
            {
                {
                    one(queue).poll();
                    will(returnValue(null));
                }
            });
        worker.run();
        assertTrue(logRecorder.getLogContent().indexOf(CompressionWorker.EXITING_MSG) >= 0);
        assertEquals(0, failed.size());
    }

    @Test
    public void testCompressionWorkerHappyCase()
    {
        final File[] files = new File[]
            { new File("a"), new File("b"), new File("c") };
        context.checking(new Expectations()
            {
                {
                    for (File f : files)
                    {
                        one(queue).poll();
                        will(returnValue(f));
                        one(compressionMethod).compress(f);
                        will(returnValue(Status.OK));
                    }
                    one(queue).poll();
                    will(returnValue(null));
                }
            });
        worker.run();
        assertTrue(logRecorder.getLogContent().indexOf(CompressionWorker.EXITING_MSG) >= 0);
        assertEquals(0, failed.size());
    }

    @Test
    public void testCompressionWorkerWithRetriableFailure()
    {
        final String faultyFile = "b";
        final Status faultyStatus = new Status(StatusFlag.RETRIABLE_ERROR, "some problem");
        final File[] files = new File[]
            { new File("a"), new File(faultyFile), new File("c") };
        context.checking(new Expectations()
            {
                {
                    for (File f : files)
                    {
                        one(queue).poll();
                        will(returnValue(f));
                        one(compressionMethod).compress(f);
                        if (faultyFile.equals(f.getName()))
                        {
                            will(returnValue(faultyStatus));
                            one(compressionMethod).compress(f);
                            will(returnValue(Status.OK));
                        } else
                        {
                            will(returnValue(Status.OK));
                        }
                    }
                    one(queue).poll();
                    will(returnValue(null));
                }
            });
        worker.run();
        assertEquals(0, failed.size());
        assertTrue(logRecorder.getLogContent().indexOf(CompressionWorker.EXITING_MSG) >= 0);
    }

    @Test
    public void testCompressionWorkerWithRetriableFailureFinallyFailed()
    {
        final String faultyFile = "b";
        final Status faultyStatus = new Status(StatusFlag.RETRIABLE_ERROR, "some problem");
        final File[] files = new File[]
            { new File("a"), new File(faultyFile), new File("c") };
        context.checking(new Expectations()
            {
                {
                    for (File f : files)
                    {
                        one(queue).poll();
                        will(returnValue(f));
                        if (faultyFile.equals(f.getName()))
                        {
                            for (int i = 0; i < CompressionWorker.MAX_RETRY_OF_FAILED_COMPRESSIONS; ++i)
                            {
                                one(compressionMethod).compress(f);
                                will(returnValue(faultyStatus));
                            }
                        } else
                        {
                            one(compressionMethod).compress(f);
                            will(returnValue(Status.OK));
                        }
                    }
                    one(queue).poll();
                    will(returnValue(null));
                }
            });
        worker.run();
        assertEquals(1, failed.size());
        final FailureRecord record = failed.iterator().next();
        assertEquals(faultyFile, record.getFailedFile().getName());
        assertEquals(StatusFlag.RETRIABLE_ERROR, record.getFailureStatus().getFlag());
        assertEquals(faultyStatus.getMessage(), record.getFailureStatus().getMessage());
        assertTrue(logRecorder.getLogContent().indexOf(CompressionWorker.EXITING_MSG) >= 0);
    }

    @Test
    public void testCompressionWorkerWithFatalFailure()
    {
        final String faultyFile = "b";
        final Status fatalStatus = new Status(StatusFlag.FATAL_ERROR, "some problem");
        final File[] files = new File[]
            { new File("a"), new File(faultyFile), new File("c") };
        context.checking(new Expectations()
            {
                {
                    for (File f : files)
                    {
                        one(queue).poll();
                        will(returnValue(f));
                        one(compressionMethod).compress(f);
                        if (faultyFile.equals(f.getName()))
                        {
                            will(returnValue(fatalStatus));
                        } else
                        {
                            will(returnValue(Status.OK));
                        }
                    }
                    one(queue).poll();
                    will(returnValue(null));
                }
            });
        worker.run();
        assertEquals(1, failed.size());
        final FailureRecord record = failed.iterator().next();
        assertEquals(faultyFile, record.getFailedFile().getName());
        assertEquals(StatusFlag.FATAL_ERROR, record.getFailureStatus().getFlag());
        assertEquals(fatalStatus.getMessage(), record.getFailureStatus().getMessage());
        assertTrue(logRecorder.getLogContent().indexOf(CompressionWorker.EXITING_MSG) >= 0);
    }

    @Test
    public void testCompressionWorkerInterrupted()
    {
        final File[] files = new File[]
            { new File("a"), new File("b"), new File("c") };
        context.checking(new Expectations()
            {
                {
                    for (File f : files)
                    {
                        one(queue).poll();
                        will(returnValue(f));
                        one(compressionMethod).compress(f);
                        if (f.getName().equals("c"))
                        {
                            will(new CustomAction("Interrupt Thread")
                                {
                                    public Object invoke(Invocation invocation) throws Throwable
                                    {
                                        Thread.currentThread().interrupt();
                                        return Status.OK;
                                    }
                                });
                        } else
                        {
                            will(returnValue(Status.OK));
                        }
                    }
                }
            });
        worker.run();
        assertTrue(logRecorder.getLogContent().indexOf(CompressionWorker.INTERRPTED_MSG) >= 0);
    }

    private final class FakedException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;
    }

    @Test
    public void testCompressionWorkerCompressorThrowsException()
    {
        final String faultyFile = "b";
        final File[] files = new File[]
            { new File("a"), new File(faultyFile), new File("c") };
        final FakedException ex = new FakedException();
        context.checking(new Expectations()
            {
                {
                    for (File f : files)
                    {
                        one(queue).poll();
                        will(returnValue(f));
                        one(compressionMethod).compress(f);
                        if (f.getName().equals(faultyFile))
                        {
                            will(new CustomAction("Throws Exception")
                                {
                                    public Object invoke(Invocation invocation) throws Throwable
                                    {
                                        throw ex;
                                    }
                                });
                        } else
                        {
                            will(returnValue(Status.OK));
                        }
                    }
                    one(queue).poll();
                    will(returnValue(null));
                }
            });
        worker.run();
        assertEquals(1, failed.size());
        final FailureRecord record = failed.iterator().next();
        assertEquals(faultyFile, record.getFailedFile().getName());
        assertEquals(StatusFlag.FATAL_ERROR, record.getFailureStatus().getFlag());
        assertEquals("Exceptional condition: " + FakedException.class.getSimpleName(), record.getFailureStatus()
                .getMessage());
        assertEquals(ex, record.tryGetThrowable());
        assertTrue(logRecorder.getLogContent().indexOf(
                String.format(CompressionWorker.EXCEPTION_COMPRESSING_MSG_TEMPLATE, faultyFile)) >= 0);
    }

}
