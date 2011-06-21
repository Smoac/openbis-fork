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
import java.io.InputStream;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;

/**
 * Tests for {@link AbstractHierarchicalContentNode}
 * 
 * @author Piotr Buczek
 */
public class AbstractHierarchicalContentNodeTest extends AssertJUnit
{
    @Test
    public void testFileOperationsOnFile()
    {
        final IHierarchicalContentNode fileNode = createDummyFileNode();

        // check only that no exception is thrown
        fileNode.getFileLength();
        fileNode.getFileContent();
        fileNode.getInputStream();
    }

    @Test
    public void testDirectoryOperationsFailOnFile()
    {
        final IHierarchicalContentNode fileNode = createDummyFileNode();

        assertUnsupportedDirectoryOperationOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    fileNode.getChildNodes();
                }
            });
    }

    @Test
    public void testDirectoryOperationsOnDirectory()
    {
        final IHierarchicalContentNode dirNode = createDummyDirectoryNode();

        // check only that no exception is thrown
        dirNode.getChildNodes();
    }

    @Test
    public void testFileOperationsFailOnDirectory()
    {
        final IHierarchicalContentNode dirNode = createDummyDirectoryNode();

        assertUnsupportedFileOperationOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    dirNode.getFileLength();
                }
            });
        assertUnsupportedFileOperationOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    dirNode.getFileContent();
                }
            });
        assertUnsupportedFileOperationOnAction(new IDelegatedAction()
            {
                public void execute()
                {
                    dirNode.getInputStream();
                }
            });
    }

    private void assertUnsupportedFileOperationOnAction(IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex)
        {
            assertEquals(AbstractHierarchicalContentNode.OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY,
                    ex.getMessage());
        }
    }

    private void assertUnsupportedDirectoryOperationOnAction(IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex)
        {
            assertEquals(AbstractHierarchicalContentNode.OPERATION_SUPPORTED_ONLY_FOR_A_DIRECTORY,
                    ex.getMessage());
        }
    }

    private IHierarchicalContentNode createDummyDirectoryNode()
    {
        return new AbstractHierarchicalContentNode()
            {

                public boolean isDirectory()
                {
                    return true;
                }

                @Override
                public String doGetRelativePath()
                {
                    return null;
                }

                public String getName()
                {
                    return null;
                }

                public File getFile() throws UnsupportedOperationException
                {
                    return null;
                }

                public boolean exists()
                {
                    return false;
                }

                public long getLastModified()
                {
                    return 0;
                }

                @Override
                protected InputStream doGetInputStream()
                {
                    return null;
                }

                @Override
                protected long doGetFileLength()
                {
                    return 0;
                }

                @Override
                protected IRandomAccessFile doGetFileContent()
                {
                    return null;
                }

                @Override
                protected List<IHierarchicalContentNode> doGetChildNodes()
                {
                    return null;
                }
            };
    }

    private IHierarchicalContentNode createDummyFileNode()
    {
        return new AbstractHierarchicalContentNode()
            {

                public boolean isDirectory()
                {
                    return false;
                }

                @Override
                public String doGetRelativePath()
                {
                    return null;
                }

                public String getName()
                {
                    return null;
                }

                public File getFile() throws UnsupportedOperationException
                {
                    return null;
                }

                public boolean exists()
                {
                    return false;
                }

                public long getLastModified()
                {
                    return 0;
                }

                @Override
                protected InputStream doGetInputStream()
                {
                    return null;
                }

                @Override
                protected long doGetFileLength()
                {
                    return 0;
                }

                @Override
                protected IRandomAccessFile doGetFileContent()
                {
                    return null;
                }

                @Override
                protected List<IHierarchicalContentNode> doGetChildNodes()
                {
                    return null;
                }
            };
    }

}
