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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.cifex.CifexDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * @author Franz-Josef Elmer
 */
public class TimeSeriesDataSetInfoExtractor implements IDataSetInfoExtractor
{
    private static final String LIST_SEPARATOR = ", ";

	private static final String[] TSV_EXTENSIONS =
        { "tsv", "txt" };

    private final CifexDataSetInfoExtractor infoExtractor;

    private final Properties properties;

    public TimeSeriesDataSetInfoExtractor(Properties globalProperties)
    {
        this.properties = globalProperties;
        infoExtractor = new CifexDataSetInfoExtractor(globalProperties);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetInformation info =
                infoExtractor.getDataSetInformation(incomingDataSetPath, openbisService);
        String email = info.tryGetUploadingUserEmail();
        if (email != null)
        {
            info.getDataSetProperties().add(
                    new NewProperty(TimeSeriesPropertyType.UPLOADER_EMAIL.toString(), email));
        }
        Collection<DataColumnHeader> headers = loadHeaders(incomingDataSetPath);
        Map<DataHeaderProperty, Set<String>> values =
                TimeSeriesHeaderUtils.extractHeaderPropertyValues(headers);
        addDataSetProperty(info, TimeSeriesPropertyType.TECHNICAL_REPLICATE_CODE_LIST, values);
        addDataSetProperty(info, TimeSeriesPropertyType.BIOLOGICAL_REPLICATE_CODE, values);
        addDataSetProperty(info, TimeSeriesPropertyType.TIME_SERIES_DATA_SET_TYPE, values);
        addDataSetProperty(info, TimeSeriesPropertyType.CEL_LOC, values);
        addDataSetProperty(info, TimeSeriesPropertyType.CG_LIST, values);
        addDataSetProperty(info, TimeSeriesPropertyType.CULTIVATION_METHOD_EXPERIMENT_CODE, values);
        addDataSetProperty(info, TimeSeriesPropertyType.EXPERIMENT_CODE, values);
        addDataSetProperty(info, TimeSeriesPropertyType.SCALE_LIST, values);
        addDataSetProperty(info, TimeSeriesPropertyType.TIME_POINT_LIST, values);
        addDataSetProperty(info, TimeSeriesPropertyType.TIME_POINT_TYPE, values);
        addDataSetProperty(info, TimeSeriesPropertyType.BI_ID, values);
        addDataSetProperty(info, TimeSeriesPropertyType.VALUE_TYPE_LIST, values);
        return info;
    }

    private Collection<DataColumnHeader> loadHeaders(File incomingDataSetPath)
    {
        Collection<DataColumnHeader> headers = new ArrayList<DataColumnHeader>();
        boolean ignoreEmptyLines =
                new TimeSeriesDataSetUploaderParameters(properties, false).isIgnoreEmptyLines();
        List<File> tsvFiles =
                FileUtilities.listFiles(incomingDataSetPath, TSV_EXTENSIONS, false, null);
        for (File tsvFile : tsvFiles)
        {
            headers.addAll(loadHeadersFromFile(ignoreEmptyLines, tsvFile));
        }
        return headers;
    }

    private void addDataSetProperty(DataSetInformation info,
            TimeSeriesPropertyType timeSeriesPropertyType, Map<DataHeaderProperty, Set<String>> map)
    {
        String propertyValue = getPropertyValue(timeSeriesPropertyType.getHeaderProperty(), map,timeSeriesPropertyType.isMultipleValues());
        NewProperty newProperty = new NewProperty(timeSeriesPropertyType.name(), propertyValue);
		info.getDataSetProperties().add(
                newProperty);
    }
 
    @Private
    static String getPropertyValue(DataHeaderProperty property,
            Map<DataHeaderProperty, Set<String>> map, boolean multipleValuesAllowed)
    {
        Set<String> set = map.get(property);
        if (set == null || set.size() < 1)
        {
            String message = String.format("%s not defined", property.name());
            throw new UserFailureException(message);
        }
        if (set.size() == 1)
        {
            return set.iterator().next();
        }
        if (multipleValuesAllowed == false){
        	String message = String.format("Inconsistent header values of '%s'. " +
        			"Expected the same value in all the columns, found: [%s].", 
        			property.name(), StringUtils.join(set, LIST_SEPARATOR));
            throw new UserFailureException(message);
        }
        return StringUtils.join(set, LIST_SEPARATOR);
    }

    private Collection<DataColumnHeader> loadHeadersFromFile(boolean ignoreEmptyLines, File tsvFile)
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(tsvFile);
            String fileName = tsvFile.toString();
            TabSeparatedValueTable table =
                    new TabSeparatedValueTable(reader, fileName, ignoreEmptyLines);
            List<Column> columns = table.getColumns();
            return TimeSeriesHeaderUtils.extractDataColumnHeaders(columns);
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
