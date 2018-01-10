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

package ch.systemsx.cisd.etlserver.path;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ICredentials;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.plugins.AbstractMaintenanceTaskWithStateFile;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetAndPathInfoDBConsistencyChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetAndPathInfoDBConsistencyCheckTask extends AbstractMaintenanceTaskWithStateFile
{
    static final String CHECKING_TIME_INTERVAL_KEY = "checking-time-interval";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            BasicConstant.DATE_WITHOUT_TIMEZONE_PATTERN);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetAndPathInfoDBConsistencyCheckTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DataSetAndPathInfoDBConsistencyCheckTask.class);

    private IApplicationServerApi service;

    private IHierarchicalContentProvider fileProvider;

    private IHierarchicalContentProvider pathInfoProvider;

    private long timeInterval;

    private ITimeProvider timeProvider;

    private IDataSetDirectoryProvider directoryProvider;

    private File stateFile;

    public DataSetAndPathInfoDBConsistencyCheckTask()
    {
    }

    DataSetAndPathInfoDBConsistencyCheckTask(IHierarchicalContentProvider fileProvider,
            IHierarchicalContentProvider pathInfoProvider, IDataSetDirectoryProvider directoryProvider,
            IApplicationServerApi service, ITimeProvider timeProvider)
    {
        this.fileProvider = fileProvider;
        this.pathInfoProvider = pathInfoProvider;
        this.directoryProvider = directoryProvider;
        this.service = service;
        this.timeProvider = timeProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        if (service == null)
        {
            service = ServiceProvider.getV3ApplicationService();
            timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
            directoryProvider = ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
        }
        stateFile = getStateFile(properties, directoryProvider.getStoreRoot());
        timeInterval =
                DateTimeUtils.getDurationInMillis(properties, CHECKING_TIME_INTERVAL_KEY,
                        DateUtils.MILLIS_PER_DAY);
    }

    @Override
    public void execute()
    {
        Date youngerThanDate = new Date(timeProvider.getTimeInMilliseconds() - timeInterval);
        String sessionToken = login();
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(youngerThanDate);
        searchCriteria.withPhysicalData().withStorageConfirmation().thatEquals(true);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        List<DataSet> dataSets = service.searchDataSets(sessionToken, searchCriteria, fetchOptions).getObjects();
        System.err.println(dataSets);
        List<String> dataSetCodes = dataSets.stream()
                .map(dataSet -> dataSet.getCode()).collect(Collectors.toList());
        operationLog.info("Check " + dataSets.size() + " data sets registered since "
                + DATE_FORMAT.format(youngerThanDate));
        DataSetAndPathInfoDBConsistencyChecker checker =
                new DataSetAndPathInfoDBConsistencyChecker(fileProvider, pathInfoProvider);
        checker.checkDataSets(dataSetCodes);
        if (checker.noErrorAndInconsitencyFound() == false)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("File system and path info DB consistency check report for all data sets since ");
            builder.append(DATE_FORMAT.format(youngerThanDate)).append("\n\n");
            builder.append(checker.createReport());
            notificationLog.error(builder.toString());
        }
        service.logout(sessionToken);
    }
    
    String login()
    {
        ICredentials credentials 
                = (ICredentials) ServiceProvider.getApplicationContext().getBean("reauthenticateInterceptor");
        String sessionToken = service.login(credentials.getUserId(), credentials.getPassword());
        return sessionToken;
    }
    
}
