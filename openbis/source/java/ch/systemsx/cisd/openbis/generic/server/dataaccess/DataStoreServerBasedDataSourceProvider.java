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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPaceholderConfigurer;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataStoreServerBasedDataSourceProvider implements IDataSourceProvider, InitializingBean
{
    private static final String ROOT_KEY = "dss-based-data-source-provider";
    private static final String DATA_STORE_SERVERS_KEY = "data-store-servers";

    @Resource(name = "propertyConfigurer")
    private ExposablePropertyPaceholderConfigurer configurer;
    
    private final IDAOFactory daoFactory;
    
    private Map<String, DataSource> dataSources;
    
    public DataStoreServerBasedDataSourceProvider(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    public void afterPropertiesSet() throws Exception
    {
        dataSources = new HashMap<String, DataSource>();
        ExtendedProperties props =
                ExtendedProperties.getSubset(configurer.getResolvedProps(), ROOT_KEY + ".", true);
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(props, DATA_STORE_SERVERS_KEY,
                        false);
        for (SectionProperties sectionProperties : sectionsProperties)
        {
            String key = sectionProperties.getKey().toUpperCase();
            Properties properties = sectionProperties.getProperties();
            dataSources.put(key, create(properties));
        }
    }

    public DataSource getDataSourceByDataSetCode(String dataSetCode, String technology)
    {
        DataPE dataSet = daoFactory.getExternalDataDAO().tryToFindDataSetByCode(dataSetCode);
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set " + dataSetCode);
        }
        return getDataSource(dataSet.getDataStore().getCode());
    }
    
    private DataSource getDataSource(String dssCode)
    {
        DataSource dataSource = dataSources.get(dssCode);
        if (dataSource == null)
        {
            throw new ConfigurationFailureException(
                    "No data source configured for Data Store Server '" + dssCode + "'");
        }
        return dataSource;
    }

    private DataSource create(Properties properties)
    {
        return new SimpleDatabaseConfigurationContext(properties).getDataSource();
    }

    public DataSource getDataSourceByExperimentPermID(String experimentPermID, String technology)
    {
        throw new IllegalArgumentException(
                "Getting data source by experiment permID is not supported for technology '"
                        + technology + "'.");
    }

    public DataSource getDataSourceBySamplePermID(String samplePermID, String technology)
    {
        throw new IllegalArgumentException(
                "Getting data source by sample permID is not supported for technology '"
                + technology + "'.");
    }

}
