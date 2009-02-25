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

package ch.systemsx.cisd.etlserver;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.filesystem.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Test cases for corresponding {@link Main} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = Main.class)
public final class MainTest extends AbstractFileSystemTestCase
{

    private final static DatabaseInstancePE createDatabaseInstance()
    {
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode("XXX");
        databaseInstancePE.setUuid("1111-2222");
        return databaseInstancePE;
    }

    @Test
    public final void testMigrateStoreRootDir()
    {
        final File instanceDir =
                new File(
                        new File(workingDirectory, IdentifiedDataStrategy.INSTANCE_PREFIX + "CISD"),
                        IdentifiedDataStrategy.GROUP_PREFIX + "CISD");
        instanceDir.mkdirs();
        assertTrue(instanceDir.exists());
        final DatabaseInstancePE databaseInstancePE = createDatabaseInstance();
        // Not same code
        Main.migrateStoreRootDir(workingDirectory, databaseInstancePE);
        assertTrue(instanceDir.exists());
        databaseInstancePE.setCode("CISD");
        // Same code
        Main.migrateStoreRootDir(workingDirectory, databaseInstancePE);
        assertFalse(instanceDir.exists());
        assertTrue(new File(workingDirectory, IdentifiedDataStrategy.INSTANCE_PREFIX
                + databaseInstancePE.getUuid()).exists());
        // Trying again does not change anything
        Main.migrateStoreRootDir(workingDirectory, databaseInstancePE);
        assertFalse(instanceDir.exists());
        assertTrue(new File(workingDirectory, IdentifiedDataStrategy.INSTANCE_PREFIX
                + databaseInstancePE.getUuid()).exists());
    }

    @Test
    public void testMigrateDataStoreByRenamingObservableTypeToDataSetType() throws Exception
    {
        String observableTypeValue = "DST1";
        String observableTypeDirPrefix = "ObservableType_";
        File instanceDir =
                new File(workingDirectory, IdentifiedDataStrategy.INSTANCE_PREFIX + "I1");
        File groupDir = new File(instanceDir, IdentifiedDataStrategy.GROUP_PREFIX + "G1");
        File projectDir = new File(groupDir, IdentifiedDataStrategy.PROJECT_PREFIX + "P1");
        File experimentDir = new File(projectDir, IdentifiedDataStrategy.EXPERIMENT_PREFIX + "E1");
        File observableTypeDir =
                new File(experimentDir, observableTypeDirPrefix + observableTypeValue);
        File sampleDir = new File(observableTypeDir, IdentifiedDataStrategy.SAMPLE_PREFIX + "S1");
        File dataSetDir = new File(sampleDir, IdentifiedDataStrategy.DATASET_PREFIX + "D1");
        File metadataDir = new File(dataSetDir, "metadata");
        File metadataDataSetDir = new File(metadataDir, "data_set");

        //
        // Don't break when directory does not exist
        //

        Main.migrateDataStoreByRenamingObservableTypeToDataSetType(workingDirectory);

        //
        // Rename ObservableType_<> directory and observable_type file
        //

        // create directories
        metadataDataSetDir.mkdirs();
        assertTrue(metadataDataSetDir.exists());
        assertTrue(observableTypeDir.getName()
                .equals(observableTypeDirPrefix + observableTypeValue));

        // create files
        String observableTypeFileName = "observable_type";
        File observableTypeFile = new File(metadataDataSetDir, observableTypeFileName);
        observableTypeFile.createNewFile();
        assertTrue(observableTypeFile.exists());
        AssertJUnit.assertEquals(observableTypeFileName, metadataDataSetDir.listFiles()[0]
                .getName());
        assertTrue(observableTypeFile.getName().equals(observableTypeFileName));

        // Do the migration
        Main.migrateDataStoreByRenamingObservableTypeToDataSetType(workingDirectory);

        // check directory renamed
        AssertJUnit.assertEquals(IdentifiedDataStrategy.DATA_SET_TYPE_PREFIX + observableTypeValue,
                experimentDir.listFiles()[0].getName());

        // update variables
        observableTypeDir =
                new File(experimentDir, IdentifiedDataStrategy.DATA_SET_TYPE_PREFIX
                        + observableTypeValue);
        sampleDir = new File(observableTypeDir, IdentifiedDataStrategy.SAMPLE_PREFIX + "S1");
        dataSetDir = new File(sampleDir, IdentifiedDataStrategy.DATASET_PREFIX + "D1");
        metadataDir = new File(dataSetDir, "metadata");
        metadataDataSetDir = new File(metadataDir, "data_set");

        // check file renamed
        AssertJUnit.assertEquals("data_set_type", metadataDataSetDir.listFiles()[0].getName());
    }

}
