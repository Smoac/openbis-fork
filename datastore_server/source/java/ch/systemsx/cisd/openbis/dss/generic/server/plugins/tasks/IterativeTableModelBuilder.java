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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnType;

/**
 * A table model builder that can take new columns and rows iteratively.
 * 
 * @author Bernd Rinn
 */
public class IterativeTableModelBuilder
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, IterativeTableModelBuilderTest.class);

    private final String rowIdentifierColumnHeader;

    /** Set of row identifiers. */
    private final Set<String> rowIdentifiers = new LinkedHashSet<String>();

    /** Map from column name to the map that maps row identifier to value. */
    private final Map<String, Map<String, String>> columnMap =
            new LinkedHashMap<String, Map<String, String>>();

    public IterativeTableModelBuilder(String rowIdentifierColumnHeader)
    {
        this.rowIdentifierColumnHeader = rowIdentifierColumnHeader;
    }

    private int findIndexOfIdentifierColumn(DatasetFileLines lines)
    {
        int idx = 0;
        for (String columnHeader : lines.getHeaderTokens())
        {
            if (rowIdentifierColumnHeader.equals(columnHeader))
            {
                return idx;
            }
            ++idx;
        }
        return -1;
    }

    public void addFile(DatasetFileLines lines)
    {
        final int colIndexOfRowId = findIndexOfIdentifierColumn(lines);
        if (colIndexOfRowId < 0)
        {
            operationLog.warn("Skip file '" + lines.getFile().getPath() + "' as it has no column '"
                    + rowIdentifierColumnHeader + "'.");
            return;
        }
        final String[] columnHeaders = lines.getHeaderTokens();
        int colIdx = 0;
        for (String columnHeader : columnHeaders)
        {
            // Does a column with that name already exist?
            Map<String, String> column = columnMap.get(columnHeader);
            if (column != null && colIdx == colIndexOfRowId)
            {
                addLineToColumn(lines, column, colIdx, colIndexOfRowId);
                ++colIdx;
                continue;
            }
            if (column != null)
            {
                columnHeader += "X";
            }
            column = new HashMap<String, String>();
            columnMap.put(columnHeader, column);
            addLineToColumn(lines, column, colIdx, colIndexOfRowId);
            ++colIdx;
        }
    }

    private void addLineToColumn(DatasetFileLines lines, Map<String, String> column, int colIdx,
            final int colIndexOfRowId)
    {
        for (String[] line : lines.getDataLines())
        {
            final String rowId = line[colIndexOfRowId];
            rowIdentifiers.add(rowId);
            column.put(rowId, line[colIdx]);
        }
    }

    /**
     * Returns the table model that was built iteratively.
     */
    public TableModel getTableModel()
    {
        final List<TableModelColumnHeader> headers =
                new ArrayList<TableModelColumnHeader>(columnMap.size());
        final List<TableModelRow> rows = new ArrayList<TableModelRow>(rowIdentifiers.size());
        int idx = 0;
        for (Entry<String, Map<String, String>> column : columnMap.entrySet())
        {
            headers.add(new TableModelColumnHeader(column.getKey(), TableModelColumnType.TEXT,
                    idx++));
        }
        for (String rowId : rowIdentifiers)
        {
            final List<String> rowValues = new ArrayList<String>(headers.size());
            for (TableModelColumnHeader header : headers)
            {
                final String valueOrNull = columnMap.get(header.getTitle()).get(rowId);
                rowValues.add((valueOrNull == null) ? "" : valueOrNull);
            }
            rows.add(new TableModelRow(rowValues));
        }
        return new TableModel(headers, rows);
    }
}
