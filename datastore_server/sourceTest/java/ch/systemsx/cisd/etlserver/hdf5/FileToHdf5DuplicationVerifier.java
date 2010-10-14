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
import org.testng.AssertJUnit;

import ch.systemsx.cisd.hdf5.IHDF5Reader;

/**
 * Helper class that verifies that a file structure is matched by the HDF5 structure.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class FileToHdf5DuplicationVerifier extends AssertJUnit
{

    private final File sourceFolderOrFile;

    private final Hdf5Container container;

    private final IHDF5Reader reader;

    public FileToHdf5DuplicationVerifier(File sourceFolderOrFile, Hdf5Container container)
    {
        this.sourceFolderOrFile = sourceFolderOrFile;
        this.container = container;
        this.reader = container.createReader();
    }

    public void verifyDuplicate()
    {
        assertTrue(container.getHdf5File().length() > 0);

        if (sourceFolderOrFile.isFile())
        {
            // Check that the HDF5 container is contains the same file as the provided file
            String name = sourceFolderOrFile.getName();
            String hdf5Path = "/" + name;
            verifyDataSet(sourceFolderOrFile, hdf5Path);
            return;
        }

        verifyGroup(sourceFolderOrFile, "/", null);
    }

    private void verifyGroup(File directory, String groupPath, String parentPathOrNull)
    {
        File[] files = directory.listFiles();
        for (File fileOrDirectory : files)
        {
            // Skip the subversion folders
            if (".svn".matches(fileOrDirectory.getName()))
            {
                continue;
            }
            if (fileOrDirectory.isDirectory())
            {
                // recursively verify the directory
                verifyGroup(fileOrDirectory, fileOrDirectory.getName(), parentPathOrNull
                        + groupPath + "/");
            } else
            {
                // verify the data set
                verifyDataSet(fileOrDirectory, groupPath + fileOrDirectory.getName());
            }

        }
    }

    private void verifyDataSet(File file, String hdf5Path)
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

    @SuppressWarnings("unused")
    private void printData(byte[] fileContent, byte[] content)
    {
        for (int i = 0; i < fileContent.length; ++i)
        {
            System.out.print(fileContent[i]);
            System.out.print(" ");
        }
        System.out.print("\n");
        for (int i = 0; i < content.length; ++i)
        {
            System.out.print(content[i]);
            System.out.print(" ");
        }
        System.out.print("\n");
        System.out.println("O: " + fileContent + "\n" + "N: " + content);

        System.out.println(new String(fileContent));
        System.out.println(new String(content));
    }
}
