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

package ch.ethz.bsse.cisd.dsu.dss;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FlowLineFeederTest extends AbstractFileSystemTestCase
{
    private static final Sample EXAMPLE_FLOW_CELL_SAMPLE = createFlowCellSample();
    private static final DataSetInformation EXAMPLE_DATA_SET_INFO = createDataSetInfo();
    private static final String SAMPLE_CODE = "fc";
    private static final String DROP_BOX_PREFIX = "drop-box-";
    
    private static DataSetInformation createDataSetInfo()
    {
        DataSetInformation dataSetInfo = new DataSetInformation();
        dataSetInfo.setSampleCode(SAMPLE_CODE);
        return dataSetInfo;
    }
    
    private static Sample createFlowCellSample()
    {
        Sample sample = new Sample();
        sample.setId(42L);
        sample.setCode(SAMPLE_CODE);
        return sample;
    }
    
    private FlowLineFeeder flowLineFeeder;
    private Mockery context;
    private IEncapsulatedOpenBISService service;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        assertEquals(true, new File(workingDirectory, DROP_BOX_PREFIX + "1").mkdirs());
        assertEquals(true, new File(workingDirectory, DROP_BOX_PREFIX + "2").mkdirs());
        Properties properties = new Properties();
        properties.setProperty(FlowLineFeeder.FLOW_LINE_DROP_BOX_TEMPLATE, new File(
                workingDirectory, DROP_BOX_PREFIX).getAbsolutePath()
                + "{0}");
        flowLineFeeder = new FlowLineFeeder(properties, service);
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingProperty()
    {
        try
        {
            new FlowLineFeeder(new Properties(), service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + FlowLineFeeder.FLOW_LINE_DROP_BOX_TEMPLATE
                    + "' not found in properties '[]'", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test 
    void testMissingDropBox()
    {
        File flowCell = new File(workingDirectory, "abc");
        assertEquals(true, flowCell.mkdir());
        FileUtilities.writeToFile(new File(flowCell, "s_3.srf"), "hello flow line 3");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);
        prepareListFlowLines(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.<Sample>asList());
        
        try
        {
            flowLineFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            AssertionUtil.assertContains(DROP_BOX_PREFIX + "3", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testUnkownFlowCell()
    {
        File flowCell = new File(workingDirectory, "abc");
        prepareLoadFlowCellSample(null);
        
        try
        {
            flowLineFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unkown flow cell sample: " + EXAMPLE_DATA_SET_INFO.getSampleIdentifier(),
                    ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testHappyCase()
    {
        File flowCell = new File(workingDirectory, "abc");
        assertEquals(true, flowCell.mkdir());
        File logs = new File(flowCell, "logs");
        assertEquals(true, logs.mkdir());
        FileUtilities.writeToFile(new File(logs, "basic.log"), "hello log");
        File srfFolder = new File(flowCell, "SRF");
        assertEquals(true, srfFolder.mkdir());
        File originalFlowLine1 = new File(srfFolder, "s_1.srf");
        FileUtilities.writeToFile(originalFlowLine1, "hello flow line 1");
        File originalFlowLine2 = new File(srfFolder, "2.srf");
        FileUtilities.writeToFile(originalFlowLine2, "hello flow line 2");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);
        
        Sample fl1 = createFlowLineSample(1);
        Sample fl2 = createFlowLineSample(2);
        prepareListFlowLines(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.asList(fl1, fl2));

        flowLineFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO);
        
        checkFlowLineDataSet(originalFlowLine1, "1");
        checkFlowLineDataSet(originalFlowLine2, "2");
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testUndoLastOperation()
    {
        testHappyCase();
        assertEquals(2, new File(workingDirectory, DROP_BOX_PREFIX + "1").list().length);
        assertEquals(2, new File(workingDirectory, DROP_BOX_PREFIX + "2").list().length);
        
        flowLineFeeder.undoLastOperation();
        
        assertEquals(0, new File(workingDirectory, DROP_BOX_PREFIX + "1").list().length);
        assertEquals(0, new File(workingDirectory, DROP_BOX_PREFIX + "2").list().length);
    }
    
    private void prepareLoadFlowCellSample(final Sample flowCellSample)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetSampleWithExperiment(EXAMPLE_DATA_SET_INFO.getSampleIdentifier());
                    will(returnValue(flowCellSample));
                }
            });
    }
    
    private void prepareListFlowLines(final Sample flowCellSample, final List<Sample> flowLineSamples)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listSamples(with(new BaseMatcher<ListSampleCriteria>()
                        {

                            public boolean matches(Object item)
                            {
                                if (item instanceof ListSampleCriteria)
                                {
                                    ListSampleCriteria criteria = (ListSampleCriteria) item;
                                    return criteria.getContainerSampleId().getId().equals(
                                            flowCellSample.getId());
                                }
                                return false;
                            }

                            public void describeTo(Description description)
                            {
                                description.appendText("Flow cell with ID "
                                        + flowCellSample.getId());
                            }
                        }));
                    will(returnValue(flowLineSamples));
                    for (Sample sample : flowLineSamples)
                    {
                        SampleIdentifier identifier =
                                SampleIdentifierFactory.parse(sample.getIdentifier());
                        one(service).getPropertiesOfTopSampleRegisteredFor(identifier);
                    }
                }
            });
    }

    private void checkFlowLineDataSet(File originalFlowLine, String flowLineNumber)
    {
        File dropBox = new File(workingDirectory, DROP_BOX_PREFIX + flowLineNumber);
        String fileName = "abc_" + flowLineNumber;
        File ds = new File(dropBox, fileName);
        assertEquals(true, ds.isDirectory());

        File flowLine = new File(ds, originalFlowLine.getName());
        assertEquals(true, flowLine.isFile());
        assertEquals(FileUtilities.loadToString(originalFlowLine), FileUtilities
                .loadToString(flowLine));
        // check hard-link copy by changing last-modified date of one file should change
        // last-modified date of the other file.
        originalFlowLine.setLastModified(4711000);
        assertEquals(4711000, flowLine.lastModified());
        assertEquals(true, new File(ds, FlowLineFeeder.META_DATA_FILE_NAME).exists());
        assertEquals(true, new File(dropBox, Constants.IS_FINISHED_PREFIX + fileName).exists());
    }
    
    private Sample createFlowLineSample(int flowLineNumber)
    {
        Sample sample = new Sample();
        sample.setCode(SAMPLE_CODE);
        sample.setSubCode(Integer.toString(flowLineNumber));
        sample.setGeneratedFrom(EXAMPLE_FLOW_CELL_SAMPLE);
        Person registrator = new Person();
        registrator.setEmail("ab@c.de");
        sample.setRegistrator(registrator);
        sample.setIdentifier(SAMPLE_CODE + SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING
                + flowLineNumber);
        return sample;
    }
    
}
