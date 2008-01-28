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
import java.util.Arrays;
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
    private final UserDTO getSampleUserFromDB()
    {
        final IUserDAO userDAO = daoFactory.getUserDAO();
        final List<UserDTO> listUsers = userDAO.listUsers();
        assertTrue(listUsers.size() > 0);
        return listUsers.get(0);
    }

    private final FileDTO createFile(final String name, final String path, final UserDTO registerer,
            final Date expirationDate)
    {
        final List<UserDTO> fileViewers = new ArrayList<UserDTO>();
        fileViewers.add(registerer);
        final FileDTO file = new FileDTO(registerer.getID());
        file.setExpirationDate(expirationDate);
        file.setName(name);
        file.setPath(path);
        file.setRegisterer(registerer);
        file.setSharingUsers(fileViewers);
        return file;
    }

    private final FileDTO createSampleFile(final UserDTO registerer)
    {
        final String name = "file.txt";
        final String path = "/files/" + registerer.getUserName() + "/" + name;
        final Date expirationDate = new Date(new Long("1222249782000").longValue());
        return createFile(name, path, registerer, expirationDate);
    }

    private final FileDTO createSampleFile()
    {
        return createSampleFile(getSampleUserFromDB());
    }

    @Transactional
    @Test
    public final void testCreateFileFailNonExistingOwner()
    {
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        final UserDTO userDTO = new UserDTO();
        userDTO.setID(new Long(-1));
        final FileDTO file1 = createSampleFile(userDTO);
        boolean exceptionThrown = false;
        try
        {
            fileDAO.createFile(file1);
        } catch (final Exception e)
        {
            exceptionThrown = true;
        } finally
        {
            assertTrue(exceptionThrown);
        }
    }

    @Transactional
    @Test
    public final void testAddSharingUsers()
    {
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        final FileDTO file1 = createSampleFile();
        fileDAO.createFile(file1);
        final Long fileId = file1.getID();
        assertNotNull(fileId);
        final List<UserDTO> sharingUsers = Arrays.asList(new UserDTO[]
            { getSampleUserFromDB() });
        fileDAO.addSharingUsers(fileId, sharingUsers);
        final FileDTO file = fileDAO.tryGetFile(fileId);
        assertNotNull(file);
        final List<UserDTO> users = file.getSharingUsers();
        assertEquals(1, users.size());
    }

    @Transactional
    @Test
    public final void testCreateFile()
    {
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        // no file in database
        List<FileDTO> files = fileDAO.listFiles();
        assertEquals(0, files.size());
        // create new sample file
        final FileDTO sampleFile = createSampleFile();
        assertNull(sampleFile.getID());
        // save file in database
        fileDAO.createFile(sampleFile);
        assertNotNull(sampleFile.getID());
        // check if number of files in database increased
        files = fileDAO.listFiles();
        assertEquals(1, files.size());
        final FileDTO file = files.get(0);
        assertEquals(sampleFile.getID(), file.getID());
        assertEquals(sampleFile.getName(), file.getName());
        assertEquals(sampleFile.getPath(), file.getPath());
        assertEquals(sampleFile.getRegistererId(), file.getRegistererId());
        assertNotNull(file.getRegistrationDate());
        assertEquals(sampleFile.getExpirationDate(), file.getExpirationDate());
    }

    @Transactional
    @Test
    public final void testDeleteFile()
    {
        final FileDTO sampleFile = createSampleFile();
        final IFileDAO fileDAO = daoFactory.getFileDAO();
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
        final FileDTO sampleFile = createSampleFile();
        final IFileDAO fileDAO = daoFactory.getFileDAO();
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
        final IFileDAO fileDAO = daoFactory.getFileDAO();
        final int numberOfFiles = 5;
        for (int i = 1; i <= numberOfFiles; i++)
        {
            final FileDTO sampleFile = createSampleFile();
            sampleFile.setPath("prefix" + i + "_" + sampleFile.getPath());
            fileDAO.createFile(sampleFile);
            assertEquals(i, fileDAO.listFiles().size());
        }

    }

    @Transactional
    @Test
    public final void testGetExpiredFiles()
    {
        Date date = new Date();
        Long past = 0L;
        Long future = date.getTime() * 2;
        IFileDAO fileDAO = daoFactory.getFileDAO();
        List<FileDTO> expiredFiles = fileDAO.getExpiredFiles();
        assertEquals(0, expiredFiles.size());
        int numberOfExpiredFiles = 2;
        for (int i = 1; i <= numberOfExpiredFiles; i++)
        {
            createFileWithExpirationTimeAndNumber(past, fileDAO, i);
        }
        createFileWithExpirationTimeAndNumber(future, fileDAO, numberOfExpiredFiles + 1);
        assertEquals(numberOfExpiredFiles, fileDAO.getExpiredFiles().size());

    }

    /**
     * Saves in DB sample file with <code>path = prefix_number_sufix</code> and expiration date created from
     * <code>expirationTime</code>. If expirationTime is null then default time is used.
     */
    private void createFileWithExpirationTimeAndNumber(Long expirationTime, IFileDAO fileDAO, int i)
    {
        FileDTO sampleFile = createSampleFile();
        sampleFile.setPath("prefix" + i + "_" + sampleFile.getPath());
        if (expirationTime != null)
        {
            sampleFile.setExpirationDate(new Date(expirationTime));
        }
        fileDAO.createFile(sampleFile);
        assertEquals(i, fileDAO.listFiles().size());
    }

}