/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin.SHARE_FINDER_KEY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PersonBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = {AbstractArchiverProcessingPlugin.class, RsyncArchiver.class})
public class DistributingArchiverTest extends AbstractArchiverTestCase
{
    private static final String LOCATION = "a/b/c/ds1";

    private static final String SHARE_ID = "1";

    private static final String DATA_SET_CODE = "ds1";

    private static final File HDF5_ARCHIVE = new File(
            "../openbis-common/resource/test-data/HDF5ContainerBasedHierarchicalContentNodeTest/thumbnails.h5");

    private File defaultArchive;

    private File ds1InStore;

    private DefaultFileBasedHierarchicalContentFactory contentFactory;

    private File archives;

    private File helloFile;

    private File readMeFile;

    private File emptyFolder;

    @BeforeMethod
    public void prepareTestData(Method method) throws IOException
    {
        wait(1); // Without waiting sometimes the meta data from a previous test is extracted from zip file.
        ds1InStore = new File(share1, LOCATION);
        File subfolder = new File(ds1InStore, "original/my-data/subfolder");
        subfolder.mkdirs();
        FileUtils.copyFile(HDF5_ARCHIVE, new File(subfolder, "my-archive.h5"));
        helloFile = new File(subfolder, "hello.txt");
        FileUtilities.writeToFile(helloFile, "Hello world!");
        readMeFile = new File(subfolder.getParentFile(), "read-me.txt");
        emptyFolder = new File(subfolder.getParentFile(), "empty-folder");
        emptyFolder.mkdirs();
        FileUtilities.writeToFile(readMeFile, "Nothing to read!");
        defaultArchive = new File(workingDirectory, "default-archive");
        defaultArchive.mkdirs();
        archives = new File(workingDirectory, "archives");
        archives.mkdirs();
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.DEFAULT_DESTINATION_KEY, defaultArchive.getPath());
        contentFactory = new DefaultFileBasedHierarchicalContentFactory();
    }

    @Test
    public void testArchivingFlatToDefaultArchive()
    {
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E")
                .registrator(new PersonBuilder().name("Albert", "Einstein").getPerson())
                .property("E-PROP", "42").getExperiment();
        experiment.setRegistrationDate(new Date(98765));
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION).fileFormat("ABC")
                        .registrationDate(new Date(12345)).store(new DataStoreBuilder(DATA_STORE_CODE).getStore())
                        .experiment(experiment).getDataSet();
        DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(experiment);
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);

        ProcessingStatus processingStatus = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);

        File archivedDataSetFile = new File(defaultArchive, ds1.getDataSetCode() + ".zip");
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - " 
                + "Data set 'ds1' archived: " + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - " 
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                        + "' to '" + RsyncArchiver.STAGING_FOLDER + "/ds1'.", logRecorder.getLogContent());
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        assertEquals("[]", errorStatuses.toString());
        assertEquals(true, archivedDataSetFile.isFile());
        assertZipContent("data_set\tcode\tds1\n"
                + "data_set\tproduction_timestamp\t\n"
                + "data_set\tproducer_code\t\n"
                + "data_set\tdata_set_type\tMY-TYPE\n"
                + "data_set\tis_measured\tTRUE\n"
                + "data_set\tis_complete\tFALSE\n"
                + "data_set\tparent_codes\t\n"
                + "experiment\tspace_code\tS\n"
                + "experiment\tproject_code\tP\n"
                + "experiment\texperiment_code\tE1\n"
                + "experiment\texperiment_type_code\tMY-E\n"
                + "experiment\tregistration_timestamp\t1970-01-01 01:01:38 +0100\n"
                + "experiment\tregistrator\tAlbert Einstein\n"
                + "experiment\tE-PROP\t42\n", archivedDataSetFile, AbstractDataSetPackager.META_DATA_FILE_NAME, true);
        assertZipContent("Hello world!", archivedDataSetFile, "original/my-data/subfolder/hello.txt", true);
        assertZipContent("Nothing to read!", archivedDataSetFile, "original/my-data/read-me.txt", true);
        assertZipContent(HDF5_ARCHIVE, archivedDataSetFile, "original/my-data/subfolder/my-archive.h5", true);
    }

    @Test
    public void testArchivingWithShardingWithoutCompressingToDefaultArchive()
    {
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.WITH_SHARDING_KEY, "true");
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.COMPRESS_KEY, "false");
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION)
                .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC").property("ANSWER", "42")
                .experiment(experiment).getDataSet();
        DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(experiment);
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        
        ProcessingStatus processingStatus = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);
        
        File archivedDataSetFile = new File(defaultArchive, LOCATION + "/" + ds1.getDataSetCode() + ".zip");
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.", logRecorder.getLogContent());
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        assertEquals("[]", errorStatuses.toString());
        assertEquals(true, archivedDataSetFile.isFile());
        assertZipContent("data_set\tcode\tds1\n"
                + "data_set\tproduction_timestamp\t\n"
                + "data_set\tproducer_code\t\n"
                + "data_set\tdata_set_type\tMY-TYPE\n"
                + "data_set\tis_measured\tTRUE\n"
                + "data_set\tis_complete\tFALSE\n"
                + "data_set\tANSWER\t42\n"
                + "data_set\tparent_codes\t\n"
                + "experiment\tspace_code\tS\n"
                + "experiment\tproject_code\tP\n"
                + "experiment\texperiment_code\tE1\n"
                + "experiment\texperiment_type_code\tMY-E\n"
                + "experiment\tregistration_timestamp\t\n"
                + "experiment\tregistrator\t\n", archivedDataSetFile, AbstractDataSetPackager.META_DATA_FILE_NAME, false);
        assertZipContent("Hello world!", archivedDataSetFile, "original/my-data/subfolder/hello.txt", false);
        assertZipContent("Nothing to read!", archivedDataSetFile, "original/my-data/read-me.txt", false);
        assertZipContent(HDF5_ARCHIVE, archivedDataSetFile, "original/my-data/subfolder/my-archive.h5", false);
    }
    
    @Test
    public void testArchivingFlatToSpaceMappedArchive()
    {
        File mappingFile = new File(workingDirectory, "mapping.tsv");
        File archive = new File(archives, "my-archive");
        FileUtilities.writeToFile(mappingFile, "Space\tLive Share\tArchive Folder\n/S\t1\t" + archive + "\n");
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.MAPPING_FILE_KEY, mappingFile.getPath());
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.CREATE_ARCHIVES_KEY, "true");
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").getExperiment();
        Sample sample = new SampleBuilder("/S/S1").type("MY-S").property("ANSWER", "42").getSample();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION)
                .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC")
                .experiment(experiment).sample(sample).getDataSet();
        DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(experiment);
        prepareTryGetSample(sample);
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        
        ProcessingStatus processingStatus = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);
        
        File archivedDataSetFile = new File(archive, ds1.getDataSetCode() + ".zip");
        assertEquals("INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" + mappingFile + "' successfully loaded.\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.", logRecorder.getLogContent());
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        assertEquals("[]", errorStatuses.toString());
        assertEquals(true, archivedDataSetFile.isFile());
        assertZipContent("data_set\tcode\tds1\n"
                + "data_set\tproduction_timestamp\t\n"
                + "data_set\tproducer_code\t\n"
                + "data_set\tdata_set_type\tMY-TYPE\n"
                + "data_set\tis_measured\tTRUE\n"
                + "data_set\tis_complete\tFALSE\n"
                + "data_set\tparent_codes\t\n"
                + "sample\ttype_code\tMY-S\n" 
                + "sample\tcode\tS1\n" 
                + "sample\tspace_code\tS\n" 
                + "sample\tregistration_timestamp\t\n" 
                + "sample\tregistrator\t\n" 
                + "sample\tANSWER\t42\n" 
                + "experiment\tspace_code\tS\n"
                + "experiment\tproject_code\tP\n"
                + "experiment\texperiment_code\tE1\n"
                + "experiment\texperiment_type_code\tMY-E\n"
                + "experiment\tregistration_timestamp\t\n"
                + "experiment\tregistrator\t\n", archivedDataSetFile, AbstractDataSetPackager.META_DATA_FILE_NAME, true);
        assertZipContent("Hello world!", archivedDataSetFile, "original/my-data/subfolder/hello.txt", true);
        assertZipContent("Nothing to read!", archivedDataSetFile, "original/my-data/read-me.txt", true);
        assertZipContent(HDF5_ARCHIVE, archivedDataSetFile, "original/my-data/subfolder/my-archive.h5", true);
    }

    @Test
    public void testUnarchivingFromDefaultArchiveNoShardingWithCompression()
    {
        properties.setProperty(SHARE_FINDER_KEY + ".class", ShareFinder.class.getName());
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION).size(28)
                        .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC")
                        .experiment(experiment).getDataSet();
        final DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(experiment);
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.ARCHIVED, true);
        context.checking(new Expectations()
            {
                {
                    one(deleter).scheduleDeletionOfDataSets(Arrays.asList(dsd1), 11, 10);
                }
            });
        // archive
        ProcessingStatus processingStatus = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, true);
        assertEquals("[]", processingStatus.getErrorStatuses().toString());
        FileUtilities.deleteRecursively(ds1InStore);     // delete in store
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        context.checking(new Expectations()
            {
                {
                    one(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));
                }
            });
        
        processingStatus = archiver.unarchive(Arrays.asList(dsd1), archiverTaskContext);
        
        File archivedDataSetFile = new File(defaultArchive, ds1.getDataSetCode() + ".zip");
        String logContent = logRecorder.getLogContent().replaceFirst("in all shares in .*s", "in all shares in ? s");
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Unarchiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Obtained the list of all datasets in all shares in ? s.\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile + "' to '"
                + ds1InStore + "'.", logContent);
        assertEquals("[]", processingStatus.getErrorStatuses().toString());
        assertEquals(true, archivedDataSetFile.isFile());
        assertZipContent("data_set\tcode\tds1\n"
                + "data_set\tproduction_timestamp\t\n"
                + "data_set\tproducer_code\t\n"
                + "data_set\tdata_set_type\tMY-TYPE\n"
                + "data_set\tis_measured\tTRUE\n"
                + "data_set\tis_complete\tFALSE\n"
                + "data_set\tparent_codes\t\n"
                + "experiment\tspace_code\tS\n"
                + "experiment\tproject_code\tP\n"
                + "experiment\texperiment_code\tE1\n"
                + "experiment\texperiment_type_code\tMY-E\n"
                + "experiment\tregistration_timestamp\t\n"
                + "experiment\tregistrator\t\n", archivedDataSetFile, AbstractDataSetPackager.META_DATA_FILE_NAME, true);
        assertZipContent("Hello world!", archivedDataSetFile, "original/my-data/subfolder/hello.txt", true);
        assertZipContent("Nothing to read!", archivedDataSetFile, "original/my-data/read-me.txt", true);
        assertZipContent(HDF5_ARCHIVE, archivedDataSetFile, "original/my-data/subfolder/my-archive.h5", true);
        assertZipDirectoryEntry(archivedDataSetFile, "original/my-data/empty-folder/");
        assertEquals(true, ds1InStore.exists());
        assertEquals("Hello world!", FileUtilities.loadToString(helloFile).trim());
        assertEquals("Nothing to read!", FileUtilities.loadToString(readMeFile).trim());
        assertEquals(true, emptyFolder.exists());
        assertEquals(true, emptyFolder.isDirectory());
    }

    @Test
    public void testUnarchivingFromSpaceMappedArchiveWithShardingWithoutCompression()
    {
        File mappingFile = new File(workingDirectory, "mapping.tsv");
        File archive = new File(archives, "my-archive");
        FileUtilities.writeToFile(mappingFile, "Space\tLive Share\tArchive Folder\n/S\t1\t" + archive + "\n");
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.MAPPING_FILE_KEY, mappingFile.getPath());
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.CREATE_ARCHIVES_KEY, "true");
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.WITH_SHARDING_KEY, "true");
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.COMPRESS_KEY, "false");
        properties.setProperty(SHARE_FINDER_KEY + ".class", ShareFinder.class.getName());
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION).size(28)
                .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC")
                .experiment(experiment).getDataSet();
        final DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(experiment);
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.ARCHIVED, true);
        context.checking(new Expectations()
            {
                {
                    one(deleter).scheduleDeletionOfDataSets(Arrays.asList(dsd1), 11, 10);
                }
            });
        // archive
        ProcessingStatus processingStatus = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, true);
        assertEquals("[]", processingStatus.getErrorStatuses().toString());
        FileUtilities.deleteRecursively(ds1InStore);     // delete in store
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        context.checking(new Expectations()
            {
                {
                    one(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));
                }
            });
        
        processingStatus = archiver.unarchive(Arrays.asList(dsd1), archiverTaskContext);
        
        File archivedDataSetFile = new File(archive, LOCATION + "/" + ds1.getDataSetCode() + ".zip");
        String logContent = logRecorder.getLogContent().replaceFirst("in all shares in .*s", "in all shares in ? s");
        assertEquals("INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" + mappingFile + "' successfully loaded.\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Unarchiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Obtained the list of all datasets in all shares in ? s.\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile + "' to '"
                + ds1InStore + "'.", logContent);
        assertEquals("[]", processingStatus.getErrorStatuses().toString());
        assertEquals(true, archivedDataSetFile.isFile());
        assertEquals(true, ds1InStore.exists());
        assertEquals("Hello world!", FileUtilities.loadToString(helloFile).trim());
        assertEquals("Nothing to read!", FileUtilities.loadToString(readMeFile).trim());
        assertEquals(true, emptyFolder.exists());
        assertEquals(true, emptyFolder.isDirectory());
    }
    
    @Test
    public void testArchivingTwice()
    {
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").property("E-PROP", "42").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION)
                        .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC")
                        .experiment(experiment).getDataSet();
        DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(ds1.getExperiment());
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        ProcessingStatus processingStatus1 = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);
        assertEquals("[]", processingStatus1.getErrorStatuses().toString());
        ds1.setExperiment(new ExperimentBuilder().identifier("/S/P/E2").type("MY-E").getExperiment());
        dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(ds1.getExperiment());
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);

        ProcessingStatus processingStatus2 = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);

        File archivedDataSetFile = new File(defaultArchive, ds1.getDataSetCode() + ".zip");
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'." + "\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.", logRecorder.getLogContent());
        assertEquals("[]", processingStatus2.getErrorStatuses().toString());
        assertEquals(true, archivedDataSetFile.isFile());
        assertZipContent("data_set\tcode\tds1\n"
                + "data_set\tproduction_timestamp\t\n"
                + "data_set\tproducer_code\t\n"
                + "data_set\tdata_set_type\tMY-TYPE\n"
                + "data_set\tis_measured\tTRUE\n"
                + "data_set\tis_complete\tFALSE\n"
                + "data_set\tparent_codes\t\n"
                + "experiment\tspace_code\tS\n"
                + "experiment\tproject_code\tP\n"
                + "experiment\texperiment_code\tE2\n"
                + "experiment\texperiment_type_code\tMY-E\n"
                + "experiment\tregistration_timestamp\t\n"
                + "experiment\tregistrator\t\n", archivedDataSetFile, AbstractDataSetPackager.META_DATA_FILE_NAME, true);
        assertZipContent("Hello world!", archivedDataSetFile, "original/my-data/subfolder/hello.txt", true);
        assertZipContent("Nothing to read!", archivedDataSetFile, "original/my-data/read-me.txt", true);
        assertZipContent(HDF5_ARCHIVE, archivedDataSetFile, "original/my-data/subfolder/my-archive.h5", true);
    }
    
    @Test
    public void testDeleteFromArchive()
    {
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.WITH_SHARDING_KEY, "true");
        properties.setProperty(RsyncArchiver.ONLY_MARK_AS_DELETED_KEY, "false");
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").property("E-PROP", "42").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION)
                        .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC")
                        .experiment(experiment).getDataSet();
        DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(ds1.getExperiment());
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        ProcessingStatus processingStatus1 = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);
        assertEquals("[]", processingStatus1.getErrorStatuses().toString());
        File archivedDataSetFile = new File(defaultArchive, LOCATION + "/" + ds1.getDataSetCode() + ".zip");
        assertEquals(true, archivedDataSetFile.exists());
        
        ProcessingStatus processingStatus2 = archiver.deleteFromArchive(Arrays.asList(
                new DatasetLocation(ds1.getCode(), ds1.getLocation(), DATA_STORE_CODE, "")));

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.", logRecorder.getLogContent());
        assertEquals("[]", processingStatus2.getErrorStatuses().toString());
        assertEquals(false, archivedDataSetFile.exists());
    }
    
    @Test
    public void testMarkAsDeletedFromArchive()
    {
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").property("E-PROP", "42").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION)
                .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC")
                .experiment(experiment).getDataSet();
        DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(ds1.getExperiment());
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        ProcessingStatus processingStatus1 = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);
        assertEquals("[]", processingStatus1.getErrorStatuses().toString());
        File archivedDataSetFile = new File(defaultArchive, ds1.getDataSetCode() + ".zip");
        assertEquals(true, archivedDataSetFile.exists());
        
        ProcessingStatus processingStatus2 = archiver.deleteFromArchive(Arrays.asList(
                new DatasetLocation(ds1.getCode(), ds1.getLocation(), DATA_STORE_CODE, "")));

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.", logRecorder.getLogContent());
        assertEquals("[]", processingStatus2.getErrorStatuses().toString());
        assertEquals(true, archivedDataSetFile.exists());
        File markerFile = new File(defaultArchive, DataSetFileOperationsManager.FOLDER_OF_AS_DELETED_MARKED_DATA_SETS 
                + "/" + ds1.getDataSetCode());
        assertEquals(true, markerFile.exists());
    }

    @Test
    public void testMarkAsDeletedFromArchiveWithSharding()
    {
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.WITH_SHARDING_KEY, "true");
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").property("E-PROP", "42").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION)
                .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC")
                .experiment(experiment).getDataSet();
        DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(ds1.getExperiment());
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        ProcessingStatus processingStatus1 = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);
        assertEquals("[]", processingStatus1.getErrorStatuses().toString());
        File archivedDataSetFile = new File(defaultArchive, LOCATION + "/" + ds1.getDataSetCode() + ".zip");
        assertEquals(true, archivedDataSetFile.exists());
        
        ProcessingStatus processingStatus2 = archiver.deleteFromArchive(Arrays.asList(
                new DatasetLocation(ds1.getCode(), ds1.getLocation(), DATA_STORE_CODE, "")));

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.", logRecorder.getLogContent());
        assertEquals("[]", processingStatus2.getErrorStatuses().toString());
        assertEquals(true, archivedDataSetFile.exists());
        File markerFile = new File(defaultArchive, DataSetFileOperationsManager.FOLDER_OF_AS_DELETED_MARKED_DATA_SETS 
                + "/" + ds1.getDataSetCode());
        assertEquals(true, markerFile.exists());
    }
    
    @Test
    public void testMarkAsDeletedFromArchiveWithShardingAndMapping()
    {
        File mappingFile = new File(workingDirectory, "mapping.tsv");
        File archive = new File(archives, "my-archive");
        FileUtilities.writeToFile(mappingFile, "Space\tLive Share\tArchive Folder\n/S\t1\t" + archive + "\n");
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.MAPPING_FILE_KEY, mappingFile.getPath());
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.CREATE_ARCHIVES_KEY, "true");
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.WITH_SHARDING_KEY, "true");
        DistributingArchiver archiver = createArchiver();
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").type("MY-E").property("E-PROP", "42").getExperiment();
        PhysicalDataSet ds1 =
                new DataSetBuilder().code(DATA_SET_CODE).type("MY-TYPE").location(LOCATION)
                .store(new DataStoreBuilder(DATA_STORE_CODE).getStore()).fileFormat("ABC")
                .experiment(experiment).getDataSet();
        DatasetDescription dsd1 = DataSetTranslator.translateToDescription(ds1);
        prepareGetShareId();
        prepareUpdateShareIdAndSize(537669);
        prepareGetDataSetDirectory(dsd1);
        prepareTryGetDataSet(ds1);
        prepareTryGetExperiment(ds1.getExperiment());
        prepareLockAndReleaseDataSet(ds1.getCode());
        prepareGetDataSetDirectory("");
        prepareGetDataSetDirectory(LOCATION);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareAsContent(ds1.getCode(), ds1InStore);
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, true);
        ProcessingStatus processingStatus1 = archiver.archive(Arrays.asList(dsd1), archiverTaskContext, false);
        assertEquals("[]", processingStatus1.getErrorStatuses().toString());
        File archivedDataSetFile = new File(archive, LOCATION + "/" + ds1.getDataSetCode() + ".zip");
        assertEquals(true, archivedDataSetFile.exists());
        
        ProcessingStatus processingStatus2 = archiver.deleteFromArchive(Arrays.asList(
                new DatasetLocation(ds1.getCode(), ds1.getLocation(), DATA_STORE_CODE, "")));
        
        assertEquals("INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" + mappingFile + "' successfully loaded.\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - Data set 'ds1' archived: "
                + archivedDataSetFile + "\n"
                + "INFO  OPERATION.DistributedPackagingDataSetFileOperationsManager - "
                + "Data set 'ds1' unzipped from archive '" + archivedDataSetFile
                + "' to '" + new File(store, RsyncArchiver.STAGING_FOLDER) + "/ds1'.", logRecorder.getLogContent());
        assertEquals("[]", processingStatus2.getErrorStatuses().toString());
        assertEquals(true, archivedDataSetFile.exists());
        File markerFile = new File(archive, DataSetFileOperationsManager.FOLDER_OF_AS_DELETED_MARKED_DATA_SETS 
                + "/" + ds1.getDataSetCode());
        assertEquals(true, markerFile.exists());
    }
    
    private void assertZipContent(File expectedContent, File file, String path, boolean compressed)
    {
        try
        {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry(path);
            assertNotNull("No entry for " + path, entry);
            if (entry.isDirectory())
            {
                fail("Directory path: " + path);
            }
            assertEquals(compressed ? ZipEntry.DEFLATED : ZipEntry.STORED, entry.getMethod());
            assertEquals(IOUtils.toByteArray(new FileInputStream(expectedContent)), 
                    IOUtils.toByteArray(zipFile.getInputStream(entry)));
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
    private void assertZipContent(String expectedContent, File file, String path, boolean compressed)
    {
        try
        {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry(path);
            assertNotNull("No entry for " + path, entry);
            if (entry.isDirectory())
            {
                fail("Directory path: " + path);
            }
            assertEquals(compressed ? ZipEntry.DEFLATED : ZipEntry.STORED, entry.getMethod());
            assertEquals(expectedContent, IOUtils.toString(zipFile.getInputStream(entry)));
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
    private void assertZipDirectoryEntry(File file, String path)
    {
        try
        {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry(path);
            assertNotNull("No entry for " + path, entry);
            assertTrue("Not a directory entry: " + path, entry.isDirectory());
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
    private void prepareAsContent(final String dataSetCode, final File file)
    {
        context.checking(new Expectations()
            {
                {
                    one(contentProvider).asContent(dataSetCode);
                    will(returnValue(contentFactory.asHierarchicalContent(file, null)));
                }
            });
    }

    private void prepareTryGetExperiment(final Experiment experiment)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetExperiment(ExperimentIdentifierFactory.parse(experiment.getIdentifier()));
                    will(returnValue(experiment));
                }
            });
    }
    
    private void prepareTryGetSample(final Sample sample)
    {
        context.checking(new Expectations()
        {
            {
                one(service).tryGetSampleWithExperiment(SampleIdentifierFactory.parse(sample.getIdentifier()));
                will(returnValue(sample));
            }
        });
    }

    private void prepareTryGetDataSet(final AbstractExternalData dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(dataSet.getCode());
                    will(returnValue(dataSet));
                }
            });
    }

    private void prepareGetDataSetDirectory(final DatasetDescription dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(dataSetDirectoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(ds1InStore));
                }
            });
    }

    private void prepareGetDataSetDirectory(final String location)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetDirectoryProvider).getDataSetDirectory(SHARE_ID, location);
                    will(returnValue(ds1InStore));
                }
            });
    }

    private void prepareUpdateStatus(final DataSetArchivingStatus status, final boolean presentInArchive)
    {
        context.checking(new Expectations()
            {
                {
                    one(statusUpdater).update(Arrays.asList(DATA_SET_CODE), status, presentInArchive);
                }
            });
    }

    private void prepareUpdateShareIdAndSize(final long size)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).updateShareIdAndSize(DATA_SET_CODE, SHARE_ID, size);
                }
            });
    }

    private void prepareLockAndReleaseDataSet(final String dataSetCode)
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(dataSetCode);
                    one(shareIdManager).releaseLock(dataSetCode);
                }
            });
    }

    private void prepareGetShareId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(shareIdManager).getShareId(DATA_SET_CODE);
                    will(returnValue(SHARE_ID));
                }
            });
    }

    private DistributingArchiver createArchiver()
    {
        DistributingArchiver archiver = new DistributingArchiver(properties, store);
        archiver.statusUpdater = statusUpdater;
        return archiver;
    }
    
    private void wait(int seconds)
    {
        try
        {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ex)
        {
            // ignored
        }
    }

}
