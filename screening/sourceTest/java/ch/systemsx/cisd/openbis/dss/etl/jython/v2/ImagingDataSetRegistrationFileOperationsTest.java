/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.jython.v2;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.impl.FeatureVectorContainerDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.impl.FeatureVectorDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.impl.ImageContainerDataSet;

/**
 * @author Franz-Josef Elmer
 */
public class ImagingDataSetRegistrationFileOperationsTest extends AssertJUnit
{
    private static final String ORIGINAL_DIR_NAME = "original-dir-name";

    private Mockery context;

    private IDataSetRegistrationFileOperations operations;

    private ImagingDataSetRegistrationFileOperations fileOperations;

    private IDataSet dataSet;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        operations = context.mock(IDataSetRegistrationFileOperations.class);
        dataSet = context.mock(IDataSet.class);
        fileOperations = new ImagingDataSetRegistrationFileOperations(operations, ORIGINAL_DIR_NAME);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testMoveFileToNormalDataSet()
    {
        context.checking(new Expectations()
            {
                {
                    one(operations).moveFile("source", dataSet, "somewhere");
                    will(returnValue("path"));
                }
            });

        String path = fileOperations.moveFile("source", dataSet, "somewhere");

        assertEquals("path", path);
        context.assertIsSatisfied();
    }

    @Test
    public void testMoveToImageContainerDataSetButMissingOriginalDataSet()
    {
        ImageContainerDataSet dst = new ImageContainerDataSet(null, null, null);

        try
        {
            fileOperations.moveFile("source", dst, "somewhere");
            fail("UserFailureException expected");
        } catch(UserFailureException e)
        {
            assertEquals("Cannot move the files because the original dataset is missing: source", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMoveToImageContainerDataSet()
    {
        ImageContainerDataSet dst = new ImageContainerDataSet(null, null, null);
        DataSetRegistrationDetails<ImageDataSetInformation> details = new DataSetRegistrationDetails<ImageDataSetInformation>();
        ImageDataSetInformation originalDataSetInfo = new ImageDataSetInformation();
        details.setDataSetInformation(originalDataSetInfo);
        DataSet<ImageDataSetInformation> originalDataSet = new DataSet<ImageDataSetInformation>(details, null, null);
        dst.setOriginalDataset(originalDataSet);
        final RecordingMatcher<IDataSet> dataSetMatcher = new RecordingMatcher<IDataSet>();
        context.checking(new Expectations()
        {
            {
                one(operations).moveFile(with("source"), with(dataSetMatcher),
                        with(ORIGINAL_DIR_NAME + "/somewhere"));
                will(returnValue("path"));
            }
        });
        
        String path = fileOperations.moveFile("source", dst, "somewhere");
        
        assertEquals("path", path);
        assertSame(originalDataSet, dataSetMatcher.recordedObject());
        assertEquals(ORIGINAL_DIR_NAME + "/somewhere",
                originalDataSetInfo.getDatasetRelativeImagesFolderPath().toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCopyToFeatureVectorContainerDataSetButMissingOriginalDataSet()
    {
        FeatureVectorContainerDataSet dst = new FeatureVectorContainerDataSet(null, null, null);

        try
        {
            fileOperations.copyFile("source", dst, "somewhere", true);
            fail("UserFailureException expected");
        } catch(UserFailureException e)
        {
            assertEquals("Cannot copy the files because the original dataset is missing: source", e.getMessage());
        }
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCopyToFeatureVectorContainerDataSet()
    {
        FeatureVectorContainerDataSet dst = new FeatureVectorContainerDataSet(null, null, null);
        DataSetRegistrationDetails<FeatureVectorDataSetInformation> details 
        = new DataSetRegistrationDetails<FeatureVectorDataSetInformation>();
        FeatureVectorDataSetInformation originalDataSetInfo = new FeatureVectorDataSetInformation();
        details.setDataSetInformation(originalDataSetInfo);
        FeatureVectorDataSet originalDataSet = new FeatureVectorDataSet(
                new DataSet<FeatureVectorDataSetInformation>(details, null, null), null);
        dst.setOriginalDataSet(originalDataSet);
        final RecordingMatcher<IDataSet> dataSetMatcher = new RecordingMatcher<IDataSet>();
        context.checking(new Expectations()
        {
            {
                one(operations).copyFile(with("source"), with(dataSetMatcher),
                        with("somewhere"), with(true));
                will(returnValue("path"));
            }
        });
        
        String path = fileOperations.copyFile("source", dst, "somewhere", true);
        
        assertEquals("path", path);
        assertSame(originalDataSet, dataSetMatcher.recordedObject());
        context.assertIsSatisfied();
    }
}
