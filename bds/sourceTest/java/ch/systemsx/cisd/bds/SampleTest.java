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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.test.EqualsHashCodeTestCase;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for corresponding {@link Sample} class.
 * 
 * @author Christian Ribeaud
 */
@Test
public final class SampleTest extends EqualsHashCodeTestCase<Sample>
{
    private static final String UNIT_TEST_WORKING_DIRECTORY = "unit-test-wd";

    private static final String TARGETS_DIRECTORY = "targets";

    private static final File UNIT_TEST_ROOT_DIRECTORY =
            new File(TARGETS_DIRECTORY + File.separator + UNIT_TEST_WORKING_DIRECTORY);

    private final File workingDirectory;

    public SampleTest()
    {
        workingDirectory = createWorkingDirectory();
    }

    private final File createWorkingDirectory()
    {
        final File directory = new File(UNIT_TEST_ROOT_DIRECTORY, getClass().getName());
        directory.mkdirs();
        directory.deleteOnExit();
        return directory;
    }

    @AfterClass
    public void afterClass() throws IOException
    {
        FileUtilities.deleteRecursively(workingDirectory);
    }

    @Test
    public final void testSaveTo()
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final String sampleCode = "code";
        final String typeDescription = "typeDescription";
        final String sampleType = "CELL_PLATE";
        final Sample sample = new Sample(sampleCode, sampleType, typeDescription);
        sample.saveTo(directory);
        final IDirectory folder = Utilities.getSubDirectory(directory, Sample.FOLDER);
        assertEquals(sampleCode, Utilities.getTrimmedString(folder, Sample.CODE));
        assertEquals(sampleType, Utilities.getTrimmedString(folder, Sample.TYPE_CODE));
        assertEquals(typeDescription, Utilities.getTrimmedString(folder, Sample.TYPE_DESCRIPTION));
    }

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new Sample(null, null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        fail = true;
        try
        {
            new Sample("", "", "");
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        new Sample(" ", " ", " ");
    }

    @DataProvider
    public final Object[][] getSampleData()
    {
        return new Object[][]
            {
                { "code", "typeDescription" } };
    }

    @Test(dataProvider = "getSampleData")
    public final void testLoadFrom(final String code, final String typeDescription)
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final String sampleType = "CELL_PLATE";
        final Sample sample = new Sample(code, sampleType, typeDescription);
        sample.saveTo(directory);
        final Sample newSample = Sample.loadFrom(directory);
        assertEquals(code, newSample.getCode());
        assertEquals(typeDescription, newSample.getTypeDescription());
    }

    //
    // EqualsHashCodeTestCase
    //

    @Override
    @BeforeMethod
    public void setUp() throws Exception
    {
        super.setUp();
        FileUtils.cleanDirectory(workingDirectory);
        assert workingDirectory.isDirectory() && workingDirectory.listFiles().length == 0;
    }

    @Override
    protected final Sample createInstance() throws Exception
    {
        return new Sample("CP1", "CELL_PLATE", "description");
    }

    @Override
    protected final Sample createNotEqualInstance() throws Exception
    {
        return new Sample("CP2", "CELL_PLATE", "description");
    }

}
