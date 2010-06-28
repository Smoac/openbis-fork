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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;

/**
 * Helps in building a {@link TableModel}
 * 
 * @author Tomasz Pylak
 * @author Franz-Josef Elmer
 */
public class SimpleTableModelBuilder
{
    private final List<TableModelRow> rows;

    private final List<TableModelColumnHeader> headers;
    
    private final Map<String, Integer> titleToIndexMap;

    private final boolean uniqueHeaderTitles;

    /**
     * Creates a new instance with non-unique header titles allowed.
     */
    public SimpleTableModelBuilder()
    {
        this(false);
    }

    /**
     * Creates a new instance.
     * 
     * @param uniqueHeaderTitles If <code>true</code> header title must be unique
     */
    public SimpleTableModelBuilder(boolean uniqueHeaderTitles)
    {
        this.uniqueHeaderTitles = uniqueHeaderTitles;
        this.rows = new ArrayList<TableModelRow>();
        this.headers = new ArrayList<TableModelColumnHeader>();
        titleToIndexMap = new HashMap<String, Integer>();
    }

    /**
     * Adds header with specified title and default column width 150.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with
     *             same title has already been added.
     */
    public void addHeader(String title)
    {
        addHeader(title, 150);
    }
    
    /**
     * Adds header with specified title and specified default column width.
     * 
     * @throws UserFailureException if non-unique header titles are not allowed and a header with
     *             same title has already been added.
     */
    public void addHeader(String title, int defaultColumnWidth)
    {
        TableModelColumnHeader header = new TableModelColumnHeader(title, headers.size());
        header.setDefaultColumnWidth(defaultColumnWidth);
        Integer replacedValue = titleToIndexMap.put(title, headers.size());
        if (uniqueHeaderTitles && replacedValue != null)
        {
            throw new UserFailureException("There is already a header with title '" + title + "'.");
        }
        headers.add(header);
    }
    
    /**
     * Adds an empty row and returns a row builder for setting values of this row.
     */
    public IRowBuilder addRow()
    {
        final List<ISerializableComparable> values = new ArrayList<ISerializableComparable>();
        StringTableCell emptyCell = new StringTableCell("");
        for (int i = 0; i < headers.size(); i++)
        {
            values.add(emptyCell);
        }
        rows.add(new TableModelRow(values));
        return new IRowBuilder()
            {
                public void setCell(String headerTitle, String value)
                {
                    setCell(headerTitle, new StringTableCell(value));
                }

                public void setCell(String headerTitle, long value)
                {
                    setCell(headerTitle, new IntegerTableCell(value));
                }

                public void setCell(String headerTitle, double value)
                {
                    setCell(headerTitle, new DoubleTableCell(value));
                }

                public void setCell(String headerTitle, ISerializableComparable value)
                {
                    Integer index = titleToIndexMap.get(headerTitle);
                    if (index == null)
                    {
                        throw new UserFailureException("Unkown column header title: " + headerTitle);
                    }
                    values.set(index, value);
                    setColumnDataType(index, value);
                }
            };
    }

    /**
     * Adds specified list of values as a row.
     * 
     * @throws UserFailureException if more or less values than headers added.
     */
    public void addRow(List<ISerializableComparable> values)
    {
        if (values.size() != headers.size())
        {
            throw new UserFailureException(headers.size() + " row values expected instead of "
                    + values.size() + ".");
        }
        for (int i = 0; i < values.size(); i++)
        {
            ISerializableComparable value = values.get(i);
            setColumnDataType(i, value);
        }
        rows.add(new TableModelRow(values));
    }

    private void setColumnDataType(int index, ISerializableComparable value)
    {
        TableModelColumnHeader header = headers.get(index);
        DataTypeCode headerDataType = header.getDataType();
        DataTypeCode dataType = getDataTypeCodeFor(value);
        if (StringUtils.isNotBlank(value.toString()))
        {
            header.setDataType(DataTypeUtils.getCompatibleDataType(headerDataType, dataType));
        }
    }

    private DataTypeCode getDataTypeCodeFor(ISerializableComparable value)
    {
        if (value instanceof IntegerTableCell)
        {
            return DataTypeCode.INTEGER;
        }
        if (value instanceof DoubleTableCell)
        {
            return DataTypeCode.REAL;
        }
        if (value instanceof DateTableCell)
        {
            return DataTypeCode.TIMESTAMP;
        }
        return DataTypeCode.VARCHAR;
    }

    public TableModel getTableModel()
    {
        return new TableModel(headers, rows);
    }

    public static ISerializableComparable asText(String textOrNull)
    {
        if (textOrNull == null)
        {
            return createNullCell();
        }
        return new StringTableCell(textOrNull);
    }

    public static ISerializableComparable asNum(int num)
    {
        return new IntegerTableCell(num);
    }

    public static ISerializableComparable asNum(double num)
    {
        return new DoubleTableCell(num);
    }

    public static ISerializableComparable asDate(Date dateOrNull)
    {
        if (dateOrNull == null)
        {
            return createNullCell();
        }
        return new DateTableCell(dateOrNull);
    }

    private static ISerializableComparable createNullCell()
    {
        return new StringTableCell("");
    }
}
