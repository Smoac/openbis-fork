/*
 * Copyright 2008 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link DataStructureV1_1} class.
 * 
 * @author Christian Ribeaud
 */
public final class DataStructureV1_1Test extends AbstractFileSystemTestCase
{
    private static final Sample SAMPLE = new Sample("a", "CELL_PLATE", "b");

    private FileStorage storage;

    private DataStructureV1_1 dataStructure;

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        storage = new FileStorage(workingDirectory);
        dataStructure = new DataStructureV1_1(storage);
    }

    @Test
    public void testGetVersion()
    {
        dataStructure.create();
        assertEquals(new Version(1, 1), dataStructure.getVersion());
    }

    @Test
    public void testSetSample()
    {
        dataStructure.create();
        boolean fail = true;
        try
        {
            dataStructure.setSample(SAMPLE);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        dataStructure.setSample(new SampleWithOwner(SAMPLE, "",
                SampleWithOwnerTest.DATABASE_INSTANCE_CODE));
    }

    @Test
    public final void testGetSample()
    {
        dataStructure.create();
        final SampleWithOwner sampleWithOwner =
                new SampleWithOwner(SAMPLE, "", SampleWithOwnerTest.DATABASE_INSTANCE_CODE);
        dataStructure.setSample(sampleWithOwner);
        final Sample sample = dataStructure.getSample();
        assertTrue(sample instanceof SampleWithOwner);
        final String databaseInstanceCode = sampleWithOwner.getDatabaseInstanceCode();
        assertTrue(databaseInstanceCode.length() > 0);
        assertEquals(databaseInstanceCode, ((SampleWithOwner) sample).getDatabaseInstanceCode());
    }

    @Test
    public final void testBackwardCompatible()
    {
        DataStructureV1_0Test.createExampleDataStructure(storage, new Version(1, 1));
        dataStructure.open();
        final Sample sample = dataStructure.getSample();
        assertFalse(sample instanceof SampleWithOwner);
        dataStructure.close();
    }
}
