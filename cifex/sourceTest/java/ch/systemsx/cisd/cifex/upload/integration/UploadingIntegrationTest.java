/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.upload.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.upload.client.IUploadListener;
import ch.systemsx.cisd.cifex.upload.client.Uploader;
import ch.systemsx.cisd.cifex.upload.server.CIFEXRPCService;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class UploadingIntegrationTest extends AssertJUnit
{
    private static final int BLOCK_SIZE = 64 * 1024;

    private static final String TEST_URL = "test-url";

    private static final File PLAYGROUND =
            new File(System.getProperty("user.dir"), "targets/playground");

    private static final File CLIENT_FOLDER = new File(PLAYGROUND, "clientFolder");

    private static final File FILE_STORE = new File(PLAYGROUND, "file-store");

    private static final String SMALL_FILE = "small-file";

    private static final long SMALL_FILE_SIZE = 10;

    private static final String LARGE_FILE = "large-file";

    private static final long LARGE_FILE_SIZE = 200;

    private static void createRandomData(File file, long sizeInKB)
    {
        Random random = new Random();
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            for (int i = 0; i < sizeInKB; i++)
            {
                random.nextBytes(bytes);
                outputStream.write(bytes);
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (outputStream != null)
            {
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    private Mockery context;

    private IFileManager fileManager;

    private Uploader uploader;

    private IUploadListener listener;

    private UserDTO user;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        fileManager = context.mock(IFileManager.class);
        CIFEXRPCService uploadService =
                new CIFEXRPCService(fileManager, null, null, null, null, "false");
        user = new UserDTO();
        user.setUserCode("Isaac");
        String sessionID = uploadService.createSession(user, TEST_URL);
        uploader = new Uploader(uploadService, sessionID);
        listener = context.mock(IUploadListener.class);
        uploader.addUploadListener(listener);

        FileUtilities.deleteRecursively(PLAYGROUND);
        CLIENT_FOLDER.mkdirs();
        FILE_STORE.mkdirs();
        createRandomData(new File(CLIENT_FOLDER, SMALL_FILE), SMALL_FILE_SIZE);
        createRandomData(new File(CLIENT_FOLDER, LARGE_FILE), LARGE_FILE_SIZE);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testNoFile() throws IOException
    {
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingFinished(true);
                    one(listener).reset();
                }
            });

        uploader.upload(Arrays.<File> asList(), "Albert\nGalileo", "no comment");

        context.assertIsSatisfied();
    }

    @Test
    public void testUploadTwice() throws IOException
    {
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingFinished(true);
                    one(listener).reset();
                    exactly(2).of(listener).exceptionOccured(
                            with(new BaseMatcher<EnvironmentFailureException>()
                                {
                                    public void describeTo(Description description)
                                    {
                                    }

                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof EnvironmentFailureException)
                                        {
                                            EnvironmentFailureException e =
                                                    (EnvironmentFailureException) item;
                                            return e.getMessage().startsWith(
                                                    "No upload session found");
                                        }
                                        return false;
                                    }

                                }));
                    one(listener).uploadingFinished(false);
                    one(listener).reset();
                }
            });

        uploader.upload(Arrays.<File> asList(), "Albert\nGalileo", "no comment");
        try
        {
            uploader.upload(Arrays.<File> asList(), "Albert\nGalileo", "no comment");
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException e)
        {
            assertTrue("Expected message: " + e.getMessage(), e.getMessage().indexOf(
                    "No upload session found") == 0);
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testSingleSmallFile() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore = new File(FILE_STORE, SMALL_FILE);
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingStarted(fileOnClient, SMALL_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, SMALL_FILE);
                    will(returnValue(fileInFileStore));

                    one(fileManager).registerFileLinkAndInformRecipients(user, SMALL_FILE,
                            "no comment", "application/octet-stream", fileInFileStore, new String[]
                                { "Albert", "Galileo" }, TEST_URL);
                    will(returnValue(Collections.emptyList()));

                    one(listener).uploadingProgress(0, 0);
                    one(listener).uploadingFinished(true);
                    one(listener).reset();
                }
            });

        uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", "no comment");

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    @Test
    public void testSingleLargeFile() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore = new File(FILE_STORE, LARGE_FILE);
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingStarted(fileOnClient, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE);
                    will(returnValue(fileInFileStore));

                    one(listener).uploadingProgress(0, 0);
                    one(listener).uploadingProgress(32, BLOCK_SIZE);
                    one(listener).uploadingProgress(64, 2 * BLOCK_SIZE);
                    one(listener).uploadingProgress(96, 3 * BLOCK_SIZE);
                    one(listener).uploadingFinished(true);
                    one(listener).reset();

                    one(fileManager).registerFileLinkAndInformRecipients(user, LARGE_FILE,
                            "no comment", "application/octet-stream", fileInFileStore, new String[]
                                { "Albert", "Galileo" }, TEST_URL);
                    will(returnValue(Collections.emptyList()));
                }
            });

        uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", "no comment");

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    @Test
    public void testTwoFiles() throws IOException
    {
        final File fileOnClient1 = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore1 = new File(FILE_STORE, SMALL_FILE);
        final File fileOnClient2 = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore2 = new File(FILE_STORE, LARGE_FILE);
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingStarted(fileOnClient1, SMALL_FILE_SIZE * 1024L);
                    one(fileManager).createFile(user, SMALL_FILE);
                    will(returnValue(fileInFileStore1));

                    one(listener).uploadingProgress(0, 0);
                    one(listener).fileUploaded();

                    one(fileManager).registerFileLinkAndInformRecipients(user, SMALL_FILE,
                            "no comment", "application/octet-stream", fileInFileStore1,
                            new String[]
                                { "Albert", "Galileo" }, TEST_URL);
                    will(returnValue(Collections.emptyList()));

                    one(listener).uploadingStarted(fileOnClient2, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE);
                    will(returnValue(fileInFileStore2));

                    one(listener).uploadingProgress(0, 0);
                    one(listener).uploadingProgress(32, BLOCK_SIZE);
                    one(listener).uploadingProgress(64, 2 * BLOCK_SIZE);
                    one(listener).uploadingProgress(96, 3 * BLOCK_SIZE);
                    one(listener).uploadingFinished(true);
                    one(listener).reset();

                    one(fileManager).registerFileLinkAndInformRecipients(user, LARGE_FILE,
                            "no comment", "application/octet-stream", fileInFileStore2,
                            new String[]
                                { "Albert", "Galileo" }, TEST_URL);
                    will(returnValue(Collections.emptyList()));
                }
            });

        uploader.upload(Arrays.asList(fileOnClient1, fileOnClient2), "Albert\nGalileo",
                "no comment");

        assertEqualContent(fileOnClient1, fileInFileStore1);
        assertEqualContent(fileOnClient2, fileInFileStore2);
        context.assertIsSatisfied();
    }

    @Test
    public void testUploadingForUnknownUser() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore = new File(FILE_STORE, SMALL_FILE);
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingStarted(fileOnClient, SMALL_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, SMALL_FILE);
                    will(returnValue(fileInFileStore));

                    one(fileManager).registerFileLinkAndInformRecipients(user, SMALL_FILE,
                            "no comment", "application/octet-stream", fileInFileStore, new String[]
                                { "Albert", "Galileo" }, TEST_URL);
                    will(returnValue(Arrays.asList("id:unknown")));

                    one(listener).exceptionOccured(with(new BaseMatcher<UserFailureException>()
                        {
                            public void describeTo(Description description)
                            {
                            }

                            public boolean matches(Object item)
                            {
                                if (item instanceof UserFailureException)
                                {
                                    UserFailureException e = (UserFailureException) item;
                                    return e.getMessage().contains("unknown");
                                }
                                return false;
                            }
                        }));

                    one(listener).uploadingFinished(false);
                    one(listener).reset();
                }
            });

        try
        {
            uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", "no comment");
            fail("UserFailureException expected");
        } catch (UserFailureException e) {
            assertEquals("Some user identifiers are invalid: [id:unknown]", e.getMessage());
        }

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    @Test
    public void testUploadingThrowingExceptionInFileManager() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore = new File(FILE_STORE, SMALL_FILE);
        final RuntimeException exception = new RuntimeException("Oops!");
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingStarted(fileOnClient, SMALL_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, SMALL_FILE);
                    will(returnValue(fileInFileStore));

                    one(fileManager).registerFileLinkAndInformRecipients(user, SMALL_FILE,
                            "no comment", "application/octet-stream", fileInFileStore, new String[]
                                { "Albert", "Galileo" }, TEST_URL);
                    will(throwException(exception));

                    one(listener).exceptionOccured(exception);

                    one(listener).uploadingFinished(false);
                    one(listener).reset();
                }
            });

        try
        {
            uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", "no comment");
            fail("RuntimeException expected");
        } catch (RuntimeException e)
        {
            assertSame(exception, e);
        }

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    @Test
    public void testCanceling() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore = new File(FILE_STORE, LARGE_FILE);
        final File tempFileInStore = new File(FILE_STORE, CIFEXRPCService.PREFIX + LARGE_FILE);
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingStarted(fileOnClient, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE);
                    will(returnValue(fileInFileStore));

                    one(listener).uploadingProgress(32, BLOCK_SIZE);
                    one(listener).uploadingProgress(with(equal(64)), with(new BaseMatcher<Long>()
                        {
                            public void describeTo(Description description)
                            {
                            }

                            public boolean matches(Object item)
                            {
                                if (item instanceof Long)
                                {
                                    long numberOfBytes = (Long) item;
                                    if (numberOfBytes == 2 * BLOCK_SIZE)
                                    {
                                        assertEquals(true, tempFileInStore.exists());
                                        assertEquals(numberOfBytes, tempFileInStore.length());
                                        uploader.cancel();
                                        return true;
                                    }
                                }
                                return false;
                            }
                        }));
                    one(listener).uploadingFinished(false);
                    one(listener).reset();
                }
            });

        uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", "no comment");

        assertEquals(false, fileInFileStore.exists());
        assertEquals(false, tempFileInStore.exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testUploading2FilesWhereSecondIsCanceledAndSecondUpload() throws IOException
    {
        final File fileOnClient1 = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore1 = new File(FILE_STORE, SMALL_FILE);
        final File fileOnClient2 = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore2 = new File(FILE_STORE, LARGE_FILE);
        context.checking(new Expectations()
            {
                {
                    one(listener).uploadingStarted(fileOnClient1, SMALL_FILE_SIZE * 1024L);
                    one(fileManager).createFile(user, SMALL_FILE);
                    will(returnValue(fileInFileStore1));

                    one(listener).uploadingProgress(0, 0);
                    one(listener).fileUploaded();

                    one(fileManager).registerFileLinkAndInformRecipients(user, SMALL_FILE,
                            "no comment", "application/octet-stream", fileInFileStore1,
                            new String[]
                                { "Albert", "Galileo" }, TEST_URL);
                    will(returnValue(Collections.emptyList()));

                    one(listener).uploadingStarted(fileOnClient2, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE);
                    will(returnValue(fileInFileStore2));

                    one(listener).uploadingProgress(32, BLOCK_SIZE);
                    one(listener).uploadingProgress(with(equal(64)), with(new BaseMatcher<Long>()
                        {
                            public void describeTo(Description description)
                            {
                            }

                            public boolean matches(Object item)
                            {
                                if (item instanceof Long)
                                {
                                    long numberOfBytes = (Long) item;
                                    if (numberOfBytes == 2 * BLOCK_SIZE)
                                    {
                                        uploader.cancel();
                                        return true;
                                    }
                                }
                                return false;
                            }
                        }));
                    one(listener).uploadingFinished(false);
                    one(listener).reset();

                    one(listener).uploadingStarted(fileOnClient2, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE);
                    will(returnValue(fileInFileStore2));

                    one(listener).uploadingProgress(0, 0);
                    one(listener).uploadingProgress(32, BLOCK_SIZE);
                    one(listener).uploadingProgress(64, 2 * BLOCK_SIZE);
                    one(listener).uploadingProgress(96, 3 * BLOCK_SIZE);
                    one(listener).uploadingFinished(true);
                    one(listener).reset();

                    one(fileManager).registerFileLinkAndInformRecipients(user, LARGE_FILE,
                            "2. try", "application/octet-stream", fileInFileStore2, new String[0],
                            TEST_URL);
                    will(returnValue(Collections.emptyList()));
                }
            });

        uploader.upload(Arrays.asList(fileOnClient1, fileOnClient2), "Albert\nGalileo",
                "no comment");

        assertEqualContent(fileOnClient1, fileInFileStore1);
        assertEquals(false, fileInFileStore2.exists());

        uploader.upload(Arrays.asList(fileOnClient1, fileOnClient2), "", "2. try");

        assertEqualContent(fileOnClient2, fileInFileStore2);
        context.assertIsSatisfied();
    }

    private void assertEqualContent(File fileWithExpectedContent, File fileWithActualContent)
            throws IOException
    {
        long length = fileWithExpectedContent.length();
        assertEquals(length, fileWithActualContent.length());
        FileInputStream streamWithExpectedContent = null;
        FileInputStream streamWithActualContent = null;
        try
        {
            byte[] expectedBytes = new byte[1024];
            byte[] actualBytes = new byte[1024];
            streamWithExpectedContent = new FileInputStream(fileWithExpectedContent);
            streamWithActualContent = new FileInputStream(fileWithActualContent);
            int n = 0;
            do
            {
                String msg = "filepointer at " + n;
                n += streamWithExpectedContent.read(expectedBytes);
                streamWithActualContent.read(actualBytes);
                assertEquals(msg, expectedBytes, actualBytes);
            } while (n < length);
        } finally
        {
            IOUtils.closeQuietly(streamWithExpectedContent);
            IOUtils.closeQuietly(streamWithActualContent);
        }

    }

}
