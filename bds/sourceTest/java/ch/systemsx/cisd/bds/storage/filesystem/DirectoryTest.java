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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.exception.StorageException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;

/**
 * Test cases for corresponding {@link Directory} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class DirectoryTest extends AbstractFileSystemTestCase
{

    private final void testInternalAddFile(final boolean move)
    {
        File dir = new File(workingDirectory, "dir");
        dir.mkdirs();
        FileUtilities.writeToFile(new File(dir, "p1"), "property 1");
        File subDir = new File(dir, "subdir");
        subDir.mkdir();
        FileUtilities.writeToFile(new File(subDir, "p2"), "property 2");
        File dest = new File(workingDirectory, "destination");
        dest.mkdir();
        final IDirectory directory = NodeFactory.createDirectoryNode(dest);
        INode copiedDir = directory.addFile(dir, null, move);
        assertEquals("dir", copiedDir.getName());
        assertTrue(copiedDir instanceof IDirectory);
        File copiedRealDir = new File(dest, "dir");
        assertTrue(copiedRealDir.isDirectory());
        IDirectory cd = (IDirectory) copiedDir;
        INode node = cd.tryGetNode("p1");
        assertNotNull(node);
        assertTrue(node instanceof IFile);
        assertEquals("property 1\n", ((IFile) node).getStringContent());
        assertEquals("property 1\n", FileUtilities.loadToString(new File(copiedRealDir, "p1")));
        node = cd.tryGetNode("subdir");
        assertEquals("subdir", node.getName());
        assertNotNull(node);
        assertTrue(node instanceof IDirectory);
        node = ((IDirectory) node).tryGetNode("p2");
        File copiedRealSubDir = new File(copiedRealDir, "subdir");
        assertTrue(copiedRealSubDir.isDirectory());
        assertEquals("p2", node.getName());
        assertTrue(node instanceof IFile);
        assertEquals("property 2\n", ((IFile) node).getStringContent());
        assertEquals("property 2\n", FileUtilities.loadToString(new File(copiedRealSubDir, "p2")));
        assertEquals(move == false, new File(workingDirectory, "dir").exists());
    }

    @Test
    public void testGetName()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        assertEquals(workingDirectory.getName(), directory.getName());
    }

    @Test
    public void testMakeDirectory()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        IDirectory subdirectory = directory.makeDirectory("sub-directory");

        assertEquals("sub-directory", subdirectory.getName());
        File subdir = new File(workingDirectory, "sub-directory");
        assertEquals(true, subdir.exists());
        assertEquals(true, subdir.isDirectory());
    }

    @Test
    public void testMakeDirectoryTwice()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        directory.makeDirectory("sub-directory");
        IDirectory subdirectory = directory.makeDirectory("sub-directory");

        assertEquals("sub-directory", subdirectory.getName());
        File subdir = new File(workingDirectory, "sub-directory");
        assertEquals(true, subdir.exists());
        assertEquals(true, subdir.isDirectory());
    }

    @Test
    public void testMakeDirectoryButThereIsAlreadyAFileWithSameName()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        directory.addKeyValuePair("sub-directory", "value");
        try
        {
            directory.makeDirectory("sub-directory");
            AssertJUnit.fail("StorageException expected.");
        } catch (StorageException e)
        {
            assertTrue(e.getMessage().indexOf("sub-directory") >= 0);
        }
    }

    @Test
    public void testAddKeyValuePair()
    {
        IFile stringFile =
                NodeFactory.createDirectoryNode(workingDirectory).addKeyValuePair("answer", "42");
        assertEquals("42\n", FileUtilities.loadToString(new File(workingDirectory, "answer")));
        assertEquals("42\n", stringFile.getStringContent());
    }

    @Test
    public void testExtractTo()
    {
        final IDirectory directory = createDirectory("dir");
        directory.addKeyValuePair("p1", "property 1");
        IDirectory subdir = directory.makeDirectory("subdir");
        subdir.addKeyValuePair("p2", "property 2");
        File destination = new File(workingDirectory, "destination");
        assertFalse(destination.exists());
        directory.extractTo(destination);
        assertTrue(destination.exists());
        File copiedDir = new File(destination, "dir");
        assertTrue(copiedDir.exists());
        assertEquals("property 1\n", FileUtilities.loadToString(new File(copiedDir, "p1")));
        File copiedSubDir = new File(copiedDir, "subdir");
        assertTrue(copiedSubDir.isDirectory());
        assertEquals("property 2\n", FileUtilities.loadToString(new File(copiedSubDir, "p2")));
        // Source directory still exists
        assertEquals(true, new File(workingDirectory, "dir").exists());
    }

    @Test
    public void testMoveTo()
    {
        final IDirectory directory = createDirectory("dir");
        directory.addKeyValuePair("p1", "property 1");
        IDirectory subdir = directory.makeDirectory("subdir");
        subdir.addKeyValuePair("p2", "property 2");
        subdir.moveTo(workingDirectory);

        Iterator<INode> iterator = directory.iterator();
        assertEquals(true, iterator.hasNext());
        INode node = iterator.next();
        assertEquals("p1", node.getName());
        assertEquals("property 1", ((IFile) node).getStringContent().trim());
        assertEquals(false, iterator.hasNext());
        File subdir2 = new File(workingDirectory, "subdir");
        assertEquals(true, subdir2.isDirectory());
        assertEquals("property 2", FileUtilities.loadToString(new File(subdir2, "p2")).trim());
    }

    private IDirectory createDirectory(String name)
    {
        File dir = new File(workingDirectory, name);
        dir.mkdirs();
        return NodeFactory.createDirectoryNode(dir);
    }

    @Test
    public void testMoveSymbolicLink() throws IOException
    {
        final IDirectory directory = createDirectory("parent");
        IDirectory original = directory.makeDirectory("original");
        IDirectory subdir = directory.makeDirectory("child");
        File linkFile = new File(asFile(directory), "link");
        // create a link to original dir in a parent dir
        boolean ok = SoftLinkMaker.createSymbolicLink(asFile(original).getAbsoluteFile(), linkFile);
        assertTrue(ok);

        // this is what we test: move the link from parent dir to child dir
        AbstractNode.moveFileToDirectory(linkFile, asFile(subdir), null);

        assertFalse(linkFile.exists());
        File newLink = new File(asFile(subdir), linkFile.getName());
        assertTrue(newLink.exists());
        assertEquals(asFile(original).getCanonicalPath(), newLink.getCanonicalPath());
    }

    private static File asFile(final IDirectory directory)
    {
        return new File(directory.getPath());
    }

    @Test
    public void testAddFileWithCopy()
    {
        testInternalAddFile(false);
    }

    @Test
    public void testAddFileWithMove()
    {
        testInternalAddFile(true);
    }
}
