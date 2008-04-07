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
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link DataSet} class.
 * 
 * @author Christian Ribeaud
 */
public final class SampleFileSystemTestCase extends AbstractFileSystemTestCase
{

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
        try
        {
            new Sample(null, null, null);
            fail("Null values not allowed.");
        } catch (final AssertionError ex)
        {
            // Nothing to do here.
        }
        try
        {
            new Sample("", "", "");
            fail("Empty values not allowed.");
        } catch (final AssertionError ex)
        {
            // Nothing to do here.
        }
        new Sample(" ", " ", " ");
    }

    @DataProvider
    public final Object[][] getDataSetData()
    {
        return new Object[][]
            {
                { "code", "typeDescription" } };
    }

    @Test(dataProvider = "getDataSetData")
    public final void testLoadFrom(final String code, final String typeDescription)
    {
        final IDirectory directory = NodeFactory.createDirectoryNode(workingDirectory);
        final String sampleType = "CELL_PLATE";
        final Sample sample = new Sample(code, sampleType, typeDescription);
        sample.saveTo(directory);
        final Sample newSample = Sample.loadFrom(directory);
        assertEquals(code == null || code.length() == 0 ? null : code, newSample.getCode());
        assertEquals(typeDescription == null || typeDescription.length() == 0 ? null
                : typeDescription, newSample.getTypeDescription());
    }
}
