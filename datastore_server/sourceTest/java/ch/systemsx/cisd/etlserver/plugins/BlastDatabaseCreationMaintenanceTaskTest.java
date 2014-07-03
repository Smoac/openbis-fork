/*
 * Copyright 2014 ETH Zuerich, SIS
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

import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.BLAST_DATABASES_FOLDER_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.BLAST_TEMP_FOLDER_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.BLAST_TOOLS_DIRECTORY_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.DATASET_TYPES_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.FILE_TYPES_PROPERTY;
import static ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask.LAST_SEEN_DATA_SET_FILE_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ContainerDataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;

import de.schlichtherle.io.File;

/**
 * @author Franz-Josef Elmer
 */
public class BlastDatabaseCreationMaintenanceTaskTest extends AbstractFileSystemTestCase
{
    private static final String INFO_PREFIX = "INFO  OPERATION." + BlastDatabaseCreationMaintenanceTask.class.getSimpleName() + " - ";

    private static final String ERROR_PREFIX = "ERROR OPERATION." + BlastDatabaseCreationMaintenanceTask.class.getSimpleName() + " - ";

    private static final class MockMaintenanceTask extends BlastDatabaseCreationMaintenanceTask
    {
        private final List<List<String>> commands = new ArrayList<List<String>>();

        private final IConfigProvider configProvider;

        private final IEncapsulatedOpenBISService service;

        private final IHierarchicalContentProvider contentProvider;

        private boolean[] processSuccesses;

        private int indexOfNextProcessSuccessValue;

        MockMaintenanceTask(IConfigProvider configProvider, IEncapsulatedOpenBISService service,
                IHierarchicalContentProvider contentProvider)
        {
            this.configProvider = configProvider;
            this.service = service;
            this.contentProvider = contentProvider;

        }

        void setProcessSuccesses(boolean... processSuccesses)
        {
            this.processSuccesses = processSuccesses;
            indexOfNextProcessSuccessValue = 0;
        }

        @Override
        boolean process(List<String> command)
        {
            commands.add(command);
            return processSuccesses[indexOfNextProcessSuccessValue++ % processSuccesses.length];
        }

        String getCommands()
        {
            StringBuilder builder = new StringBuilder();
            for (List<String> command : commands)
            {
                for (int i = 0; i < command.size(); i++)
                {
                    if (i > 0)
                    {
                        builder.append(' ');
                    }
                    builder.append(command.get(i));
                }
                builder.append('\n');
            }
            return builder.toString();
        }

        @Override
        IConfigProvider getConfigProvider()
        {
            return configProvider;
        }

        @Override
        IEncapsulatedOpenBISService getOpenBISService()
        {
            return service;
        }

        @Override
        IHierarchicalContentProvider getContentProvider()
        {
            return contentProvider;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IConfigProvider configProvider;

    private IEncapsulatedOpenBISService service;

    private IHierarchicalContentProvider contentProvider;

    private MockMaintenanceTask maintenanceTask;

    private File store;

    @BeforeMethod
    public void setUpTask()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        configProvider = context.mock(IConfigProvider.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        maintenanceTask = new MockMaintenanceTask(configProvider, service, contentProvider);
        maintenanceTask.setProcessSuccesses(true);
        store = new File(workingDirectory, "store");
        store.mkdirs();
        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getStoreRoot();
                    will(returnValue(store));
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testSetUpMissingDataSetTypesProperty()
    {
        try
        {
            maintenanceTask.setUp("", new Properties());
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + BlastDatabaseCreationMaintenanceTask.DATASET_TYPES_PROPERTY
                    + "' not found in properties '[]'", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testSetUpInvalidDataSetTypeRegex()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, "[a-z");
        try
        {
            maintenanceTask.setUp("", properties);
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Property '" + DATASET_TYPES_PROPERTY + "' has invalid regular expression '[A-Z': "
                    + "Unclosed character class near index 3\n[A-Z\n   ^", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testWrongBlastToolsFolder()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".+");
        maintenanceTask.setProcessSuccesses(false);

        maintenanceTask.setUp("", properties);

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + store + "/blast-databases\n"
                + INFO_PREFIX + "Temp folder '" + store + "/blast-databases/tmp' created.\n"
                + ERROR_PREFIX + "BLAST isn't installed or property '" + BLAST_TOOLS_DIRECTORY_PROPERTY
                + "' hasn't been correctly specified.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testMinimumSetUp()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".+");

        maintenanceTask.setUp("BLAST databases creation", properties);

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + store + "/blast-databases\n"
                + INFO_PREFIX + "Temp folder '" + store + "/blast-databases/tmp' created.",
                logRecorder.getLogContent());
        assertEquals("makeblastdb -version\n", maintenanceTask.getCommands());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteOneDataSetWithThreeFastaFiles()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, "BLAST.*");
        maintenanceTask.setUp("", properties);
        AbstractExternalData ds1 = new ContainerDataSetBuilder(1L).type("BLAST_CONTAINER").code("DS-1").getContainerDataSet();
        AbstractExternalData ds2 = new DataSetBuilder(2L).type("NOBLAST").code("DS-2")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        AbstractExternalData ds3 = new DataSetBuilder(3L).type("BLAST").code("DS-3")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        AbstractExternalData ds4 = new DataSetBuilder(3L).type("BLAST").code("DS-4")
                .status(DataSetArchivingStatus.ARCHIVED).getDataSet();
        RecordingMatcher<TrackingDataSetCriteria> lastSeenIdMatcher = prepareListNewerDataSet(ds4, ds2, ds3, ds1);
        File dataSetFolder1 = new File(workingDirectory, "data-set-example1");
        File dataFolder = new File(dataSetFolder1, "data");
        dataFolder.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta.txt"), ">1\nGATTACA\n");
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta1.fa"), ">2\nGATTACA\nGATTACA\n");
        FileUtilities.writeToFile(new File(dataFolder, "fasta2.fastq"), "@3\nIAKKATA\n+\nznhjnxzx\n");
        prepareContentProvider(ds3, dataSetFolder1);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 11e5; i++)
        {
            stringBuilder.append("GATTACA".charAt(i % 7));
        }
        // Creating a big sequence dummy file in order to trigger index creation
        FileUtilities.writeToFile(new File(store, "blast-databases/DS-3-prot.nsq"), stringBuilder.toString());

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + store + "/blast-databases\n"
                + INFO_PREFIX + "Temp folder '" + store + "/blast-databases/tmp' created.\n"
                + INFO_PREFIX + "Scan 4 data sets for creating BLAST databases.", logRecorder.getLogContent());
        assertEquals("makeblastdb -version\n"
                + "makeblastdb -in " + store.getAbsolutePath() + "/blast-databases/tmp/DS-3-nucl.fa"
                + " -dbtype nucl -title DS-3-nucl -out " + store.getAbsolutePath() + "/blast-databases/DS-3-nucl\n"
                + "makeblastdb -in " + store.getAbsolutePath() + "/blast-databases/tmp/DS-3-prot.fa"
                + " -dbtype prot -title DS-3-prot -out " + store.getAbsolutePath() + "/blast-databases/DS-3-prot\n"
                + "makembindex -iformat blastdb -input " + store.getAbsolutePath() + "/blast-databases/DS-3-prot"
                + " -old_style_index false\n",
                maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST DS-3-nucl",
                FileUtilities.loadToString(new File(store, "blast-databases/all-nucl.nal")).trim());
        assertEquals("TITLE all-prot\nDBLIST DS-3-prot",
                FileUtilities.loadToString(new File(store, "blast-databases/all-prot.nal")).trim());
        assertEquals("[]", Arrays.asList(new File(store, "blast-databases/tmp").listFiles()).toString());
        assertEquals(0L, lastSeenIdMatcher.recordedObject().getLastSeenDataSetId());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteThreeDataSetsWhereTwoHaveFastaFiles()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".*");
        maintenanceTask.setUp("", properties);
        AbstractExternalData ds1 = new DataSetBuilder(1L).type("NOBLAST").code("DS1")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        AbstractExternalData ds2 = new DataSetBuilder(2L).type("BLAST").code("DS2")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        AbstractExternalData ds3 = new DataSetBuilder(3L).type("BLAST").code("DS3")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        RecordingMatcher<TrackingDataSetCriteria> lastSeenIdMatcher = prepareListNewerDataSet(ds3, ds2, ds1);
        File dataSetFolder1 = new File(workingDirectory, "data-set-example1");
        dataSetFolder1.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta.txt"), ">1\nGATTACA\n");
        prepareContentProvider(ds1, dataSetFolder1);
        File dataSetFolder2 = new File(workingDirectory, "data-set-example2");
        dataSetFolder2.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder2, "fasta.fa"), ">2\nGATTACA\nGATTACA\n");
        prepareContentProvider(ds2, dataSetFolder2);
        File dataSetFolder3 = new File(workingDirectory, "data-set-example3");
        dataSetFolder3.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder3, "fasta.fasta"), ">3\nGATTACA\nGATTACA\nGATTACA\n");
        prepareContentProvider(ds3, dataSetFolder3);

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + store + "/blast-databases\n"
                + INFO_PREFIX + "Temp folder '" + store + "/blast-databases/tmp' created.\n"
                + INFO_PREFIX + "Scan 3 data sets for creating BLAST databases.", logRecorder.getLogContent());
        assertEquals("makeblastdb -version\n"
                + "makeblastdb -in " + store.getAbsolutePath() + "/blast-databases/tmp/DS2-nucl.fa"
                + " -dbtype nucl -title DS2-nucl -out " + store.getAbsolutePath() + "/blast-databases/DS2-nucl\n"
                + "makeblastdb -in " + store.getAbsolutePath() + "/blast-databases/tmp/DS3-nucl.fa"
                + " -dbtype nucl -title DS3-nucl -out " + store.getAbsolutePath() + "/blast-databases/DS3-nucl\n",
                maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST DS2-nucl DS3-nucl",
                FileUtilities.loadToString(new File(store, "blast-databases/all-nucl.nal")).trim());
        assertEquals(false, new File(store, "blast-databases/all-prot.nal").exists());
        assertEquals("[]", Arrays.asList(new File(store, "blast-databases/tmp").listFiles()).toString());
        assertEquals(0L, lastSeenIdMatcher.recordedObject().getLastSeenDataSetId());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteFaileds()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".*");
        maintenanceTask.setUp("", properties);
        AbstractExternalData ds1 = new DataSetBuilder(1L).type("BLAST").code("DS1")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        RecordingMatcher<TrackingDataSetCriteria> lastSeenIdMatcher = prepareListNewerDataSet(ds1);
        File dataSetFolder1 = new File(workingDirectory, "data-set-example1");
        dataSetFolder1.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta.fastq"), "@1\nGATTACA\n+\nznhjnxzx\n");
        prepareContentProvider(ds1, dataSetFolder1);
        maintenanceTask.setProcessSuccesses(false);

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.fasta, .fa, .fsa, .fastq]\n"
                + INFO_PREFIX + "BLAST databases folder: " + store + "/blast-databases\n"
                + INFO_PREFIX + "Temp folder '" + store + "/blast-databases/tmp' created.\n"
                + INFO_PREFIX + "Scan 1 data sets for creating BLAST databases.\n"
                + ERROR_PREFIX + "Creation of BLAST database failed for data set 'DS1'. Temporary fasta file: "
                + store + "/blast-databases/tmp/DS1-nucl.fa", logRecorder.getLogContent());
        assertEquals("makeblastdb -version\n"
                + "makeblastdb -in " + store.getAbsolutePath() + "/blast-databases/tmp/DS1-nucl.fa"
                + " -dbtype nucl -title DS1-nucl -out " + store.getAbsolutePath() + "/blast-databases/DS1-nucl\n",
                maintenanceTask.getCommands());
        assertEquals(false, new File(store, "blast-databases/all-nucl.nal").exists());
        assertEquals(false, new File(store, "blast-databases/all-prot.nal").exists());
        assertEquals("[]", Arrays.asList(new File(store, "blast-databases/tmp").listFiles()).toString());
        assertEquals(0L, lastSeenIdMatcher.recordedObject().getLastSeenDataSetId());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteTwiceWithNonDefaultParameters()
    {
        Properties properties = new Properties();
        properties.setProperty(DATASET_TYPES_PROPERTY, ".*");
        File blastDatabasesFolder = new File(workingDirectory, "blast-dbs");
        properties.setProperty(BLAST_DATABASES_FOLDER_PROPERTY, blastDatabasesFolder.toString());
        File tempFolder = new File(workingDirectory, "temp");
        properties.setProperty(BLAST_TEMP_FOLDER_PROPERTY, tempFolder.toString());
        properties.setProperty(BLAST_TOOLS_DIRECTORY_PROPERTY, "/usr/bin/blast");
        properties.setProperty(FILE_TYPES_PROPERTY, ".txt .f");
        File lastSeenFile = new File(workingDirectory, "last.txt");
        properties.setProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY, lastSeenFile.toString());
        maintenanceTask.setUp("", properties);
        AbstractExternalData ds1 = new DataSetBuilder(11L).type("BLAST").code("DS1")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        RecordingMatcher<TrackingDataSetCriteria> lastSeenIdMatcher = prepareListNewerDataSet(ds1);
        File dataSetFolder1 = new File(workingDirectory, "data-set-example1");
        dataSetFolder1.mkdirs();
        FileUtilities.writeToFile(new File(dataSetFolder1, "fasta.txt"), ">1\nGATTACA\n");
        prepareContentProvider(ds1, dataSetFolder1);
        maintenanceTask.execute();
        prepareListsDataSets("-" + ds1.getCode());
        AbstractExternalData ds2 = new DataSetBuilder(12L).type("BLAST").code("DS2")
                .status(DataSetArchivingStatus.AVAILABLE).getDataSet();
        lastSeenIdMatcher = prepareListNewerDataSet(ds2);
        prepareContentProvider(ds2, dataSetFolder1);

        maintenanceTask.execute();

        assertEquals(INFO_PREFIX + "File types: [.txt, .f]\n"
                + INFO_PREFIX + "BLAST databases folder: " + blastDatabasesFolder + "\n"
                + INFO_PREFIX + "Temp folder '" + tempFolder + "' created.\n"
                + INFO_PREFIX + "Scan 1 data sets for creating BLAST databases.\n"
                + INFO_PREFIX + "BLAST database DS1-nucl successfully deleted.\n" 
                + INFO_PREFIX + "BLAST database DS1-prot successfully deleted.\n" 
                + INFO_PREFIX + "Scan 1 data sets for creating BLAST databases.", logRecorder.getLogContent());
        assertEquals("/usr/bin/blast/makeblastdb -version\n"
                + "/usr/bin/blast/makeblastdb -in " + tempFolder.getAbsolutePath() + "/DS1-nucl.fa"
                + " -dbtype nucl -title DS1-nucl -out " + blastDatabasesFolder.getAbsolutePath() + "/DS1-nucl\n"
                + "/usr/bin/blast/makeblastdb -in " + tempFolder.getAbsolutePath() + "/DS2-nucl.fa"
                + " -dbtype nucl -title DS2-nucl -out " + blastDatabasesFolder.getAbsolutePath() + "/DS2-nucl\n",
                maintenanceTask.getCommands());
        assertEquals("TITLE all-nucl\nDBLIST DS2-nucl",
                FileUtilities.loadToString(new File(blastDatabasesFolder, "all-nucl.nal")).trim());
        assertEquals(false, new File(store, "blast-databases/all-prot.nal").exists());
        assertEquals("[]", Arrays.asList(tempFolder.listFiles()).toString());
        assertEquals(11L, lastSeenIdMatcher.recordedObject().getLastSeenDataSetId());
        assertEquals("12", FileUtilities.loadToString(lastSeenFile).trim());
        context.assertIsSatisfied();
    }

    private void prepareContentProvider(final AbstractExternalData dataSet, final File dataSetFolder)
    {
        context.checking(new Expectations()
            {
                {
                    one(contentProvider).asContent(dataSet);
                    will(returnValue(new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(dataSetFolder, null)));
                }
            });
    }

    private void prepareListsDataSets(final String... dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
                    List<String> codes = new ArrayList<String>();
                    for (String dataSetCode : dataSetCodes)
                    {
                        if (dataSetCode.startsWith("-") == false)
                        {
                            result.add(new DataSetBuilder().code(dataSetCode).getDataSet());
                            codes.add(dataSetCode);
                        } else
                        {
                            codes.add(dataSetCode.substring(1));
                        }
                    }
                    one(service).listDataSetsByCode(codes);
                    will(returnValue(result));
                }
            });
    }

    private RecordingMatcher<TrackingDataSetCriteria> prepareListNewerDataSet(final AbstractExternalData... dataSets)
    {
        final RecordingMatcher<TrackingDataSetCriteria> recordingMatcher = new RecordingMatcher<TrackingDataSetCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(service).listNewerDataSets(with(recordingMatcher));
                    will(returnValue(new ArrayList<AbstractExternalData>(Arrays.asList(dataSets))));
                }
            });
        return recordingMatcher;
    }

}
