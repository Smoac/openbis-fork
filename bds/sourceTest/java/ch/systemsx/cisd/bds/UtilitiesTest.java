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

package ch.systemsx.cisd.bds;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.io.File;

import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link Utilities} class.
 * 
 * @author Christian Ribeaud
 */
public class UtilitiesTest extends AbstractFileSystemTestCase
{
    @Test
    public final void testGetNumber()
    {
        final IDirectory directory = (IDirectory) NodeFactory.createNode(workingDirectory);
        final String key = "age";
        final String value = "35";
        final IFile file = directory.addKeyValuePair(key, value);
        final File[] listFiles = workingDirectory.listFiles();
        assertEquals(1, listFiles.length);
        assertEquals(key, listFiles[0].getName());
        assertEquals(value, file.getStringContent().trim());
        try
        {
            Utilities.getNumber(null, null);
            fail("Directory and name can not be null.");
        } catch (AssertionError ex)
        {
            // Nothing to do here
        }
        try
        {
            Utilities.getNumber(directory, "doesNotExist");
            fail("File 'doesNotExist' missing");
        } catch (DataStructureException ex)
        {
            // Nothing to do here
        }
        assertEquals(35, Utilities.getNumber(directory, key));
    }
}