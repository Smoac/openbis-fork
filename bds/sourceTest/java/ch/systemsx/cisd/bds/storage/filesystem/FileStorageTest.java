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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FileStorageTest extends StorageTestCase
{
    @Test
    public void testGetRoot()
    {
        FileStorage fileStorage = new FileStorage(TEST_DIR);
        fileStorage.mount();
        assertEquals(TEST_DIR.getName(), fileStorage.getRoot().getName());
    }
    
    @Test
    public void testGetRootOfNeverMountedStorage()
    {
        FileStorage fileStorage = new FileStorage(TEST_DIR);
        try
        {
            fileStorage.getRoot();
            AssertJUnit.fail("UserFailureException because storage isn't mounted.");
        } catch (UserFailureException e)
        {
            assertEquals("Can not get root of an unmounted storage.", e.getMessage());
        }
    }
    
    @Test
    public void testGetRootOfUnMountedStorage()
    {
        FileStorage fileStorage = new FileStorage(TEST_DIR);
        fileStorage.mount();
        fileStorage.unmount();
        try
        {
            fileStorage.getRoot();
            AssertJUnit.fail("UserFailureException because storage isn't mounted.");
        } catch (UserFailureException e)
        {
            assertEquals("Can not get root of an unmounted storage.", e.getMessage());
        }
    }
}
