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

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetFileOperationsManager.STAGING_DESTINATION_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDatasetArchiver.MINIMUM_CONTAINER_SIZE_IN_BYTES;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.MockDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDatasetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SimpleFileContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class MultiDatasetArchiverTest extends AbstractFileSystemTestCase
{
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

    private static final class MockMultiDatasetArchiverDBTransaction implements IMultiDatasetArchiverDBTransaction
    {
        private int id;

        private List<MultiDataSetArchiverContainerDTO> containers = new ArrayList<MultiDataSetArchiverContainerDTO>();

        private List<MultiDataSetArchiverDataSetDTO> dataSets = new ArrayList<MultiDataSetArchiverDataSetDTO>();

        private boolean committed;

        private boolean rolledBack;

        private boolean closed;

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
            containers.add(container);
            return container;
        }

        @Override
        public MultiDataSetArchiverDataSetDTO insertDataset(DatasetDescription dataSet, MultiDataSetArchiverContainerDTO container)
        {
            String dataSetCode = dataSet.getDataSetCode();
            Long dataSetSize = dataSet.getDataSetSize();
            MultiDataSetArchiverDataSetDTO dataSetDTO = new MultiDataSetArchiverDataSetDTO(id++, dataSetCode, container.getId(), dataSetSize);
            dataSets.add(dataSetDTO);
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
        public void commit()
        {
            committed = true;
        }

        @Override
        public void rollback()
        {
            rolledBack = true;
        }

        @Override
        public void close()
        {
            closed = true;
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
            builder.append("\ncomitted: ").append(committed);
            builder.append(", rolledBack: ").append(rolledBack);
            builder.append(", closed: ").append(closed);
            return builder.toString();
        }

    }

    private static final class MockMultiDatasetArchiver extends MultiDatasetArchiver
    {
        private static final long serialVersionUID = 1L;

        private IMultiDatasetArchiverDBTransaction transaction;

        public MockMultiDatasetArchiver(Properties properties, File storeRoot,
                IEncapsulatedOpenBISService openBISService, IShareIdManager shareIdManager,
                IDataSetStatusUpdater statusUpdater, IMultiDatasetArchiverDBTransaction transaction)
        {
            super(properties, storeRoot);
            this.transaction = transaction;
            setService(openBISService);
            setShareIdManager(shareIdManager);
            setStatusUpdater(statusUpdater);
        }

        @Override
        IMultiDatasetArchiverDBTransaction getTransaction()
        {
            return transaction;
        }
    }

    private static final String EXPERIMENT_IDENTIFIER = "/S/P/E";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IMultiDatasetArchiverDBTransaction transaction;

    private IDataSetDirectoryProvider directoryProvider;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private ArchiverTaskContext archiverContext;

    private IShareIdManager shareIdManager;

    private DatasetDescription ds1;

    private DatasetDescription ds2;

    private DatasetDescription ds3;

    private DatasetDescription ds4;

    private File store;

    private File share;

    private IEncapsulatedOpenBISService openBISService;

    private IDataSetStatusUpdater statusUpdater;

    private Properties properties;

    private File staging;

    private File archive;

    private IDataStoreServiceInternal dssService;

    private Experiment experiment;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        transaction = new MockMultiDatasetArchiverDBTransaction();
        store = new File(workingDirectory, "store");
        share = new File(store, "1");
        share.mkdirs();
        context = new Mockery();
        BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        shareIdManager = ServiceProviderTestWrapper.mock(context, IShareIdManager.class);
        openBISService = ServiceProviderTestWrapper.mock(context, IEncapsulatedOpenBISService.class);
        dssService = ServiceProviderTestWrapper.mock(context, IDataStoreServiceInternal.class);
        hierarchicalContentProvider = new SimpleFileContentProvider(share);
                ServiceProviderTestWrapper.addMock(context, IHierarchicalContentProvider.class, 
                hierarchicalContentProvider);
        statusUpdater = new RecordingStatusUpdater();
        staging = new File(workingDirectory, "staging");
        staging.mkdirs();
        archive = new File(workingDirectory, "archive");
        archive.mkdirs();
        ds1 = dataSet("ds1", "0123456789");
        ds2 = dataSet("ds2", "01234567890123456789");
        ds3 = dataSet("ds3", "012345678901234567890123456789");
        ds4 = dataSet("ds4", "0123456789012345678901234567890123456789");
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

                    allowing(openBISService).tryGetExperiment(ExperimentIdentifierFactory.parse(EXPERIMENT_IDENTIFIER));
                    will(returnValue(experiment));
                }
            });
    }

    @AfterMethod
    public void checkMockExpectations()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testArchiveOneDataSet()
    {
        prepareUpdateShareIdAndSize(ds2, 20);
        prepareLockAndReleaseDataSet(ds2);
        properties.setProperty(MINIMUM_CONTAINER_SIZE_IN_BYTES, "15");

        MultiDatasetArchiver archiver = createArchiver();
        ProcessingStatus status = archiver.archive(Arrays.asList(ds2), archiverContext, false);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds2']\n" 
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Archive dataset ds2 in ds2.tar\n" 
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Data sets archived: ds2.tar\n" 
                + "INFO  OPERATION.MultiDataSetFileOperationsManager - Copy archive container from '"
                + staging.getAbsolutePath() + "/ds2.tar' to '" + archive.getAbsolutePath(), 
                logRecorder.getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals("[]", Arrays.asList(staging.listFiles()).toString());
        assertEquals(true, new File(archive, ds2.getDataSetCode() + ".tar").exists());
        assertEquals("[ds2]: AVAILABLE true\n", statusUpdater.toString());
        assertEquals("Containers:\nMultiDataSetArchiverContainerDTO [id=0, path=ds2.tar]\n" 
                + "Data sets:\nMultiDataSetArchiverDataSetDTO [id=1, code=ds2, containerId=0, sizeInBytes=20]\n" 
                + "comitted: true, rolledBack: false, closed: true", transaction.toString());
        context.assertIsSatisfied();
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

    private MultiDatasetArchiver createArchiver()
    {
        return new MockMultiDatasetArchiver(properties, store, openBISService, shareIdManager, statusUpdater,
                transaction);
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
                            .store(new DataStoreBuilder("DSS").getStore()).fileFormat("TXT")
                            .experiment(experiment).location(code).getDataSet();
                    will(returnValue(physicalDataSet));
                }
            });
        return description;
    }

}
