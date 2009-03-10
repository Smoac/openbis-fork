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

package ch.systemsx.cisd.etlserver;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * Test cases for corresponding {@link IdentifiedDataStrategy} class.
 * 
 * @author Christian Ribeaud
 */
public class IdentifiedDataStrategyTest extends AbstractFileSystemTestCase
{
    private static final String EXAMPLE_PROJECT_CODE = "P";

    private static final String EXAMPLE_EXPERIMENT_CODE = "E";

    private final static String FILE_NAME = "AX14";

    private final static IdentifiedDataStrategy strategy = new IdentifiedDataStrategy();

    private final static DataSetType dataSetType =
            new DataSetType(DataSetTypeCode.HCS_IMAGE.getCode());

    private static final String EXAMPLE_GROUP_CODE = "G";

    final static DataSetInformation createDataSetInfo()
    {
        final DataSetInformation dataSetInfo = new DataSetInformation();
        final ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier();
        experimentIdentifier.setExperimentCode(EXAMPLE_EXPERIMENT_CODE);
        experimentIdentifier.setProjectCode(EXAMPLE_PROJECT_CODE);
        experimentIdentifier.setGroupCode(EXAMPLE_GROUP_CODE);
        dataSetInfo.setExperimentIdentifier(experimentIdentifier);
        dataSetInfo.setSampleCode("S");
        dataSetInfo.setInstanceCode("my-instance");
        dataSetInfo.setInstanceUUID("1111-2222");
        dataSetInfo.setDataSetCode("data-set-code");
        return dataSetInfo;
    }

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
    }

    @Test
    public final void testGetBaseDirectory() throws IOException
    {
        boolean exceptionThrown = false;
        try
        {
            strategy.getBaseDirectory(null, null, null);
        } catch (final AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null values not permited here", exceptionThrown);
        final DataSetInformation dataSetInfo = createDataSetInfo();
        File baseDirectory = strategy.getBaseDirectory(workingDirectory, dataSetInfo, dataSetType);
        final File file =
                new File(workingDirectory, "Instance_1111-2222/Group_G/Project_P/Experiment_E/"
                        + "DataSetType_HCS_IMAGE/Sample_S/Dataset_data-set-code");
        assertEquals(file, baseDirectory);
        assertTrue(baseDirectory.exists() == false);
        // Create a file instead of a directory
        FileUtils.touch(file);
        try
        {
            strategy.getBaseDirectory(workingDirectory, dataSetInfo, dataSetType);
            fail("illegal storage layout not detected");
        } catch (final EnvironmentFailureException ex)
        {
            assertTrue("Unexpected exception message: " + ex.getMessage(), ex.getMessage()
                    .startsWith(IdentifiedDataStrategy.STORAGE_LAYOUT_ERROR_MSG_PREFIX));
        }
        FileUtils.forceDelete(file);
        assert file.exists() == false;
        // Create base directory
        baseDirectory.mkdirs();
        assertTrue(baseDirectory.exists() && baseDirectory.isDirectory());
        try
        {
            baseDirectory = strategy.getBaseDirectory(workingDirectory, dataSetInfo, dataSetType);
            fail("illegal storage layout not detected");
        } catch (final EnvironmentFailureException ex)
        {
            assertTrue("Unexpected exception message: " + ex.getMessage(), ex.getMessage()
                    .startsWith(IdentifiedDataStrategy.STORAGE_LAYOUT_ERROR_MSG_PREFIX));
        }
    }

    @Test
    public final void testGetTargetPath()
    {
        File file = new File(FILE_NAME);
        File targetPath = strategy.getTargetPath(workingDirectory, file);
        assertEquals(new File(workingDirectory, FILE_NAME), targetPath);
        final String property = System.getProperty("java.io.tmpdir");
        assertNotNull(property);
        file = new File(property, FILE_NAME);
        targetPath = strategy.getTargetPath(workingDirectory, file);
        assertEquals(new File(workingDirectory, FILE_NAME), targetPath);
    }
}
