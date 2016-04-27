/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;

/**
 * Builder for a {@link DataSet} based {@link TableModel}.
 *
 * @author Franz-Josef Elmer
 */
public class TableModelBuilder
{
    private static final class Column
    {
        private final String name;

        private final List<String> values = new ArrayList<String>();

        Column(String name)
        {
            this.name = name;
        }

        String getName()
        {
            return name;
        }

        int getSize()
        {
            return values.size();
        }

        String getValue(int rowIndex)
        {
            return rowIndex < getSize() ? values.get(rowIndex) : "";
        }

        void addValueAt(int rowIndex, String value)
        {
            while (values.size() < rowIndex)
            {
                values.add("");
            }
            values.add(value == null ? "" : value);
        }

        boolean isEmpty()
        {
            for (String value : values)
            {
                if (value.length() > 0)
                {
                    return false;
                }
            }
            return true;
        }

        boolean hasOnlyOneDistinctValue()
        {
            String distinctValue = null;
            for (String value : values)
            {
                if (distinctValue == null)
                {
                    distinctValue = value;
                } else if (distinctValue.equals(value) == false)
                {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class DataSetTableModel extends AbstractTableModel
    {
        private static final long serialVersionUID = 1L;

        private final List<Column> columns;

        private DataSetTableModel(List<Column> columns)
        {
            this.columns = columns;
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        @Override
        public int getRowCount()
        {
            int rowCount = 0;
            for (Column column : columns)
            {
                rowCount = Math.max(rowCount, column.getSize());
            }
            return rowCount;
        }

        @Override
        public int getColumnCount()
        {
            return columns.size();
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            return columns.get(columnIndex).getName();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return columns.get(columnIndex).getValue(rowIndex);
        }
    }

    private final Column codeColumn = new Column("Code");

    private final Column typeColumn = new Column("Type");

    private final Column experimentColumn = new Column("Experiment");

    private final Column sampleColumn = new Column("Sample");

    private final Map<String, Column> propertyColumns = new HashMap<String, Column>();

    private int numberOfAddedDataSets;

    public TableModel getTableModel()
    {
        final List<Column> columns = new ArrayList<Column>(propertyColumns.values());
        Collections.sort(columns, new Comparator<Column>()
            {
                @Override
                public int compare(Column c1, Column c2)
                {
                    return c1.getName().compareTo(c2.getName());
                }
            });
        columns.add(0, experimentColumn);
        if (sampleColumn.isEmpty() == false)
        {
            columns.add(0, sampleColumn);
        }
        if (typeColumn.hasOnlyOneDistinctValue() == false)
        {
            columns.add(0, typeColumn);
        }
        columns.add(0, codeColumn);
        return new DataSetTableModel(columns);
    }

    public void add(DataSet dataSet)
    {
        codeColumn.addValueAt(numberOfAddedDataSets, dataSet.getCode());
        typeColumn.addValueAt(numberOfAddedDataSets, dataSet.getDataSetTypeCode());
        experimentColumn.addValueAt(numberOfAddedDataSets, dataSet.getExperimentIdentifier());
        sampleColumn.addValueAt(numberOfAddedDataSets, dataSet.getSampleIdentifierOrNull());
        HashMap<String, String> properties = dataSet.getProperties();
        Set<java.util.Map.Entry<String, String>> entrySet = properties.entrySet();
        for (Entry<String, String> entry : entrySet)
        {
            String key = entry.getKey();
            Column column = propertyColumns.get(key);
            if (column == null)
            {
                column = new Column(key);
                propertyColumns.put(key, column);
            }
            column.addValueAt(numberOfAddedDataSets, entry.getValue());
        }
        numberOfAddedDataSets++;
    }

}