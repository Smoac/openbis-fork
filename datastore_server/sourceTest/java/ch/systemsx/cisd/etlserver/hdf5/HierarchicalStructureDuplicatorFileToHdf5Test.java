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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
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
        File sourceFolder = getTestData("basic-file-structure");
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
        File sourceFolder = getTestData("basic-file-structure");
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
        File sourceFolder = getTestData("basic-file-structure/file0.txt");
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
        File sourceFolder = getTestData("file-structure-with-links");
        HierarchicalStructureDuplicatorFileToHdf5 duplicator =
                new HierarchicalStructureDuplicatorFileToHdf5(sourceFolder, createWriter(false));
        duplicator.makeDuplicate();
    }

    @Test(expectedExceptions =
        { IllegalArgumentException.class })
    public void testNonexistentFile()
    {
        File sourceFolder = getTestData("does-not-exist");
        HierarchicalStructureDuplicatorFileToHdf5 duplicator =
                new HierarchicalStructureDuplicatorFileToHdf5(sourceFolder, createWriter(false));
        duplicator.makeDuplicate();
    }

    private File getTestData(String folderOrFile)
    {
        return new File("sourceTest/java/ch/systemsx/cisd/etlserver/hdf5/", folderOrFile);
    }

    private IHDF5SimpleWriter createWriter(boolean isContentCompressed)
    {
        return container.createSimpleWriter(isContentCompressed);
    }

    private void verifyDuplicate(File sourceFolderOrFile)
    {
        FileToHdf5DuplicationVerifier verifier =
                new FileToHdf5DuplicationVerifier(sourceFolderOrFile, container);
        verifier.verifyDuplicate();
    }
}
