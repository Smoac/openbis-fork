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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Tests for {@link SimpleFileBasedHierarchicalContent}
 * 
 * @author Piotr Buczek
 */
public class SimpleFileBasedHierarchicalContentTest extends AbstractFileSystemTestCase
{

    private File rootDir;

    private File file1;

    private File file2;

    private File subDir;

    private File subFile1;

    private File subFile2;

    private File subFile3;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        rootDir = new File(workingDirectory, "rootDir");
        rootDir.mkdirs();
        file1 = new File(rootDir, "file1");
        file2 = new File(rootDir, "file2");
        subDir = new File(rootDir, "subDir");
        subDir.mkdirs();
        subFile1 = new File(subDir, "subFile1");
        subFile2 = new File(subDir, "subFile2");
        subFile3 = new File(subDir, "subFile3");
        for (File f : Arrays.asList(file1, file2, subFile1, subFile2, subFile3))
        {
            FileUtilities.writeToFile(f, f.getName() + " data");
        }
    }

    @Test
    public void testFailWithNonExistentRoot()
    {
        final File fakeFile = new File(workingDirectory, "fakeFile");
        try
        {
            new SimpleFileBasedHierarchicalContent(fakeFile);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals(fakeFile.getAbsolutePath() + " doesn't exist", ex.getMessage());
        }
    }

    @Test
    public void testFailWithNonDirectoryRoot() throws IOException
    {
        final File rootFile = new File(workingDirectory, "rootFile");
        rootFile.createNewFile();
        try
        {
            new SimpleFileBasedHierarchicalContent(rootFile);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals(rootFile.getAbsolutePath() + " is not a directory", ex.getMessage());
        }
    }

    @Test
    public void testRootNodeFileAccess()
    {
        SimpleFileBasedHierarchicalContent content =
                new SimpleFileBasedHierarchicalContent(rootDir);
        IHierarchicalContentNode rootNode = content.getRootNode();
        checkDirNode(rootDir, rootNode);
    }

    @Test
    public void testRootNodeAccessChildren() throws IOExceptionUnchecked, IOException
    {
        SimpleFileBasedHierarchicalContent content =
                new SimpleFileBasedHierarchicalContent(rootDir);
        IHierarchicalContentNode rootNode = content.getRootNode();

        List<IHierarchicalContentNode> childNodes = rootNode.getChildNodes();
        List<File> expectedFiles = Arrays.asList(file1, file2, subDir);
        Set<File> remainingFiles = new HashSet<File>(expectedFiles);
        assertEquals(expectedFiles.size(), childNodes.size());
        for (IHierarchicalContentNode node : childNodes)
        {
            File nodeFile = node.getFile();
            assertTrue("unexpected file: " + nodeFile, expectedFiles.contains(nodeFile));
            remainingFiles.remove(nodeFile);
            if (nodeFile.isDirectory())
            {
                checkDirNode(nodeFile, node);
            } else
            {
                checkFileNode(nodeFile, node);
            }
        }
        assertTrue("missing files: " + remainingFiles, remainingFiles.isEmpty());
    }

    @Test
    public void testGetNodeByRelativePath() throws IOExceptionUnchecked, IOException
    {
        SimpleFileBasedHierarchicalContent content =
                new SimpleFileBasedHierarchicalContent(rootDir);

        // top level file
        IHierarchicalContentNode fileNode1 = content.getNode(file1.getName());
        checkFileNode(file1, fileNode1);

        // top level dir
        IHierarchicalContentNode subDirNode = content.getNode(subDir.getName());
        checkDirNode(subDir, subDirNode);

        // 2nd level file
        IHierarchicalContentNode subFileNode1 =
                content.getNode(subDir.getName() + File.separator + subFile1.getName());
        checkFileNode(subFile1, subFileNode1);
    }

    @Test
    public void testGetNodeByFakeRelativePath() throws IOExceptionUnchecked, IOException
    {
        SimpleFileBasedHierarchicalContent content =
                new SimpleFileBasedHierarchicalContent(rootDir);

        // top level fake file
        String fakeFileName = "fakeFileName";
        String fakeFileExpectedPath = rootDir.getPath() + File.separator + fakeFileName;
        IHierarchicalContentNode fakeFileNode = content.getNode(fakeFileName);
        checkFakeFileNode(fakeFileName, fakeFileExpectedPath, fakeFileNode);

        // 2nd level fake file
        String subFakeFileRelativePath = subDir.getName() + File.separator + fakeFileName;
        String subFakeFileExpectedPath = subDir.getPath() + File.separator + fakeFileName;
        IHierarchicalContentNode subFakeFileNode = content.getNode(subFakeFileRelativePath);
        checkFakeFileNode(fakeFileName, subFakeFileExpectedPath, subFakeFileNode);
    }

    private static void checkFileNode(File expectedFile, final IHierarchicalContentNode fileNode)
            throws IOExceptionUnchecked, IOException
    {
        assertFalse(fileNode.isDirectory());
        assertEquals(expectedFile, fileNode.getFile());
        assertEquals(expectedFile.getName(), fileNode.getName());
        assertEquals(expectedFile.length(), fileNode.getSize());

        final String expectedFileData = expectedFile.getName() + " data";
        // check random access to file content
        IRandomAccessFile fileContent = fileNode.getFileContent();
        assertEquals(expectedFileData, fileContent.readLine());
        assertEquals(null, fileContent.readLine());
        fileContent.seek(expectedFile.getName().length());
        assertEquals(" data", fileContent.readLine());
        // check sequential input stream read
        assertEquals("[" + expectedFileData + "]", IOUtils.readLines(fileNode.getInputStream())
                .toString());

        assertUnsupportedFileOperationOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    fileNode.getChildNodes();
                }
            });
    }

    private static void checkFakeFileNode(String fakeFileName, String fakeFileExpectedPath,
            IHierarchicalContentNode fakeFileNode) throws IOExceptionUnchecked, IOException
    {
        assertFalse(fakeFileNode.isDirectory());
        assertEquals(fakeFileName, fakeFileNode.getName());
        assertEquals(0, fakeFileNode.getSize());
        assertEquals(fakeFileExpectedPath, fakeFileNode.getFile().toString());
        assertIOExceptionOnFileContentAccess(fakeFileNode);
        assertIOExceptionOnInputStreamAccess(fakeFileNode);
    }

    private static void checkDirNode(File expectedDir, final IHierarchicalContentNode dirNode)
    {
        assertTrue(dirNode.isDirectory());
        assertEquals(expectedDir, dirNode.getFile());
        assertEquals(expectedDir.getName(), dirNode.getName());
        assertEquals(expectedDir.list().length, dirNode.getChildNodes().size());
        assertUnsupportedDirectoryOperationOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    dirNode.getSize();
                }
            });
        assertUnsupportedDirectoryOperationOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    dirNode.getFileContent();
                }
            });
        assertUnsupportedDirectoryOperationOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    dirNode.getInputStream();
                }
            });
    }

    private static void assertIOExceptionOnFileContentAccess(final IHierarchicalContentNode node)
    {
        assertIOExceptionThrownOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    node.getFileContent();
                }
            });
    }

    private static void assertIOExceptionOnInputStreamAccess(final IHierarchicalContentNode node)
    {
        assertIOExceptionThrownOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    node.getInputStream();
                }
            });
    }

    private static void assertIOExceptionThrownOnAction(IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("expected IOException");
        } catch (IOExceptionUnchecked ex)
        {
            // no expectation - IOException messages are OS dependent
        }
    }

    private static void assertUnsupportedDirectoryOperationOnAction(IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex)
        {
            assertEquals(AbstractHierarchicalContentNode.OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY,
                    ex.getMessage());
        }
    }

    private static void assertUnsupportedFileOperationOnAction(IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex)
        {
            assertEquals(AbstractHierarchicalContentNode.OPERATION_SUPPORTED_ONLY_FOR_A_DIRECTORY,
                    ex.getMessage());
        }
    }

    private interface IDelegatedAction
    {
        void execute();
    }
}
