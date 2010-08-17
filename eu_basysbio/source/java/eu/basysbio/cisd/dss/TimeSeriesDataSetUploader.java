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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Franz-Josef Elmer
 */
class TimeSeriesDataSetUploader extends AbstractDataSetUploader
{
    static final IDataSetUploaderFactory FACTORY = new IDataSetUploaderFactory()
        {

            public IDataSetUploader create(DataSetInformation dataSetInformation,
                    DataSource dataSource, IEncapsulatedOpenBISService service,
                    TimeSeriesDataSetUploaderParameters parameters)
            {
                return new TimeSeriesDataSetUploader(dataSource, service, parameters);
            }

            public IDataSetUploader create(DataSetInformation dataSetInformation,
                    ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
                    TimeSeriesDataSetUploaderParameters parameters)
            {
                return new TimeSeriesDataSetUploader(dao, service, parameters);
            }
        };
        
    private final DatabaseFeeder databaseFeeder;

    TimeSeriesDataSetUploader(DataSource dataSource, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        super(dataSource, service, parameters);
        databaseFeeder = new DatabaseFeeder(dao, service, parameters);
    }

    TimeSeriesDataSetUploader(ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        super(dao, service, parameters);
        databaseFeeder = new DatabaseFeeder(dao, service, parameters);
    }

    @Override
    protected void handleTSVFile(File tsvFile, DataSetInformation dataSetInformation)
    {
        // TODO 2010-08-08, IA: Workaround enabling upload of LCA_MIC data
        // ("dataset for header ... already registered"). Remove after problem is fixed.
        if (dataSetInformation.getDataSetType().getCode().startsWith("LCA_MIC"))
        {
//            return;
        }
        FileReader reader = null;
        try
        {
            reader = new FileReader(tsvFile);
            String fileName = tsvFile.toString();
            TabSeparatedValueTable table =
                    new TabSeparatedValueTable(reader, fileName, parameters.isIgnoreEmptyLines(),
                            true, false);
            List<Column> columns = table.getColumns();
            List<Column> commonColumns = new ArrayList<Column>();
            List<Column> dataColumns = new ArrayList<Column>();
            for (Column column : columns)
            {
                String header = column.getHeader();
                if (HeaderUtils.isDataColumnHeader(header))
                {
                    dataColumns.add(column);
                } else
                {
                    commonColumns.add(column);
                }
            }
            databaseFeeder.feedDatabase(dataSetInformation, commonColumns, dataColumns);
        } catch (RuntimeException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

}
