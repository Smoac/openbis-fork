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

package ch.systemsx.cisd.cifex.rpc.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.zip.CRC32;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.rpc.client.Uploader;
import ch.systemsx.cisd.cifex.rpc.client.gui.IUploadProgressListener;
import ch.systemsx.cisd.cifex.rpc.server.CIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.server.SessionManager;
import ch.systemsx.cisd.cifex.server.business.FileInformation;
import ch.systemsx.cisd.cifex.server.business.IBusinessContext;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.PreCreatedFileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class UploadingIntegrationTest extends AssertJUnit
{
    private static final int BLOCK_SIZE_KB = 256;

    private static final int BLOCK_SIZE = BLOCK_SIZE_KB * 1024;

    private static final String TEST_URL = "test-url";

    private static final File PLAYGROUND =
            new File(System.getProperty("user.dir"), "targets/playground");

    private static final File CLIENT_FOLDER = new File(PLAYGROUND, "clientFolder");

    private static final File FILE_STORE = new File(PLAYGROUND, "file-store");

    private static final String SMALL_FILE = "small-file";

    private static final long SMALL_FILE_SIZE = 10;

    private static final long SMALL_FILE_ID = 17L;

    private static final FilePreregistrationDTO SMALL_FILE_INFO =
            new FilePreregistrationDTO(new File(CLIENT_FOLDER, SMALL_FILE).getAbsolutePath(),
                    SMALL_FILE_SIZE * 1024L);

    private static final String LARGE_FILE = "large-file";

    private static final long LARGE_FILE_SIZE = 4000;

    private static final long LARGE_FILE_ID = 18L;

    private static int LARGE_FILE_FIRST_TWO_BLOCKS_CRC32;

    private static final FilePreregistrationDTO LARGE_FILE_INFO =
            new FilePreregistrationDTO(new File(CLIENT_FOLDER, LARGE_FILE).getAbsolutePath(),
                    LARGE_FILE_SIZE * 1024L);

    private static String COMMENT = "no comment";

    private static void createRandomData(File file, long sizeInKB, CRC32 crc32OrNull, int maxKbCRC32)
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
                if (crc32OrNull != null && i < maxKbCRC32)
                {
                    crc32OrNull.update(bytes);
                }
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
    
    private IUserManager userManager;

    private CIFEXRPCService uploadService;

    private String sessionID;

    private Uploader uploader;

    private IUploadProgressListener listener;

    private IDomainModel domainModel;

    private IBusinessContext businessContext;

    private IUserActionLog userActionLog;

    private UserDTO user;

    @BeforeMethod
    public void setUp() throws IOException
    {
        context = new Mockery();
        fileManager = context.mock(IFileManager.class);
        userManager = context.mock(IUserManager.class);
        domainModel = context.mock(IDomainModel.class);
        businessContext = context.mock(IBusinessContext.class);
        userActionLog = context.mock(IUserActionLog.class);
        uploadService =
                new CIFEXRPCService(fileManager, domainModel, null, userActionLog, null,
                        new SessionManager(null, null, "false"), 60000L, 10, "false");
        user = new UserDTO();
        user.setID(42L);
        user.setUserCode("Isaac");
        sessionID = uploadService.createSession(user, TEST_URL);
        uploader = new Uploader(uploadService, sessionID);
        listener = context.mock(IUploadProgressListener.class);
        uploader.addProgressListener(listener);

        FileUtilities.deleteRecursively(PLAYGROUND);
        CLIENT_FOLDER.mkdirs();
        FILE_STORE.mkdirs();
        final File smallFile = new File(CLIENT_FOLDER, SMALL_FILE);
        createRandomData(smallFile, SMALL_FILE_SIZE, null, 0);
        final File largeFile = new File(CLIENT_FOLDER, LARGE_FILE);
        final CRC32 crc32FirstBlock = new CRC32();
        createRandomData(largeFile, LARGE_FILE_SIZE, crc32FirstBlock, 2 * BLOCK_SIZE_KB);
        LARGE_FILE_FIRST_TWO_BLOCKS_CRC32 = (int) crc32FirstBlock.getValue();
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
        uploader.upload(Arrays.<File> asList(), "Albert\nGalileo", COMMENT);

        context.assertIsSatisfied();
    }

    @Test
    public void testSingleSmallFile() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore = new File(FILE_STORE, SMALL_FILE);
        final FileDTO fileDTO = createFileDTO(false);
        context.checking(new Expectations()
            {
                {
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), SMALL_FILE,
                            SMALL_FILE_SIZE * 1024L);
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(SMALL_FILE, true);
                    one(userActionLog).logUploadFileFinished(SMALL_FILE, true);
                    one(listener).start(fileOnClient, SMALL_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, SMALL_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore, fileDTO)));

                    one(fileManager).getFileInformation(SMALL_FILE_ID);
                    will(returnValue(new FileInformation(SMALL_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)));
                    will(returnValue(Collections.emptyList()));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);
                    one(listener).fileUploaded();
                    one(listener).finished(true);
                    one(listener).reset();
                }
            });
        addUpdateUploadProgress(SMALL_FILE_INFO.getFileSize(), SMALL_FILE_INFO.getFileSize());

        uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", COMMENT);

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    @Test
    public void testNoResumeWrongChecksum() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore = new File(FILE_STORE, LARGE_FILE);
        final FileDTO fileDTO = createFileDTO(true);
        final FileDTO fileDTOPartial = createFileDTO(true);
        fileDTOPartial.setSize(BLOCK_SIZE);
        fileDTOPartial.setCrc32Value(0);
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(LARGE_FILE, true);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    will(returnValue(fileDTOPartial));
                    one(listener).start(fileOnClient, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore, fileDTO)));

                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)));
                    will(returnValue(Collections.emptyList()));
                }
            });
        addRegularProgressExpectations();
        addUpdateUploadProgress(LARGE_FILE_INFO.getFileSize(), LARGE_FILE_INFO.getFileSize());

        uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", COMMENT);

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    @Test
    public void testResume() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore = new File(FILE_STORE, LARGE_FILE);
        copyBlocks(fileOnClient, fileInFileStore, 2);
        final FileDTO fileDTO = createFileDTO(true);
        final FileDTO fileDTOPartial = createFileDTO(true);
        fileDTOPartial.setSize(2 * BLOCK_SIZE);
        fileDTOPartial.setCrc32Value(LARGE_FILE_FIRST_TWO_BLOCKS_CRC32);
        fileDTOPartial.setName(LARGE_FILE);
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    one(userActionLog).logUploadFileStart(LARGE_FILE, true);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    will(returnValue(fileDTOPartial));
                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTOPartial,
                            fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTOPartial);
                    will(returnValue(true));
                    one(listener).start(fileOnClient, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)));
                    will(returnValue(Collections.emptyList()));
                }
            });
        addRegularProgressExpectationsExceptFirstTwoBlocks();
        addUpdateUploadProgress(LARGE_FILE_INFO.getFileSize(), 3 * BLOCK_SIZE, LARGE_FILE_INFO
                .getFileSize());

        uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", COMMENT);

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    private void copyBlocks(File f1, File f2, int numberOfBlocks) throws IOException
    {
        RandomAccessFile raf1 = new RandomAccessFile(f1, "r");
        try
        {
            RandomAccessFile raf2 = new RandomAccessFile(f2, "rw");
            try
            {
                final byte[] bytes = new byte[BLOCK_SIZE];
                for (int i = 0; i < numberOfBlocks; ++i)
                {
                    raf1.readFully(bytes);
                    raf2.write(bytes);
                }
            } finally
            {
                raf2.close();
            }

        } finally
        {
            raf1.close();
        }
    }

    private void addRegularProgressExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(6, BLOCK_SIZE);
                    one(listener).reportProgress(12, 2 * BLOCK_SIZE);
                    one(listener).reportProgress(19, 3 * BLOCK_SIZE);
                    one(listener).reportProgress(25, 4 * BLOCK_SIZE);
                    one(listener).reportProgress(32, 5 * BLOCK_SIZE);
                    one(listener).reportProgress(38, 6 * BLOCK_SIZE);
                    one(listener).reportProgress(44, 7 * BLOCK_SIZE);
                    one(listener).reportProgress(51, 8 * BLOCK_SIZE);
                    one(listener).reportProgress(57, 9 * BLOCK_SIZE);
                    one(listener).reportProgress(64, 10 * BLOCK_SIZE);
                    one(listener).reportProgress(70, 11 * BLOCK_SIZE);
                    one(listener).reportProgress(76, 12 * BLOCK_SIZE);
                    one(listener).reportProgress(83, 13 * BLOCK_SIZE);
                    one(listener).reportProgress(89, 14 * BLOCK_SIZE);
                    one(listener).reportProgress(96, 15 * BLOCK_SIZE);
                    one(listener).reportProgress(100, LARGE_FILE_INFO.getFileSize());
                    one(listener).fileUploaded();
                    one(listener).finished(true);
                    one(listener).reset();
                }
            });
    }

    private void addRegularProgressExpectationsExceptFirstTwoBlocks()
    {
        context.checking(new Expectations()
            {
                {
                    one(listener).reportProgress(12, 2 * BLOCK_SIZE);
                    one(listener).reportProgress(19, 3 * BLOCK_SIZE);
                    one(listener).reportProgress(25, 4 * BLOCK_SIZE);
                    one(listener).reportProgress(32, 5 * BLOCK_SIZE);
                    one(listener).reportProgress(38, 6 * BLOCK_SIZE);
                    one(listener).reportProgress(44, 7 * BLOCK_SIZE);
                    one(listener).reportProgress(51, 8 * BLOCK_SIZE);
                    one(listener).reportProgress(57, 9 * BLOCK_SIZE);
                    one(listener).reportProgress(64, 10 * BLOCK_SIZE);
                    one(listener).reportProgress(70, 11 * BLOCK_SIZE);
                    one(listener).reportProgress(76, 12 * BLOCK_SIZE);
                    one(listener).reportProgress(83, 13 * BLOCK_SIZE);
                    one(listener).reportProgress(89, 14 * BLOCK_SIZE);
                    one(listener).reportProgress(96, 15 * BLOCK_SIZE);
                    one(listener).reportProgress(100, LARGE_FILE_INFO.getFileSize());
                    one(listener).fileUploaded();
                    one(listener).finished(true);
                    one(listener).reset();
                }
            });
    }

    private void addUpdateUploadProgress(final long completeSize, final long maxSize)
    {
        addUpdateUploadProgress(completeSize, -1L, maxSize);
    }

    private void addUpdateUploadProgress(final long completeSize, final long minSize,
            final long maxSize)
    {
        context.checking(new Expectations()
            {
                {
                    final long[] size = new long[1];
                    size[0] = minSize < 0 ? (BLOCK_SIZE > maxSize ? maxSize : BLOCK_SIZE) : minSize;
                    while (true)
                    {
                        one(fileManager).updateUploadProgress(with(new BaseMatcher<FileDTO>()
                            {
                                final long currentSize = size[0];

                                public boolean matches(Object item)
                                {
                                    final FileDTO fileDTO = (FileDTO) item;
                                    return fileDTO.getCompleteSize() == completeSize
                                            && fileDTO.getSize() == currentSize;
                                }

                                public void describeTo(Description description)
                                {
                                    description.appendText("fileDTO<completeSize=" + completeSize
                                            + ", size=" + currentSize + ">");
                                }
                            }));
                        if (size[0] == maxSize)
                        {
                            break;
                        }
                        size[0] += BLOCK_SIZE;
                        if (size[0] > maxSize)
                        {
                            size[0] = maxSize;
                        }
                    }
                }
            });

    }

    @Test
    public void testSingleLargeFile() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore = new File(FILE_STORE, LARGE_FILE);
        final FileDTO fileDTO = createFileDTO(true);
        context.checking(new Expectations()
            {
                {
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(LARGE_FILE, true);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, true);
                    one(listener).start(fileOnClient, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore, fileDTO)));

                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)));
                    will(returnValue(Collections.emptyList()));
                }
            });
        addRegularProgressExpectations();
        addUpdateUploadProgress(LARGE_FILE_INFO.getFileSize(), LARGE_FILE_INFO.getFileSize());

        uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", COMMENT);

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testSharingInsufficientPrivileges() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore = new File(FILE_STORE, LARGE_FILE);
        final FileDTO fileDTO = createFileDTO(true);
        context.checking(new Expectations()
            {
                {
                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(false));
                    one(userActionLog).logShareFilesAuthorizationFailure(
                            Collections.singletonList(fileDTO), Arrays.asList("Albert", "Galileo"));
                }
            });

        uploadService.shareFiles(sessionID, Collections.singletonList(LARGE_FILE_ID),
                "Albert\nGalileo");

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    private FileDTO createFileDTO(boolean large)
    {
        final FileDTO fileDTO = new FileDTO();
        fileDTO.setSize(0L);
        fileDTO.setCompleteSize(large ? LARGE_FILE_INFO.getFileSize() : SMALL_FILE_INFO
                .getFileSize());
        fileDTO.setID(large ? LARGE_FILE_ID : SMALL_FILE_ID);
        fileDTO.setComment(COMMENT);
        return fileDTO;
    }

    @Test
    public void testTwoFiles() throws IOException
    {
        final File fileOnClient1 = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore1 = new File(FILE_STORE, SMALL_FILE);
        final FileDTO fileDTO1 = createFileDTO(false);
        final File fileOnClient2 = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore2 = new File(FILE_STORE, LARGE_FILE);
        final FileDTO fileDTO2 = createFileDTO(true);
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    exactly(2).of(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(SMALL_FILE, true);
                    one(userActionLog).logUploadFileFinished(SMALL_FILE, true);
                    one(userActionLog).logUploadFileStart(LARGE_FILE, true);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient1, SMALL_FILE_SIZE * 1024L);
                    one(fileManager).createFile(user, SMALL_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore1, fileDTO1)));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);
                    one(listener).fileUploaded();

                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), SMALL_FILE,
                            SMALL_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient2, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore2, fileDTO2)));

                    one(fileManager).getFileInformation(SMALL_FILE_ID);
                    will(returnValue(new FileInformation(SMALL_FILE_ID, fileDTO1, fileInFileStore1)));
                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTO2, fileInFileStore2)));
                    one(fileManager).isControlling(user, fileDTO1);
                    will(returnValue(true));
                    one(fileManager).isControlling(user, fileDTO2);
                    will(returnValue(true));

                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(equal(Arrays.asList(fileDTO1, fileDTO2))), with(equal(COMMENT)));
                    will(returnValue(Collections.emptyList()));
                }
            });
        addRegularProgressExpectations();
        addUpdateUploadProgress(SMALL_FILE_INFO.getFileSize(), SMALL_FILE_INFO.getFileSize());
        addUpdateUploadProgress(LARGE_FILE_INFO.getFileSize(), LARGE_FILE_INFO.getFileSize());

        uploader.upload(Arrays.asList(fileOnClient1, fileOnClient2), "Albert\nGalileo", COMMENT);

        assertEqualContent(fileOnClient1, fileInFileStore1);
        assertEqualContent(fileOnClient2, fileInFileStore2);
        context.assertIsSatisfied();
    }

    @Test
    public void testUploadingForUnknownUser() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore = new File(FILE_STORE, SMALL_FILE);
        final FileDTO fileDTO = createFileDTO(false);
        context.checking(new Expectations()
            {
                {
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), SMALL_FILE,
                            SMALL_FILE_SIZE * 1024L);
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(SMALL_FILE, true);
                    one(userActionLog).logUploadFileFinished(SMALL_FILE, true);
                    one(listener).start(fileOnClient, SMALL_FILE_SIZE * 1024L);
                    one(listener).reportProgress(0, 0L);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);
                    one(listener).fileUploaded();

                    one(fileManager).createFile(user, SMALL_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore, fileDTO)));

                    one(fileManager).getFileInformation(SMALL_FILE_ID);
                    will(returnValue(new FileInformation(SMALL_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)));
                    will(returnValue(Collections.singletonList("id:unknown")));

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

                    one(listener).finished(false);
                    one(listener).reset();
                }
            });
        addUpdateUploadProgress(SMALL_FILE_INFO.getFileSize(), SMALL_FILE_INFO.getFileSize());

        try
        {
            uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", COMMENT);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
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
        final FileDTO fileDTO = createFileDTO(false);
        final RuntimeException exception = new RuntimeException("Oops!");
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(SMALL_FILE, true);
                    one(userActionLog).logUploadFileFinished(SMALL_FILE, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), SMALL_FILE,
                            SMALL_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient, SMALL_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, SMALL_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore, fileDTO)));

                    one(fileManager).getFileInformation(SMALL_FILE_ID);
                    will(returnValue(new FileInformation(SMALL_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)));
                    will(throwException(exception));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);
                    one(listener).fileUploaded();

                    one(listener).exceptionOccured(exception);

                    one(listener).finished(false);
                    one(listener).reset();
                }
            });
        addUpdateUploadProgress(SMALL_FILE_INFO.getFileSize(), SMALL_FILE_INFO.getFileSize());

        try
        {
            uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", COMMENT);
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
        final FileDTO fileDTO = createFileDTO(true);
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(LARGE_FILE, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore, fileDTO)));

                    one(listener).reportProgress(0, 0L);
                    one(listener).reportProgress(6, BLOCK_SIZE);
                    one(listener).reportProgress(with(equal(12)), with(new BaseMatcher<Long>()
                        {
                            public void describeTo(Description description)
                            {
                                description.appendText("two block sizes");
                            }

                            public boolean matches(Object item)
                            {
                                if (item instanceof Long)
                                {
                                    long numberOfBytes = (Long) item;
                                    if (numberOfBytes == 2 * BLOCK_SIZE)
                                    {
                                        assertEquals(true, fileInFileStore.exists());
                                        assertEquals(numberOfBytes, fileInFileStore.length());
                                        uploader.cancel();
                                        return true;
                                    }
                                }
                                return false;
                            }
                        }));
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, false);
                    one(listener).finished(false);
                    one(listener).reset();
                }
            });
        addUpdateUploadProgress(LARGE_FILE_INFO.getFileSize(), 2 * BLOCK_SIZE);

        uploader.upload(Arrays.asList(fileOnClient), "Albert\nGalileo", COMMENT);

        context.assertIsSatisfied();
    }

    @Test
    public void testUploading2FilesWhereSecondIsCanceledAndSecondUpload() throws IOException
    {
        final File fileOnClient1 = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore1 = new File(FILE_STORE, SMALL_FILE);
        final FileDTO fileDTO1 = createFileDTO(false);
        final File fileOnClient2 = new File(CLIENT_FOLDER, LARGE_FILE);
        final File fileInFileStore2 = new File(FILE_STORE, LARGE_FILE);
        final FileDTO fileDTO2 = createFileDTO(true);
        fileDTO2.setPath(fileInFileStore2.getPath());
        final FileDTO fileDTO2NewComment = createFileDTO(true);
        fileDTO2NewComment.setPath(fileInFileStore2.getPath());
        fileDTO2NewComment.setComment("2. try.");
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    exactly(3).of(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(SMALL_FILE, true);
                    one(userActionLog).logUploadFileFinished(SMALL_FILE, true);
                    exactly(2).of(userActionLog).logUploadFileStart(LARGE_FILE, true);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, false);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), SMALL_FILE,
                            SMALL_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient1, SMALL_FILE_SIZE * 1024L);
                    one(fileManager).createFile(user, SMALL_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore1, fileDTO1)));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);
                    one(listener).fileUploaded();

                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient2, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE_INFO, COMMENT);
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore2, fileDTO2)));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(6, BLOCK_SIZE);
                    one(listener).reportProgress(with(equal(12)), with(new BaseMatcher<Long>()
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
                    one(listener).finished(false);
                    one(listener).reset();

                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient2, LARGE_FILE_SIZE * 1024L);

                    one(fileManager).createFile(user, LARGE_FILE_INFO, "2. try");
                    will(returnValue(new PreCreatedFileDTO(fileInFileStore2, fileDTO2)));
                }
            });
        addRegularProgressExpectations();
        addUpdateUploadProgress(SMALL_FILE_INFO.getFileSize(), SMALL_FILE_INFO.getFileSize());
        addUpdateUploadProgress(LARGE_FILE_INFO.getFileSize(), 2 * BLOCK_SIZE);
        addUpdateUploadProgress(LARGE_FILE_INFO.getFileSize(), LARGE_FILE_INFO.getFileSize());

        uploader.upload(Arrays.asList(fileOnClient1, fileOnClient2), "Albert\nGalileo", COMMENT);

        assertEqualContent(fileOnClient1, fileInFileStore1);
        assertEquals(true, fileInFileStore2.exists());

        uploader.upload(Arrays.asList(fileOnClient2), " ", "2. try");

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

    private BaseMatcher<Collection<FileDTO>> singleFileDTO(final FileDTO fileDTO)
    {
        return new BaseMatcher<Collection<FileDTO>>()
            {
                @SuppressWarnings("unchecked")
                public boolean matches(Object item)
                {
                    Collection<FileDTO> col = (Collection<FileDTO>) item;
                    return col.size() == 1 && col.iterator().next().equals(fileDTO);
                }

                public void describeTo(Description description)
                {
                    description.appendText("single unchanged fileDTO");
                }
            };
    }

}
