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

package ch.systemsx.cisd.etlserver.utils;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Helper class for get table data out of a TSV file.
 *
 * @author Franz-Josef Elmer
 */
public class TabSeparatedValueTable
{
    private final LineIterator lineIterator;
    private final List<String> headers;

    /**
     * Creates an instance for the specified reader. The constructor already reads the header line.
     * It will be immediately available via {@link #getHeaders()}.
     * 
     * @param reader Reader pointing to the header line of the table.
     * @param nameOfReadingSource Source (usually file name) from which the table will be read. This
     *            is used for error messages only.
     * @throws IllegalArgumentException if there is not at least a header line.
     */
    public TabSeparatedValueTable(Reader reader, String nameOfReadingSource)
    {
        lineIterator = IOUtils.lineIterator(reader);
        if (lineIterator.hasNext() == false)
        {
            throw new IllegalArgumentException("Empty file '" + nameOfReadingSource + "'.");
        }
        headers = getRowCells(lineIterator.nextLine());
    }
    
    /**
     * Returns header line.
     */
    public List<String> getHeaders()
    {
        return headers;
    }

    /**
     * Returns all columns. This method should not be invoked if {@link #tryToGetNextRow()} has
     * already been invoked.
     */
    public List<Column> getColumns()
    {
        List<Column> columns = new ArrayList<Column>(headers.size());
        for (String header : headers)
        {
            columns.add(new Column(header));
        }
        while (hasMoreRows())
        {
            List<String> row = tryToGetNextRow();
            for (int i = 0, n = columns.size(); i < n; i++)
            {
                Column column = columns.get(i);
                column.add(i < row.size() ? row.get(i) : "");
            }
        }
        return columns;
    }
    
    /**
     * Returns <code>true</code> if more rows available. This method returns always <code>false</code>
     * if {@link #getColumns()} has already been invoked.
     */
    public boolean hasMoreRows()
    {
        return lineIterator.hasNext();
    }
    
    /**
     * Returns the next row as a list of its cell values.
     * 
     * @return <code>null</code> if there are no more rows.
     */
    public List<String> tryToGetNextRow()
    {
        String line = lineIterator.nextLine();
        return line == null ? null : getRowCells(line);
    }
    
    private List<String> getRowCells(String line)
    {
        String[] cells = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "\t");
        return cells == null ? Collections.<String>emptyList() : Arrays.asList(cells);
    }


}
