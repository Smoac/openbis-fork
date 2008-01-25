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
        String name = "file01.txt";
        UserDTO registerer = getSampleUserFromDB();
        String path = "/files/" + registerer.getUserName() + "/";
        Date registrationDate = new Date(new Long("1222249782000").longValue());
        Date expirationDate = new Date(new Long("1222249782000").longValue());
        return createFile(name, path, registerer, registrationDate, expirationDate);
    }

    @Transactional
    public final void testFileDAO()
    {
        IFileDAO fileDAO = daoFactory.getFileDAO();
        // no file in database
        List<FileDTO> files = fileDAO.listFiles();
        assertEquals(0, files.size());
        // create new sample file
        FileDTO file1 = createSampleFile();
        assertNull(file1.getID());
        // save file in database
        fileDAO.createFile(file1);
        assertNotNull(file1.getID());
        // check if number of files in database increased
        files = fileDAO.listFiles();
        assertEquals(1, files.size());
        assertEquals(file1.getID(), files.get(0).getID());
        assertEquals(file1.getName(), files.get(0).getName());
        assertEquals(file1.getPath(), files.get(0).getPath());
        assertEquals(file1.getRegisterer().getID(), files.get(0).getRegisterer().getID());
        assertEquals(file1.getRegistrationDate(), files.get(0).getRegistrationDate());
        assertEquals(file1.getExpirationDate(), files.get(0).getExpirationDate());
        // delete file
        fileDAO.deleteFile(file1.getID());
        files = fileDAO.listFiles();
        assertEquals(0, files.size());
        // try get file with id
        FileDTO file3 = createSampleFile();
        file3.setName("file02.txt");
        fileDAO.createFile(file3);
        FileDTO file4 = fileDAO.tryGetFile(file3.getID());
        assertEquals(file3.getSharingUsers().size(), file4.getSharingUsers().size());

        setComplete();
    }

    @Transactional
    @Test
    public final void testCreateFile()
    { // with-without

    }

    @Transactional
    @Test
    public final void testCreateFileFail()
    {

    }

    @Transactional
    @Test
    public final void testDeleteFile()
    {

    }

    @Transactional
    @Test
    public final void testTryGetFile()
    {

    }

    @Transactional
    @Test
    public final void testListFiles()
    {

    }

    private UserDTO getSampleUserFromDB()
    {
        IUserDAO userDAO = daoFactory.getUserDAO();
        List<UserDTO> listUsers = userDAO.listUsers();
        assertTrue(listUsers.size() > 0);
        return listUsers.get(0);
    }

}