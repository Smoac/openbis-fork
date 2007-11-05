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

import java.io.File;
import java.util.Date;

import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

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
        File dir = new File(workingDirectory, "ds");
        assert dir.mkdir();
        DataStructureV1_0 dataStructure = new DataStructureV1_0(new FileStorage(dir));
        dataStructure.create();
        dataStructure.getOriginalData().addKeyValuePair("answer", "42");
        dataStructure.setFormat(UnknownFormat1_0.UNKNOWN_1_0);
        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier("g", "p", "e");
        dataStructure.setExperimentIdentifier(experimentIdentifier);
        ExperimentRegistrator experimentRegistrator = new ExperimentRegistrator("john", "doe", "j@doe");
        dataStructure.setExperimentRegistrator(experimentRegistrator);
        dataStructure.setExperimentRegistartionDate(new ExperimentRegistratorDate(new Date(0)));
        dataStructure.setMeasurementEntity(new MeasurementEntity("a", "b"));
        dataStructure.setProcessingType(ProcessingType.RAW_DATA);
        dataStructure.close();

        IDataStructure ds = new DataStructureLoader(workingDirectory).load("ds");
        assertEquals(DataStructureV1_0.class, ds.getClass());
        assertEquals(experimentIdentifier, ((DataStructureV1_0) ds).getExperimentIdentifier());
    }
}
