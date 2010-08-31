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

import static ch.ethz.bsse.cisd.dsu.dss.FlowLaneDataSetInfoExtractor.FLOW_LANE_NUMBER_SEPARATOR;

import java.io.File;
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

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class FlowLaneFeederTest extends AbstractFileSystemTestCase
{
    private static final String META_DATA_PREFIX = "meta-";

    private static final String AFFILIATION = "fmi";

    private static final String EXTERNAL_SAMPLE_NAME = "ext23";

    private static final String TRANSFER_DROP_BOX = "transfer-drop-box";

    private static final Sample EXAMPLE_FLOW_CELL_SAMPLE = createFlowCellSample();

    private static final DataSetInformation EXAMPLE_DATA_SET_INFO = createDataSetInfo();

    private static final String FLOW_LANE = "ILLUMINA_FLOW_LANE";

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

    private File srfInfo;

    @BeforeMethod
    public void beforeMethod() throws Exception
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
        srfInfo = new File(workingDirectory, "srfInfo");
        flowLaneFeeder = createFeeder(null);
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
    void testUnkownFlowLaneSample()
    {
        File flowCell = new File(workingDirectory, SAMPLE_CODE);
        assertEquals(true, flowCell.mkdir());
        FileUtilities.writeToFile(new File(flowCell, "s_3.srf"), "hello flow lane 3");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.<Sample> asList());

        try
        {
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No flow lane sample for flow lane 3 found.", ex.getMessage());
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
        Sample fl = createFlowLaneSample(3);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.<Sample> asList(fl));

        try
        {
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);
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
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);
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
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Only 1 flow lane files found instead of 2.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testFlowLaneTwice()
    {
        flowLaneFeeder.setUpdateDataTransferredProperty(false);
        File flowCell = new File(workingDirectory, SAMPLE_CODE);
        assertEquals(true, flowCell.mkdir());
        File logs = new File(flowCell, "logs");
        assertEquals(true, logs.mkdir());
        FileUtilities.writeToFile(new File(logs, "basic.log"), "hello log");
        File srfFolder = new File(flowCell, "SRF");
        assertEquals(true, srfFolder.mkdir());
        File originalFlowLane1 = new File(srfFolder, "s_1.srf");
        FileUtilities.writeToFile(originalFlowLane1, "hello flow lane 1");
        File originalFlowLane2 = new File(srfFolder, "1.srf");
        FileUtilities.writeToFile(originalFlowLane2, "hello second flow lane 1");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);
        Sample fl1 = createFlowLaneSample(1);
        Sample fl2 = createFlowLaneSample(1);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.asList(fl1, fl2));
        prepareGetProperties(Arrays.asList(fl1));

        try
        {
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Flow lane 1 already registered.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCase()
    {
        flowLaneFeeder.setUpdateDataTransferredProperty(true);
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
        File srf_info_file = new File(srfFolder, "srf_info.txt");
        FileUtilities.writeToFile(srf_info_file,
                "Reading archive ETHZ_BSSE_100602_61VVHAAXX_1.srf.\n"
                        + "Reading srf null index block\n" + "Reads: GOOD : 20712959\n"
                        + "Reads: BAD : 7270034\n" + "Reads: TOTAL : 27982993");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);

        SampleTypePropertyType p1 = createPropertyType("P1");
        SampleTypePropertyType p2 = createPropertyType("P2");
        SampleTypePropertyType p3 = createPropertyType("DATA_TRANSFERRED");
        IEntityProperty fl1p1v = new GenericValueEntityProperty();
        fl1p1v.setPropertyType(p1.getPropertyType());
        fl1p1v.setValue("v1");
        IEntityProperty fl2p2v = new GenericValueEntityProperty();
        fl2p2v.setPropertyType(p2.getPropertyType());
        fl2p2v.setValue("v2");

        Sample fl1 = createFlowLaneSample(1, fl1p1v);
        Sample fl2 = createFlowLaneSample(2, fl2p2v);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.asList(fl1, fl2));
        prepareGetProperties(Arrays.asList(fl1, fl2));

        prepareGetSampleType(FLOW_LANE, p1, p2, p3);
        prepareUpdateSamples(fl1, fl2);

        flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);

        checkFlowLaneDataSet(originalFlowLane1, "1");
        checkFlowLaneDataSet(originalFlowLane2, "2");

        File[] transferedFiles = transferDropBox.listFiles();
        Arrays.sort(transferedFiles);
        assertEquals(2, transferedFiles.length);
        assertEquals("2.srf", transferedFiles[0].getName());
        File metaFile = transferedFiles[1];
        assertEquals(META_DATA_PREFIX + SAMPLE_CODE + "_2" + FlowLaneFeeder.META_DATA_FILE_TYPE,
                metaFile.getName());
        assertHardLinkOnSameFile(originalFlowLane2, transferedFiles[0]);

        context.assertIsSatisfied();
    }

    private void prepareUpdateSamples(final Sample... samples)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(service).updateSample(with(new BaseMatcher<SampleUpdatesDTO>()
                        {
                            public boolean matches(Object item)
                            {
                                if (item instanceof SampleUpdatesDTO)
                                {
                                    SampleUpdatesDTO sampleUpdate = (SampleUpdatesDTO) item;
                                    Long updateId = sampleUpdate.getSampleIdOrNull().getId();
                                    boolean found = false;
                                    for (Sample s : samples)
                                    {
                                        if (s.getId().equals(updateId))
                                        {
                                            found = true;
                                            assertEquals(s.getId(), sampleUpdate
                                                    .getSampleIdOrNull().getId());
                                            assertEquals(0, sampleUpdate.getAttachments().size());
                                            assertEquals(null, sampleUpdate
                                                    .getContainerIdentifierOrNull());
                                            assertEquals(null, sampleUpdate
                                                    .getExperimentIdentifierOrNull());
                                            assertEquals(s.getIdentifier(), sampleUpdate
                                                    .getSampleIdentifier().toString());
                                            List<IEntityProperty> properties =
                                                    sampleUpdate.getProperties();
                                            assertEquals(s.getProperties().size() + 1, properties
                                                    .size());
                                            assertEquals("DATA_TRANSFERRED", properties.get(
                                                    properties.size() - 1).getPropertyType()
                                                    .getCode());
                                            break;
                                        }
                                    }
                                    if (found == false)
                                    {
                                        fail("Didn't expect update of sample with id " + updateId);
                                    }
                                    return true;
                                }
                                return false;
                            }

                            public void describeTo(Description description)
                            {
                            }
                        }));
                }
            });
    }

    private void prepareGetSampleType(final String sampleTypeCode,
            final SampleTypePropertyType... sampleTypePropertyTypes)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(service).getSampleType(sampleTypeCode);
                    SampleType sampleType = new SampleType();
                    sampleType.setCode(sampleTypeCode);
                    sampleType.setSampleTypePropertyTypes(Arrays.asList(sampleTypePropertyTypes));
                    will(returnValue(sampleType));
                }
            });
    }

    private static SampleTypePropertyType createPropertyType(String key)
    {
        SampleTypePropertyType stpt = new SampleTypePropertyType();
        stpt.setMandatory(false);
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(key);
        stpt.setPropertyType(propertyType);
        return stpt;
    }

    @Test
    public void testHappyCaseWithSRFInfo()
    {
        flowLaneFeeder = createFeeder("echo option: $1\necho file: $2");
        flowLaneFeeder.setUpdateDataTransferredProperty(false);

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
        File srf_info_file = new File(srfFolder, "srf_info.txt");
        FileUtilities.writeToFile(srf_info_file,
                "Reading archive ETHZ_BSSE_100602_61VVHAAXX_1.srf.\n"
                        + "Reading srf null index block\n" + "Reads: GOOD : 20712959\n"
                        + "Reads: BAD : 7270034\n" + "Reads: TOTAL : 27982993");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);

        Sample fl1 = createFlowLaneSample(1);
        Sample fl2 = createFlowLaneSample(2);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.asList(fl1, fl2));
        prepareGetProperties(Arrays.asList(fl1, fl2));

        flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);

        checkFlowLaneDataSet(originalFlowLane1, "1");
        checkFlowLaneDataSet(originalFlowLane2, "2");

        File[] transferedFiles = transferDropBox.listFiles();
        Arrays.sort(transferedFiles);
        assertEquals(2, transferedFiles.length);
        assertEquals("2.srf", transferedFiles[0].getName());
        File metaFile = transferedFiles[1];
        assertEquals(META_DATA_PREFIX + SAMPLE_CODE + "_2" + FlowLaneFeeder.META_DATA_FILE_TYPE,
                metaFile.getName());
        List<String> metaData = FileUtilities.loadToStringList(metaFile);
        String lastLine = metaData.remove(metaData.size() - 1);
        assertEquals("[Parent\tnull, Code\tfc:2, Contact Person Email\tab@c.de, "
                + "AFFILIATION\tfmi, EXTERNAL_SAMPLE_NAME\text23, , "
                + "==== SRF Info ====\t, option: -l1\t]", metaData.toString());
        AssertionUtil.assertContains("file: ", lastLine);
        assertEquals(8, metaData.size());
        assertHardLinkOnSameFile(originalFlowLane2, transferedFiles[0]);

        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCaseWithAdditionalFiles()
    {
        flowLaneFeeder.setUpdateDataTransferredProperty(false);
        File flowCell = new File(workingDirectory, SAMPLE_CODE);
        assertEquals(true, flowCell.mkdir());
        File logs = new File(flowCell, "logs");
        assertEquals(true, logs.mkdir());
        FileUtilities.writeToFile(new File(logs, "basic.log"), "hello log");
        File srfFolder = new File(flowCell, "SRF");
        assertEquals(true, srfFolder.mkdir());
        File originalFlowLane1 = new File(srfFolder, "s_1.srf");
        File originalFlowLane1Fastq = new File(srfFolder, "s_1.fastq");
        FileUtilities.writeToFile(originalFlowLane1, "hello flow lane 1");
        FileUtilities.writeToFile(originalFlowLane1Fastq, "fastq");
        File originalFlowLane2 = new File(srfFolder, "2.srf");
        File originalFlowLane2Fasta = new File(srfFolder, "2.fasta");
        File originalFlowLane2Pdf = new File(srfFolder, "2_boxplot.pdf");
        File originalFlowLane2Xxx = new File(srfFolder, "2abc.xxx");
        FileUtilities.writeToFile(originalFlowLane2, "hello flow lane 2");
        FileUtilities.writeToFile(originalFlowLane2Fasta, "fasta");
        FileUtilities.writeToFile(originalFlowLane2Pdf, "pdf");
        FileUtilities.writeToFile(originalFlowLane2Xxx, "xxx");
        File originalFlowLane2Dir = new File(srfFolder, "2dir");
        assertEquals(true, originalFlowLane2Dir.mkdir());
        File internalDirFile1 = new File(originalFlowLane2Dir, "file1");
        File internalDirFile2 = new File(originalFlowLane2Dir, "file2");
        FileUtilities.writeToFile(internalDirFile1, "internal 1");
        FileUtilities.writeToFile(internalDirFile2, "internal 2");
        File srf_info_file = new File(srfFolder, "srf_info.txt");
        FileUtilities.writeToFile(srf_info_file,
                "Reading archive ETHZ_BSSE_100602_61VVHAAXX_1.srf.\n"
                        + "Reading srf null index block\n" + "Reads: GOOD : 20712959\n"
                        + "Reads: BAD : 7270034\n" + "Reads: TOTAL : 27982993");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);

        Sample fl1 = createFlowLaneSample(1);
        Sample fl2 = createFlowLaneSample(2);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.asList(fl1, fl2));
        prepareGetProperties(Arrays.asList(fl1, fl2));

        flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);

        checkFlowLaneDataSet(originalFlowLane1, "1");
        checkFlowLaneDataSet(originalFlowLane2, "2");

        File[] transferedFiles = transferDropBox.listFiles();
        Arrays.sort(transferedFiles);
        assertEquals(6, transferedFiles.length);

        assertEquals("2.fasta", transferedFiles[0].getName());
        assertEquals("2.srf", transferedFiles[1].getName());
        assertEquals("2_boxplot.pdf", transferedFiles[2].getName());
        assertEquals("2abc.xxx", transferedFiles[3].getName());
        assertEquals("2dir", transferedFiles[4].getName());
        File metaFile = transferedFiles[5];
        assertEquals(META_DATA_PREFIX + SAMPLE_CODE + "_2" + FlowLaneFeeder.META_DATA_FILE_TYPE,
                metaFile.getName());
        assertHardLinkOnSameFile(originalFlowLane2Fasta, transferedFiles[0]);
        assertHardLinkOnSameFile(originalFlowLane2, transferedFiles[1]);
        assertHardLinkOnSameFile(originalFlowLane2Pdf, transferedFiles[2]);
        assertHardLinkOnSameFile(originalFlowLane2Xxx, transferedFiles[3]);

        File[] internalDirFiles = transferedFiles[4].listFiles();
        Arrays.sort(internalDirFiles);
        assertEquals(2, internalDirFiles.length);

        assertEquals("file1", internalDirFiles[0].getName());
        assertEquals("file2", internalDirFiles[1].getName());
        assertHardLinkOnSameFile(internalDirFile1, internalDirFiles[0]);
        assertHardLinkOnSameFile(internalDirFile2, internalDirFiles[1]);

        context.assertIsSatisfied();
    }

    @Test
    public void testInvalidSRFFile()
    {
        flowLaneFeeder = createFeeder("exit 1");
        flowLaneFeeder.setUpdateDataTransferredProperty(false);
        File flowCell = new File(workingDirectory, SAMPLE_CODE);
        assertEquals(true, flowCell.mkdir());
        File originalFlowLane1 = new File(flowCell, "s_1.srf");
        FileUtilities.writeToFile(originalFlowLane1, "hello flow lane 1");
        prepareLoadFlowCellSample(EXAMPLE_FLOW_CELL_SAMPLE);

        Sample fl1 = createFlowLaneSample(1);
        prepareListFlowLanes(EXAMPLE_FLOW_CELL_SAMPLE, Arrays.asList(fl1));

        try
        {
            flowLaneFeeder.handle(flowCell, EXAMPLE_DATA_SET_INFO, null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            AssertionUtil.assertContains("Invalid SRF file", ex.getMessage());
            AssertionUtil.assertContains("s_1.srf", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUndoLastOperation()
    {
        testHappyCase();
        assertEquals(2, dropBox1.list().length);
        assertEquals(2, dropBox2.list().length);
        assertEquals(2, transferDropBox.list().length);

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
                    one(service).tryGetSampleWithExperiment(
                            EXAMPLE_DATA_SET_INFO.getSampleIdentifier());
                    will(returnValue(flowCellSample));
                }
            });
    }

    private void prepareListFlowLanes(final Sample flowCellSample,
            final List<Sample> flowLaneSamples)
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
                    p.setValue(FlowLaneFeeder.escapeSampleCode(value));
                    PropertyType propertyType = new PropertyType();
                    propertyType.setCode(FlowLaneFeeder.escapeSampleCode(key));
                    propertyType.setLabel(FlowLaneFeeder.escapeSampleCode(key).toLowerCase());
                    p.setPropertyType(propertyType);
                    return p;
                }
            });
    }

    private void checkFlowLaneDataSet(File originalFlowLane, String flowLaneNumber)
    {
        File dropBox =
                new File(workingDirectory, DROP_BOX_PREFIX
                        + FlowLaneFeeder.escapeSampleCode(flowLaneNumber));
        String fileName =
                "G" + FlowLaneFeeder.escapeSampleCode(flowLaneNumber)
                        + FlowLaneFeeder.DEFAULT_ENTITY_SEPARATOR + SAMPLE_CODE
                        + FLOW_LANE_NUMBER_SEPARATOR
                        + FlowLaneFeeder.escapeSampleCode(flowLaneNumber);
        File ds = new File(dropBox, FlowLaneFeeder.escapeSampleCode(fileName));
        assertEquals(true, ds.isDirectory());

        File flowLane = new File(ds, originalFlowLane.getName());
        assertEquals(true, flowLane.isFile());
        assertEquals(FileUtilities.loadToString(originalFlowLane), FileUtilities
                .loadToString(flowLane));
        assertHardLinkOnSameFile(originalFlowLane, flowLane);
        String metaDataFileName =
                META_DATA_PREFIX
                        + (SAMPLE_CODE + "_" + FlowLaneFeeder.escapeSampleCode(flowLaneNumber))
                        + FlowLaneFeeder.META_DATA_FILE_TYPE;
        assertEquals(true, new File(ds, FlowLaneFeeder.escapeSampleCode(metaDataFileName)).exists());
        assertEquals(true, new File(dropBox, Constants.IS_FINISHED_PREFIX
                + FlowLaneFeeder.escapeSampleCode(fileName)).exists());
    }

    private void assertHardLinkOnSameFile(File file1, File file2)
    {
        // check hard-link copy by changing last-modified date of one file should change
        // last-modified date of the other file.
        file1.setLastModified(4711000);
        assertEquals(4711000, file2.lastModified());
    }

    private Sample createFlowLaneSample(int flowLaneNumber, IEntityProperty... properties)
    {
        Sample sample = new Sample();
        sample.setId((long) flowLaneNumber);
        sample.setCode(SAMPLE_CODE + ":" + flowLaneNumber);
        Space space = new Space();
        space.setCode("G" + flowLaneNumber);
        sample.setSpace(space);
        sample.setSubCode(Integer.toString(flowLaneNumber));
        sample.setGeneratedFrom(EXAMPLE_FLOW_CELL_SAMPLE);
        Person registrator = new Person();
        registrator.setEmail("ab@c.de");
        sample.setRegistrator(registrator);
        sample.setIdentifier(SAMPLE_CODE + ":" + flowLaneNumber);
        SampleType type = new SampleType();
        type.setCode(FLOW_LANE);
        sample.setSampleType(type);
        sample.setProperties(Arrays.asList(properties));
        return sample;
    }

    private FlowLaneFeeder createFeeder(String srfInfoScriptOrNull)
    {
        Properties properties = new Properties();
        properties.setProperty(FlowLaneFeeder.FLOW_LANE_DROP_BOX_TEMPLATE, new File(
                workingDirectory, DROP_BOX_PREFIX).getAbsolutePath()
                + "{0}");
        properties.setProperty(FlowLaneFeeder.META_DATA_FILE_PREFIX, META_DATA_PREFIX);
        properties.setProperty(FlowLaneFeeder.TRANSFER_PREFIX + AFFILIATION, transferDropBox
                .getAbsolutePath());
        if (srfInfoScriptOrNull != null)
        {
            properties.setProperty(FlowLaneFeeder.SRF_INFO_PATH, srfInfo.getAbsolutePath());
            FileUtilities.writeToFile(srfInfo, srfInfoScriptOrNull);
            File chmod = OSUtilities.findExecutable("chmod");
            assertNotNull(chmod);
            try
            {
                String cmd = chmod.getAbsolutePath() + " +x " + srfInfo.getAbsolutePath();
                Runtime.getRuntime().exec(cmd).waitFor();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        return new FlowLaneFeeder(properties, service);
    }

}
