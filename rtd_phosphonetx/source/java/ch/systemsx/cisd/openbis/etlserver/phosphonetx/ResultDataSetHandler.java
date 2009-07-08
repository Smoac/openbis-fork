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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinProphetDetails;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummary;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ResultDataSetHandler implements IDataSetHandler
{
    private static final String DATABASE_ENGINE = "database.engine";
    private static final String DATABASE_URL_HOST_PART = "database.url-host-part";
    private static final String DATABASE_BASIC_NAME = "database.basic-name";
    private static final String DATABASE_KIND = "database.kind";
    private static final String DATABASE_OWNER = "database.owner";
    private static final String DATABASE_PASSWORD = "database.password";
    
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ResultDataSetHandler.class);
    
    private static DataSource createDataSource(Properties properties)
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode(properties.getProperty(DATABASE_ENGINE, "postgresql"));
        context.setUrlHostPart(properties.getProperty(DATABASE_URL_HOST_PART, ""));
        context.setBasicDatabaseName(properties.getProperty(DATABASE_BASIC_NAME, "phosphonetx"));
        context.setDatabaseKind(PropertyUtils.getMandatoryProperty(properties, DATABASE_KIND));
        context.setOwner(properties.getProperty(DATABASE_OWNER, ""));
        context.setPassword(properties.getProperty(DATABASE_PASSWORD, ""));
        return context.getDataSource();
    }
    
    private final IDataSetHandler delegator;
    private final Unmarshaller unmarshaller;
    private final IEncapsulatedOpenBISService openbisService;
    private final DataSource dataSource;

    public ResultDataSetHandler(Properties properties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        dataSource =
                createDataSource(ExtendedProperties.getSubset(properties,
                        IDataSetHandler.DATASET_HANDLER_KEY + '.', true));
        this.delegator = delegator;
        this.openbisService = openbisService;
        try
        {
            JAXBContext context =
                    JAXBContext.newInstance(ProteinSummary.class, ProteinProphetDetails.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public List<DataSetInformation> handleDataSet(File dataSet)
    {
        long time = System.currentTimeMillis();
        ProteinSummary summary = readProtXML(dataSet);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(summary.getProteinGroups().size()
                    + " protein groups are successfully read from '" + dataSet + "' in "
                    + (System.currentTimeMillis() - time) + " msec");
        }
        time = System.currentTimeMillis();
        List<DataSetInformation> dataSets = delegator.handleDataSet(dataSet);
        if (dataSets.isEmpty())
        {
            throw new ConfigurationFailureException(
                    "Data set not registered due to some error. See error folder in data store.");
        }
        if (dataSets.size() != 1)
        {
            throw new ConfigurationFailureException(
                    dataSets.size() + " data set registered: " +
                    "Only data set handlers (like the primary one) " +
                    "registering exactly one data set are allowed.");
        }
        DataSetInformation dataSetInformation = dataSets.get(0);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Registration at openBIS took "
                    + (System.currentTimeMillis() - time) + " msec: " + dataSetInformation);
        }
        time = System.currentTimeMillis();
        Connection connection;
        try
        {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        ResultDataSetUploader upLoader = new ResultDataSetUploader(connection, openbisService);
        upLoader.upload(dataSetInformation, summary);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Feeding result database took "
                    + (System.currentTimeMillis() - time) + " msec.");
        }
        return dataSets;
    }
    
    private ProteinSummary readProtXML(File dataSet)
    {
        try
        {
            Object object = unmarshaller.unmarshal(dataSet);
            if (object instanceof ProteinSummary == false)
            {
                throw new IllegalArgumentException("Wrong type: " + object);
            }
            ProteinSummary summary = (ProteinSummary) object;
            return summary;
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
