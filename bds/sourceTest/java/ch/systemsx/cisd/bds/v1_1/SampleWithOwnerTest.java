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

package ch.systemsx.cisd.bds.v1_1;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;

/**
 * Test cases for corresponding {@link SampleWithOwner} class.
 * 
 * @author Christian Ribeaud
 */
public final class SampleWithOwnerTest extends AbstractFileSystemTestCase
{
    private static final String TYPE_DESCRIPTION = "typeDescription";

    private static final String TYPE_CODE = "typeCode";

    private static final String CODE = "code";

    static final String INSTANCE_CODE = "DB1";

    static final String INSTANCE_UUID = "111-222";

    static final String SPACE_CODE = "S1";

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new SampleWithOwner(CODE, TYPE_CODE, TYPE_DESCRIPTION, null, null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        try
        {
            new SampleWithOwner(CODE, TYPE_CODE, TYPE_DESCRIPTION, "", "", "");
            fail();
        } catch (final DataStructureException e)
        {
            // Nothing to do here.
        }
        try
        {
            new SampleWithOwner(CODE, TYPE_CODE, TYPE_DESCRIPTION, "", "", SPACE_CODE);
            fail();
        } catch (final DataStructureException e)
        {
            // Nothing to do here.
        }
        try
        {
            new SampleWithOwner(CODE, TYPE_CODE, TYPE_DESCRIPTION, "", INSTANCE_CODE, SPACE_CODE);
            fail();
        } catch (final DataStructureException e)
        {
            // Nothing to do here.
        }
    }

    @DataProvider
    public final Object[][] getSampleData()
    {
        return new Object[][]
            {
                { INSTANCE_UUID, INSTANCE_CODE, SPACE_CODE },
                { INSTANCE_UUID, INSTANCE_CODE, "" }, };
    }

    @Test(dataProvider = "getSampleData")
    public final void testLoadFrom(final String instanceUUID, final String instanceCode,
            final String spaceCode)
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final Sample sample =
                new SampleWithOwner(CODE, TYPE_CODE, TYPE_DESCRIPTION, instanceUUID, instanceCode,
                        spaceCode);
        sample.saveTo(directory);
        final SampleWithOwner newSample = SampleWithOwner.loadFrom(directory);
        if (spaceCode.length() > 0)
        {
            assertEquals(SPACE_CODE, newSample.getSpaceCode());
            assertEquals(INSTANCE_CODE, newSample.getInstanceCode());
            assertEquals(INSTANCE_UUID, newSample.getInstanceUUID());
        } else
        {
            assertEquals("", newSample.getSpaceCode());
            assertEquals(INSTANCE_CODE, newSample.getInstanceCode());
            assertEquals(INSTANCE_UUID, newSample.getInstanceUUID());
        }
    }
}
