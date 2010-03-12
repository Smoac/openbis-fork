/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.IMaintenanceTask;
import ch.systemsx.cisd.etlserver.plugins.HierarchicalStorageUpdater;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class PostRegistrationDatabaseUploadTask implements IMaintenanceTask
{
    private static final IDropBoxFeeder DUMMY_FEEDER = new IDropBoxFeeder()
        {

            public void feed(String userEmail, String sampleCode, List<Column> commonColumns,
                    Column dataColumn)
            {
            }
        };

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PostRegistrationDatabaseUploadTask.class);

    private final IEncapsulatedOpenBISService service;
    private final File storeRoot;
    
    private TimeSeriesDataSetUploaderParameters parameters;

    private DataSource dataSource;

    public PostRegistrationDatabaseUploadTask()
    {
        LogInitializer.init();
        service = ServiceProvider.getOpenBISService();
        Properties properties = PropertyParametersUtil.loadServiceProperties();
        storeRoot =
                new File(PropertyUtils.getMandatoryProperty(properties,
                        HierarchicalStorageUpdater.STOREROOT_DIR_KEY));
    }

    public void setUp(String pluginName, Properties properties)
    {
        parameters = new TimeSeriesDataSetUploaderParameters(properties, false);
        dataSource = DBUtils.createDBContext(properties).getDataSource();
    }
    
    public void execute()
    {
        Set<String> knownDataSets = getKnownDataSets();
        List<SimpleDataSetInformationDTO> dataSets = service.listDataSets();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            String dataSetType = dataSet.getDataSetType();
            if (TimeSeriesDataSetUploader.TIME_SERIES.equals(dataSetType)
                    && knownDataSets.contains(dataSet.getDataSetCode()) == false)
            {
                File pathToDataSet = new File(storeRoot, dataSet.getDataSetLocation());
                File[] dataSetFiles = new File(pathToDataSet, "original").listFiles();
                if (dataSetFiles != null && dataSetFiles.length > 0)
                {
                    for (File dataSetFile : dataSetFiles)
                    {
                        TimeSeriesDataSetUploader uploader =
                                new TimeSeriesDataSetUploader(dataSource, service, parameters);
                        DataSetInformation dataSetInformation = createDataSetInformation(dataSet);
                        try
                        {
                            uploader.upload(dataSetFile, dataSetInformation, DUMMY_FEEDER);
                            uploader.commit();
                            if (operationLog.isInfoEnabled())
                            {
                                operationLog.info("Data set " + dataSet.getDataSetCode()
                                        + " successfully uploaded.");
                            }
                        } catch (Exception ex)
                        {
                            uploader.rollback();
                            operationLog.error("Uploading of data set " + dataSet.getDataSetCode()
                                    + " failed: ", ex);
                        }
                    }
                }
            }
        }
    }

    private DataSetInformation createDataSetInformation(SimpleDataSetInformationDTO dataSet)
    {
        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(dataSet.getDataSetCode());
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(dataSet.getDataSetType());
        dataSetInformation.setDataSetType(dataSetType);
        String groupCode = dataSet.getGroupCode();
        dataSetInformation.setSpaceCode(groupCode);
        String databaseInstanceCode = dataSet.getDatabaseInstanceCode();
        String projectCode = dataSet.getProjectCode();
        String experimentCode = dataSet.getExperimentCode();
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(databaseInstanceCode,
                groupCode, projectCode, experimentCode));
        return dataSetInformation;
    }

    private Set<String> getKnownDataSets()
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            ITimeSeriesDAO dao = QueryTool.getQuery(connection, ITimeSeriesDAO.class);
            DataSet<String> dataSet = dao.findDataSets();
            Set<String> dataSets = new HashSet<String>();
            dataSets.addAll(dataSet);
            dataSet.close();
            return dataSets;
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                } catch (SQLException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

    }


}
