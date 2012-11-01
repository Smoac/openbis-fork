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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.rpc.client.CIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.FileWithOverrideName;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXUploader;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.rpc.io.CopyUtils;
import ch.systemsx.cisd.cifex.rpc.server.CIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.server.SessionManager;
import ch.systemsx.cisd.cifex.server.business.FileInformation;
import ch.systemsx.cisd.cifex.server.business.IBusinessContext;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;

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

    private ICIFEXUploader uploader;

    private IProgressListener listener;

    private IDomainModel domainModel;

    private IBusinessContext businessContext;

    private IUserActionLog userActionLog;

    private IRequestContextProvider requestContextProvider;

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
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    allowing(userActionLog).getUserHostSessionDescription();
                    will(returnValue("userHostSessionDescr"));
                }
            });
        requestContextProvider = context.mock(IRequestContextProvider.class);
        uploadService =
                new CIFEXRPCService(fileManager, domainModel, requestContextProvider,
                        userActionLog, null, new SessionManager(null, null, "false"), 60000L, 10,
                        "false");
        user = new UserDTO();
        user.setID(42L);
        user.setUserCode("Isaac");
        sessionID = uploadService.createSession(user, TEST_URL);
        uploader = new CIFEXComponent(uploadService).createUploader(sessionID);
        listener = context.mock(IProgressListener.class);
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

    private static class CopyFileAction extends CustomAction
    {

        private final File fileInFileStore;

        private final FileDTO fileDTO;

        private final boolean resume;

        CopyFileAction(final File fileInFileStore, final FileDTO fileDTO, final boolean resume)
        {
            super("copy file");
            this.fileInFileStore = fileInFileStore;
            this.fileDTO = fileDTO;
            this.resume = resume;
        }

        @Override
        public Object invoke(Invocation invocation) throws Throwable
        {
            try
            {
                final long startPos = resume ? (Long) invocation.getParameter(4) : 0L;
                final InputStream input = (InputStream) invocation.getParameter(5);
                final OutputStream output = new FileOutputStream(fileInFileStore, resume);
                final int crc32Client =
                        CopyUtils.copyAndReturnChecksum(input, output, fileDTO.getCompleteSize(),
                                startPos);
                output.close();
                input.close();
                final int crc32Server = (int) FileUtils.checksumCRC32(fileInFileStore);
                assertEquals(String.format("s: %x, c: %x", crc32Server, crc32Client), crc32Server,
                        crc32Client);
                return resume ? null : fileDTO;
            } catch (Exception ex)
            {
                ex.printStackTrace();
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

    }

    @Test
    public void testNoFile() throws IOException
    {
        uploader.upload(Arrays.<FileWithOverrideName> asList(), "Albert\nGalileo", COMMENT);

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
                    allowing(businessContext).getUserActionLogHttp();
                    will(returnValue(null));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(SMALL_FILE, null, 0L);
                    one(userActionLog).logUploadFileFinished(SMALL_FILE, fileDTO, true);
                    one(listener).start(fileOnClient, "Uploading", SMALL_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(SMALL_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(SMALL_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore, fileDTO, false));

                    one(fileManager).getFileInformation(SMALL_FILE_ID);
                    will(returnValue(new FileInformation(SMALL_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)),
                            with(any((IUserActionLog.class))));
                    will(returnValue(Collections.emptyList()));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);
                    finishedSuccessful(this);
                }
            });

        uploader.upload(Arrays.asList(new FileWithOverrideName(fileOnClient, null)),
                "Albert\nGalileo", COMMENT);

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    private void finishedSuccessful(Expectations exp)
    {
        exp.one(listener).finished(true);

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
                    allowing(businessContext).getUserActionLogHttp();
                    will(returnValue(null));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(LARGE_FILE, null, 0L);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, fileDTO, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    will(returnValue(fileDTOPartial));
                    one(listener).start(fileOnClient, "Uploading", LARGE_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(LARGE_FILE)),
                            with(equal(COMMENT)), with(equal("application/octet-stream")),
                            with(equal(LARGE_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore, fileDTO, false));

                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)),
                            with(any(IUserActionLog.class)));
                    will(returnValue(Collections.emptyList()));
                }
            });
        addRegularProgressExpectations();

        uploader.upload(wrapFiles(Arrays.asList(fileOnClient)), "Albert\nGalileo", COMMENT);

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
                    allowing(businessContext).getUserActionLogHttp();
                    will(returnValue(null));
                    one(userActionLog).logUploadFileStart(LARGE_FILE, fileDTO, 2 * BLOCK_SIZE);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, fileDTO, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    will(returnValue(fileDTOPartial));
                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTOPartial,
                            fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTOPartial);
                    will(returnValue(true));
                    one(listener).start(fileOnClient, "Uploading", LARGE_FILE_SIZE * 1024L, null);
                    one(fileManager).resumeSaveFile(with(equal(user)), with(equal(fileDTOPartial)),
                            with(equal(fileInFileStore)), with(equal(COMMENT)),
                            with(equal(2L * BLOCK_SIZE)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore, fileDTO, true));

                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)),
                            with(any(IUserActionLog.class)));
                    will(returnValue(Collections.emptyList()));
                }
            });
        addRegularProgressExpectationsExceptFirstTwoBlocks();

        uploader.upload(wrapFiles(Arrays.asList(fileOnClient)), "Albert\nGalileo", COMMENT);

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    private List<FileWithOverrideName> wrapFiles(List<File> files)
    {
        final List<FileWithOverrideName> filesWithOverrideName =
                new ArrayList<FileWithOverrideName>(files.size());
        for (File file : files)
        {
            filesWithOverrideName.add(new FileWithOverrideName(file, null));
        }
        return filesWithOverrideName;
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
                    one(listener).reportProgress(0, 0L);
                    one(listener).reportProgress(100, LARGE_FILE_INFO.getFileSize());
                    finishedSuccessful(this);
                }
            });
    }

    private void addRegularProgressExpectationsExceptFirstTwoBlocks()
    {
        context.checking(new Expectations()
            {
                {
                    one(listener).reportProgress(12, 2 * BLOCK_SIZE);
                    one(listener).reportProgress(100, LARGE_FILE_INFO.getFileSize());
                    finishedSuccessful(this);
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
                    allowing(businessContext).getUserActionLogHttp();
                    will(returnValue(null));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(LARGE_FILE, null, 0L);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, fileDTO, true);
                    one(listener).start(fileOnClient, "Uploading", LARGE_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(LARGE_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(LARGE_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore, fileDTO, false));

                    one(fileManager).getFileInformation(LARGE_FILE_ID);
                    will(returnValue(new FileInformation(LARGE_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)),
                            with(any((IUserActionLog.class))));
                    will(returnValue(Collections.emptyList()));
                }
            });
        addRegularProgressExpectations();

        uploader.upload(wrapFiles(Arrays.asList(fileOnClient)), "Albert\nGalileo", COMMENT);

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
                    allowing(businessContext).getUserActionLogHttp();
                    will(returnValue(null));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    exactly(2).of(userManager).refreshQuotaInformation(user);
                    one(userActionLog).logUploadFileStart(SMALL_FILE, null, 0L);
                    one(userActionLog).logUploadFileFinished(SMALL_FILE, fileDTO1, true);
                    one(userActionLog).logUploadFileStart(LARGE_FILE, null, 0L);
                    one(userActionLog).logUploadFileFinished(LARGE_FILE, fileDTO2, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient1, "Uploading", SMALL_FILE_SIZE * 1024L, null);
                    one(fileManager).saveFile(with(equal(user)), with(equal(SMALL_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(SMALL_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore1, fileDTO1, false));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);

                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), SMALL_FILE,
                            SMALL_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient2, "Uploading", LARGE_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(LARGE_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(LARGE_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore2, fileDTO2, false));

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
                            with(equal(Arrays.asList(fileDTO1, fileDTO2))), with(equal(COMMENT)),
                            with(any(IUserActionLog.class)));
                    will(returnValue(Collections.emptyList()));
                }
            });
        addRegularProgressExpectations();

        uploader.upload(wrapFiles(Arrays.asList(fileOnClient1, fileOnClient2)), "Albert\nGalileo",
                COMMENT);

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
                    allowing(businessContext).getUserActionLogHttp();
                    will(returnValue(null));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    atMost(1).of(userActionLog).logUploadFileStart(SMALL_FILE, null, 0L);
                    atMost(1).of(userActionLog).logUploadFileFinished(SMALL_FILE, fileDTO, true);
                    one(listener).start(fileOnClient, "Uploading", SMALL_FILE_SIZE * 1024L, null);
                    one(listener).reportProgress(0, 0L);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);

                    one(fileManager).saveFile(with(equal(user)), with(equal(SMALL_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(SMALL_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore, fileDTO, false));

                    one(fileManager).getFileInformation(SMALL_FILE_ID);
                    will(returnValue(new FileInformation(SMALL_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)),
                            with(any(IUserActionLog.class)));
                    will(returnValue(Collections.singletonList("id:unknown")));

                    one(listener).exceptionOccured(with(any(UserFailureException.class)));
                    one(listener).finished(false);
                }
            });

        try
        {
            uploader.upload(wrapFiles(Arrays.asList(fileOnClient)), "Albert\nGalileo", COMMENT);
        } catch (UserFailureException e)
        {
            assertEquals("Some user identifiers are invalid: [id:unknown]", e.getMessage());
        }

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    private void prepareFailure(Expectations exp)
    {
        exp.one(listener).finished(false);
    }

    @Test
    public void testUploadingThrowingExceptionInFileManager() throws IOException
    {
        final File fileOnClient = new File(CLIENT_FOLDER, SMALL_FILE);
        final File fileInFileStore = new File(FILE_STORE, SMALL_FILE);
        final FileDTO fileDTO = createFileDTO(false);
        final Throwable exception = new RuntimeException("Oops!");
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(businessContext).getUserActionLogHttp();
                    will(returnValue(null));
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).refreshQuotaInformation(user);
                    atMost(1).of(userActionLog).logUploadFileStart(SMALL_FILE, null, 0L);
                    atMost(1).of(userActionLog).logUploadFileFinished(SMALL_FILE, fileDTO, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), SMALL_FILE,
                            SMALL_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient, "Uploading", SMALL_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(SMALL_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(SMALL_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore, fileDTO, false));

                    one(fileManager).getFileInformation(SMALL_FILE_ID);
                    will(returnValue(new FileInformation(SMALL_FILE_ID, fileDTO, fileInFileStore)));
                    one(fileManager).isControlling(user, fileDTO);
                    will(returnValue(true));
                    one(fileManager).shareFilesWith(with(equal(TEST_URL)), with(equal(user)),
                            with(equal(Arrays.asList("Albert", "Galileo"))),
                            with(singleFileDTO(fileDTO)), with(equal(COMMENT)),
                            with(any((IUserActionLog.class))));
                    will(throwException(exception));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);

                    one(listener).exceptionOccured(with(any(RuntimeException.class)));
                    one(listener).finished(false);
                }
            });

        try
        {
            uploader.upload(wrapFiles(Arrays.asList(fileOnClient)), "Albert\nGalileo", COMMENT);
        } catch (RuntimeException ex)
        {
            assertEquals("Oops!", ex.getMessage());
        }

        assertEqualContent(fileOnClient, fileInFileStore);
        context.assertIsSatisfied();
    }

    @Test(invocationCount = 10, successPercentage = 10, sequential = true)
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
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient, "Uploading", LARGE_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(LARGE_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(LARGE_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CustomAction("copy file")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                try
                                {
                                    final InputStream input =
                                            (InputStream) invocation.getParameter(5);
                                    final OutputStream output =
                                            new FileOutputStream(fileInFileStore);
                                    final int crc32Client =
                                            CopyUtils.copyAndReturnChecksum(input, output,
                                                    LARGE_FILE_SIZE * 1024L, 0L);
                                    output.close();
                                    input.close();
                                    final int crc32Server =
                                            (int) FileUtils.checksumCRC32(fileInFileStore);
                                    assertEquals(crc32Server, crc32Client);
                                    return fileDTO;
                                } catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                                }
                            }
                        });

                    one(listener).reportProgress(0, 0L);
                    will(new CustomAction("cancel")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                uploader.cancel();
                                return null;
                            }
                        });
                    one(userActionLog).logUploadFileStart(LARGE_FILE, null, 0L);
                    atMost(1).of(userActionLog).logUploadFileFinished(LARGE_FILE, null, false);
                    prepareFailure(this);
                }
            });

        uploader.upload(wrapFiles(Arrays.asList(fileOnClient)), "Albert\nGalileo", COMMENT);

        context.assertIsSatisfied();
    }

    @Test(invocationCount = 10, successPercentage = 10, sequential = true)
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
                    allowing(userActionLog).logUploadFileStart(SMALL_FILE, null, 0L);
                    allowing(userActionLog).logUploadFileFinished(SMALL_FILE, fileDTO1, true);
                    allowing(userActionLog).logUploadFileStart(LARGE_FILE, null, 0L);
                    allowing(userActionLog).logUploadFileFinished(LARGE_FILE, null, false);
                    // Note: we don't do resume in this test, see below.
                    allowing(userActionLog).logUploadFileStart(LARGE_FILE, null, 0L);
                    allowing(userActionLog).logUploadFileFinished(LARGE_FILE, fileDTO2, true);
                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), SMALL_FILE,
                            SMALL_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient1, "Uploading", SMALL_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(SMALL_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(SMALL_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore1, fileDTO1, false));

                    one(listener).reportProgress(0, 0);
                    one(listener).reportProgress(100, SMALL_FILE_SIZE * 1024L);

                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    // That means: no suitable file to resume uploading!
                    will(returnValue(null));
                    one(listener).start(fileOnClient2, "Uploading", LARGE_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(LARGE_FILE)),
                            with(equal(COMMENT)), with(any(String.class)),
                            with(equal(LARGE_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore2, fileDTO2, false));

                    one(listener).reportProgress(0, 0);
                    will(new CustomAction("cancel")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                uploader.cancel();
                                return null;
                            }
                        });
                    prepareFailure(this);

                    one(fileManager).tryGetUploadResumeCandidate(user.getID(), LARGE_FILE,
                            LARGE_FILE_SIZE * 1024L);
                    one(listener).start(fileOnClient2, "Uploading", LARGE_FILE_SIZE * 1024L, null);

                    one(fileManager).saveFile(with(equal(user)), with(equal(LARGE_FILE)),
                            with(equal("2. try")), with(any(String.class)),
                            with(equal(LARGE_FILE_SIZE * 1024L)), with(any(InputStream.class)));
                    will(new CopyFileAction(fileInFileStore2, fileDTO2, false));
                    allowing(listener).reportProgress(with(any(int.class)), with(any(long.class)));
                    finishedSuccessful(this);
                }
            });

        uploader.upload(wrapFiles(Arrays.asList(fileOnClient1, fileOnClient2)), "Albert\nGalileo",
                COMMENT);

        assertEqualContent(fileOnClient1, fileInFileStore1);
        assertEquals(true, fileInFileStore2.exists());

        uploader.upload(wrapFiles(Arrays.asList(fileOnClient2)), " ", "2. try");

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
                @Override
                @SuppressWarnings("unchecked")
                public boolean matches(Object item)
                {
                    Collection<FileDTO> col = (Collection<FileDTO>) item;
                    return col.size() == 1 && col.iterator().next().equals(fileDTO);
                }

                @Override
                public void describeTo(Description description)
                {
                    description.appendText("single unchanged fileDTO");
                }
            };
    }

}
