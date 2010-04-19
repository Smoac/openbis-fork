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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Tools for working with headers.
 * 
 * @author Izabela Adamczyk
 */

class HeaderUtils
{
    private static final String DOTS = "...";

    private static final String QUANTIFIED_PEPTIDES = "QuantifiedPeptides";

    private static final String PROTEIN_LCMS_RATIO = "ProteinLcmsRatio";

    private static final Pattern DATA_COLUMN_HEADER_PATTERN =
            Pattern.compile(".*(" + DataColumnHeader.SEPARATOR + ".*)+");

    private static final String LIST_SEPARATOR = ", ";

    private static final String[] TSV_EXTENSIONS =
        { "tsv", "TSV", "txt", "TXT" };

    public static final TimeSeriesPropertyType[] TIME_SERIES_HEADER_PROPERTIES =
                { TimeSeriesPropertyType.TECHNICAL_REPLICATE_CODE_LIST,
                        TimeSeriesPropertyType.BIOLOGICAL_REPLICATE_CODE,
                        TimeSeriesPropertyType.TIME_SERIES_DATA_SET_TYPE,
                        TimeSeriesPropertyType.CEL_LOC, TimeSeriesPropertyType.CG_LIST,
                        TimeSeriesPropertyType.CULTIVATION_METHOD_EXPERIMENT_CODE,
                        TimeSeriesPropertyType.EXPERIMENT_CODE, TimeSeriesPropertyType.SCALE_LIST,
                        TimeSeriesPropertyType.TIME_POINT_LIST,
                        TimeSeriesPropertyType.TIME_POINT_TYPE, TimeSeriesPropertyType.BI_ID,
                        TimeSeriesPropertyType.VALUE_TYPE_LIST };

    /**
     * Extracts data column headers, skips other columns.
     * 
     * @param ignoreTimePoint If <code>true</code> ignore time point in header. Time point will be
     *            set to 0.
     */
    private static Collection<DataColumnHeader> extractDataColumnHeaders(Collection<Column> columns, boolean ignoreTimePoint)
    {
        ArrayList<DataColumnHeader> result = new ArrayList<DataColumnHeader>();
        for (Column c : columns)
        {
            String header = c.getHeader();
            if (isDataColumnHeader(header))
            {
                result.add(new DataColumnHeader(header, ignoreTimePoint));
            }
        }
        return result;
    }

    /**
     * Returns a map of {@link DataHeaderProperty}s and sets of values defined in given headers.
     */
    private static Map<DataHeaderProperty, Set<String>> extractHeaderPropertyValues(
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

    private static Collection<DataColumnHeader> loadHeaders(File dir, boolean ignoreEmptyLines,
            boolean ignoreHashedLines, boolean ignoreTimePoint)
    {
        Collection<DataColumnHeader> headers = new ArrayList<DataColumnHeader>();
        List<File> tsvFiles = listFiles(dir);
        for (File tsvFile : tsvFiles)
        {
            headers.addAll(HeaderUtils.loadHeadersFromFile(tsvFile, ignoreEmptyLines, ignoreHashedLines, 
                    ignoreTimePoint));
        }
        return headers;
    }
    
    static File getTabSeparatedValueFile(File dir)
    {
        List<File> files = listFiles(dir);
        if (files.size() > 1)
        {
            throw new UserFailureException("Exactly one file with extensions ["
                    + StringUtils.join(TSV_EXTENSIONS) + "] expected instead of " + files.size());
        }
        return files.get(0);
    }

    private static List<File> listFiles(File dir)
    {
        List<File> tsvFiles =
                FileUtilities.listFiles(dir, HeaderUtils.TSV_EXTENSIONS, true, null);
        if (tsvFiles.size() == 0)
        {
            throw new UserFailureException(String.format(
                    "Could not find any files with extensions [%s].", StringUtils.join(
                            HeaderUtils.TSV_EXTENSIONS, ",")));
        }
        return tsvFiles;
    }

    private static Collection<DataColumnHeader> loadHeadersFromFile(File tsvFile,
            boolean ignoreEmptyLines, boolean ignoreHashedLines, boolean ignoreTimePoint)
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(tsvFile);
            String fileName = tsvFile.toString();
            TabSeparatedValueTable table =
                    new TabSeparatedValueTable(reader, fileName, ignoreEmptyLines, ignoreHashedLines);
            List<Column> columns = table.getColumns();
            return extractDataColumnHeaders(columns, ignoreTimePoint);
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

    private static NewProperty extractProperty(TimeSeriesPropertyType timeSeriesPropertyType,
            boolean multipleValuesAllowed,
            Map<DataHeaderProperty, Set<String>> map)
    {
        String propertyValue =
                HeaderUtils.getPropertyValue(timeSeriesPropertyType.getHeaderProperty(),
                        map, multipleValuesAllowed || timeSeriesPropertyType.isMultipleValues());
        NewProperty newProperty = new NewProperty(timeSeriesPropertyType.name(), propertyValue);
        return newProperty;
    }

    @Private
    static String getPropertyValue(DataHeaderProperty property,
            Map<DataHeaderProperty, Set<String>> map, boolean multipleValuesAllowed)
    {
        return getPropertyValue(property, map, multipleValuesAllowed, true);
    }

    @Private
    static String getPropertyValue(DataHeaderProperty property,
            Map<DataHeaderProperty, Set<String>> map, boolean multipleValuesAllowed,
            boolean treatQuantifiedPeptidesAsProteins)
    {
        Set<String> set = map.get(property);
        if (set == null || set.size() < 1)
        {
            String message = String.format("%s not defined", property.name());
            throw new UserFailureException(message);
        }
        Iterator<String> iterator = set.iterator();
        if (set.size() == 1)
        {
            return iterator.next();
        }
        if (multipleValuesAllowed == false)
        {
            if (treatQuantifiedPeptidesAsProteins
                    && property.equals(DataHeaderProperty.DataSetType) && set.size() == 2)
            {
                String first = iterator.next();
                String second = iterator.next();
                if (equal(first, second, PROTEIN_LCMS_RATIO, QUANTIFIED_PEPTIDES))
                {
                    return PROTEIN_LCMS_RATIO;
                }
            }
            String message =
                    String.format("Inconsistent header values of '%s'. "
                            + "Expected the same value in all the columns, found: [%s].", property
                            .name(), StringUtils.join(set, HeaderUtils.LIST_SEPARATOR));
            throw new UserFailureException(message);
        }
        List<String> list = new ArrayList<String>(set);
        Collections.sort(list, new Comparator<String>()
            {
                public int compare(String s1, String s2)
                {
                    try
                    {
                        int i1 = Integer.parseInt(s1);
                        int i2 = Integer.parseInt(s2);
                        return i1 - i2;
                    } catch (NumberFormatException ex)
                    {
                        return s1.compareTo(s2);
                    }
                }
            });
        return join(list);
    }

    static String join(List<String> list)
    {
        return join(list, HeaderUtils.LIST_SEPARATOR, 200);
    }
    
    @Private static String join(List<String> list, String separator, int maxSize)
    {
        String lastElement = list.get(list.size() - 1);
        int maxSizeWithoutLastElement = maxSize - separator.length() - lastElement.length();
        int n = 0;
        int totalLength = 0;
        for (; n < list.size() - 1; n++)
        {
            int length = calculateLengthOfElement(list, n, separator);
            totalLength += length;
            if (totalLength > maxSizeWithoutLastElement)
            {
                totalLength -= length;
                length = (n == 0 ? 0 : separator.length()) + DOTS.length();
                if (totalLength + length > maxSizeWithoutLastElement)
                {
                    n--;
                }
                break;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++)
        {
            if (builder.length() > 0)
            {
                builder.append(separator);
            }
            builder.append(list.get(i));
        }
        if (n < list.size() - 1)
        {
            if (builder.length() > 0)
            {
                builder.append(separator);
            }
            builder.append(DOTS);
        }
        builder.append(separator).append(lastElement);
        return builder.toString();
    }

    private static int calculateLengthOfElement(List<String> list, int i, String separator)
    {
        return (i == 0 ? 0 : separator.length()) + list.get(i).length();
    }

    private static boolean equal(String first, String second, String expectedFirst,
            String expectedSecond)
    {
        return first.equals(expectedFirst) && second.equals(expectedSecond)
                || first.equals(expectedSecond) && second.equals(expectedFirst);
    }

    /**
     * Extracts a list of "time series" data sets properties defined in the tsv files located in
     * chosen directory.
     */
    public static List<NewProperty> extractHeaderProperties(File dir, boolean ignoreEmptyLines)
    {
        return extractHeaderProperties(dir, ignoreEmptyLines, false, false);
    }

    /**
     * Extracts a list of "time series" data sets properties defined in the tsv files located in
     * chosen directory.
     * 
     * @param ignoreTimePoint If <code>true</code> ignore time point in header. Time point will be
     *            set to 0.
     */
    public static List<NewProperty> extractHeaderProperties(File dir, boolean ignoreEmptyLines,
            boolean ignoreHashedLines, boolean ignoreTimePoint)
    {

        Collection<DataColumnHeader> headers = loadHeaders(dir, ignoreEmptyLines, ignoreHashedLines, ignoreTimePoint);
        Map<DataHeaderProperty, Set<String>> values = extractHeaderPropertyValues(headers);

        List<NewProperty> headerProperties = new ArrayList<NewProperty>();
        for (TimeSeriesPropertyType pt : TIME_SERIES_HEADER_PROPERTIES)
        {
            headerProperties.add(extractProperty(pt, ignoreTimePoint, values));
        }
        return headerProperties;
    }

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

}
