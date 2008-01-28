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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

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

    private IFileManager fileManager;

    File fileStore;

    private IBusinessObjectFactory boFactory;

    @BeforeMethod
    public final void setUp()
    {
        daoFactory = context.mock(IDAOFactory.class);
        fileDAO = context.mock(IFileDAO.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        fileStore = workingDirectory;
        BusinessContext businessContext = new BusinessContext();
        businessContext.setFileRetention(5);
        businessContext.setFileStore(fileStore);
        fileManager = new FileManager(daoFactory, boFactory, businessContext);
    }

    @Test
    public void testDeleteExpiredFiles()
    {
        final List<FileDTO> fileDTOs = new ArrayList<FileDTO>();
        Long ownerId = 1L;
        final Long fileId = 1L;
        String filename = "file.txt";
        String path = "/" + filename;
        FileDTO fileDTO = new FileDTO(ownerId);
        fileDTO.setID(fileId);
        fileDTO.setPath(path);
        fileDTOs.add(fileDTO);
        File realFile = createRealFile(path);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getFileDAO();
                    will(returnValue(fileDAO));
                    one(fileDAO).getExpiredFiles();
                    will(returnValue(fileDTOs));
                    one(fileDAO).deleteFile(fileId);
                }
            });

        fileManager.deleteExpiredFiles();
        assertFalse(realFile.exists());
        context.assertIsSatisfied();
    }

    private File createRealFile(String path)
    {
        File realFile = new File(fileStore, path);
        boolean fileCannotBeCreated = false;
        try
        {
            realFile.createNewFile();
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

}
