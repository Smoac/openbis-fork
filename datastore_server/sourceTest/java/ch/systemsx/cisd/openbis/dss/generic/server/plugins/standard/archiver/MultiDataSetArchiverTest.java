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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetArchiver.MAXIMUM_CONTAINER_SIZE_IN_BYTES;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetArchiver.MINIMUM_CONTAINER_SIZE_IN_BYTES;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetFileOperationsManager.STAGING_DESTINATION_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.ShareFactory.SHARE_PROPS_FILE;
import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.ShareFactory.UNARCHIVING_SCRATCH_SHARE_PROP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.springframework.beans.factory.BeanFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.TarBasedHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.MockDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SimpleFileContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = MultiDataSetArchiver.class)
public class MultiDataSetArchiverTest extends AbstractFileSystemTestCase
{
    private static final String DSS_CODE = "DSS";

    private static final class RecordingStatusUpdater implements IDataSetStatusUpdater
    {
        private StringBuilder builder = new StringBuilder();

        @Override
        public void update(List<String> dataSetCodes, DataSetArchivingStatus status, boolean presentInArchive)
        {
            builder.append(dataSetCodes).append(": ").append(status).append(" ").append(presentInArchive);
            builder.append("\n");
        }

        @Override
        public String toString()
        {
            return builder.toString();
        }
    }

    private static final class MockMultiDataSetArchiverDBTransaction
            implements IMultiDataSetArchiverDBTransaction, IMultiDataSetArchiverReadonlyQueryDAO
    {
        private int id;

        private List<MultiDataSetArchiverContainerDTO> containers = new ArrayList<MultiDataSetArchiverContainerDTO>();

        private List<MultiDataSetArchiverDataSetDTO> dataSets = new ArrayList<MultiDataSetArchiverDataSetDTO>();

        private List<MultiDataSetArchiverContainerDTO> uncommittedContainers = new ArrayList<MultiDataSetArchiverContainerDTO>();

        private List<MultiDataSetArchiverDataSetDTO> uncommittedDataSets = new ArrayList<MultiDataSetArchiverDataSetDTO>();

        private boolean committed;

        private boolean rolledBack;

        @Override
        public List<MultiDataSetArchiverDataSetDTO> getDataSetsForContainer(MultiDataSetArchiverContainerDTO container)
        {
            List<MultiDataSetArchiverDataSetDTO> result = new ArrayList<MultiDataSetArchiverDataSetDTO>();
            for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
            {
                if (dataSet.getContainerId() == container.getId())
                {
                    result.add(dataSet);
                }
            }
            return result;
        }

        @Override
        public MultiDataSetArchiverContainerDTO createContainer(String path)
        {
            MultiDataSetArchiverContainerDTO container = new MultiDataSetArchiverContainerDTO(id++, path);
            uncommittedContainers.add(container);
            return container;
        }

        @Override
        public MultiDataSetArchiverDataSetDTO insertDataset(DatasetDescription dataSet, MultiDataSetArchiverContainerDTO container)
        {
            String dataSetCode = dataSet.getDataSetCode();
            Long dataSetSize = dataSet.getDataSetSize();
            MultiDataSetArchiverDataSetDTO dataSetDTO = new MultiDataSetArchiverDataSetDTO(id++, dataSetCode, container.getId(), dataSetSize);
            uncommittedDataSets.add(dataSetDTO);
            return dataSetDTO;
        }

        @Override
        public MultiDataSetArchiverDataSetDTO getDataSetForCode(String code)
        {
            for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
            {
                if (dataSet.getCode().equals(code))
                {
                    return dataSet;
                }
            }
            return null;
        }

        @Override
        public MultiDataSetArchiverContainerDTO getContainerForId(long containerId)
        {
            for (MultiDataSetArchiverContainerDTO container : containers)
            {
                if (container.getId() == containerId)
                {
                    return container;
                }
            }
            return null;
        }

        @Override
        public MultiDataSetArchiverDataSetDTO getDataSetForId(long dataSetId)
        {
            for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
            {
                if (dataSet.getId() == dataSetId)
                {
                    return dataSet;
                }
            }
            return null;
        }

        @Override
        public List<MultiDataSetArchiverDataSetDTO> listDataSetsForContainerId(long containerId)
        {
            List<MultiDataSetArchiverDataSetDTO> result = new ArrayList<MultiDataSetArchiverDataSetDTO>();
            for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
            {
                if (dataSet.getContainerId() == containerId)
                {
                    result.add(dataSet);
                }
            }
            return result;
        }

        @Override
        public void commit()
        {
            containers.addAll(uncommittedContainers);
            dataSets.addAll(uncommittedDataSets);
            committed = true;
            clearUncommitted();
        }

        @Override
        public void rollback()
        {
            rolledBack = true;
            clearUncommitted();
        }

        private void clearUncommitted()
        {
            uncommittedContainers.clear();
            uncommittedDataSets.clear();
        }

        @Override
        public void close()
        {
        }

        @Override
        public boolean isClosed()
        {
            return false;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("Containers:");
            for (MultiDataSetArchiverContainerDTO container : containers)
            {
                builder.append('\n').append(container);
            }
            builder.append("\nData sets:");
            for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
            {
                builder.append('\n').append(dataSet);
            }
            if (uncommittedContainers.isEmpty() == false)
            {
                builder.append("\nUncommitted containers:");
                for (MultiDataSetArchiverContainerDTO container : uncommittedContainers)
                {
                    builder.append('\n').append(container);
                }
            }
            if (uncommittedDataSets.isEmpty() == false)
            {
                builder.append("\nUncommitted data sets:");
                for (MultiDataSetArchiverDataSetDTO dataSet : uncommittedDataSets)
                {
                    builder.append('\n').append(dataSet);
                }
            }
            builder.append("\ncommitted: ").append(committed);
            builder.append(", rolledBack: ").append(rolledBack);
            return builder.toString();
        }

    }

    private static final class MockMultiDataSetArchiver extends MultiDataSetArchiver
    {
        private static final long serialVersionUID = 1L;

        private IMultiDataSetArchiverDBTransaction transaction;

        private IMultiDataSetFileOperationsManager fileManager;

        private IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO;

        private IFreeSpaceProvider freeSpaceProvider;

        public MockMultiDataSetArchiver(Properties properties, File storeRoot,
                IEncapsulatedOpenBISService openBISService, IShareIdManager shareIdManager,
                IDataSetStatusUpdater statusUpdater, IMultiDataSetArchiverDBTransaction transaction,
                IMultiDataSetFileOperationsManager fileManager, IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO,
                IFreeSpaceProvider freeSpaceProvider)
        {
            super(properties, storeRoot);
            this.transaction = transaction;
            this.fileManager = fileManager;
            this.readonlyDAO = readonlyDAO;
            this.freeSpaceProvider = freeSpaceProvider;
            setService(openBISService);
            setShareIdManager(shareIdManager);
            setStatusUpdater(statusUpdater);
        }

        @Override
        IMultiDataSetFileOperationsManager getFileOperations()
        {
            return fileManager == null ? super.getFileOperations() : fileManager;
        }

        @Override
        IMultiDataSetArchiverDBTransaction getTransaction()
        {
            return transaction;
        }

        @Override
        IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
        {
            return readonlyDAO;
        }

        @Override
        protected IFreeSpaceProvider createFreeSpaceProvider()
        {
            return freeSpaceProvider;
        }
    }

    private static final class MockDataSetDeleter implements IDataSetDeleter
    {
        private StringBuilder recorder = new StringBuilder();

        private File share;

        MockDataSetDeleter(File share)
        {
            this.share = share;
        }

        @Override
        public void scheduleDeletionOfDataSets(List<? extends IDatasetLocation> dataSets,
                int maxNumberOfRetries, long waitingTimeBetweenRetries)
        {
            recorder.append(dataSets).append('\n');
            for (IDatasetLocation datasetLocation : dataSets)
            {
                FileUtilities.deleteRecursively(new File(share, datasetLocation.getDataSetLocation()));
            }
        }

        @Override
        public String toString()
        {
            return recorder.toString();
        }

    }

    private static final String EXPERIMENT_IDENTIFIER = "/S/P/E";

    private BufferedAppender logRecorder;

    private Mockery context;

    private MockMultiDataSetArchiverDBTransaction transaction;

    private IDataSetDirectoryProvider directoryProvider;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private ArchiverTaskContext archiverContext;

    private IShareIdManager shareIdManager;

    private DatasetDescription ds1;

    private DatasetDescription ds2;

    private File store;

    private File share;

    private IEncapsulatedOpenBISService openBISService;

    private IDataSetStatusUpdater statusUpdater;

    private Properties properties;

    private File staging;

    private File archive;

    private IDataStoreServiceInternal dssService;

    private Experiment experiment;

    private IMultiDataSetFileOperationsManager fileOperations;

    private IDataSetDeleter dataSetDeleter;

    private IConfigProvider configProvider;

    private IFreeSpaceProvider freeSpaceProvider;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        logRecorder.addRegexForLoggingEventsToBeDropped("OPERATION.*FullTextIndex.*");
        transaction = new MockMultiDataSetArchiverDBTransaction();
        store = new File(workingDirectory, "store");
        share = new File(store, "1");
        share.mkdirs();
        FileUtilities.writeToFile(new File(share, SHARE_PROPS_FILE), UNARCHIVING_SCRATCH_SHARE_PROP + " = true");
        context = new Mockery();
        BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        shareIdManager = ServiceProviderTestWrapper.mock(context, IShareIdManager.class);
        openBISService = ServiceProviderTestWrapper.mock(context, IEncapsulatedOpenBISService.class);
        dssService = ServiceProviderTestWrapper.mock(context, IDataStoreServiceInternal.class);
        configProvider = ServiceProviderTestWrapper.mock(context, IConfigProvider.class);
        hierarchicalContentProvider = new SimpleFileContentProvider(share);
        ServiceProviderTestWrapper.addMock(context, IHierarchicalContentProvider.class,
                hierarchicalContentProvider);
        fileOperations = context.mock(IMultiDataSetFileOperationsManager.class);
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        dataSetDeleter = new MockDataSetDeleter(share);
        statusUpdater = new RecordingStatusUpdater();
        staging = new File(workingDirectory, "staging");
        staging.mkdirs();
        archive = new File(workingDirectory, "archive");
        archive.mkdirs();
        ds1 = dataSet("ds1", "0123456789");
        ds2 = dataSet("ds2", "01234567890123456789");
        properties = new Properties();
        properties.setProperty(STAGING_DESTINATION_KEY, staging.getAbsolutePath());
        properties.setProperty(FINAL_DESTINATION_KEY, archive.getAbsolutePath());
        directoryProvider = new MockDataSetDirectoryProvider(store, share.getName(), shareIdManager);
        archiverContext = new ArchiverTaskContext(directoryProvider, hierarchicalContentProvider);
        experiment = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER).type("MET").getExperiment();
        context.checking(new Expectations()
            {
                {
                    allowing(dssService).getDataSetDirectoryProvider();
                    will(returnValue(directoryProvider));

                    allowing(dssService).getDataSetDeleter();
                    will(returnValue(dataSetDeleter));

                    allowing(openBISService).tryGetExperiment(ExperimentIdentifierFactory.parse(EXPERIMENT_IDENTIFIER));
                    will(returnValue(experiment));

                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DSS_CODE));
                }
            });
    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        if (result.getStatus() == ITestResult.FAILURE)
        {
            String logContent = getLogContent();
            fail(result.getName() + " failed. Log content:\n" + logContent);
        }
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();

        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testArchiveDataSetsWhichAreTooSmall()
    {
        prepareUpdateShareIdAndSize(ds1, 10);
        prepareUpdateShareIdAndSize(ds2, 20);
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "35");

        MultiDataSetArchiver archiver = createArchiver(null);
        ProcessingStatus status = archiver.archive(Arrays.asList(ds1, ds2), archiverContext, false);

        assertEquals("[ERROR: \"Set of data sets specified for archiving is too small (30 bytes) "
                + "to be archived with multi dataset archiver because minimum size is 35 bytes.\"]",
                status.getErrorStatuses().toString());
        assertEquals("[ds1, ds2]: AVAILABLE false\n", statusUpdater.toString());
        assertEquals("Containers:\nData sets:\ncommitted: false, rolledBack: true", transaction.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveDataSetsWhichAreTooBig()
    {
        prepareUpdateShareIdAndSize(ds1, 10);
        prepareUpdateShareIdAndSize(ds2, 20);
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "25");
        properties.setProperty(MAXIMUM_CONTAINER_SIZE_IN_BYTES, "27");

        MultiDataSetArchiver archiver = createArchiver(null);
        ProcessingStatus status = archiver.archive(Arrays.asList(ds1, ds2), archiverContext, false);

        assertEquals("[ERROR: \"Set of data sets specified for archiving is too big (30 bytes) "
                + "to be archived with multi dataset archiver because maximum size is 27 bytes.\"]",
                status.getErrorStatuses().toString());
        assertEquals("[ds1, ds2]: AVAILABLE false\n", statusUpdater.toString());
        assertEquals("Containers:\nData sets:\ncommitted: false, rolledBack: true", transaction.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveOneDataSet()
    {
        prepareUpdateShareIdAndSize(ds2, 20);
        prepareLockAndReleaseDataSet(ds2);
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "15");

        MultiDataSetArchiver archiver = createArchiver(null);
        ProcessingStatus status = archiver.archive(Arrays.asList(ds2), archiverContext, false);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds2']\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Archive dataset ds2 in ds2-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Data sets archived: ds2-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copy archive container from '"
                + staging.getAbsolutePath() + "/ds2-yyyyMMdd-HHmmss.tar' to '" + archive.getAbsolutePath() + "\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copying archive container took 0:??:??.???\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Start sanity check on [Dataset 'ds2']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Sanity check finished.",
                getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals("[]", Arrays.asList(staging.list()).toString());
        assertContent(":\n"
                + "  ds2:\n"
                + "    data:\n"
                + "      >01234567890123456789\n"
                + "    meta-data.tsv:\n"
                + "      >data_set\tcode\tds2\n"
                + "      >data_set\tproduction_timestamp\t\n"
                + "      >data_set\tproducer_code\t\n"
                + "      >data_set\tdata_set_type\tMDT\n"
                + "      >data_set\tis_measured\tTRUE\n"
                + "      >data_set\tis_complete\tFALSE\n"
                + "      >data_set\tparent_codes\t\n"
                + "      >experiment\tspace_code\tS\n"
                + "      >experiment\tproject_code\tP\n"
                + "      >experiment\texperiment_code\tE\n"
                + "      >experiment\texperiment_type_code\tMET\n"
                + "      >experiment\tregistration_timestamp\t\n"
                + "      >experiment\tregistrator\t\n", getArchiveFile(ds2));
        assertEquals("[ds2]: AVAILABLE true\n", statusUpdater.toString());
        assertEquals("Containers:\nMultiDataSetArchiverContainerDTO [id=0, path=ds2-yyyyMMdd-HHmmss.tar]\n"
                + "Data sets:\nMultiDataSetArchiverDataSetDTO [id=1, code=ds2, containerId=0, sizeInBytes=20]\n"
                + "committed: true, rolledBack: false", removeTimeInformationFromContent(transaction.toString()));
        context.assertIsSatisfied();
    }

    private File getArchiveFile(final DatasetDescription dataSet)
    {
        File[] files = archive.listFiles(new FileFilter()
            {

                @Override
                public boolean accept(File pathname)
                {
                    String name = pathname.getName();
                    return name.startsWith(dataSet.getDataSetCode()) && name.endsWith(".tar");
                }
            });
        assertNotNull(files);
        assertEquals(1, files.length);
        return files[0];
    }

    @Test
    public void testArchiveTwoDataSets()
    {
        prepareUpdateShareIdAndSize(ds1, 10);
        prepareLockAndReleaseDataSet(ds1);
        prepareUpdateShareIdAndSize(ds2, 20);
        prepareLockAndReleaseDataSet(ds2);
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "15");

        MultiDataSetArchiver archiver = createArchiver(null);
        ProcessingStatus status = archiver.archive(Arrays.asList(ds1, ds2), archiverContext, false);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Archive dataset ds1 in ds1-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Archive dataset ds2 in ds1-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Data sets archived: ds1-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copy archive container from '"
                + staging.getAbsolutePath() + "/ds1-yyyyMMdd-HHmmss.tar' to '" + archive.getAbsolutePath() + "\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copying archive container took 0:??:??.???\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Start sanity check on [Dataset 'ds1', Dataset 'ds2']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Sanity check finished.",
                getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals("[]", Arrays.asList(staging.list()).toString());
        assertEquals("[ds1-yyyyMMdd-HHmmss.tar]", removeTimeInformationFromContent(Arrays.asList(archive.list()).toString()));
        assertContent(":\n"
                + "  ds1:\n"
                + "    data:\n"
                + "      >0123456789\n"
                + "    meta-data.tsv:\n"
                + "      >data_set\tcode\tds1\n"
                + "      >data_set\tproduction_timestamp\t\n"
                + "      >data_set\tproducer_code\t\n"
                + "      >data_set\tdata_set_type\tMDT\n"
                + "      >data_set\tis_measured\tTRUE\n"
                + "      >data_set\tis_complete\tFALSE\n"
                + "      >data_set\tparent_codes\t\n"
                + "      >experiment\tspace_code\tS\n"
                + "      >experiment\tproject_code\tP\n"
                + "      >experiment\texperiment_code\tE\n"
                + "      >experiment\texperiment_type_code\tMET\n"
                + "      >experiment\tregistration_timestamp\t\n"
                + "      >experiment\tregistrator\t\n"
                + "  ds2:\n"
                + "    data:\n"
                + "      >01234567890123456789\n"
                + "    meta-data.tsv:\n"
                + "      >data_set\tcode\tds2\n"
                + "      >data_set\tproduction_timestamp\t\n"
                + "      >data_set\tproducer_code\t\n"
                + "      >data_set\tdata_set_type\tMDT\n"
                + "      >data_set\tis_measured\tTRUE\n"
                + "      >data_set\tis_complete\tFALSE\n"
                + "      >data_set\tparent_codes\t\n"
                + "      >experiment\tspace_code\tS\n"
                + "      >experiment\tproject_code\tP\n"
                + "      >experiment\texperiment_code\tE\n"
                + "      >experiment\texperiment_type_code\tMET\n"
                + "      >experiment\tregistration_timestamp\t\n"
                + "      >experiment\tregistrator\t\n", getArchiveFile(ds1));
        assertEquals("[ds1, ds2]: AVAILABLE true\n", statusUpdater.toString());
        assertEquals("Containers:\nMultiDataSetArchiverContainerDTO [id=0, path=ds1-yyyyMMdd-HHmmss.tar]\n"
                + "Data sets:\nMultiDataSetArchiverDataSetDTO [id=1, code=ds1, containerId=0, sizeInBytes=10]\n"
                + "MultiDataSetArchiverDataSetDTO [id=2, code=ds2, containerId=0, sizeInBytes=20]\n"
                + "committed: true, rolledBack: false", removeTimeInformationFromContent(transaction.toString()));
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveAlreadyArchivedDataSet()
    {
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "15");
        MultiDataSetArchiverContainerDTO container = transaction.createContainer("path");
        ds2.setDataSetSize(20L);
        transaction.insertDataset(ds2, container);
        transaction.commit();

        MultiDataSetArchiver archiver = createArchiver(null);
        ProcessingStatus status = archiver.archive(Arrays.asList(ds2), archiverContext, false);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds2']",
                getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals("[]", Arrays.asList(staging.list()).toString());
        assertEquals("[ds2]: AVAILABLE true\n", statusUpdater.toString());
        assertEquals("Containers:\nMultiDataSetArchiverContainerDTO [id=0, path=path]\n"
                + "Data sets:\nMultiDataSetArchiverDataSetDTO [id=1, code=ds2, containerId=0, sizeInBytes=20]\n"
                + "committed: true, rolledBack: false", transaction.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveTwoDataSetsOneAlreadyArchived()
    {
        prepareUpdateShareIdAndSize(ds1, 10);
        prepareLockAndReleaseDataSet(ds1);
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "5");
        MultiDataSetArchiverContainerDTO container = transaction.createContainer("path");
        ds2.setDataSetSize(20L);
        transaction.insertDataset(ds2, container);
        transaction.commit();
        assertEquals(true, new File(share, ds1.getDataSetCode()).exists());
        assertEquals(true, new File(share, ds2.getDataSetCode()).exists());

        MultiDataSetArchiver archiver = createArchiver(null);
        ProcessingStatus status = archiver.archive(Arrays.asList(ds1, ds2), archiverContext, true);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Archive dataset ds1 in ds1-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Data sets archived: ds1-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copy archive container from '"
                + staging.getAbsolutePath() + "/ds1-yyyyMMdd-HHmmss.tar' to '" + archive.getAbsolutePath() + "\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copying archive container took 0:??:??.???\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Start sanity check on [Dataset 'ds1']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Sanity check finished.",
                getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals("[]", Arrays.asList(staging.list()).toString());
        assertEquals(false, new File(share, ds1.getDataSetCode()).exists());
        assertEquals(false, new File(share, ds2.getDataSetCode()).exists());
        assertContent(":\n"
                + "  ds1:\n"
                + "    data:\n"
                + "      >0123456789\n"
                + "    meta-data.tsv:\n"
                + "      >data_set\tcode\tds1\n"
                + "      >data_set\tproduction_timestamp\t\n"
                + "      >data_set\tproducer_code\t\n"
                + "      >data_set\tdata_set_type\tMDT\n"
                + "      >data_set\tis_measured\tTRUE\n"
                + "      >data_set\tis_complete\tFALSE\n"
                + "      >data_set\tparent_codes\t\n"
                + "      >experiment\tspace_code\tS\n"
                + "      >experiment\tproject_code\tP\n"
                + "      >experiment\texperiment_code\tE\n"
                + "      >experiment\texperiment_type_code\tMET\n"
                + "      >experiment\tregistration_timestamp\t\n"
                + "      >experiment\tregistrator\t\n", getArchiveFile(ds1));
        assertEquals("[ds1, ds2]: ARCHIVED true\n", statusUpdater.toString());
        assertEquals("Containers:\nMultiDataSetArchiverContainerDTO [id=0, path=path]\n"
                + "MultiDataSetArchiverContainerDTO [id=2, path=ds1-yyyyMMdd-HHmmss.tar]\n"
                + "Data sets:\nMultiDataSetArchiverDataSetDTO [id=1, code=ds2, containerId=0, sizeInBytes=20]\n"
                + "MultiDataSetArchiverDataSetDTO [id=3, code=ds1, containerId=2, sizeInBytes=10]\n"
                + "committed: true, rolledBack: false", removeTimeInformationFromContent(transaction.toString()));
        assertEquals("[Dataset 'ds1', Dataset 'ds2']\n", dataSetDeleter.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testArchiveDataSetFails()
    {
        prepareUpdateShareIdAndSize(ds2, 20);
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "15");
        final String containerPath = "data-set-path";
        prepareFileOperationsGenerateContainerPath(containerPath, ds2);
        prepareIsReplicatedArchiveDefined(false);
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).createContainerInStage(containerPath, Arrays.asList(ds2));
                    will(returnValue(Status.createError("Failed")));
                }
            });
        prepareFileOperationsDelete(containerPath);

        MultiDataSetArchiver archiver = createArchiver(fileOperations);
        ProcessingStatus status = archiver.archive(Arrays.asList(ds2), archiverContext, false);

        assertEquals("[ERROR: \"Couldn't create package file in stage archive data-set-path\"]", status.getErrorStatuses().toString());
        assertEquals("[]", Arrays.asList(staging.listFiles()).toString());
        File archiveFile = new File(archive, ds2.getDataSetCode() + ".tar");
        assertEquals(false, archiveFile.exists());
        assertEquals("[ds2]: AVAILABLE false\n", statusUpdater.toString());
        assertEquals("Containers:\nData sets:\ncommitted: false, rolledBack: true", transaction.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testUnarchive()
    {
        prepareUpdateShareIdAndSize(ds1, 10);
        prepareLockAndReleaseDataSet(ds1);
        prepareUpdateShareIdAndSize(ds2, 20);
        prepareLockAndReleaseDataSet(ds2);
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "15");
        MultiDataSetArchiver archiver = createArchiver(null);
        ProcessingStatus status = archiver.archive(Arrays.asList(ds1, ds2), archiverContext, true);
        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Archive dataset ds1 in ds1-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Archive dataset ds2 in ds1-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Data sets archived: ds1-yyyyMMdd-HHmmss.tar\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copy archive container from '"
                + staging.getAbsolutePath() + "/ds1-yyyyMMdd-HHmmss.tar' to '" + archive.getAbsolutePath() + "\n"
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copying archive container took 0:??:??.???\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Start sanity check on [Dataset 'ds1', Dataset 'ds2']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Sanity check finished.",
                getLogContent());
        logRecorder.resetLogContent();
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals(false, new File(share, ds1.getDataSetCode()).exists());
        assertEquals(false, new File(share, ds2.getDataSetCode()).exists());
        assertEquals("[ds1, ds2]: ARCHIVED true\n", statusUpdater.toString());
        ds1.setDataSetSize(10 * FileUtils.ONE_GB);
        ds2.setDataSetSize(20 * FileUtils.ONE_GB);
        prepareFreeSpace(35 * FileUtils.ONE_GB);
        prepareListDataSetsByCode(DataSetArchivingStatus.ARCHIVED, ds1, ds2);
        prepareListPhysicalDataSets();

        prepareNotifyDataSetAccess(ds1.getDataSetCode(), ds2.getDataSetCode());

        status = archiver.unarchive(Arrays.asList(ds1, ds2), archiverContext);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - Unarchiving of the following datasets "
                + "has been requested: [Dataset 'ds1', Dataset 'ds2']\n"
                + "INFO  OPERATION.AbstractDatastorePlugin - Free space on unarchiving scratch share '1': "
                + "35.00 GB, requested space for unarchiving 2 data sets: 30.00 GB\n", getFilteredLogContent());
        assertEquals("[ds1, ds2]: ARCHIVED true\n[ds1, ds2]: AVAILABLE true\n", statusUpdater.toString());
        assertContent("ds1:\n  data:\n    >0123456789\n", new File(share, ds1.getDataSetCode()));
        assertContent("ds2:\n  data:\n    >01234567890123456789\n", new File(share, ds2.getDataSetCode()));
        context.assertIsSatisfied();
    }

    @Test
    public void testUnarchiveWithDataSetsFromDifferentContainers()
    {
        MultiDataSetArchiverContainerDTO c1 = transaction.createContainer("c1");
        MultiDataSetArchiverContainerDTO c2 = transaction.createContainer("c2");
        ds1.setDataSetSize(10L);
        ds2.setDataSetSize(20L);
        transaction.insertDataset(ds1, c1);
        transaction.insertDataset(ds2, c2);
        transaction.commit();
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "15");
        MultiDataSetArchiver archiver = createArchiver(null);

        ProcessingStatus status = archiver.unarchive(Arrays.asList(ds1, ds2), archiverContext);

        assertEquals("[ERROR: \"Unarchiving failed: Datasets selected for unarchiving do not all "
                + "belong to one container, but to 2 different containers: {0=[ds1], 1=[ds2]}\"]",
                status.getErrorStatuses().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSetCodesForUnarchiving()
    {
        MultiDataSetArchiverContainerDTO c1 = transaction.createContainer("c1");
        ds1.setDataSetSize(10L);
        ds2.setDataSetSize(20L);
        transaction.insertDataset(ds1, c1);
        transaction.insertDataset(ds2, c1);
        transaction.commit();
        MultiDataSetArchiver archiver = createArchiver(null);

        List<String> codes = archiver.getDataSetCodesForUnarchiving(Arrays.asList(ds2.getDataSetCode()));

        assertEquals("[ds1, ds2]", codes.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSetCodesForUnarchivingFromDifferentContainers()
    {
        MultiDataSetArchiverContainerDTO c1 = transaction.createContainer("c1");
        MultiDataSetArchiverContainerDTO c2 = transaction.createContainer("c2");
        ds1.setDataSetSize(10L);
        ds2.setDataSetSize(20L);
        transaction.insertDataset(ds1, c1);
        transaction.insertDataset(ds2, c2);
        transaction.commit();
        MultiDataSetArchiver archiver = createArchiver(null);

        try
        {
            archiver.getDataSetCodesForUnarchiving(Arrays.asList(ds1.getDataSetCode(), ds2.getDataSetCode()));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Datasets selected for unarchiving do not all belong to one container, "
                    + "but to 2 different containers: {0=[ds1], 1=[ds2]}", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    private RecordingMatcher<HostAwareFile> prepareFreeSpace(final long... freeSpaceValues)
    {
        final RecordingMatcher<HostAwareFile> recorder = new RecordingMatcher<HostAwareFile>();
        final Sequence sequence = context.sequence("free space");
        context.checking(new Expectations()
            {
                {
                    try
                    {
                        for (long freeSpace : freeSpaceValues)
                        {
                            one(freeSpaceProvider).freeSpaceKb(with(recorder));
                            will(returnValue((freeSpace + SegmentedStoreUtils.MINIMUM_FREE_SCRATCH_SPACE) / 1024));
                            inSequence(sequence);
                        }
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
        return recorder;
    }

    private void prepareListPhysicalDataSets(final DatasetDescription... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    List<SimpleDataSetInformationDTO> dataSetInfos = new ArrayList<SimpleDataSetInformationDTO>();
                    for (DatasetDescription dataSet : dataSets)
                    {
                        SimpleDataSetInformationDTO dataSetInfo = new SimpleDataSetInformationDTO();
                        dataSetInfo.setDataStoreCode(DSS_CODE);
                        dataSetInfo.setDataSetCode(dataSet.getDataSetCode());
                        dataSetInfo.setDataSetShareId(share.getName());
                        dataSetInfo.setStatus(DataSetArchivingStatus.AVAILABLE);
                        dataSetInfo.setDataSetSize(dataSet.getDataSetSize());
                        dataSetInfos.add(dataSetInfo);
                    }
                    one(openBISService).listPhysicalDataSets();
                    will(returnValue(dataSetInfos));
                }
            });
    }

    private void prepareListDataSetsByCode(final DataSetArchivingStatus archivingStatus, final DatasetDescription... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    List<String> codes = new ArrayList<String>();
                    List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
                    for (DatasetDescription dataSet : dataSets)
                    {
                        codes.add(dataSet.getDataSetCode());
                        result.add(new DataSetBuilder().code(dataSet.getDataSetCode())
                                .status(archivingStatus).getDataSet());
                    }
                    one(openBISService).listDataSetsByCode(codes);
                    will(returnValue(result));
                }
            });
    }

    private void prepareNotifyDataSetAccess(final String... dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    for (String dataSetCode : dataSetCodes)
                    {
                        one(openBISService).notifyDatasetAccess(dataSetCode);
                    }
                }
            });
    }

    private String getFilteredLogContent()
    {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new StringReader(getLogContent()));
        try
        {
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                if (line.contains("Obtained the list of all") == false)
                {
                    builder.append(line).append('\n');
                }
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return builder.toString();
    }

    private void assertContent(String expectedContent, File file)
    {
        assertEquals(true, file.exists());
        IHierarchicalContent content;
        if (file.getName().endsWith("tar"))
        {
            content = new TarBasedHierarchicalContent(file, staging);
        } else
        {
            content = new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(file, null);
        }
        IHierarchicalContentNode rootNode = content.getRootNode();
        StringBuilder builder = new StringBuilder();
        renderContent(rootNode, builder, "");
        assertEquals(expectedContent, builder.toString());
    }

    private void renderContent(IHierarchicalContentNode node, StringBuilder builder, String indent)
    {
        builder.append(indent).append(node.getName()).append(":\n");
        if (node.isDirectory())
        {
            List<IHierarchicalContentNode> childNodes = node.getChildNodes();
            Collections.sort(childNodes, new Comparator<IHierarchicalContentNode>()
                {
                    @Override
                    public int compare(IHierarchicalContentNode n1, IHierarchicalContentNode n2)
                    {
                        return n1.getName().compareTo(n2.getName());
                    }
                });
            for (IHierarchicalContentNode childNode : childNodes)
            {
                renderContent(childNode, builder, indent + "  ");
            }
        } else
        {
            addContent(node, builder, indent + "  >");
        }
    }

    private void addContent(IHierarchicalContentNode node, StringBuilder builder, String indent)
    {
        InputStream inputStream = node.getInputStream();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(indent).append(line).append('\n');
            }
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private void prepareIsReplicatedArchiveDefined(final boolean defined)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).isReplicatedArchiveDefined();
                    will(returnValue(defined));
                }
            });
    }

    private void prepareFileOperationsGenerateContainerPath(final String containerPath, final DatasetDescription... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).generateContainerPath(Arrays.asList(dataSets));
                    will(returnValue(containerPath));
                }
            });
    }

    private void prepareFileOperationsDelete(final String containerPath)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).deleteContainerFromFinalDestination(containerPath);
                    one(fileOperations).deleteContainerFromStage(containerPath);
                }
            });
    }

    private void prepareLockAndReleaseDataSet(final DatasetDescription dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(dataSet.getDataSetCode());
                    one(shareIdManager).releaseLock(dataSet.getDataSetCode());
                }
            });
    }

    private void prepareUpdateShareIdAndSize(final DatasetDescription dataSet, final long expectedSize)
    {
        context.checking(new Expectations()
            {
                {
                    one(openBISService).updateShareIdAndSize(dataSet.getDataSetCode(), share.getName(), expectedSize);
                }
            });
    }

    private MultiDataSetArchiver createArchiver(IMultiDataSetFileOperationsManager fileManagerOrNull)
    {
        return new MockMultiDataSetArchiver(properties, store, openBISService, shareIdManager, statusUpdater,
                transaction, fileManagerOrNull, transaction, freeSpaceProvider);
    }

    private DatasetDescription dataSet(final String code, String content)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode(code);
        description.setDataSetLocation(code);
        description.setExperimentIdentifier(EXPERIMENT_IDENTIFIER);
        File folder = new File(share, code);
        folder.mkdirs();
        FileUtilities.writeToFile(new File(folder, "data"), content);
        context.checking(new Expectations()
            {
                {
                    allowing(shareIdManager).getShareId(code);
                    will(returnValue(share.getName()));

                    allowing(openBISService).tryGetDataSet(code);
                    PhysicalDataSet physicalDataSet = new DataSetBuilder(0).code(code).type("MDT")
                            .store(new DataStoreBuilder(DSS_CODE).getStore()).fileFormat("TXT")
                            .experiment(experiment).location(code).getDataSet();
                    will(returnValue(physicalDataSet));
                }
            });
        return description;
    }

    private String getLogContent()
    {
        return removeTimeInformationFromContent(logRecorder.getLogContent());
    }

    private String removeTimeInformationFromContent(String content)
    {
        return content.replaceAll("0:\\d{2}:\\d{2}\\.\\d{3}", "0:??:??.???")
                .replaceAll("\\d{8}-\\d{6}\\.tar", "yyyyMMdd-HHmmss.tar");
    }

}
