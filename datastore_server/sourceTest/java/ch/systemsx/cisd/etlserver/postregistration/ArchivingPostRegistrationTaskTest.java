/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.postregistration;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.AVAILABLE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.BACKUP_PENDING;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
 * @author Kaloyan Enimanev
 */
public class ArchivingPostRegistrationTaskTest extends AssertJUnit
{
    private String DATASET_CODE = "ds1";

    private String ARCHIVE_ERROR = "rsync: timeout in data send/receive (30)";

    private IEncapsulatedOpenBISService service;

    private IArchiverPlugin archiver;

    private IDataStoreServiceInternal dataStoreService;

    private IDataSetDirectoryProvider directoryProvider;

    private BeanFactory applicationContext;

    private Mockery context;

    private IHierarchicalContentProvider contentProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dataStoreService = context.mock(IDataStoreServiceInternal.class);
        archiver = context.mock(IArchiverPlugin.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        applicationContext = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(applicationContext);
    }

    @AfterMethod
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testEmailSendOnArchiveError()
    {
        prepareExpectations();

        ArchivingPostRegistrationTask task =
                new ArchivingPostRegistrationTask(new Properties(), service);

        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        createEmailMessage(DATASET_CODE));

        task.createExecutor(DATASET_CODE, false).execute();

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    private final void prepareExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(applicationContext).getBean("data-store-service");
                    will(returnValue(dataStoreService));

                    allowing(applicationContext).getBean("hierarchical-content-provider");
                    will(returnValue(contentProvider));

                    allowing(dataStoreService).getArchiverPlugin();
                    will(returnValue(archiver));

                    allowing(dataStoreService).getDataSetDirectoryProvider();
                    will(returnValue(directoryProvider));

                    one(service).listDataSetsByCode(Arrays.asList(DATASET_CODE));
                    List<ExternalData> externalDatas = Arrays.asList(createDataSet());
                    will(returnValue(externalDatas));

                    one(service).compareAndSetDataSetStatus(DATASET_CODE, AVAILABLE,
                            BACKUP_PENDING, false);
                    will(returnValue(true));

                    one(archiver).archive(with(any(List.class)),
                            with(any(ArchiverTaskContext.class)), with(any(boolean.class)));
                    will(returnValue(createFailedProcesingStatus()));

                    one(service).compareAndSetDataSetStatus(DATASET_CODE, BACKUP_PENDING,
                            AVAILABLE, true);
                    will(returnValue(true));

                }
            });
    }

    private ProcessingStatus createFailedProcesingStatus()
    {
        ProcessingStatus status = new ProcessingStatus();
        status.addDatasetStatus(DATASET_CODE, Status.createError(true, ARCHIVE_ERROR));
        return status;
    }

    private String createEmailMessage(String dataSetCode2)
    {
        return "Eager archiving of dataset '" + DATASET_CODE + "' has failed. \n"
                + "Error encountered : " + ARCHIVE_ERROR + "\n"
                + "If you wish to archive the dataset in the future, "
                + "you can configure an \'AutoArchiverTask\'.";
    }

    private ExternalData createDataSet()
    {
        DataStore dataStore = new DataStore();
        dataStore.setCode("STANDARD");

        DataSet dataSet = new DataSet();
        dataSet.setCode(DATASET_CODE);
        dataSet.setFileFormatType(new FileFormatType("DATA"));
        dataSet.setDataStore(dataStore);
        return dataSet;
    }
}
