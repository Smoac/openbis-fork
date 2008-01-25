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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * Test cases for corresponding {@link FileDAO} class.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "file" })
public final class FileDAOTest extends AbstractDAOTest
{

    final FileDTO createFile(String name, String path, UserDTO registerer, Date registrationDate, Date expirationDate)
    {
        List<UserDTO> fileViewers = new ArrayList<UserDTO>();
        fileViewers.add(registerer);
        FileDTO file = new FileDTO();
        file.setExpirationDate(expirationDate);
        file.setName(name);
        file.setPath(path);
        file.setRegisterer(registerer);
        file.setRegistrationDate(registrationDate);
        file.setSharingUsers(fileViewers);
        return file;
    }

    final FileDTO createSampleFile()
    {
        String name = "file.txt";
        UserDTO registerer = getSampleUserFromDB();
        String path = "/files/" + registerer.getUserName() + "/" + name;
        Date registrationDate = new Date(new Long("1222249782000").longValue());
        Date expirationDate = new Date(new Long("1222249782000").longValue());
        return createFile(name, path, registerer, registrationDate, expirationDate);
    }

    @Transactional
    @Test
    public final void testCreateFileFailNonExistingOwner()
    {
        IFileDAO fileDAO = daoFactory.getFileDAO();
        FileDTO file1 = createSampleFile();
        file1.getRegisterer().setID(-1L);
        boolean exceptionThrown = false;
        try
        {
            fileDAO.createFile(file1);
        } catch (Exception e)
        {
            exceptionThrown = true;
        } finally
        {
            assertTrue(exceptionThrown);
        }
    }

    @Transactional
    @Test
    public final void testCreateFileFailNonExistingSharingUser()
    {

        IFileDAO fileDAO = daoFactory.getFileDAO();
        FileDTO file1 = createSampleFile();
        file1.getSharingUsers().get(0).setID(-1L);
        boolean exceptionThrown = false;
        try
        {
            fileDAO.createFile(file1);
        } catch (Exception e)
        {
            exceptionThrown = true;
        } finally
        {
            assertTrue(exceptionThrown);
        }
    }

    @Transactional
    @Test
    public final void testCreateFile()
    {
        IFileDAO fileDAO = daoFactory.getFileDAO();
        // no file in database
        List<FileDTO> files = fileDAO.listFiles();
        assertEquals(0, files.size());
        // create new sample file
        FileDTO sampleFile = createSampleFile();
        assertNull(sampleFile.getID());
        // save file in database
        fileDAO.createFile(sampleFile);
        assertNotNull(sampleFile.getID());
        // check if number of files in database increased
        files = fileDAO.listFiles();
        assertEquals(1, files.size());
        assertEquals(sampleFile.getID(), files.get(0).getID());
        assertEquals(sampleFile.getName(), files.get(0).getName());
        assertEquals(sampleFile.getPath(), files.get(0).getPath());
        assertEquals(sampleFile.getRegisterer().getID(), files.get(0).getRegisterer().getID());
        assertEquals(sampleFile.getRegistrationDate(), files.get(0).getRegistrationDate());
        assertEquals(sampleFile.getExpirationDate(), files.get(0).getExpirationDate());
    }

    @Transactional
    @Test
    public final void testDeleteFile()
    {
        FileDTO sampleFile = createSampleFile();
        IFileDAO fileDAO = daoFactory.getFileDAO();
        fileDAO.createFile(sampleFile);
        assertNotNull(fileDAO.tryGetFile(sampleFile.getID()));
        fileDAO.deleteFile(sampleFile.getID());
        assertNull(fileDAO.tryGetFile(sampleFile.getID()));
    }

    @Transactional
    @Test
    public final void testTryGetFile()
    {
        // Get existing file
        FileDTO sampleFile = createSampleFile();
        IFileDAO fileDAO = daoFactory.getFileDAO();
        fileDAO.createFile(sampleFile);
        assertNotNull(fileDAO.tryGetFile(sampleFile.getID()));

        // Try get non existing file
        fileDAO.deleteFile(sampleFile.getID());
        assertNull(fileDAO.tryGetFile(sampleFile.getID()));
    }

    @Transactional
    @Test
    public final void testListFiles()
    {
        IFileDAO fileDAO = daoFactory.getFileDAO();
        int numberOfFiles = 5;
        for (int i = 1; i <= numberOfFiles; i++)
        {
            FileDTO sampleFile = createSampleFile();
            sampleFile.setPath("prefix" + i + "_" + sampleFile.getPath());
            fileDAO.createFile(sampleFile);
            assertEquals(i, fileDAO.listFiles().size());
        }

    }

    private UserDTO getSampleUserFromDB()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();
        List<UserDTO> listUsers = userDAO.listUsers();
        assertTrue(listUsers.size() > 0);
        return listUsers.get(0);
    }

}