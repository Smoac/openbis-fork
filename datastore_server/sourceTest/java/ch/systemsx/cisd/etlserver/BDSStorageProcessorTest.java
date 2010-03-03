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

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.DataSet;
import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.ExperimentRegistrator;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.IDataStructure;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.UnknownFormatV1_0;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.Version;
import ch.systemsx.cisd.bds.Utilities.Boolean;
import ch.systemsx.cisd.bds.hcs.Channel;
import ch.systemsx.cisd.bds.hcs.HCSImageFormatV1_0;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.WellGeometry;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.v1_1.IDataStructureV1_1;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * Test cases for corresponding {@link BDSStorageProcessor} class.
 * 
 * @author Christian Ribeaud
 */
public final class BDSStorageProcessorTest extends AbstractFileSystemTestCase
{
    private static final String EXAMPLE_EMAIL = "j@d";

    private static final String DATA_SET_CODE = "D";

    private static final String INCOMING_DATA_SET_DIR = "NEMO.EXP1==CP001A-3AB";

    private static final String EXAMPLE_TYPE_DESCRIPTION = "Screening Plate";

    private static final Date REGISTRATION_DATE = new Date(47110000);

    private static final String EXAMPLE_INSTANCE = "I";

    private static final String EXAMPLE_INSTANCE_GLOBAL = "222-333";

    private static final String EXAMPLE_GROUP = "G";

    private static final String DATA_STRUCTURE_NAME = "originalData";

    private static final String EXAMPLE_DATA = "hello world!";

    private static final String ORIGINAL_DATA_TXT = DATA_STRUCTURE_NAME + ".txt";

    private static final String VERSION_PROPERTY_KEY = BDSStorageProcessor.VERSION_KEY;

    private static final String FORMAT_KEY = BDSStorageProcessor.FORMAT_KEY;

    private static final String SAMPLE_TYPE_DESCRIPTION_KEY =
            BDSStorageProcessor.SAMPLE_TYPE_DESCRIPTION_KEY;

    private static final String SAMPLE_TYPE_CODE_KEY =

    BDSStorageProcessor.SAMPLE_TYPE_CODE_KEY;

    private static final String CHANNEL_COUNT_KEY = HCSImageFormatV1_0.NUMBER_OF_CHANNELS;

    private static final String CONTAINS_ORIGINAL_DATA_KEY =
            HCSImageFormatV1_0.CONTAINS_ORIGINAL_DATA;

    private static final String WELL_GEOMETRY_KEY = WellGeometry.WELL_GEOMETRY;

    private static final String FILE_EXTRACTOR_KEY = IHCSImageFileExtractor.FILE_EXTRACTOR;

    private final static ITypeExtractor TYPE_EXTRACTOR =
            new DefaultStorageProcessorTest.TestProcedureAndDataTypeExtractor();

    private final static String STORE_ROOT_DIR = "store";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IMailClient mailClient;

    private final static Properties createProperties(final Format format)
    {
        final Properties props = createPropertiesWithVersion();
        props.setProperty(ETLDaemon.STOREROOT_DIR_KEY, "store");
        props.setProperty(SAMPLE_TYPE_DESCRIPTION_KEY, EXAMPLE_TYPE_DESCRIPTION);
        props.setProperty(SAMPLE_TYPE_CODE_KEY, "CELL_PLATE");
        props.setProperty(FORMAT_KEY, format.getCode() + " " + format.getVersion());
        props.setProperty(CHANNEL_COUNT_KEY, "1");
        props.setProperty(CONTAINS_ORIGINAL_DATA_KEY, Utilities.Boolean.TRUE.toString());
        props.setProperty(WELL_GEOMETRY_KEY, "3x3");
        props.setProperty(FILE_EXTRACTOR_KEY, TestImageFileExtractor.class.getName());
        return props;
    }

    final static DataSetInformation createDataSetInformation()
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setInstanceCode(EXAMPLE_INSTANCE);
        dataSetInformation.setInstanceUUID(EXAMPLE_INSTANCE_GLOBAL);
        final ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier();
        experimentIdentifier.setExperimentCode("E");
        experimentIdentifier.setProjectCode("P");
        experimentIdentifier.setGroupCode(EXAMPLE_GROUP);
        dataSetInformation.setExperimentIdentifier(experimentIdentifier);
        dataSetInformation.setSampleCode("S");
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        final IEntityProperty plateGeometry =
                createSampleProperty(PlateDimensionParser.PLATE_GEOMETRY_PROPERTY_NAME,
                        DataTypeCode.VARCHAR, "_16X24");
        dataSetInformation.setProperties(new IEntityProperty[]
            { plateGeometry });
        return dataSetInformation;
    }

    private final static IEntityProperty createSampleProperty(final String code,
            final DataTypeCode dataType, final String value)
    {
        final IEntityProperty property = new EntityProperty();
        final SampleTypePropertyType entityTypePropertyType = new SampleTypePropertyType();
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        propertyType.setLabel(code);
        final DataType type = new DataType();
        type.setCode(dataType);
        propertyType.setDataType(type);
        entityTypePropertyType.setPropertyType(propertyType);
        property.setPropertyType(propertyType);
        property.setValue(value);
        return property;
    }

    private final File createOriginalDataInDir() throws IOException
    {
        final File dir = createDirInIncoming(INCOMING_DATA_SET_DIR);
        final File originalData = new File(dir, ORIGINAL_DATA_TXT);
        FileUtilities.writeToFile(originalData, EXAMPLE_DATA);
        return dir;
    }

    private File createDirInIncoming(String incomingItemName)
    {
        final File incoming = getIncomingDir();
        final File dir = new File(incoming, incomingItemName);
        dir.mkdir();
        return dir;
    }

    private File getIncomingDir()
    {
        final File incoming = new File(workingDirectory, "incoming");
        incoming.mkdir();
        return incoming;
    }

    static final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample createSample()
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample();
        sample.setExperiment(createExperiment());
        return sample;
    }

    static final Experiment createExperiment()
    {
        final Experiment baseExperiment = new Experiment();
        baseExperiment.setRegistrationDate(REGISTRATION_DATE);
        final Person person = new Person();
        person.setFirstName("Joe");
        person.setLastName("Doe");
        person.setEmail(EXAMPLE_EMAIL);
        final Space group = new Space();
        group.setCode(EXAMPLE_GROUP);
        final Project project = new Project();
        project.setSpace(group);
        baseExperiment.setProject(project);
        baseExperiment.setRegistrator(person);
        return baseExperiment;
    }

    private final static Properties createPropertiesWithVersion()
    {
        final Properties props = new Properties();
        props.setProperty(VERSION_PROPERTY_KEY, "1.1");
        return props;
    }

    @BeforeClass
    public void startQueueingPathRemover()
    {
        if (QueueingPathRemoverService.isRunning() == false)
        {
            QueueingPathRemoverService.start();
        }
    }

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testConstructorWithUnspecifiedArgument()
    {
        boolean fail = true;
        try
        {
            new BDSStorageProcessor(null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testCheckVersionInProperties()
    {
        final Properties props = new Properties();
        props.setProperty(ETLDaemon.STOREROOT_DIR_KEY, "store");
        final String version = "ae";
        props.setProperty(VERSION_PROPERTY_KEY, version);
        try
        {
            new BDSStorageProcessor(props);
            fail("No '.' in given version.");
        } catch (final ConfigurationFailureException ex)
        {
            assertEquals(String.format(BDSStorageProcessor.NO_VERSION_FORMAT,
                    BDSStorageProcessor.VERSION_KEY, version), ex.getMessage());
        }
    }

    @Test
    public final void testCheckVersionCompatible()
    {
        final Properties props = new Properties();
        props.setProperty(ETLDaemon.STOREROOT_DIR_KEY, "store");
        props.setProperty(VERSION_PROPERTY_KEY, "1.2");
        try
        {
            new BDSStorageProcessor(props);
            fail("ConfigurationFailureException expected");
        } catch (final ConfigurationFailureException e)
        {
            assertEquals("Invalid version: V1.2", e.getMessage());
        }
    }

    @Test
    public final void testMissingFormat() throws Exception
    {
        final Properties props = createPropertiesWithVersion();
        try
        {
            new BDSStorageProcessor(props);
            fail("ConfigurationFailureException expected");
        } catch (final ConfigurationFailureException e)
        {
            assertEquals("Given key 'format' not found in properties '[version]'", e.getMessage());
        }
    }

    @Test
    public final void testMissingSampleTypeDescription() throws Exception
    {
        final Properties props = createPropertiesWithVersion();
        final Format format = UnknownFormatV1_0.UNKNOWN_1_0;
        props.setProperty(FORMAT_KEY, format.getCode() + " " + format.getVersion());
        try
        {
            new BDSStorageProcessor(props);
            fail("ConfigurationFailureException expected");
        } catch (final ConfigurationFailureException e)
        {
            assertEquals("Given key 'sampleTypeDescription' not found in properties "
                    + "'[version, format]'", e.getMessage());
        }
    }

    @Test
    // we check the mode where the incoming folder has just a link to the real data. The original
    // data folder in the store should point to this data (and not to the data in incoming folder)
    public final void testStoreDataWithSymbolicLink() throws Exception
    {
        final Properties properties = createProperties(UnknownFormatV1_0.UNKNOWN_1_0);
        properties.setProperty(HCSImageFormatV1_0.IS_INCOMING_SYMBOLIC_LINK, "true");
        final BDSStorageProcessor storageProcessor = new BDSStorageProcessor(properties);

        final File orgData = new File(workingDirectory, "org-data.txt");
        FileUtilities.writeToFile(orgData, "hello!");

        final File incoming = getIncomingDir();
        SoftLinkMaker.createSymbolicLink(orgData, incoming);

        final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample baseSample = createSample();
        final DataSetInformation dataSetInformation = createDataSetInformation();
        dataSetInformation.setSample(baseSample);

        storeData(storageProcessor, new File(incoming, orgData.getName()), dataSetInformation);
        File datasetOriginalDir = new File(getStoreDir(), "data/original/" + orgData.getName());
        assertTrue("link to original file has not been created", datasetOriginalDir.isFile());
        String orgDataAbsPath = orgData.getAbsolutePath();
        String datasetOriginalDirReference = datasetOriginalDir.getCanonicalPath();
        assertEquals("link to original file does not point to the original data " + orgDataAbsPath
                + ", but to " + datasetOriginalDirReference, datasetOriginalDirReference,
                orgDataAbsPath);
    }

    @DataProvider(name = "formatProvider")
    public Object[][] getFormats()
    {
        return new Object[][]
            {
                { UnknownFormatV1_0.UNKNOWN_1_0 },
                { HCSImageFormatV1_0.HCS_IMAGE_1_0 } };
    }

    @Test(dataProvider = "formatProvider")
    public final void testStoreData(final Format format) throws Exception
    {
        final Properties properties = createProperties(format);
        final BDSStorageProcessor storageProcessor = new BDSStorageProcessor(properties);
        assertEquals(0, workingDirectory.list().length);
        final File incomingDataSetDirectory = createOriginalDataInDir();
        assertEquals(true, incomingDataSetDirectory.exists());
        final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample baseSample = createSample();
        final DataSetInformation dataSetInformation = createDataSetInformation();
        dataSetInformation.setSample(baseSample);
        prepareMailClient(format);
        final File dataFile =
                storeData(storageProcessor, incomingDataSetDirectory, dataSetInformation);
        assertEquals(getStoreDir().getAbsolutePath(), dataFile.getAbsolutePath());
        final IDataStructure dataStructure =
                new DataStructureLoader(workingDirectory).load(STORE_ROOT_DIR, true);
        assertEquals(true, dataStructure instanceof IDataStructureV1_1);
        final IDataStructureV1_1 ds = (IDataStructureV1_1) dataStructure;
        assertEquals(new Version(1, 1), ds.getVersion());
        final ch.systemsx.cisd.bds.ExperimentIdentifier eid = ds.getExperimentIdentifier();
        assertEquals(EXAMPLE_INSTANCE, eid.getInstanceCode());
        assertEquals(EXAMPLE_GROUP, eid.getSpaceCode());
        assertEquals(dataSetInformation.getExperimentIdentifier().getProjectCode(), eid
                .getProjectCode());
        assertEquals(dataSetInformation.getExperimentIdentifier().getExperimentCode(), eid
                .getExperimentCode());
        final ExperimentRegistrator registrator = ds.getExperimentRegistrator();
        final Experiment baseExperiment = baseSample.getExperiment();
        assertEquals(baseExperiment.getRegistrator().getFirstName(), registrator.getFirstName());
        assertEquals(baseExperiment.getRegistrator().getLastName(), registrator.getLastName());
        assertEquals(baseExperiment.getRegistrator().getEmail(), registrator.getEmail());
        assertEquals(REGISTRATION_DATE, ds.getExperimentRegistratorTimestamp().getDate());
        final Sample sample = ds.getSample();
        assertEquals(EXAMPLE_TYPE_DESCRIPTION, sample.getTypeDescription());
        assertEquals(dataSetInformation.getSampleIdentifier().getSampleCode(), sample.getCode());
        final Format f = ds.getFormattedData().getFormat();
        assertEquals(format, f);
        final IDirectory directory =
                (IDirectory) ds.getOriginalData().tryGetNode(INCOMING_DATA_SET_DIR);
        assertEquals(EXAMPLE_DATA, Utilities.getTrimmedString(directory, ORIGINAL_DATA_TXT));
        assertEquals(false, incomingDataSetDirectory.exists());
        // DataSet
        final DataSet dataSet = ds.getDataSet();
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(TYPE_EXTRACTOR.getDataSetType(null).getCode(), dataSet.getDataSetTypeCode());
        assertEquals(0, dataSet.getParentCodes().size());
        assertNull(dataSet.getProducerCode());
        assertNull(dataSet.getProductionTimestamp());
        assertEquals(Boolean.TRUE, dataSet.isMeasured());

        context.assertIsSatisfied();
    }

    private File storeData(final BDSStorageProcessor storageProcessor,
            final File incomingDataSetDirectory, final DataSetInformation dataSetInformation)
    {
        return storageProcessor.storeData(dataSetInformation, TYPE_EXTRACTOR, mailClient,
                incomingDataSetDirectory, getStoreDir());
    }

    private File getStoreDir()
    {
        return new File(workingDirectory, STORE_ROOT_DIR);
    }

    @Test(dataProvider = "formatProvider")
    public final void testUnstoreData(final Format format) throws Exception
    {
        final Properties properties = createProperties(format);
        final BDSStorageProcessor storageAdapter = new BDSStorageProcessor(properties);
        assertEquals(0, workingDirectory.list().length);
        final File incomingDirectoryData = createOriginalDataInDir();
        // incoming/NEMO.EXP1==CP001A-3AB in 'workingDirectory'
        assert incomingDirectoryData.exists();
        final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample baseSample = createSample();
        final DataSetInformation dataSetInformation = createDataSetInformation();
        dataSetInformation.setSample(baseSample);
        // NEMO.EXP1==CP001A-3AB in 'workingDirectory'
        prepareMailClient(format);
        final File storeRootDir = getStoreDir();
        final File dataStore =
                storageAdapter.storeData(dataSetInformation, TYPE_EXTRACTOR, mailClient,
                        incomingDirectoryData, storeRootDir);
        assertEquals(true, dataStore.isDirectory());
        assertEquals(false, incomingDirectoryData.exists());
        storageAdapter.rollback(incomingDirectoryData, storeRootDir, null);
        assertEquals(false, dataStore.exists());
        assertEquals(true, incomingDirectoryData.isDirectory());

        context.assertIsSatisfied();
    }

    @Test(dataProvider = "formatProvider")
    public void testTryToGetOriginalData(final Format format) throws Exception
    {
        final Properties properties = createProperties(format);
        final BDSStorageProcessor storageProcessor = new BDSStorageProcessor(properties);
        final File incomingDirectoryData = createOriginalDataInDir();
        final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample baseSample = createSample();
        final DataSetInformation dataSetInformation = createDataSetInformation();
        dataSetInformation.setSample(baseSample);
        prepareMailClient(format);
        final File storeData =
                storageProcessor.storeData(dataSetInformation, TYPE_EXTRACTOR, mailClient,
                        incomingDirectoryData, workingDirectory);
        final File originalDataSet = storageProcessor.tryGetProprietaryData(storeData);
        assertNotNull(originalDataSet);
        assertEquals(INCOMING_DATA_SET_DIR, originalDataSet.getName());
        assertEquals(true, originalDataSet.isDirectory());
        assertEquals(format == UnknownFormatV1_0.UNKNOWN_1_0 ? BooleanOrUnknown.U
                : BooleanOrUnknown.F, dataSetInformation.getIsCompleteFlag());
        final File[] files = originalDataSet.listFiles();
        assertEquals(1, files.length);
        final File file = files[0];
        assertEquals(true, file.exists());
        assertEquals(false, file.isDirectory());
        assertEquals(ORIGINAL_DATA_TXT, file.getName());

        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetOriginalDataWhichAreNotAvailable() throws Exception
    {
        final Properties properties = createProperties(HCSImageFormatV1_0.HCS_IMAGE_1_0);
        properties.setProperty(CONTAINS_ORIGINAL_DATA_KEY, Utilities.Boolean.FALSE.toString());
        final BDSStorageProcessor storageProcessor = new BDSStorageProcessor(properties);
        final File incomingDirectoryData = createOriginalDataInDir();
        final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample baseSample = createSample();
        final DataSetInformation dataSetInformation = createDataSetInformation();
        dataSetInformation.setSample(baseSample);
        prepareMailClient(HCSImageFormatV1_0.HCS_IMAGE_1_0);
        final File storeData =
                storageProcessor.storeData(dataSetInformation, TYPE_EXTRACTOR, mailClient,
                        incomingDirectoryData, workingDirectory);
        logRecorder.resetLogContent();
        final File originalDataSet = storageProcessor.tryGetProprietaryData(storeData);
        assertEquals(null, originalDataSet);
        assertEquals("WARN  OPERATION.BDSStorageProcessor - " + "Original data are not available.",
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testConstructorWithInvalidFormat() throws Exception
    {
        final Properties properties = createProperties(new Format("bla", new Version(1, 2), "v"));
        try
        {
            new BDSStorageProcessor(properties);
            fail("ConfigurationFailureException expected");
        } catch (final ConfigurationFailureException e)
        {
            assertEquals("Property 'format': no valid and known format could be extracted "
                    + "from text 'bla V1.2'.", e.getMessage());
        }
    }

    private void prepareMailClient(final Format format)
    {
        if (format != UnknownFormatV1_0.UNKNOWN_1_0)
        {
            context.checking(new Expectations()
                {
                    {
                        one(mailClient)
                                .sendMessage(
                                        "Incomplete data set 'NEMO.EXP1==CP001A-3AB'",
                                        "Incomplete data set 'NEMO.EXP1==CP001A-3AB': "
                                                + "3455 image file(s) are missing (locations: "
                                                + "[[well=[x=16,y=1],tile=[x=3,y=3]], [well=[x=16,y=2],tile=[x=2,y=3]], "
                                                + "[well=[x=16,y=3],tile=[x=1,y=3]], [well=[x=24,y=10],tile=[x=1,y=3]], "
                                                + "[well=[x=24,y=9],tile=[x=2,y=3]], [well=[x=24,y=8],tile=[x=3,y=3]], "
                                                + "[well=[x=7,y=6],tile=[x=1,y=2]], [well=[x=7,y=5],tile=[x=2,y=2]], "
                                                + "[well=[x=7,y=4],tile=[x=3,y=2]], [well=[x=14,y=6],tile=[x=3,y=1]], "
                                                + "... (3445 left)])", null, null, EXAMPLE_EMAIL);
                    }
                });
        }
    }

    //
    // Helper classes
    //

    public final static class TestImageFileExtractor implements IHCSImageFileExtractor
    {

        public TestImageFileExtractor(final Properties properties)
        {
        }

        //
        // IHCSImageFileExtractor
        //

        public final HCSImageFileExtractionResult process(
                final IDirectory incomingDataSetDirectory,
                final DataSetInformation dataSetInformation, final IHCSImageFileAccepter accepter)
        {
            assertEquals(INCOMING_DATA_SET_DIR, incomingDataSetDirectory.getName());
            final List<IFile> listFiles = incomingDataSetDirectory.listFiles(null, true);
            assertEquals(1, listFiles.size());
            final IFile file = listFiles.get(0);
            assertEquals(ORIGINAL_DATA_TXT, file.getName());
            accepter.accept(1, new Location(1, 1), new Location(1, 1), file);
            return new HCSImageFileExtractionResult(0, listFiles.size(), new ArrayList<IFile>(),
                    Collections.singleton(new Channel(1, 123)));
        }
    }

}
