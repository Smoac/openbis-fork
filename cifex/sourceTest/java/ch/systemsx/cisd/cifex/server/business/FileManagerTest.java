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

package ch.systemsx.cisd.cifex.server.business;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.filesystem.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PasswordGenerator;

/**
 * Test cases for corresponding {@link FileManager} class.
 * 
 * @author Izabela Adamczyk
 */
public class FileManagerTest extends AbstractFileSystemTestCase
{

    private static final long DEFAULT_FILE_ID = 1L;

    private static final String NONEXISTENT_PATH = "nonexistentFilePath";

    private Mockery context;

    private IDAOFactory daoFactory;

    private IFileDAO fileDAO;

    private IUserDAO userDAO;

    private IMailClient mailClient;

    private IFileManager fileManager;

    private File fileStore;

    private IBusinessObjectFactory boFactory;

    private UserDTO userAlice;

    private FileDTO imageFile;

    private BusinessContext businessContext;

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        userAlice = createSampleUserDTO(1L, "alice@users.com");
        imageFile = cerateSampleFileDTO(1L, userAlice, "image.jpg", "image");
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        fileDAO = context.mock(IFileDAO.class);
        userDAO = context.mock(IUserDAO.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        fileStore = workingDirectory;
        businessContext = new BusinessContext();
        businessContext.setFileRetention(5);
        businessContext.setFileStore(fileStore);
        businessContext.setPasswordGenerator(new PasswordGenerator()
            {

                //
                // PasswordGenerator
                //

                @Override
                public final String generatePassword(final int length)
                {
                    return "newpasswd";
                }

            });
        businessContext.setUserActionLog(new DummyUserActionLog());
        mailClient = context.mock(IMailClient.class);
        businessContext.setMailClient(mailClient);
        fileManager = new FileManager(daoFactory, boFactory, businessContext);

    }

    @AfterMethod
    public final void tearDown()
    {
        final File userFolder = new File(fileStore, userAlice.getEmail());
        deleteFileRecursive(userFolder);
        context.assertIsSatisfied();
    }

    private final void deleteFileRecursive(final File file)
    {
        if (file.isDirectory())
        {
            final File[] files = file.listFiles();
            for (final File f : files)
            {
                deleteFileRecursive(f);
            }
        }
        file.delete();
    }

    @Test
    public final void testDeleteExpiredFiles()
    {
        final List<FileDTO> fileDTOs = new ArrayList<FileDTO>();
        fileDTOs.add(imageFile);
        final File realFile = createRealFile(imageFile.getPath());
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).getExpiredFiles();
                    will(returnValue(fileDTOs));
                    one(fileDAO).deleteFile(imageFile.getID());
                }
            });

        fileManager.deleteExpiredFiles();
        assertFalse(realFile.exists());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegistratorIsAllowedAccessAndDeletion()
    {
        final long userId = 17L;
        final UserDTO user = new UserDTO();
        user.setID(userId);
        final FileDTO file = new FileDTO(userId);
        assertTrue(fileManager.isAllowedDeletion(user, file));
        assertTrue(fileManager.isAllowedAccess(user, file));
    }

    @Test
    public final void testAdminIsAllowedAccessAndDeletion()
    {
        final long adminId = 42L;
        final long userId = 17L;
        final UserDTO admin = new UserDTO();
        admin.setID(adminId);
        admin.setAdmin(true);
        final FileDTO file = new FileDTO(userId);
        assertTrue(fileManager.isAllowedDeletion(admin, file));
        assertTrue(fileManager.isAllowedAccess(admin, file));
    }

    @Test
    public final void testSharingUserIsAllowedAccessButNotDeletion()
    {
        final long sharingUserId = 1L;
        final long registratorIdId = 17L;
        final UserDTO sharingUser = new UserDTO();
        sharingUser.setID(sharingUserId);
        final FileDTO file = new FileDTO(registratorIdId);
        file.setSharingUsers(Arrays.asList(sharingUser));
        assertFalse(fileManager.isAllowedDeletion(sharingUser, file));
        assertTrue(fileManager.isAllowedAccess(sharingUser, file));
    }

    @Test
    public final void testNonInvolvedUserIsNotAllowedAccessAndDeletion()
    {
        final long sharingUserId = 1L;
        final long registratorIdId = 17L;
        final UserDTO sharingUser = new UserDTO();
        sharingUser.setID(sharingUserId);
        final FileDTO file = new FileDTO(registratorIdId);
        assertFalse(fileManager.isAllowedDeletion(sharingUser, file));
        assertFalse(fileManager.isAllowedAccess(sharingUser, file));
    }

    @Test(dependsOnMethods =
        { "testSaveFile", "testGetFile" })
    public final void testDeleteFile()
    {

        final long fileId = imageFile.getID();
        final File realFile = createRealFile(imageFile.getPath());
        assertTrue(realFile.exists());
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).deleteFile(fileId);
                    will(returnValue(true));
                }
            });
        fileManager.deleteFile(imageFile);
        assertFalse(realFile.exists());
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetFileInformationForFileNotExistingInDB()
    {
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));

                    one(fileDAO).tryGetFile(DEFAULT_FILE_ID);
                    will(returnValue(null));
                }
            });

        final FileInformation fileInformation = fileManager.getFileInformation(DEFAULT_FILE_ID);
        assertEquals(fileInformation.isFileAvailable(), false);
        assertEquals(fileInformation.getErrorMessage(), "File [id=" + DEFAULT_FILE_ID
                + "] not found in the database. Try to refresh the page.");

        context.assertIsSatisfied();
    }

    @Test
    public final void testGetFileInformationForFileNotExistingInStore()
    {
        final FileDTO fileDTO = new FileDTO(DEFAULT_FILE_ID);
        fileDTO.setPath(NONEXISTENT_PATH);

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));

                    one(fileDAO).tryGetFile(DEFAULT_FILE_ID);
                    will(returnValue(fileDTO));

                }
            });

        final FileInformation fileInformation = fileManager.getFileInformation(DEFAULT_FILE_ID);
        assertEquals(fileInformation.isFileAvailable(), false);
        assertEquals(fileInformation.getErrorMessage(),
                "File 'targets/unit-test-wd/ch.systemsx.cisd.cifex.server.business.FileManagerTest/"
                        + NONEXISTENT_PATH + "' [id=" + DEFAULT_FILE_ID
                        + "] not found in the file store.");
        context.assertIsSatisfied();
    }

    @Test
    public final void getFileInformationFilestoreUnimportantFileNotExistsInFilestore()
    {
        final FileDTO fileDTO = new FileDTO(DEFAULT_FILE_ID);
        fileDTO.setPath(NONEXISTENT_PATH);

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));

                    one(fileDAO).tryGetFile(DEFAULT_FILE_ID);
                    will(returnValue(fileDTO));

                }
            });

        final FileInformation fileInformation =
                fileManager.getFileInformationFilestoreUnimportant(DEFAULT_FILE_ID);
        assertEquals(fileInformation.isFileAvailable(), true);
        assertEquals(fileInformation.getFileDTO(), fileDTO);
        context.assertIsSatisfied();
    }

    @Test
    public final void getFileInformationFilestoreUnimportantFileNotExistsInDB()
    {

        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));

                    one(fileDAO).tryGetFile(DEFAULT_FILE_ID);
                    will(returnValue(null));

                }
            });

        final FileInformation fileInformation =
                fileManager.getFileInformationFilestoreUnimportant(DEFAULT_FILE_ID);
        assertEquals(fileInformation.isFileAvailable(), false);
        assertEquals(fileInformation.getErrorMessage(), "File [id=" + DEFAULT_FILE_ID
                + "] not found in the database. Try to refresh the page.");
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithExistingUser()
    {
        final String url = "https://server/instance";
        final long requestUserId = 42;
        final String requestUserCode = "requestuser";
        final long receivingUserId = 43;
        final String receivingUserCode = "receivinguser";
        final String emailOfRequestUser = "request.user@organization.edu";
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setUserCode(requestUserCode);
        requestUser.setPermanent(true);
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfUserToShareWith = "receiving.user@organization.edu";
        final UserDTO receivingUser = new UserDTO();
        receivingUser.setID(receivingUserId);
        receivingUser.setUserCode(receivingUserCode);
        receivingUser.setEmail(emailOfUserToShareWith);
        final String comment = "some comment";
        final long fileId = 17;
        final FileDTO file = new FileDTO(requestUserId);
        final long now = System.currentTimeMillis();
        final long expirationPeriod = 24 * 3600 * 1000L;
        final Date expirationDate = new Date(now + expirationPeriod);
        file.setID(fileId);
        file.setExpirationDate(expirationDate);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    allowing(userDAO).listUsers();
                    will(returnValue(Arrays.asList(requestUser, receivingUser)));
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).createSharingLink(fileId, receivingUserId);
                    one(mailClient).sendMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/index.html?fileId=%d&user=%s", fileId,
                                            receivingUserCode))),
                            with(Matchers.containsString(requestUserCode + " <"
                                    + emailOfRequestUser + ">")), with(equal(new String[]
                                { emailOfUserToShareWith })));
                }
            });
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser, Collections
                        .singleton(emailOfUserToShareWith), Collections.singleton(file), comment);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithExistingUserWithUserID()
    {
        final String url = "https://server/instance";
        final long requestUserId = 42;
        final String requestUserCode = "requestuser";
        final long receivingUserId = 43;
        final String receivingUserCode = "receivinguser";
        final String emailOfRequestUser = "request.user@organization.edu";
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setUserCode(requestUserCode);
        requestUser.setPermanent(true);
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfUserToShareWith = "receiving.user@organization.edu";
        final UserDTO receivingUser = new UserDTO();
        receivingUser.setID(receivingUserId);
        receivingUser.setUserCode(receivingUserCode);
        receivingUser.setEmail(emailOfUserToShareWith);
        final String comment = "some comment";
        final long fileId = 17;
        final FileDTO file = new FileDTO(requestUserId);
        final long now = System.currentTimeMillis();
        final long expirationPeriod = 24 * 3600 * 1000L;
        final Date expirationDate = new Date(now + expirationPeriod);
        file.setID(fileId);
        file.setExpirationDate(expirationDate);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    allowing(userDAO).listUsers();
                    will(returnValue(Arrays.asList(requestUser, receivingUser)));
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).createSharingLink(fileId, receivingUserId);
                    one(mailClient).sendMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/index.html?fileId=%d&user=%s", fileId,
                                            receivingUserCode))),
                            with(Matchers.containsString(requestUserCode + " <"
                                    + emailOfRequestUser + ">")), with(equal(new String[]
                                { emailOfUserToShareWith })));
                }
            });
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser, Collections
                        .singleton(Constants.USER_ID_PREFIX + receivingUserCode), Collections
                        .singleton(file), comment);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithTwoExistingUser()
    {
        final String url = "https://server/instance";
        // TODO 2008-06-03, Basil Neff: Extract user generation to local method (fpr all test cases)
        final long requestUserId = 42;
        final String requestUserCode = "requestuser";
        final long firstReceivingUserId = 43;
        final long secondReceivingUserId = 44;
        final String firstReceivingUserCode = "firstreceivinguser";
        final String secondReceivingUserCode = "secondreceivinguser";
        final String emailOfRequestUser = "request.user@organization.edu";
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setUserCode(requestUserCode);
        requestUser.setPermanent(true);
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfFirstUserToShareWith = "first.receiving.user@organization.edu";
        final UserDTO firstReceivingUser = new UserDTO();
        firstReceivingUser.setID(firstReceivingUserId);
        firstReceivingUser.setUserCode(firstReceivingUserCode);
        firstReceivingUser.setEmail(emailOfFirstUserToShareWith);
        final String emailOfSecondUserToShareWith = "second.receiving.user@organization.edu";
        final UserDTO secondReceivingUser = new UserDTO();
        secondReceivingUser.setID(secondReceivingUserId);
        secondReceivingUser.setUserCode(secondReceivingUserCode);
        secondReceivingUser.setEmail(emailOfSecondUserToShareWith);
        final String comment = "some comment";
        final long fileId = 17;
        final FileDTO file = new FileDTO(requestUserId);
        final long now = System.currentTimeMillis();
        final long expirationPeriod = 24 * 3600 * 1000L;
        final Date expirationDate = new Date(now + expirationPeriod);
        file.setID(fileId);
        file.setExpirationDate(expirationDate);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    allowing(userDAO).listUsers();
                    will(returnValue(Arrays.asList(requestUser, firstReceivingUser,
                            secondReceivingUser)));
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).createSharingLink(fileId, firstReceivingUserId);
                    final String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    one(mailClient).sendMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/index.html?fileId=%d&user=%s", fileId,
                                            firstReceivingUserCode))),
                            with(Matchers.containsString(replyTo)), with(equal(new String[]
                                { emailOfFirstUserToShareWith })));
                    one(fileDAO).createSharingLink(fileId, secondReceivingUserId);
                    one(mailClient).sendMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/index.html?fileId=%d&user=%s", fileId,
                                            secondReceivingUserCode))),
                            with(Matchers.containsString(replyTo)), with(equal(new String[]
                                { emailOfSecondUserToShareWith })));
                }
            });
        List<String> users = new ArrayList<String>();
        users.add(Constants.USER_ID_PREFIX + firstReceivingUserCode);
        users.add(emailOfSecondUserToShareWith);
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser, users, Collections.singleton(file),
                        comment);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithExistingUserButDifferentCapitalization()
    {
        final String url = "https://server/instance";
        final long requestUserId = 42;
        final String requestUserCode = "requestuser";
        final long receivingUserId = 43;
        final String receivingUserCode = "receivinguser";
        final String emailOfRequestUser = "request.user@organization.edu";
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setUserCode(requestUserCode);
        requestUser.setPermanent(true);
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfUserToShareWith = "Receiving.User@organization.edu";
        final String emailOfReceivingUser = "receiving.user@ORGANIZATION.EDU";
        final String emailOfReceivingUserLowerCase = emailOfReceivingUser.toLowerCase();
        final UserDTO receivingUser = new UserDTO();
        receivingUser.setID(receivingUserId);
        receivingUser.setUserCode(receivingUserCode);
        receivingUser.setEmail(emailOfReceivingUser);
        final String comment = "some comment";
        final long fileId = 17;
        final FileDTO file = new FileDTO(requestUserId);
        final long now = System.currentTimeMillis();
        final long expirationPeriod = 24 * 3600 * 1000L;
        final Date expirationDate = new Date(now + expirationPeriod);
        file.setID(fileId);
        file.setExpirationDate(expirationDate);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));
                    allowing(userDAO).listUsers();
                    will(returnValue(Arrays.asList(requestUser, receivingUser)));
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).createSharingLink(fileId, receivingUserId);
                    String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    one(mailClient).sendMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/index.html?fileId=%d&user=%s", fileId,
                                            receivingUserCode))), with(equal(replyTo)),
                            with(equal(new String[]
                                { emailOfReceivingUserLowerCase })));
                }
            });
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser, Collections
                        .singleton(emailOfUserToShareWith), Collections.singleton(file), comment);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithInvalidRecipient()
    {
        final long requestUserId = 42;
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setPermanent(true);
        requestUser.setUserCode("hello");
        requestUser.setEmail("hello@a.bc");
        final FileDTO file = new FileDTO(requestUserId);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getUserDAO();
                    will(returnValue(userDAO));

                    allowing(userDAO).listUsers();
                    will(returnValue(Arrays.asList(requestUser)));

                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                }
            });
        
        final List<String> invalidUsers =
            fileManager.shareFilesWith("", requestUser, Collections
                    .singleton("hello"), Collections.singleton(file), "");
        
        assertEquals("[hello]", invalidUsers.toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testGetFile()
    {
        final File realFile = createRealFile(imageFile.getPath());
        assertTrue(realFile.exists());
        final FileContent returnedFile = fileManager.getFileContent(imageFile);
        assertNotNull(returnedFile);
        assertEquals(imageFile.getName(), returnedFile.getBasicFile().getName());
        assertEquals(imageFile.getSize(), returnedFile.getBasicFile().getSize());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "booleans")
    public final void testListFiles(final boolean listOfSharedFilesEmpty)
    {
        final long userId = userAlice.getID();
        final List<FileDTO> files = new ArrayList<FileDTO>();
        if (listOfSharedFilesEmpty == false)
        {
            files.add(new FileDTO(userAlice.getID()));
        }
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).listDownloadFiles(userId);
                    will(returnValue(files));
                    one(fileDAO).listFiles();
                    will(returnValue(files));
                    one(fileDAO).listUploadedFiles(userId);
                    will(returnValue(files));
                }
            });
        /* listDownloadFiles */
        assertEquals(files, fileManager.listDownloadFiles(userId));
        /* listFiles */
        assertEquals(files, fileManager.listFiles());
        /* listUploadedFiles */
        assertEquals(files, fileManager.listUploadedFiles(userId));
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unused")
    @DataProvider(name = "booleans")
    private Object[][] provideAllBooleans()
    {
        return new Object[][]
            {
                { true },
                { false } };
    }

    @Transactional
    @Test(dataProvider = "booleans")
    public final void testSaveFile(final boolean fileAlreadyExists) throws FileNotFoundException
    {
        final UserDTO user = userAlice;
        final String filePath = imageFile.getPath();
        final String comment = "This is a test comment for a test file";
        final File inputFile = createRealFile(filePath + "_user");
        final InputStream inputStream = new FileInputStream(inputFile);
        File filePathIfFileNotExisted = new File(fileStore, filePath);
        File expectedFilePath;
        if (fileAlreadyExists == false)
        {
            filePathIfFileNotExisted = createRealFile(filePath);
            assertTrue(filePathIfFileNotExisted.exists());
            expectedFilePath =
                    FileUtilities.createNextNumberedFile(new File(fileStore, filePath), null);
            assertFalse(expectedFilePath.compareTo(filePathIfFileNotExisted) == 0);

        } else
        {
            filePathIfFileNotExisted.delete();
            assertFalse(filePathIfFileNotExisted.exists());
            expectedFilePath = filePathIfFileNotExisted;
        }
        assertFalse(expectedFilePath.exists());
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).createFile((FileDTO) this.with(new IsInstanceOf(FileDTO.class)));
                }
            });
        final FileDTO createdFileDTO =
                fileManager.saveFile(user, imageFile.getName(), comment,
                        imageFile.getContentType(), inputStream);
        final File createdFile = new File(fileStore, createdFileDTO.getPath());
        assertTrue(createdFile.exists());
        assertEquals(expectedFilePath.getPath(), createdFile.getPath());
        assertEquals(imageFile.getContentType(), createdFileDTO.getContentType());
        assertEquals(imageFile.getRegistratorId(), createdFileDTO.getRegistratorId());
        assertEquals(imageFile.getSharingUsers(), createdFileDTO.getSharingUsers());
        assertEquals(imageFile.getName(), createdFileDTO.getName());
        assertEquals(comment, createdFileDTO.getComment());
        assertEquals(inputFile.length(), createdFileDTO.getSize().longValue());
        context.assertIsSatisfied();
    }

    private final File createRealFile(final String path)
    {
        final File realFile = new File(fileStore, path);
        boolean fileCannotBeCreated = false;
        try
        {
            final String directoryName = FilenameUtils.getPathNoEndSeparator(path);
            if (directoryName.equals("") == false)
            {
                final File directory = new File(fileStore, directoryName);
                directory.mkdirs();
            }
            realFile.createNewFile();
            if (realFile.length() == 0)
            {
                FileUtilities.writeToFile(realFile, "Lorem ipsum.");
            }
        } catch (final IOException ex)
        {
            fileCannotBeCreated = true;
        } finally
        {
            assertTrue(realFile.exists());
            assertFalse(fileCannotBeCreated);
        }
        return realFile;
    }

    final static UserDTO createSampleUserDTO(final long id, final String email)
    {
        final UserDTO user = new UserDTO();
        user.setID(id);
        user.setUserCode(email);
        user.setEmail(email);
        return user;
    }

    final static private FileDTO cerateSampleFileDTO(final long id, final UserDTO owner,
            final String fileName, final String contentType)
    {
        final FileDTO fileDTO = new FileDTO(owner.getID());
        fileDTO.setID(id);
        fileDTO.setName(fileName);
        final String path = owner.getEmail() + "/" + fileName;
        fileDTO.setPath(path);
        fileDTO.setContentType(contentType);
        fileDTO.setRegisterer(owner);
        return fileDTO;
    }

}
