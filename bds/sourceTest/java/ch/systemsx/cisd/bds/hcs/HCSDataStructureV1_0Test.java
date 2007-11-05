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

package ch.systemsx.cisd.bds.hcs;

import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.DataStructureV1_0;
import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.ExperimentRegistrator;
import ch.systemsx.cisd.bds.ExperimentRegistratorDate;
import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.IDataStructure;
import ch.systemsx.cisd.bds.MeasurementEntity;
import ch.systemsx.cisd.bds.ProcessingType;
import ch.systemsx.cisd.bds.Version;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link DataStructureV1_0} class.
 * 
 * @author Christian Ribeaud
 */
public final class HCSDataStructureV1_0Test extends AbstractFileSystemTestCase
{
    private FileStorage storage;

    private DataStructureV1_0 dataStructure;

    private final static ChannelList createChannelList()
    {
        final List<Channel> list = new ArrayList<Channel>();
        list.add(new Channel(1, 123));
        list.add(new Channel(2, 456));
        return new ChannelList(list);
    }

    private void createExampleDataStructure()
    {
        storage.mount();
        IDirectory root = storage.getRoot();
        new Version(1, 0).saveTo(root);
        final IDirectory data = root.makeDirectory(DataStructureV1_0.DIR_DATA);
        final IDirectory originalDataDir = data.makeDirectory(DataStructureV1_0.DIR_ORIGINAL);
        originalDataDir.addKeyValuePair("hello", "world");
        final IDirectory metaData = root.makeDirectory(DataStructureV1_0.DIR_METADATA);
        new ExperimentIdentifier("g", "p", "e").saveTo(metaData);
        new ExperimentRegistratorDate(new Date(0)).saveTo(metaData);
        new ExperimentRegistrator("john", "doe", "j@doe").saveTo(metaData);
        new MeasurementEntity("a", "b").saveTo(metaData);
        metaData.addKeyValuePair(DataStructureV1_0.MAPPING_FILE, "");
        ProcessingType.COMPUTED_DATA.saveTo(metaData);
        storage.unmount();
    }

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public final void setup() throws IOException
    {
        super.setup();
        storage = new FileStorage(workingDirectory);
        dataStructure = new DataStructureV1_0(storage);
    }

    @Test
    public final void testHCSImageDataStructure()
    {
        // Creating...
        dataStructure.create();
        createExampleDataStructure();
        dataStructure.setFormat(ImageHCSFormat1_0.IMAGE_HCS_1_0);
        dataStructure.addFormatParameter(new FormatParameter(ImageHCSFormat1_0.DEVICE_ID, "M1"));
        dataStructure.addFormatParameter(new FormatParameter(ImageHCSFormat1_0.CONTAINS_ORIGINAL_DATA, Boolean.TRUE));
        dataStructure.addFormatParameter(new FormatParameter("doesNotMatter", createChannelList()));
        dataStructure.addFormatParameter(new FormatParameter(PlateGeometry.PLATE_GEOMETRY, new PlateGeometry(2, 3)));
        dataStructure.addFormatParameter(new FormatParameter(WellGeometry.WELL_GEOMETRY, new WellGeometry(7, 5)));
        dataStructure.close();
        // And loading...
        final IDataStructure ds =
                new DataStructureLoader(workingDirectory.getParentFile()).load(getClass().getSimpleName());
        assertNotNull(ds);
    }
}