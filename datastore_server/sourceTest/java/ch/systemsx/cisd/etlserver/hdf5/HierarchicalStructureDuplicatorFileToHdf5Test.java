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
import ch.systemsx.cisd.etlserver.hdf5.Hdf5Container.IHdf5ReaderClient;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;

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
        container.runWriterClient(false,
                new HierarchicalStructureDuplicatorFileToHdf5.DuplicatorWriterClient(sourceFolder));
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
        container.runWriterClient(true,
                new HierarchicalStructureDuplicatorFileToHdf5.DuplicatorWriterClient(sourceFolder));
        verifyDuplicate(sourceFolder);
    }

    /**
     * Check that an individual file is put into an HDF5 container as expected
     */
    @Test
    public void testIndividualFileDuplicationCompression()
    {
        File sourceFolder = getTestData("basic-file-structure/file0.txt");
        container.runWriterClient(true,
                new HierarchicalStructureDuplicatorFileToHdf5.DuplicatorWriterClient(sourceFolder));
        verifyDuplicate(sourceFolder);
    }

    /**
     * Convert a portion of the files, then re-convert.
     */
    @Test
    public void testAbortAndContinue()
    {
        File sourceFile = getTestData("basic-file-structure/file0.txt");
        container.runWriterClient(true,
                new HierarchicalStructureDuplicatorFileToHdf5.DuplicatorWriterClient(sourceFile));

        File sourceFolder = getTestData("basic-file-structure");
        container.runWriterClient(true,
                new HierarchicalStructureDuplicatorFileToHdf5.DuplicatorWriterClient(sourceFolder));

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
        container.runWriterClient(false,
                new HierarchicalStructureDuplicatorFileToHdf5.DuplicatorWriterClient(sourceFolder));
    }

    @Test(expectedExceptions =
        { IllegalArgumentException.class })
    public void testNonexistentFile()
    {
        File sourceFolder = getTestData("does-not-exist");
        container.runWriterClient(false,
                new HierarchicalStructureDuplicatorFileToHdf5.DuplicatorWriterClient(sourceFolder));
    }

    private File getTestData(String folderOrFile)
    {
        return new File("sourceTest/java/ch/systemsx/cisd/etlserver/hdf5/", folderOrFile);
    }

    private void verifyDuplicate(final File sourceFolderOrFile)
    {
        container.runReaderClient(new IHdf5ReaderClient()
            {
                public void runWithSimpleReader(IHDF5SimpleReader reader)
                {
                    FileToHdf5DuplicationVerifier verifier =
                            new FileToHdf5DuplicationVerifier(sourceFolderOrFile, container, reader);
                    verifier.verifyDuplicate();
                }
            });
    }
}
