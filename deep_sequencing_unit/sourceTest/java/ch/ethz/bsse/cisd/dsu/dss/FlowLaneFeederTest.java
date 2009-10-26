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
import java.io.FilenameFilter;
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
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FlowLaneFeederTest extends AbstractFileSystemTestCase
{
    private static final String AFFILIATION = "fmi";
    private static final String EXTERNAL_SAMPLE_NAME = "ext23";
    private static final String TRANSFER_DROP_BOX = "transfer-drop-box";
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
    
    private FlowLaneFeeder flowLaneFeeder;
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private File dropBox1;
    private File dropBox2;
    private File transferDropBox;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        LogInitializer.init();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        FileUtilities.deleteRecursively(workingDirectory);
        dropBox1 = new File(workingDirectory, DROP_BOX_PREFIX + "1");
        assertEquals(true, dropBox1.mkdirs());
        dropBox2 = new File(workingDirectory, DROP_BOX_PREFIX + "2");
        assertEquals(true, dropBox2.mkdirs());
        transferDropBox = new File(workingDirectory, TRANSFER_DROP_BOX);
        assertEquals(true, transferDropBox.mkdirs());
        Properties properties = new Properties();
        properties.setProperty(FlowLaneFeeder.FLOW_LANE_DROP_BOX_TEMPLATE, new File(
                workingDirectory, DROP_BOX_PREFIX).getAbsolutePath()
                + "{0}");
        properties.setProperty(FlowLaneFeeder.TRANSFER_PREFIX + AFFILIATION, transferDropBox.getAbsolutePath());
        flowLaneFeeder = new FlowLaneFeeder(properties, service);
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
            new FlowLaneFeeder(new Properties(), service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + FlowLaneFeeder.FLOW_LANE_DROP_BOX_TEMPLATE
                    + "' not found in properties '[]'", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test 
    void testMissingDropBox()
    {
        File flowCell = new File(workingDirectory, SAMPLE_CODE);
        assertEquals(true, flowCell.mkdir());
        FileUtilities.writeToFile(new File(flowCell, "s_3.srf"), "hello flow lane 3");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.<Sample>asList());
        
        try
        {
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO);
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
        File flowCell = new File(workingDirectory, SAMPLE_CODE);
        prepareLoadFlowCellSample(null);
        
        try
        {
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unkown flow cell sample: " + EXAMPLE_DATA_SET_INFO.getSampleIdentifier(),
                    ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testNotEnoughFlowLaneFiles()
    {
        File flowCell = new File(workingDirectory, SAMPLE_CODE);
        assertEquals(true, flowCell.mkdir());
        File logs = new File(flowCell, "logs");
        assertEquals(true, logs.mkdir());
        FileUtilities.writeToFile(new File(logs, "basic.log"), "hello log");
        File srfFolder = new File(flowCell, "SRF");
        assertEquals(true, srfFolder.mkdir());
        File originalFlowLane1 = new File(srfFolder, "s_1.srf");
        FileUtilities.writeToFile(originalFlowLane1, "hello flow lane 1");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);
        
        Sample fl1 = createFlowLaneSample(1);
        Sample fl2 = createFlowLaneSample(2);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.asList(fl1, fl2));

        try
        {
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Only 1 flow lane files found instead of 2.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testHappyCase()
    {
        File flowCell = new File(workingDirectory, SAMPLE_CODE);
        assertEquals(true, flowCell.mkdir());
        File logs = new File(flowCell, "logs");
        assertEquals(true, logs.mkdir());
        FileUtilities.writeToFile(new File(logs, "basic.log"), "hello log");
        File srfFolder = new File(flowCell, "SRF");
        assertEquals(true, srfFolder.mkdir());
        File originalFlowLane1 = new File(srfFolder, "s_1.srf");
        FileUtilities.writeToFile(originalFlowLane1, "hello flow lane 1");
        File originalFlowLane2 = new File(srfFolder, "2.srf");
        FileUtilities.writeToFile(originalFlowLane2, "hello flow lane 2");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);
        
        Sample fl1 = createFlowLaneSample(1);
        Sample fl2 = createFlowLaneSample(2);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.asList(fl1, fl2));
        prepareGetProperties(Arrays.asList(fl1, fl2));
        
        flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO);
        
        checkFlowLaneDataSet(originalFlowLane1, "1");
        checkFlowLaneDataSet(originalFlowLane2, "2");
        
        File[] transferedFiles = transferDropBox.listFiles();
        assertEquals(1, transferedFiles.length);
        String sampleName =
                SAMPLE_CODE + SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING + "2";
        assertEquals("G2_" + sampleName, transferedFiles[0].getName());
        File metaFile = getFile(transferedFiles[0], FlowLaneFeeder.META_DATA_FILE_TYPE);
        assertEquals(sampleName + "_" + EXTERNAL_SAMPLE_NAME + FlowLaneFeeder.META_DATA_FILE_TYPE,
                metaFile.getName());
        assertHardLinkOnSameFile(originalFlowLane2, getFile(transferedFiles[0], "2.srf"));
        
        context.assertIsSatisfied();
    }
    
    private File getFile(File folder, final String fileNameExtension)
    {
        File[] files = folder.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(fileNameExtension);
                }
            });
        assertEquals(1, files.length);
        return files[0];
    }
    
    @Test
    public void testUndoLastOperation()
    {
        testHappyCase();
        assertEquals(2, dropBox1.list().length);
        assertEquals(2, dropBox2.list().length);
        assertEquals(1, transferDropBox.list().length);
        
        flowLaneFeeder.undoLastOperation();
        
        assertEquals(0, dropBox1.list().length);
        assertEquals(0, dropBox2.list().length);
        assertEquals(0, transferDropBox.list().length);
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
    
    private void prepareListFlowLanes(final Sample flowCellSample, final List<Sample> flowLaneSamples)
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
                    will(returnValue(flowLaneSamples));
                }
            });
    }

    private void prepareGetProperties(final List<Sample> flowLaneSamples)
    {
        context.checking(new Expectations()
            {
                {
                    for (Sample sample : flowLaneSamples)
                    {
                        SampleIdentifier identifier =
                                SampleIdentifierFactory.parse(sample.getIdentifier());
                        one(service).getPropertiesOfTopSampleRegisteredFor(identifier);
                        if (sample.getSubCode().equals("2"))
                        {
                            GenericValueEntityProperty p1 =
                                    createProperty(FlowLaneFeeder.AFFILIATION_KEY, AFFILIATION);
                            GenericValueEntityProperty p2 =
                                    createProperty(FlowLaneFeeder.EXTERNAL_SAMPLE_NAME_KEY,
                                            EXTERNAL_SAMPLE_NAME);
                            will(returnValue(new GenericValueEntityProperty[]
                                { p1, p2 }));
                        }
                    }
                }

                private GenericValueEntityProperty createProperty(String key, String value)
                {
                    GenericValueEntityProperty p = new GenericValueEntityProperty();
                    p.setValue(value);
                    PropertyType propertyType = new PropertyType();
                    propertyType.setCode(key);
                    propertyType.setLabel(key.toLowerCase());
                    p.setPropertyType(propertyType);
                    return p;
                }
            });
    }
    
    private void checkFlowLaneDataSet(File originalFlowLane, String flowLaneNumber)
    {
        File dropBox = new File(workingDirectory, DROP_BOX_PREFIX + flowLaneNumber);
        String flowLaneSampleCode =
                SAMPLE_CODE + SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING
                        + flowLaneNumber;
        String fileName =
                "G" + flowLaneNumber + FlowLaneFeeder.DEFAULT_ENTITY_SEPARATOR + flowLaneSampleCode;
        File ds = new File(dropBox, fileName);
        assertEquals(true, ds.isDirectory());

        File flowLane = new File(ds, originalFlowLane.getName());
        assertEquals(true, flowLane.isFile());
        assertEquals(FileUtilities.loadToString(originalFlowLane), FileUtilities
                .loadToString(flowLane));
        assertHardLinkOnSameFile(originalFlowLane, flowLane);
        String metaDataFileName =
                flowLaneSampleCode + (flowLaneNumber.equals("2") ? "_" + EXTERNAL_SAMPLE_NAME : "")
                        + FlowLaneFeeder.META_DATA_FILE_TYPE;
        assertEquals(true, new File(ds, metaDataFileName).exists());
        assertEquals(true, new File(dropBox, Constants.IS_FINISHED_PREFIX + fileName).exists());
    }

    private void assertHardLinkOnSameFile(File file1, File file2)
    {
        // check hard-link copy by changing last-modified date of one file should change
        // last-modified date of the other file.
        file1.setLastModified(4711000);
        assertEquals(4711000, file2.lastModified());
    }
    
    private Sample createFlowLaneSample(int flowLaneNumber)
    {
        Sample sample = new Sample();
        sample.setCode(SAMPLE_CODE + SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING
                + flowLaneNumber);
        Group group = new Group();
        group.setCode("G" + flowLaneNumber);
        sample.setGroup(group);
        sample.setSubCode(Integer.toString(flowLaneNumber));
        sample.setGeneratedFrom(EXAMPLE_FLOW_CELL_SAMPLE);
        Person registrator = new Person();
        registrator.setEmail("ab@c.de");
        sample.setRegistrator(registrator);
        sample.setIdentifier(SAMPLE_CODE + SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING
                + flowLaneNumber);
        return sample;
    }
    
}
