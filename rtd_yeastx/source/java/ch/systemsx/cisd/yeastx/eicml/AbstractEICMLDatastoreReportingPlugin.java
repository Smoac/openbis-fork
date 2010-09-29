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

package ch.systemsx.cisd.yeastx.eicml;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractTableModelReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.yeastx.db.DBUtils;

/**
 * Abstract superclass for reporting plugins operating on the metabol database.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractEICMLDatastoreReportingPlugin extends
        AbstractTableModelReportingPlugin
{
    private static final long serialVersionUID = 1L;

    /** creates a report for specified datasets using a given DAO. */
    abstract protected TableModel createReport(List<DatasetDescription> datasets, IEICMSRunDAO query);

    private final String dataSourceName;

    public AbstractEICMLDatastoreReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        dataSourceName = DataSourceProvider.extractDataSourceName(properties);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        DataSource dataSource =
                ServiceProvider.getDataSourceProvider().getDataSource(dataSourceName);
        IEICMSRunDAO query = DBUtils.getQuery(dataSource, IEICMSRunDAO.class);
        try
        {
            return createReport(datasets, query);
        } finally
        {
            query.close();
        }
    }
}
