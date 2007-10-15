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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataStructureFactoryTest
{
    private Mockery context;
    private IStorage storage;

    @BeforeMethod
    public void startUp()
    {
        context = new Mockery();
        storage = context.mock(IStorage.class);
    }
    
    @Test
    public void testGetDataStructureClassFor()
    {
        assertEquals(DataStructureV1_0.class, DataStructureFactory.getDataStructureClassFor(new Version(1, 0)));
        assertEquals(DataStructureV1_0.class, DataStructureFactory.getDataStructureClassFor(new Version(1, 1)));
    }
    
    @Test
    public void testGetDataStructureClassForIncompatibleVersion()
    {
        try
        {
            DataStructureFactory.getDataStructureClassFor(new Version(2, 0));
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("No class found for version V2.0", e.getMessage());
        }
    }
    
    @Test
    public void testCreateDataStructure()
    {
        context.checking(new Expectations()
            {
                {
                    one(storage).mount();
                    one(storage).getRoot();
                }

            });
        IDataStructure dataStructure = DataStructureFactory.createDataStructure(storage, new Version(1, 0));
        assertEquals(DataStructureV1_0.class, dataStructure.getClass());
    }
}
