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
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.etlserver.IDataSetUploader;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummary;

/**
 * @author Franz-Josef Elmer
 */
public class ProtXMLUploader implements IDataSetUploader
{
    private static final String VALIDATING_XML = "validating-xml";
    
    private static final String DATABASE_ENGINE = "database.engine";

    private static final String DATABASE_URL_HOST_PART = "database.url-host-part";

    private static final String DATABASE_BASIC_NAME = "database.basic-name";

    private static final String DATABASE_KIND = "database.kind";

    private static final String DATABASE_OWNER = "database.owner";

    private static final String DATABASE_PASSWORD = "database.password";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ProtXMLUploader.class);

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

    private final ProtXMLLoader loader;

    private final IEncapsulatedOpenBISService openbisService;

    private final DataSource dataSource;

    public ProtXMLUploader(Properties properties, IEncapsulatedOpenBISService openbisService)
    {
        dataSource = createDataSource(properties);
        this.openbisService = openbisService;
        loader = new ProtXMLLoader(PropertyUtils.getBoolean(properties, VALIDATING_XML, false));
    }

    public void upload(File dataSet, DataSetInformation dataSetInformation)
    {
        long time = System.currentTimeMillis();
        File protXMLFile = getProtXMLFile(dataSet);
        ProteinSummary summary = loader.readProtXML(protXMLFile);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(summary.getProteinGroups().size()
                    + " protein groups are successfully read from '" + protXMLFile + "' in "
                    + (System.currentTimeMillis() - time) + " msec");
        }
        time = System.currentTimeMillis();
        ResultDataSetUploader upLoader = createUploader();
        upLoader.upload(dataSetInformation, summary);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Feeding result database took " + (System.currentTimeMillis() - time)
                    + " msec.");
        }
    }

    protected ResultDataSetUploader createUploader()
    {
        Connection connection;
        try
        {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return new ResultDataSetUploader(connection, openbisService);
    }

    private File getProtXMLFile(File dataSet)
    {
        if (dataSet.isDirectory() == false)
        {
            return dataSet;
        }
        File[] files = dataSet.listFiles();
        for (File file : files)
        {
            if (file.getName().endsWith("prot.xml"))
            {
                return file;
            }
        }
        throw new UserFailureException("No *prot.xml file found in data set '" + dataSet + "'.");
    }

}
