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
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.cifex.CifexExtractorHelper;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class LcaMicDataSetUploader extends AbstractDataSetUploader
{
    static final IDataSetUploaderFactory FACTORY = new IDataSetUploaderFactory()
    {
        
        public IDataSetUploader create(DataSetInformation dataSetInformation,
                DataSource dataSource, IEncapsulatedOpenBISService service,
                TimeSeriesDataSetUploaderParameters parameters)
        {
            return new LcaMicDataSetUploader(dataSource, service, parameters);
        }

        public IDataSetUploader create(DataSetInformation dataSetInformation,
                ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
                TimeSeriesDataSetUploaderParameters parameters)
        {
            return new LcaMicDataSetUploader(dao, service, parameters);
        }
    };

    LcaMicDataSetUploader(DataSource dataSource, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        super(dataSource, service, parameters);
    }

    LcaMicDataSetUploader(ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        super(dao, service, parameters);
    }

    @Override
    protected void handleTSVFile(File tsvFile, DataSetInformation dataSetInformation,
            IDropBoxFeeder feeder)
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(tsvFile);
            String fileName = tsvFile.toString();
            TabSeparatedValueTable table =
                    new TabSeparatedValueTable(reader, fileName, parameters.isIgnoreEmptyLines(), true, true);
            List<Column> columns = table.getColumns();
            File timeSeriesDropBox = parameters.getTimeSeriesDropBox();
            List<String> timeValues = columns.get(0).getValues();
            List<NewProperty> properties = dataSetInformation.getDataSetProperties();
            for (NewProperty property : properties)
            {
                if (property.getPropertyCode().equals(TimeSeriesPropertyType.TIME_POINT_LIST.toString()))
                {
                    property.setValue(HeaderUtils.join(timeValues));
                }
            }
            String lastBBAID = null;
            for (int i = 1; i < columns.size(); i++)
            {
                Column column = columns.get(i);
                String header = column.getHeader();
                File dataSet = new File(timeSeriesDropBox, DataSetHandler.LCA_MIC_TIME_SERIES + i);
                dataSet.mkdir();
                String[] items = header.split(DataColumnHeader.SEPARATOR);
                if (items.length < 11)
                {
                    throw new UserFailureException("Invalid header: Missing BBA ID: " + header);
                }
                String bbaIDOfColumn = items[10];
                if (bbaIDOfColumn.startsWith("BBA") == false)
                {
                    throw new UserFailureException("Invalid header: BBA ID doesn't start with 'BBA': " + header);
                }
                if (lastBBAID != null && bbaIDOfColumn.equals(lastBBAID) == false)
                {
                    throw new UserFailureException(
                            "Invalid headers: All BBA IDs should be the same. "
                                    + "The folowing two different BBA IDs found: " + lastBBAID
                                    + " " + bbaIDOfColumn);
                }
                lastBBAID = bbaIDOfColumn;
                items[10] = "NB";
                StringBuilder builder = new StringBuilder("BBA ID");
                for (String value : timeValues)
                {
                    items[3] = value;
                    builder.append("\t").append(StringUtils.join(items, DataColumnHeader.SEPARATOR));
                }
                builder.append("\n").append(bbaIDOfColumn);
                List<String> values = column.getValues();
                for (String value : values)
                {
                    builder.append("\t").append(value);
                }
                builder.append("\n");
                FileUtilities.writeToFile(new File(dataSet, header + ".txt"), builder.toString());
                builder.setLength(0);
                builder.append("comment=null,");
                builder.append(dataSetInformation.getExperimentIdentifier()).append(",");
                builder.append(dataSetInformation.getDataSetCode()).append(",");
                builder.append(DataSetHandler.LCA_MIC_TIME_SERIES).append(",TSV\n");
                builder.append("user-email=").append(dataSetInformation.tryGetUploadingUserEmail());
                FileUtilities.writeToFile(new File(dataSet, CifexExtractorHelper.REQUEST_PROPERTIES_FILE), builder.toString());
            }
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
