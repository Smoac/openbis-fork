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

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.ExperimentIdentifierTest;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;

/**
 * Test cases for corresponding {@link ExperimentIdentifierWithUUID} class.
 * 
 * @author Christian Ribeaud
 */
public final class ExperimentIdentifierWithUUIDTest extends AbstractFileSystemTestCase
{
    final static ExperimentIdentifierWithUUID createExperimentIdentifierWithUUID()
    {
        return new ExperimentIdentifierWithUUID(SampleWithOwnerTest.INSTANCE_CODE,
                SampleWithOwnerTest.INSTANCE_UUID, SampleWithOwnerTest.SPACE_CODE,
                ExperimentIdentifierTest.PROJECT_CODE, ExperimentIdentifierTest.EXPERMENT_CODE);
    }

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new ExperimentIdentifierWithUUID(SampleWithOwner.INSTANCE_CODE, null,
                    SampleWithOwner.SPACE_CODE, ExperimentIdentifierTest.PROJECT_CODE,
                    ExperimentIdentifierTest.EXPERMENT_CODE);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            new ExperimentIdentifierWithUUID(SampleWithOwner.INSTANCE_CODE, "",
                    SampleWithOwner.SPACE_CODE, ExperimentIdentifierTest.PROJECT_CODE,
                    ExperimentIdentifierTest.EXPERMENT_CODE);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        new ExperimentIdentifierWithUUID(SampleWithOwner.INSTANCE_CODE,
                SampleWithOwner.INSTANCE_UUID, SampleWithOwner.SPACE_CODE,
                ExperimentIdentifierTest.PROJECT_CODE, ExperimentIdentifierTest.EXPERMENT_CODE);
    }

    @Test
    public final void testLoadFrom()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final ExperimentIdentifierWithUUID experimentIdentifier =
                createExperimentIdentifierWithUUID();
        experimentIdentifier.saveTo(directory);
        final ExperimentIdentifierWithUUID loaded =
                ExperimentIdentifierWithUUID.loadFrom(directory);
        assertEquals(experimentIdentifier, loaded);
        assertNotNull(loaded.getInstanceUUID());
        assertEquals(experimentIdentifier.getInstanceUUID(), loaded.getInstanceUUID());
    }
}
