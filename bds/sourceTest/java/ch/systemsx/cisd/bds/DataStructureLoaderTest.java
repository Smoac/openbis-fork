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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.Utilities.Boolean;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.bds.v1_0.IDataStructureV1_0;

/**
 * Test cases for corresponding {@link DataStructureLoader} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class DataStructureLoaderTest extends AbstractFileSystemTestCase
{

    @Test
    public final void testOpen()
    {
        final File dir = new File(workingDirectory, "ds");
        assert dir.mkdir();
        final IDataStructureV1_0 dataStructure =
                (IDataStructureV1_0) DataStructureFactory.createDataStructure(new FileStorage(dir),
                        new Version(1, 0));
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormatV1_0.UNKNOWN_1_0);
        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier("i", "g", "p", "e");
        dataStructure.setExperimentIdentifier(experimentIdentifier);
        final ExperimentRegistrator experimentRegistrator =
                new ExperimentRegistrator("john", "doe", "j@doe");
        dataStructure.setExperimentRegistrator(experimentRegistrator);
        dataStructure.setExperimentRegistrationTimestamp(new ExperimentRegistrationTimestamp(
                new Date(0)));
        dataStructure.setSample(new Sample("a", "CELL_PLATE", "b"));
        final List<String> parentCodes = createParentCodes();
        dataStructure.setDataSet(new DataSet("s", "HCS_IMAGE", Boolean.FALSE, null, null,
                parentCodes));
        dataStructure.close();

        final IDataStructure ds = new DataStructureLoader(workingDirectory).load("ds", true);
        assertTrue(ds instanceof IDataStructureV1_0);
        assertEquals(new Version(1, 0), ds.getVersion());
        assertEquals(experimentIdentifier, ((IDataStructureV1_0) ds).getExperimentIdentifier());
    }

    private final static List<String> createParentCodes()
    {
        final List<String> parentCodes = new ArrayList<String>();
        parentCodes.add("parent1");
        parentCodes.add("parent2");
        return parentCodes;
    }
}
