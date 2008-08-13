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

package ch.systemsx.cisd.bds.storage;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.filesystem.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link NodeFilters} class.
 * 
 * @author Christian Ribeaud
 */
public class NodeFiltersTest extends AbstractFileSystemTestCase
{

    private final INode createNode(final String name) throws IOException
    {
        final File file = new File(workingDirectory, name);
        FileUtils.touch(file);
        return NodeFactory.createFileNode(file);
    }

    @Test
    public final void testExtensionNodeFilterWithNull()
    {
        boolean fail = true;
        try
        {
            NodeFilters.createExtensionNodeFilter(true, (String[]) null);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
    }

    @Test
    public final void testExtensionNodeFilterWithoutCase() throws IOException
    {
        final INodeFilter filter = NodeFilters.createExtensionNodeFilter(true, "tXt");
        assertTrue(filter.accept(createNode("test.txt")));
        assertTrue(filter.accept(createNode("test.TxT")));
        assertFalse(filter.accept(createNode("txt")));
        assertFalse(filter.accept(createNode("test.png")));
    }

    @Test
    public final void testExtensionNodeFilterWithCase() throws IOException
    {
        final INodeFilter filter = NodeFilters.createExtensionNodeFilter(false, "png");
        assertTrue(filter.accept(createNode("test.png")));
        assertFalse(filter.accept(createNode("test.PNG")));
    }

    @Test
    public final void testExtensionNodeMultipleExtensions() throws IOException
    {
        final INodeFilter filter = NodeFilters.createExtensionNodeFilter(true, "tif", "tiff");
        assertTrue(filter.accept(createNode("test.tif")));
        assertTrue(filter.accept(createNode("test.tiff")));
        assertTrue(filter.accept(createNode("test.TIFF")));
        assertTrue(filter.accept(createNode("test.TIF")));
    }
}
