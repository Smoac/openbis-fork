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

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.DataStructureFactory;
import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.ExperimentIdentifierTest;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.Version;
import ch.systemsx.cisd.bds.IDataStructure.Mode;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.bds.v1_0.DataStructureV1_0Test;

/**
 * Test cases for corresponding {@link DataStructureV1_1} class.
 * 
 * @author Christian Ribeaud
 */
public final class DataStructureV1_1Test extends AbstractFileSystemTestCase
{
    private static final Sample SAMPLE = new Sample("a", "CELL_PLATE", "b");

    private static final ExperimentIdentifier EXPERIMENT_IDENTIFIER =
            new ExperimentIdentifier(SampleWithOwnerTest.INSTANCE_CODE,
                    SampleWithOwnerTest.GROUP_CODE, ExperimentIdentifierTest.PROJECT_CODE,
                    ExperimentIdentifierTest.EXPERMENT_CODE);

    private FileStorage storage;

    private IDataStructureV1_1 dataStructure;

    private final static SampleWithOwner createSampleWithOwner()
    {
        return new SampleWithOwner(SAMPLE, SampleWithOwnerTest.INSTANCE_UUID,
                SampleWithOwnerTest.INSTANCE_CODE, "");
    }

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        storage = new FileStorage(workingDirectory);
        dataStructure =
                (IDataStructureV1_1) DataStructureFactory.createDataStructure(storage, new Version(
                        1, 1));
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
        try
        {
            dataStructure.setSample(SAMPLE);
            fail();
        } catch (final DataStructureException ex)
        {
            // Nothing to do here.
        }
        dataStructure.setSample(createSampleWithOwner());
    }

    @Test
    public final void testGetSample()
    {
        dataStructure.create();
        final SampleWithOwner sampleWithOwner = createSampleWithOwner();
        dataStructure.setSample(sampleWithOwner);
        final Sample sample = dataStructure.getSample();
        assertTrue(sample instanceof SampleWithOwner);
        final String databaseInstanceCode = sampleWithOwner.getInstanceCode();
        assertTrue(databaseInstanceCode.length() > 0);
        assertEquals(databaseInstanceCode, dataStructure.getSampleWithOwner().getInstanceCode());
    }

    @Test
    public final void testGetExperimentIdentifier()
    {
        dataStructure.create();
        final ExperimentIdentifierWithUUID experimentIdentifierWithUUID =
                ExperimentIdentifierWithUUIDTest.createExperimentIdentifierWithUUID();
        dataStructure.setExperimentIdentifier(experimentIdentifierWithUUID);
        final ExperimentIdentifier experimentIdentifier = dataStructure.getExperimentIdentifier();
        assertTrue(experimentIdentifier instanceof ExperimentIdentifierWithUUID);
        assertEquals(SampleWithOwnerTest.INSTANCE_UUID,
                ((ExperimentIdentifierWithUUID) experimentIdentifier).getInstanceUUID());
    }

    @Test
    public final void testSetExperimentIdentifier()
    {
        dataStructure.create();
        try
        {
            dataStructure.setExperimentIdentifier(EXPERIMENT_IDENTIFIER);
            fail();
        } catch (final DataStructureException ex)
        {
            // Nothing to do here.
        }
        dataStructure.setExperimentIdentifier(ExperimentIdentifierWithUUIDTest
                .createExperimentIdentifierWithUUID());
    }

    @Test
    public final void testOpenVersionV1_0()
    {
        DataStructureV1_0Test.createExampleDataStructure(storage, new Version(1, 0));
        storage.mount();
        final IDirectory root = storage.getRoot();
        new Version(1, 1).saveTo(root);
        storage.unmount();
        dataStructure.open(Mode.READ_ONLY);
    }

    @Test
    public final void testBackwardCompatible()
    {
        DataStructureV1_0Test.createExampleDataStructure(storage, new Version(1, 0));
        dataStructure.open(Mode.READ_ONLY);
        final Sample sample = dataStructure.getSample();
        assertFalse(sample instanceof SampleWithOwner);
        try
        {
            dataStructure.getSampleWithOwner();
            fail();
        } catch (final DataStructureException e)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testClose()
    {
        DataStructureV1_0Test.createExampleDataStructure(storage, new Version(1, 0));
        dataStructure.open(Mode.READ_ONLY);
        // Closes without exception.
        dataStructure.close();
        dataStructure.open(Mode.READ_ONLY);
        assertEquals(new Version(1, 0), new DataStructureLoader(workingDirectory.getParentFile())
                .load(getClass().getName(), true).getVersion());
        try
        {
            dataStructure.setSample(createSampleWithOwner());
            fail("read-only mode");
        } catch (final UnsupportedOperationException ex)
        {
            // Nothing to do here.
        }
        dataStructure.open(Mode.READ_WRITE);
        dataStructure.setSample(createSampleWithOwner());
        dataStructure.setExperimentIdentifier(ExperimentIdentifierWithUUIDTest
                .createExperimentIdentifierWithUUID());
        dataStructure.close();
        dataStructure.open(Mode.READ_ONLY);
        assertEquals(new Version(1, 1), dataStructure.getVersion());
    }

    @Test
    public final void testReadOnly()
    {
        DataStructureV1_0Test.createExampleDataStructure(storage, new Version(1, 0));
        // IDirectory.addKeyValuePair
        dataStructure.open(Mode.READ_ONLY);
        try
        {
            dataStructure.getStandardData().addKeyValuePair("key", "value");
            fail("Should not be allowed as we are in read-only mode.");
        } catch (final UnsupportedOperationException ex)
        {
        }
        dataStructure.open(Mode.READ_WRITE);
        dataStructure.getStandardData().addKeyValuePair("key", "value");
        // IDirectory.listFiles
        dataStructure.open(Mode.READ_ONLY);
        List<IFile> files = dataStructure.getOriginalData().listFiles(null, true);
        assertTrue(files.size() > 0);
        try
        {
            files.get(0).moveTo(workingDirectory);
            fail("Should not be allowed as we are in read-only mode.");
        } catch (final UnsupportedOperationException ex)
        {
        }
        dataStructure.open(Mode.READ_WRITE);
        files = dataStructure.getOriginalData().listFiles(null, true);
        files.get(0).moveTo(workingDirectory);
    }
}
