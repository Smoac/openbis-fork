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
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.security.PasswordGenerator;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * Test cases for corresponding {@link FileManager} class.
 * 
 * @author Izabela Adamczyk
 */
public class FileManagerTest extends AbstractFileSystemTestCase
{

    private static final int DEFAULT_FILE_RETENTION = 5;

    private static final int MAX_USER_RETENTION = 15;

    private static final long DEFAULT_FILE_ID = 1L;

    private static final String NONEXISTENT_PATH = "nonexistentFilePath";

    private Mockery context;

    private IDAOFactory daoFactory;

    private IFileDAO fileDAO;

    private IMailClient mailClient;

    private IUserManager userManager;

    private IFileManager fileManager;

    private ITriggerManager triggerManager;

    private File fileStore;

    private IBusinessObjectFactory boFactory;

    private UserDTO userAlice;

    private FileDTO imageFile;

    private BusinessContext businessContext;

    private ITimeProvider timeProvider;

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        LogInitializer.init();
        super.setUp();
        userAlice = createSampleUserDTO(1L, "alice@users.com");
        imageFile = cerateSampleFileDTO(1L, userAlice, "image.jpg", "image");
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        fileDAO = context.mock(IFileDAO.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        fileStore = workingDirectory;
        businessContext = new BusinessContext();
        businessContext.setFileRetention(DEFAULT_FILE_RETENTION);
        businessContext.setMaxFileRetention(DEFAULT_FILE_RETENTION);
        businessContext.setFileStore(fileStore);
        businessContext.setMaxUserRetention(MAX_USER_RETENTION);
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
        mailClient = context.mock(IMailClient.class);
        businessContext.setMailClient(mailClient);
        timeProvider = context.mock(ITimeProvider.class);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                }
            });
        triggerManager = context.mock(ITriggerManager.class);
        // context.checking(new Expectations()
        // {
        // {
        // allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
        // will(returnValue(false));
        // }
        // });
        userManager = context.mock(IUserManager.class);
        fileManager =
                new FileManager(daoFactory, boFactory, userManager, businessContext,
                        triggerManager, timeProvider);

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
                    one(fileDAO).getExpiredFiles();
                    will(returnValue(fileDTOs));
                    one(fileDAO).deleteFile(imageFile.getID());
                }
            });

        fileManager.deleteExpiredFiles(null);
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
        assertTrue(fileManager.isControlling(user, file));
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
        assertTrue(fileManager.isControlling(admin, file));
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
        assertFalse(fileManager.isControlling(sharingUser, file));
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
        assertFalse(fileManager.isControlling(sharingUser, file));
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
                    one(fileDAO).tryGetFile(DEFAULT_FILE_ID);
                    will(returnValue(null));
                }
            });

        final FileInformation fileInformation = fileManager.getFileInformation(DEFAULT_FILE_ID);
        assertEquals(false, fileInformation.isFileAvailable());
        assertEquals("File [id=" + DEFAULT_FILE_ID + "] not found in CIFEX database.",
                fileInformation.getErrorMessage());

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
                    one(fileDAO).tryGetFile(DEFAULT_FILE_ID);
                    will(returnValue(fileDTO));
                }
            });

        final FileInformation fileInformation = fileManager.getFileInformation(DEFAULT_FILE_ID);
        assertEquals(false, fileInformation.isFileAvailable());
        assertEquals(
                "Unexpected: File 'targets/unit-test-wd/ch.systemsx.cisd.cifex.server.business.FileManagerTest/"
                        + NONEXISTENT_PATH + "' [id=" + DEFAULT_FILE_ID
                        + "] is missing in CIFEX file store.", fileInformation.getErrorMessage()
                        .replace('\\', '/'));
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
                    one(fileDAO).tryGetFile(DEFAULT_FILE_ID);
                    will(returnValue(fileDTO));
                }
            });

        final FileInformation fileInformation =
                fileManager.getFileInformationFilestoreUnimportant(DEFAULT_FILE_ID);
        assertEquals(true, fileInformation.isFileAvailable());
        assertEquals(fileDTO, fileInformation.getFileDTO());
        context.assertIsSatisfied();
    }

    @Test
    public final void getFileInformationFilestoreUnimportantFileNotExistsInDB()
    {

        context.checking(new Expectations()
            {
                {
                    one(fileDAO).tryGetFile(DEFAULT_FILE_ID);
                    will(returnValue(null));
                }
            });

        final FileInformation fileInformation =
                fileManager.getFileInformationFilestoreUnimportant(DEFAULT_FILE_ID);
        assertEquals(false, fileInformation.isFileAvailable());
        assertEquals("File [id=" + DEFAULT_FILE_ID + "] not found in CIFEX database.",
                fileInformation.getErrorMessage());
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
                    one(userManager).getUsers(Collections.<String> emptyList(),
                            Arrays.asList(emailOfUserToShareWith), null);
                    will(returnValue(Arrays.asList(receivingUser)));
                    exactly(2).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, receivingUserId);
                    context.checking(new Expectations()
                        {
                            {
                                allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                                will(returnValue(false));
                            }
                        });
                    String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            receivingUserCode))), with(containsEmail(replyTo)),
                            with(matchesEmail(replyTo)), with(matchesEmail(new String[]
                                { emailOfUserToShareWith })));
                }
            });
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser,
                        Collections.singleton(emailOfUserToShareWith), Collections.singleton(file),
                        comment, null);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithExistingTemporaryUser()
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
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfUserToShareWith = "receiving.user@organization.edu";
        final UserDTO receivingUser = new UserDTO();
        receivingUser.setID(receivingUserId);
        receivingUser.setUserCode(receivingUserCode);
        receivingUser.setEmail(emailOfUserToShareWith);
        receivingUser.setRegistrator(requestUser);
        final String comment = "some comment";
        final long fileId = 17;
        final FileDTO file = new FileDTO(requestUserId);
        final long now = System.currentTimeMillis();
        final long expirationPeriod = 24 * 3600 * 1000L;
        final Date expirationDate = new Date(now + expirationPeriod);
        file.setID(fileId);
        file.setExpirationDate(expirationDate);
        receivingUser.setExpirationDate(DateUtils.addDays(expirationDate, -2));
        context.checking(new Expectations()
            {
                {
                    one(userManager).getUsers(Collections.<String> emptyList(),
                            Arrays.asList(emailOfUserToShareWith), null);
                    will(returnValue(Arrays.asList(receivingUser)));
                    exactly(2).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, receivingUserId);
                    one(userManager).updateUser(receivingUser, receivingUser, null, requestUser,
                            null);
                    context.checking(new Expectations()
                        {
                            {
                                allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                                will(returnValue(false));
                            }
                        });
                    String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            receivingUserCode))), with(containsEmail(replyTo)),
                            with(matchesEmail(replyTo)), with(matchesEmail(new String[]
                                { emailOfUserToShareWith })));
                }
            });
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser,
                        Collections.singleton(emailOfUserToShareWith), Collections.singleton(file),
                        comment, null);
        assertEquals(0, invalidUsers.size());
        // The expiration date of the temporary receiving user should have been updated to match the
        // expiration date of the file.
        assertEquals(DateTimeUtils.extendUntilEndOfDay(expirationDate),
                receivingUser.getExpirationDate());
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
                    one(userManager).getUsers(Arrays.asList(receivingUserCode),
                            Collections.<String> emptyList(), null);
                    will(returnValue(Arrays.asList(receivingUser)));
                    exactly(2).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, receivingUserId);
                    context.checking(new Expectations()
                        {
                            {
                                allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                                will(returnValue(false));
                            }
                        });
                    String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            receivingUserCode))), with(containsEmail(replyTo)),
                            with(matchesEmail(replyTo)), with(containsEmail(new String[]
                                { emailOfUserToShareWith })));
                }
            });
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser,
                        Collections.singleton(Constants.USER_ID_PREFIX + receivingUserCode),
                        Collections.singleton(file), comment, null);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    class EmailAddressMatcher extends BaseMatcher<EMailAddress>
    {

        private final String emailAddress;

        private final boolean exact;

        public EmailAddressMatcher(String mailAddress, boolean exact)
        {
            this.emailAddress = mailAddress;
            this.exact = exact;
        }

        @Override
        public boolean matches(Object item)
        {
            final EMailAddress email = (EMailAddress) item;
            String name = email.tryGetPersonalName();
            String emailStr;
            if (name == null)
            {
                emailStr = email.tryGetEmailAddress();
            } else
            {
                boolean needsQuoting =
                        name.contains(",") || name.contains(";") || name.contains("\"");
                if (needsQuoting)
                {
                    name = "\"" + name.replace("\"", "\\\"") + "\"";
                }
                emailStr = name + " <" + email.tryGetEmailAddress() + ">";
            }
            if (exact)
            {
                return emailStr.equals(emailAddress);
            } else
            {
                return emailStr.contains(emailAddress);
            }
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText(exact ? "the email '" + emailAddress + "'"
                    : "an email containing '" + emailAddress + "'");
        }

    }

    class EmailAddressArrayMatcher extends BaseMatcher<EMailAddress[]>
    {

        private final String[] emailAddresses;

        private final boolean exact;

        public EmailAddressArrayMatcher(String[] mailAddresses, boolean exact)
        {
            this.emailAddresses = mailAddresses;
            this.exact = exact;
        }

        @Override
        public boolean matches(Object item)
        {
            final EMailAddress[] emails = (EMailAddress[]) item;
            int i = 0;
            for (EMailAddress email : emails)
            {
                String name = email.tryGetPersonalName();
                String emailStr;
                if (name == null)
                {
                    emailStr = email.tryGetEmailAddress();
                } else
                {
                    boolean needsQuoting =
                            name.contains(",") || name.contains(";") || name.contains("\"");
                    if (needsQuoting)
                    {
                        name = "\"" + name.replace("\"", "\\\"") + "\"";
                    }
                    emailStr = name + " <" + email.tryGetEmailAddress() + ">";
                }
                if (exact)
                {
                    if (emailStr.equals(emailAddresses[i]) == false)
                    {
                        return false;
                    }
                } else
                {
                    if (emailStr.contains(emailAddresses[i]) == false)
                    {
                        return false;
                    }
                }
                ++i;
            }
            return true;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText(exact ? "emails '" + Arrays.toString(emailAddresses) + "'"
                    : "emails containing '" + Arrays.toString(emailAddresses) + "'");
        }

    }

    EmailAddressMatcher matchesEmail(String mailAddress)
    {
        return new EmailAddressMatcher(mailAddress, true);
    }

    EmailAddressMatcher containsEmail(String mailAddress)
    {
        return new EmailAddressMatcher(mailAddress, false);
    }

    EmailAddressArrayMatcher matchesEmail(String[] mailAddress)
    {
        return new EmailAddressArrayMatcher(mailAddress, true);
    }

    EmailAddressArrayMatcher containsEmail(String[] mailAddress)
    {
        return new EmailAddressArrayMatcher(mailAddress, false);
    }

    @Test
    public final void testShareFileWithTwoExistingUser()
    {
        final String url = "https://server/instance";
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
                    one(userManager).getUsers(Arrays.asList(firstReceivingUserCode),
                            Arrays.asList(emailOfSecondUserToShareWith), null);
                    will(returnValue(Arrays.asList(firstReceivingUser, secondReceivingUser)));
                    exactly(3).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, firstReceivingUserId);
                    final String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    context.checking(new Expectations()
                        {
                            {
                                allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                                will(returnValue(false));
                            }
                        });
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            firstReceivingUserCode))),
                            with(containsEmail(replyTo)), with(matchesEmail(replyTo)),
                            with(matchesEmail(new String[]
                                { emailOfFirstUserToShareWith })));
                    one(fileDAO).createSharingLink(fileId, secondReceivingUserId);
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            secondReceivingUserCode))),
                            with(containsEmail(replyTo)), with(matchesEmail(replyTo)),
                            with(matchesEmail(new String[]
                                { emailOfSecondUserToShareWith })));
                }
            });
        List<String> users = new ArrayList<String>();
        users.add(Constants.USER_ID_PREFIX + firstReceivingUserCode);
        users.add(emailOfSecondUserToShareWith);
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser, users, Collections.singleton(file),
                        comment, null);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithUserDuplicatedCodeAndEmail()
    {
        final String url = "https://server/instance";
        final long requestUserId = 42;
        final String requestUserCode = "requestuser";
        final long firstReceivingUserId = 43;
        final String firstReceivingUserCode = "firstreceivinguser";
        final String emailOfRequestUser = "request.user@organization.edu";
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setUserCode(requestUserCode);
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfFirstUserToShareWith = "first.receiving.user@organization.edu";
        final UserDTO firstReceivingUser = new UserDTO();
        firstReceivingUser.setID(firstReceivingUserId);
        firstReceivingUser.setUserCode(firstReceivingUserCode);
        firstReceivingUser.setEmail(emailOfFirstUserToShareWith);
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
                    one(userManager).getUsers(Arrays.<String> asList(firstReceivingUserCode),
                            Arrays.<String> asList(emailOfFirstUserToShareWith), null);
                    will(returnValue(Arrays.asList(firstReceivingUser)));
                    exactly(2).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, firstReceivingUserId);
                    final String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    context.checking(new Expectations()
                        {
                            {
                                allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                                will(returnValue(false));
                            }
                        });
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            firstReceivingUserCode))),
                            with(containsEmail(replyTo)), with(matchesEmail(replyTo)),
                            with(matchesEmail(new String[]
                                { emailOfFirstUserToShareWith })));
                }
            });
        List<String> users = new ArrayList<String>();
        users.add(Constants.USER_ID_PREFIX + firstReceivingUserCode);
        users.add(emailOfFirstUserToShareWith); // That's the same user
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser, users, Collections.singleton(file),
                        comment, null);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithUserDuplicatedEmailTwice()
    {
        final String url = "https://server/instance";
        final long requestUserId = 42;
        final String requestUserCode = "requestuser";
        final long firstReceivingUserId = 43;
        final String firstReceivingUserCode = "firstreceivinguser";
        final String emailOfRequestUser = "request.user@organization.edu";
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setUserCode(requestUserCode);
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfFirstUserToShareWith = "first.receiving.user@organization.edu";
        final UserDTO firstReceivingUser = new UserDTO();
        firstReceivingUser.setID(firstReceivingUserId);
        firstReceivingUser.setUserCode(firstReceivingUserCode);
        firstReceivingUser.setEmail(emailOfFirstUserToShareWith);
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
                    one(userManager)
                            .getUsers(
                                    Collections.<String> emptyList(),
                                    Arrays.asList(emailOfFirstUserToShareWith,
                                            emailOfFirstUserToShareWith), null);
                    will(returnValue(Arrays.asList(firstReceivingUser)));
                    exactly(2).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, firstReceivingUserId);
                    final String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    context.checking(new Expectations()
                        {
                            {
                                allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                                will(returnValue(false));
                            }
                        });
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            firstReceivingUserCode))),
                            with(containsEmail(replyTo)), with(matchesEmail(replyTo)),
                            with(matchesEmail(new String[]
                                { emailOfFirstUserToShareWith })));
                }
            });
        List<String> users = new ArrayList<String>();
        users.add(emailOfFirstUserToShareWith);
        users.add(emailOfFirstUserToShareWith);
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser, users, Collections.singleton(file),
                        comment, null);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithUserDuplicatedCodeTwice()
    {
        final String url = "https://server/instance";
        final long requestUserId = 42;
        final String requestUserCode = "requestuser";
        final long firstReceivingUserId = 43;
        final String firstReceivingUserCode = "firstreceivinguser";
        final String emailOfRequestUser = "request.user@organization.edu";
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setUserCode(requestUserCode);
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfFirstUserToShareWith = "first.receiving.user@organization.edu";
        final UserDTO firstReceivingUser = new UserDTO();
        firstReceivingUser.setID(firstReceivingUserId);
        firstReceivingUser.setUserCode(firstReceivingUserCode);
        firstReceivingUser.setEmail(emailOfFirstUserToShareWith);
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
                    one(userManager).getUsers(
                            Arrays.asList(firstReceivingUserCode, firstReceivingUserCode),
                            Collections.<String> emptyList(), null);
                    will(returnValue(Arrays.asList(firstReceivingUser)));
                    exactly(2).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, firstReceivingUserId);
                    final String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    context.checking(new Expectations()
                        {
                            {
                                allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                                will(returnValue(false));
                            }
                        });
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            firstReceivingUserCode))),
                            with(containsEmail(replyTo)), with(matchesEmail(replyTo)),
                            with(matchesEmail(new String[]
                                { emailOfFirstUserToShareWith })));
                }
            });
        List<String> users = new ArrayList<String>();
        users.add(Constants.USER_ID_PREFIX + firstReceivingUserCode);
        users.add(Constants.USER_ID_PREFIX + firstReceivingUserCode);
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser, users, Collections.singleton(file),
                        comment, null);
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
                    one(userManager).getUsers(Collections.<String> emptyList(),
                            Arrays.asList(emailOfReceivingUserLowerCase), null);
                    will(returnValue(Arrays.asList(receivingUser)));
                    exactly(2).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, receivingUserId);
                    String replyTo = requestUserCode + " <" + emailOfRequestUser + ">";
                    context.checking(new Expectations()
                        {
                            {
                                allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                                will(returnValue(false));
                            }
                        });
                    one(mailClient).sendEmailMessage(
                            with(Matchers.containsString(requestUserCode)),
                            with(Matchers.containsString(url
                                    + String.format("/?fileId=%d&user=%s", fileId,
                                            receivingUserCode))), with(matchesEmail(replyTo)),
                            with(matchesEmail(replyTo)), with(matchesEmail(new String[]
                                { emailOfReceivingUserLowerCase })));
                }
            });
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser,
                        Collections.singleton(emailOfUserToShareWith), Collections.singleton(file),
                        comment, null);
        assertEquals(0, invalidUsers.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testShareFileWithInvalidRecipient()
    {
        final long requestUserId = 42;
        final UserDTO requestUser = new UserDTO();
        requestUser.setID(requestUserId);
        requestUser.setUserCode("hello");
        requestUser.setEmail("hello@a.bc");
        final FileDTO file = new FileDTO(requestUserId);
        context.checking(new Expectations()
            {
                {
                    one(userManager).getUsers(Collections.<String> emptyList(),
                            Collections.<String> emptyList(), null);
                    will(returnValue(Collections.emptyList()));
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                }
            });

        final List<String> invalidUsers =
                fileManager.shareFilesWith("", requestUser, Collections.singleton("hello"),
                        Collections.singleton(file), "", null);

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

    @DataProvider(name = "booleans")
    private Object[][] provideAllBooleans()
    {
        return new Object[][]
            {
                { true },
                { false } };
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
                    one(fileDAO).listDownloadFiles(userId);
                    will(returnValue(files));
                    one(fileDAO).listFiles();
                    will(returnValue(files));
                    one(fileDAO).listDirectlyAndIndirectlyOwnedFiles(userId);
                    will(returnValue(files));
                }
            });
        /* listDownloadFiles */
        assertEquals(files, fileManager.listDownloadFiles(userId));
        /* listFiles */
        assertEquals(files, fileManager.listFiles());
        /* listUploadedFiles */
        assertEquals(files, fileManager.listOwnedFiles(userId));
        context.assertIsSatisfied();
    }

    @DataProvider(name = "saveFileTestData")
    private Object[][] provideAllSaveFileTestData()
    {
        return new Object[][]
            {
                { true, null },
                { false, 123 } };
    }

    @Transactional
    @Test(dataProvider = "saveFileTestData")
    public final void testSaveFile(final boolean fileAlreadyExists, final Integer fileRetention)
            throws FileNotFoundException
    {
        final UserDTO user = userAlice;
        user.setMaxFileRetention(fileRetention);
        if (fileRetention != null)
        {
            businessContext.setFileRetention(fileRetention);
        }
        try
        {
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
                        one(fileDAO).createFile(
                                (FileDTO) this.with(new IsInstanceOf(FileDTO.class)));

                        one(timeProvider).getTimeInMilliseconds();
                        will(returnValue(4711L));
                    }
                });
            final FileDTO createdFileDTO =
                    fileManager.saveFile(user, imageFile.getName(), comment,
                            imageFile.getContentType(), inputStream);
            final File createdFile = new File(fileStore, createdFileDTO.getPath());
            assertTrue(createdFile.exists());
            assertEquals(expectedFilePath.getPath(), createdFile.getPath());
            assertEquals(imageFile.getContentType(), createdFileDTO.getContentType());
            assertEquals(imageFile.getOwnerId(), createdFileDTO.getOwnerId());
            assertEquals(imageFile.getSharingUsers(), createdFileDTO.getSharingUsers());
            assertEquals(imageFile.getName(), createdFileDTO.getName());
            assertEquals(comment, createdFileDTO.getComment());
            assertEquals(inputFile.length(), createdFileDTO.getSize().longValue());
            final long expectedExpirationDate =
                    DateTimeUtils.extendUntilEndOfDay(
                            new Date(calculateFileRetention(fileRetention) * 86400000L + 4711))
                            .getTime();
            assertEquals(expectedExpirationDate, createdFileDTO.getExpirationDate().getTime());

            context.assertIsSatisfied();
        } finally
        {
            user.setMaxFileRetention(null);
        }
    }

    @DataProvider(name = "fileRetentions")
    private Object[][] provideAllFileRetentions()
    {
        return new Object[][]
            {
                { null },
                { 99 } };
    }

    @Test(dataProvider = "fileRetentions")
    public void testUpdateFileUserData(final Integer fileRetention)
    {
        if (fileRetention != null)
        {
            businessContext.setFileRetention(fileRetention);
            userAlice.setMaxFileRetention(fileRetention);
        }
        final long registrationTime = 4711L;
        final long retentionTime = 1111L;
        final Date registrationDate = new Date(registrationTime);
        final String newName = "new name";
        final String newComment = "new comment";
        final Date newExpirationDate = new Date(registrationTime + retentionTime);
        try
        {
            context.checking(new Expectations()
                {
                    {
                        one(timeProvider).getTimeInMilliseconds();
                        will(returnValue(System.currentTimeMillis()));
                        one(fileDAO).getFileRegistrationDate(DEFAULT_FILE_ID);
                        will(returnValue(registrationDate));

                        one(fileDAO).updateFileUserEdit(DEFAULT_FILE_ID, newName, newComment,
                                DateTimeUtils.extendUntilEndOfDay(newExpirationDate));
                    }
                });

            fileManager.updateFileUserData(DEFAULT_FILE_ID, newName, newComment, newExpirationDate,
                    userAlice);

            context.assertIsSatisfied();
        } finally
        {
            businessContext.setFileRetention(DEFAULT_FILE_RETENTION);
            userAlice.setMaxFileRetention(null);
        }
    }

    @Test
    public void testTrigger() throws IOException
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
        requestUser.setEmail(emailOfRequestUser);
        final String emailOfUserToShareWith = "receiving.user@organization.edu";
        final UserDTO receivingUser = new UserDTO();
        receivingUser.setID(receivingUserId);
        receivingUser.setUserCode(receivingUserCode);
        receivingUser.setEmail(emailOfUserToShareWith);
        final String comment = "some comment";
        final long fileId = 17;
        final FileDTO file = new FileDTO(requestUserId);
        file.setPath("TestFile.dat");
        createRealFile(file.getPath());
        final long now = System.currentTimeMillis();
        final long expirationPeriod = 24 * 3600 * 1000L;
        final Date expirationDate = new Date(now + expirationPeriod);
        file.setID(fileId);
        file.setExpirationDate(expirationDate);
        context.checking(new Expectations()
            {
                {
                    one(userManager).getUsers(Collections.<String> emptyList(),
                            Arrays.asList(emailOfUserToShareWith), null);
                    will(returnValue(Arrays.asList(receivingUser)));
                    exactly(2).of(timeProvider).getTimeInMilliseconds();
                    will(returnValue(System.currentTimeMillis()));
                    one(fileDAO).createSharingLink(fileId, receivingUserId);
                    allowing(triggerManager).isTriggerUser(with(any(UserDTO.class)));
                    will(returnValue(true));
                    one(triggerManager).handle(with(equal(receivingUser)), with(equal(file)),
                            with(equal(fileManager)));
                }
            });
        final List<String> invalidUsers =
                fileManager.shareFilesWith(url, requestUser,
                        Collections.singleton(emailOfUserToShareWith), Collections.singleton(file),
                        comment, null);
        assertEquals(0, invalidUsers.size());
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
            realFile.deleteOnExit();
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

    private int calculateFileRetention(final Integer fileRetention)
    {
        return fileRetention == null ? DEFAULT_FILE_RETENTION : fileRetention.intValue();
    }

    final static UserDTO createSampleUserDTO(final Long id, final String email)
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
        fileDTO.setOwner(owner);
        return fileDTO;
    }

}
