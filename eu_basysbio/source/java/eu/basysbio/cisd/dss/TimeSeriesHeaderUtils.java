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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.utils.Column;

/**
 * Tools for working with time series headers.
 * 
 * @author Izabela Adamczyk
 */
public class TimeSeriesHeaderUtils
{
    private static final Pattern DATA_COLUMN_HEADER_PATTERN =
            Pattern.compile(".*(" + DataColumnHeader.SEPARATOR + ".*)+");

    /**
     * Checks weather given header matches data column pattern.
     */
    static public boolean isDataColumnHeader(String header)
    {
        return DATA_COLUMN_HEADER_PATTERN.matcher(header).matches();
    }

    /**
     * Chosen data columns should have the same metadata.
     * 
     * @throws UserFailureException when chosen {@link DataHeaderProperty}s are not the same in the
     *             headers
     */
    public static void assertMetadataConsistent(Collection<DataColumnHeader> headers,
            Collection<DataHeaderProperty> consistentProperties)
    {
        Map<DataHeaderProperty, Set<String>> map =
                extractHeaderPropertyValues(headers, consistentProperties);
        StringBuilder sb = new StringBuilder();
        for (DataHeaderProperty key : map.keySet())
        {
            if (map.get(key).size() > 1)
            {
                if (sb.length() > 0)
                {
                    sb.append(",");
                }
                sb.append(key);
                sb.append("(");
                sb.append(StringUtils.join(map.get(key), ","));
                sb.append(")");
            }
        }
        if (sb.length() > 0)
        {
            throw new UserFailureException("Inconsistent data column headers: [" + sb + "]");
        }
    }

    /**
     * Extracts data column headers, skips other columns.
     */
    static public Collection<DataColumnHeader> extractDataColumnHeaders(Collection<Column> columns)
    {
        ArrayList<DataColumnHeader> result = new ArrayList<DataColumnHeader>();
        for (Column c : columns)
        {
            String header = c.getHeader();
            if (isDataColumnHeader(header))
            {
                result.add(new DataColumnHeader(header));
            }
        }
        return result;
    }

    /**
     * Returns a map of {@link DataHeaderProperty}s and sets of values defined in given headers.
     */
    public static Map<DataHeaderProperty, Set<String>> extractHeaderPropertyValues(
            Collection<DataColumnHeader> headers)
    {
        return extractHeaderPropertyValues(headers, Arrays.asList(DataHeaderProperty.values()));
    }

    private static Map<DataHeaderProperty, Set<String>> extractHeaderPropertyValues(
            Collection<DataColumnHeader> headers, Collection<DataHeaderProperty> properties)
    {
        Map<DataHeaderProperty, Set<String>> map = new HashMap<DataHeaderProperty, Set<String>>();
        for (DataColumnHeader dataColumnHeader : headers)
        {
            for (DataHeaderProperty p : properties)
            {
                updateMap(map, p, p.getValue(dataColumnHeader));
            }
        }
        return map;
    }

    private static void updateMap(Map<DataHeaderProperty, Set<String>> map,
            DataHeaderProperty property, String value)
    {
        if (map.containsKey(property) == false)
        {
            map.put(property, new HashSet<String>());
        }
        map.get(property).add(value);
    }

}
