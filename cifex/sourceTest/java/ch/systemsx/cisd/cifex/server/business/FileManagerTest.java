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
import java.util.List;

import org.apache.commons.io.FilenameUtils;
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
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for corresponding {@link FileManager} class.
 * 
 * @author Izabela Adamczyk
 */
public class FileManagerTest extends AbstractFileSystemTestCase
{

    Mockery context = new Mockery();

    private IDAOFactory daoFactory;

    private IFileDAO fileDAO;

    private IMailClient mailClient;

    private IFileManager fileManager;

    File fileStore;

    private IBusinessObjectFactory boFactory;

    private UserDTO userAlice;

    private FileDTO imageFile;

    @BeforeMethod
    public final void setUp() throws IOException
    {
        userAlice = createSampleUserDTO(1L, "alice@users.com");
        imageFile = cerateSampleFileDTO(1L, userAlice, "image.jpg", "image");
        daoFactory = context.mock(IDAOFactory.class);
        fileDAO = context.mock(IFileDAO.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        fileStore = workingDirectory;
        BusinessContext businessContext = new BusinessContext();
        businessContext.setFileRetention(5);
        businessContext.setFileStore(fileStore);
        mailClient = context.mock(IMailClient.class);
        businessContext.setMailClient(mailClient);
        fileManager = new FileManager(daoFactory, boFactory, businessContext);

    }

    @AfterMethod
    public void tearDown()
    {
        File userFolder = new File(fileStore, userAlice.getEmail());
        deleteFileRecursive(userFolder);
        context.assertIsSatisfied();
    }

    private void deleteFileRecursive(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (File f : files)
            {
                deleteFileRecursive(f);
            }
        }
        file.delete();
    }

    @Test
    public void testDeleteExpiredFiles()
    {
        final List<FileDTO> fileDTOs = new ArrayList<FileDTO>();
        fileDTOs.add(imageFile);
        File realFile = createRealFile(imageFile.getPath());
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
    public void testRegistratorIsAllowedAccessAndDeletion()
    {
        final long userId = 17L;
        final UserDTO user = new UserDTO();
        user.setID(userId);
        final FileDTO file = new FileDTO(userId);
        assertTrue(fileManager.isAllowedDeletion(user, file));
        assertTrue(fileManager.isAllowedAccess(user, file));
    }
    
    @Test
    public void testAdminIsAllowedAccessAndDeletion()
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
    public void testSharingUserIsAllowedAccessButNotDeletion()
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
    public void testNonInvolvedUserIsNotAllowedAccessAndDeletion()
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
    public void testDeleteFile()
    {

        final long fileId = imageFile.getID();
        File realFile = createRealFile(imageFile.getPath());
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
    public void testGetFile()
    {
        File realFile = createRealFile(imageFile.getPath());
        assertTrue(realFile.exists());
        FileContent returnedFile = fileManager.getFileContent(imageFile);
        assertNotNull(returnedFile);
        assertEquals(imageFile.getName(), returnedFile.getBasicFile().getName());
        assertEquals(imageFile.getSize(), returnedFile.getBasicFile().getSize());
        context.assertIsSatisfied();
    }

    @Test(dataProvider = "booleans")
    public void testListFiles(boolean listOfSharedFilesEmpty)
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
    public void testSaveFile(final boolean fileAlreadyExists) throws FileNotFoundException
    {
        UserDTO user = userAlice;
        final String filePath = imageFile.getPath();
        File inputFile = createRealFile(filePath + "_user");
        InputStream inputStream = new FileInputStream(inputFile);
        File filePathIfFileNotExisted = new File(fileStore, filePath);
        File expectedFilePath;
        if (fileAlreadyExists == false)
        {
            filePathIfFileNotExisted = createRealFile(filePath);
            assertTrue(filePathIfFileNotExisted.exists());
            expectedFilePath = FileUtilities.createNextNumberedFile(new File(fileStore, filePath), null);
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
                    one(fileDAO).createFile(this.with(new IsInstanceOf<FileDTO>(FileDTO.class)));
                }
            });
        FileDTO createdFileDTO =
                fileManager.saveFile(user, imageFile.getName(), imageFile.getContentType(), inputStream);
        File createdFile = new File(fileStore, createdFileDTO.getPath());
        assertTrue(createdFile.exists());
        assertEquals(createdFile.getPath(), expectedFilePath.getPath());
        assertEquals(createdFileDTO.getContentType(), imageFile.getContentType());
        assertEquals(createdFileDTO.getRegistratorId(), imageFile.getRegistratorId());
        assertEquals(createdFileDTO.getSharingUsers(), imageFile.getSharingUsers());
        assertEquals(createdFileDTO.getName(), imageFile.getName());
        assertEquals(createdFileDTO.getSize().longValue(), inputFile.length());
        context.assertIsSatisfied();
    }

    private File createRealFile(String path)
    {
        File realFile = new File(fileStore, path);
        boolean fileCannotBeCreated = false;
        try
        {
            String directoryName = FilenameUtils.getPathNoEndSeparator(path);
            if (directoryName.equals("") == false)
            {
                File directory = new File(fileStore, directoryName);
                directory.mkdirs();
            }
            realFile.createNewFile();
            if (realFile.length() == 0)
            {
                FileUtilities.writeToFile(realFile, "Lorem ipsum.");
            }
        } catch (IOException ex)
        {
            fileCannotBeCreated = true;
        } finally
        {
            assertTrue(realFile.exists());
            assertFalse(fileCannotBeCreated);
        }
        return realFile;
    }

    final static UserDTO createSampleUserDTO(long id, String email)
    {
        UserDTO user = new UserDTO();
        user.setID(id);
        user.setUserCode(email);
        user.setEmail(email);
        return user;
    }

    final static private FileDTO cerateSampleFileDTO(long id, UserDTO owner, String fileName, String contentType)
    {
        FileDTO fileDTO = new FileDTO(owner.getID());
        fileDTO.setID(id);
        fileDTO.setName(fileName);
        String path = owner.getEmail() + "/" + fileName;
        fileDTO.setPath(path);
        fileDTO.setContentType(contentType);
        fileDTO.setRegisterer(owner);
        return fileDTO;
    }

}
