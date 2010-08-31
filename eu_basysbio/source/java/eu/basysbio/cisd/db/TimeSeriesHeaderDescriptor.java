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

package eu.basysbio.cisd.db;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.basysbio.cisd.dss.ValueGroupDescriptor;

/**
 * A descriptor for a header line in a time series data set file.
 * 
 * @author Bernd Rinn
 */
public class TimeSeriesHeaderDescriptor
{
    public String identifierColumn;

    /** -1 for: doesn't exist. */
    public final int humanReadableColumnIdx;

    public final int firstValueColumn;

    public final TimeSeriesColumnDescriptor[] columnDescriptors;

    /** Maps value groups to their id in the database. */
    public final Map<ValueGroupDescriptor, Long> valueGroupMap;

    public TimeSeriesHeaderDescriptor(File file, IBaSysBioUpdater updater, String headerLine)
    {
        final String[] columnHeaders = StringUtils.split(headerLine, '\t');
        if (columnHeaders.length < 2)
        {
            throw new ParsingException(file, "Less than 2 columns found in header.");
        }
        valueGroupMap = new LinkedHashMap<ValueGroupDescriptor, Long>();
        identifierColumn = columnHeaders[0];
        if ("HumanReadable".equals(columnHeaders[1]))
        {
            humanReadableColumnIdx = 1;
            firstValueColumn = 2;
        } else
        {
            humanReadableColumnIdx = -1;
            firstValueColumn = 1;
        }
        columnDescriptors = new TimeSeriesColumnDescriptor[columnHeaders.length - firstValueColumn];
        for (int i = firstValueColumn; i < columnHeaders.length; ++i)
        {
            final TimeSeriesColumnDescriptor desc =
                    new TimeSeriesColumnDescriptor(file, columnHeaders[i]);
            columnDescriptors[i - firstValueColumn] = desc;
            if (valueGroupMap.containsKey(desc.getValueGroupDescriptor()) == false)
            {
                valueGroupMap.put(desc.getValueGroupDescriptor(), updater.nextValueGroupId());
            }
        }
    }
}
