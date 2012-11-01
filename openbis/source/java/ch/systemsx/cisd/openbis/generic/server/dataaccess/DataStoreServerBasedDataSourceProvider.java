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
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * Data source provider based on configuration per Data Store Server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServerBasedDataSourceProvider implements IDataSourceProvider,
        InitializingBean
{
    public static final String ROOT_KEY = "dss-based-data-source-provider";

    public static final String DATA_STORE_SERVERS_KEY = "data-store-servers";

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private final IDAOFactory daoFactory;

    private Map<String, DataSource> dataSources;

    public DataStoreServerBasedDataSourceProvider(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        init(ExtendedProperties.getSubset(configurer.getResolvedProps(), ROOT_KEY + ".", true));
    }

    void init(Properties props)
    {
        dataSources = new HashMap<String, DataSource>();
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(props, DATA_STORE_SERVERS_KEY,
                        false);
        for (SectionProperties sectionProperties : sectionsProperties)
        {
            String key = sectionProperties.getKey().toUpperCase();
            Properties properties = sectionProperties.getProperties();
            SimpleDatabaseConfigurationContext context =
                    new SimpleDatabaseConfigurationContext(properties);
            dataSources.put(key, context.getDataSource());
        }
    }

    @Override
    public DataSource getDataSourceByDataSetCode(String dataSetCode, String technology)
    {
        DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode(dataSetCode);
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set: " + dataSetCode);
        }
        return getDataSourceByDataStoreServerCode(dataSet.getDataStore().getCode(), technology);
    }

    @Override
    public DataSource getDataSourceByDataStoreServerCode(String dssCode, String technology)
    {
        DataSource dataSource =
                dataSources.get(dssCode.toUpperCase() + "[" + technology.toUpperCase() + "]");
        if (dataSource == null)
        {
            dataSource = dataSources.get(dssCode.toUpperCase());
        }
        if (dataSource == null)
        {
            throw new ConfigurationFailureException(
                    "No data source configured for Data Store Server '" + dssCode
                            + "' and technology '" + technology + "'.");
        }
        return dataSource;
    }

}
