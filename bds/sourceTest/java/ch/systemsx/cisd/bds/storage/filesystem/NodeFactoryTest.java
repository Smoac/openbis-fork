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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class NodeFactoryTest extends StorageTestCase
{
    @Test
    public void testCreateFileNode()
    {
        java.io.File file = new java.io.File(TEST_DIR, "text.txt");
        FileUtilities.writeToFile(file, "hello");
        INode node = NodeFactory.createNode(file);
        assertTrue(node instanceof IFile);
        assertEquals("text.txt", node.getName());
    }
    
    @Test
    public void testCreateDirectoryNode()
    {
        java.io.File file = new java.io.File(TEST_DIR, "dir");
        file.mkdir();
        INode node = NodeFactory.createNode(file);
        assertTrue(node instanceof IDirectory);
        assertEquals("dir", node.getName());
    }
}
