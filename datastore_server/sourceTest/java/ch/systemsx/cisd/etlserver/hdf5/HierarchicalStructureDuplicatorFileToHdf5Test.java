/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.hdf5;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class HierarchicalStructureDuplicatorFileToHdf5Test extends AbstractFileSystemTestCase
{
    private File containerFile;

    private Hdf5Container container;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        containerFile = new File(workingDirectory, "test-container.h5");
        container = new Hdf5Container(containerFile);
    }

    /**
     * Create a basic file structure and see that the duplicator correctly translated it into the
     * HDF5 container.
     */
    @Test
    public void testBasicDuplicationNoCompression()
    {
        File sourceFolder = getSourceFolder("basic-file-structure");
        HierarchicalStructureDuplicatorFileToHdf5 duplicator =
                new HierarchicalStructureDuplicatorFileToHdf5(sourceFolder, createWriter(false));
        duplicator.makeDuplicate();
        verifyDuplicate(sourceFolder);
    }

    /**
     * Create a basic file structure and see that the duplicator correctly translated it into the
     * HDF5 container.
     */
    @Test
    public void testBasicDuplicationCompression()
    {
        File sourceFolder = getSourceFolder("basic-file-structure");
        HierarchicalStructureDuplicatorFileToHdf5 duplicator =
                new HierarchicalStructureDuplicatorFileToHdf5(sourceFolder, createWriter(true));
        duplicator.makeDuplicate();
        verifyDuplicate(sourceFolder);
    }

    /**
     * Check that an individual file is put into an HDF5 container as expected
     */
    @Test
    public void testIndividualFileDuplicationCompression()
    {
        File sourceFolder = getSourceFolder("basic-file-structure/file0.txt");
        HierarchicalStructureDuplicatorFileToHdf5 duplicator =
                new HierarchicalStructureDuplicatorFileToHdf5(sourceFolder, createWriter(true));
        duplicator.makeDuplicate();
        verifyDuplicate(sourceFolder);
    }

    /**
     * Symbolic links are not supported
     */
    @Test(expectedExceptions =
        { IllegalArgumentException.class })
    public void testSymbolicLinks()
    {
        File sourceFolder = getSourceFolder("file-structure-with-links");
        HierarchicalStructureDuplicatorFileToHdf5 duplicator =
                new HierarchicalStructureDuplicatorFileToHdf5(sourceFolder, createWriter(false));
        duplicator.makeDuplicate();
    }

    @Test(expectedExceptions =
        { IllegalArgumentException.class })
    public void testNonexistentFile()
    {
        File sourceFolder = getSourceFolder("does-not-exist");
        HierarchicalStructureDuplicatorFileToHdf5 duplicator =
                new HierarchicalStructureDuplicatorFileToHdf5(sourceFolder, createWriter(false));
        duplicator.makeDuplicate();
    }

    private File getSourceFolder(String folderName)
    {
        return new File("sourceTest/java/ch/systemsx/cisd/etlserver/hdf5/", folderName);
    }

    private IHDF5SimpleWriter createWriter(boolean isContentCompressed)
    {
        return container.createSimpleWriter(isContentCompressed);
    }

    private void verifyDuplicate(File sourceFolderOrFile)
    {
        assertTrue(container.getHdf5File().length() > 0);

        IHDF5Reader reader = container.createReader();

        if (sourceFolderOrFile.isFile())
        {
            // Check that the HDF5 container is contains the same file as the provided file
            String name = sourceFolderOrFile.getName();
            String hdf5Path = "/" + name;
            verifyDataSet(sourceFolderOrFile, reader, hdf5Path);
            return;
        }

        verifyGroup(sourceFolderOrFile, reader, "/", null);
    }

    private void verifyGroup(File directory, IHDF5Reader reader, String groupPath,
            String parentPathOrNull)
    {
        File[] files = directory.listFiles();
        for (File fileOrDirectory : files)
        {
            if (fileOrDirectory.isDirectory())
            {
                // recursively verify the directory
                verifyGroup(fileOrDirectory, reader, fileOrDirectory.getName(), parentPathOrNull
                        + groupPath + "/");
            } else
            {
                // verify the data set
                verifyDataSet(fileOrDirectory, reader, groupPath + fileOrDirectory.getName());
            }

        }
    }

    private void verifyDataSet(File file, IHDF5Reader reader, String hdf5Path)
    {
        assertTrue(reader.exists(hdf5Path));
        try
        {
            byte[] fileContent = FileUtils.readFileToByteArray(file);
            byte[] content = reader.readByteArray(hdf5Path);
            assertEquals(fileContent, content);
        } catch (IOException ex)
        {
            // This should not happen
            fail("Could not read file content " + file);
        }

    }
}
