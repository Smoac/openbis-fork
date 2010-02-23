/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.HashSet;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Test cases for {@link DataSetHierarchyHelper}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = DataSetHierarchyHelper.class)
public class DataSetHierarchyHelperTest extends AbstractFileSystemTestCase
{

    private static final String DATASET_PATH =
            "Instance_DB-I/Space_GROUP-G/Project_PROJECT-P/Experiment_EXP-E/DataSetType_TYPE-T/Sample_SAMPLE-S/Dataset_DATASET-D";

    private static final String SAMPLE = "SAMPLE-S";

    private static final String PROJECT = "PROJECT-P";

    private static final String GROUP = "GROUP-G";

    private static final String EXPERIMENT = "EXP-E";

    private static final String TYPE = "TYPE-T";

    private static final String LOCATION = "location/L";

    private static final String DATASET = "DATASET-D";

    private static final String DATABASE_INSTANCE = "DB-I";

    private SimpleDataSetInformationDTO createDataSetInfo()
    {
        SimpleDataSetInformationDTO dsInfo = new SimpleDataSetInformationDTO();
        dsInfo.setDatabaseInstanceCode(DATABASE_INSTANCE);
        dsInfo.setDataSetCode(DATASET);
        dsInfo.setDataSetLocation(LOCATION);
        dsInfo.setDataSetType(TYPE);
        dsInfo.setExperimentCode(EXPERIMENT);
        dsInfo.setGroupCode(GROUP);
        dsInfo.setProjectCode(PROJECT);
        dsInfo.setSampleCode(SAMPLE);
        return dsInfo;
    }

    @Test
    public void testCreateDataSetPath() throws Exception
    {
        SimpleDataSetInformationDTO dsInfo = createDataSetInfo();
        String path = DataSetHierarchyHelper.createHierarchicalPath(dsInfo);
        assertEquals(DATASET_PATH, path);
    }

    @Test
    public void testEntitySeparator() throws Exception
    {
        assertEquals(DataSetHierarchyHelper.ENTITY_SEPARATOR, "_");
    }

    @Test
    public void testExtractValidPair() throws Exception
    {
        String merged = "Instance_DB-I";
        DataSetHierarchyHelper.Pair pair = DataSetHierarchyHelper.tryExtractPair(merged);
        assertNotNull(pair);
        assertEquals(DataSetHierarchyHelper.PathElementKey.Instance, pair.getKey());
        assertEquals("DB-I", pair.getValue());
    }

    @Test
    public void testExtractValidPairWithMoreUnderscores() throws Exception
    {
        String merged = "Instance_DB_I_2";
        DataSetHierarchyHelper.Pair pair = DataSetHierarchyHelper.tryExtractPair(merged);
        assertNotNull(pair);
        assertEquals(DataSetHierarchyHelper.PathElementKey.Instance, pair.getKey());
        assertEquals("DB_I_2", pair.getValue());
    }

    @Test
    public void testExtractNoPairNoUnderscore() throws Exception
    {
        String merged = "Instance-DB-I";
        DataSetHierarchyHelper.Pair pair = DataSetHierarchyHelper.tryExtractPair(merged);
        assertNull(pair);
    }

    @Test
    public void testExtractNoPairUnknown() throws Exception
    {
        String merged = "Instance2_DB-I";
        DataSetHierarchyHelper.Pair pair = DataSetHierarchyHelper.tryExtractPair(merged);
        assertNull(pair);
    }

    @Test
    public void testAddPaths() throws Exception
    {
        HashSet<String> set = new HashSet<String>();
        File root = new File(workingDirectory, "root");
        root.mkdirs();
        File dataSetPath = new File(root, DATASET_PATH);
        dataSetPath.mkdirs();
        DataSetHierarchyHelper.addPaths(set, root, DataSetHierarchyHelper.PathElementKey.Instance);
        assertTrue(set.contains(dataSetPath.getAbsolutePath()));
        FileUtilities.deleteRecursively(root);
        assertFalse(root.exists());
    }
}
