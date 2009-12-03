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
import java.util.List;
import java.util.NoSuchElementException;

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
     * Returns all columns. This method returns only rows which are not consumed by previous
     * invocations of {@link #tryToGetNextRow()}.
     */
    public List<Column> getColumns()
    {
        TableBuilder builder = new TableBuilder(headers);
        while (hasMoreRows())
        {
            List<String> row = tryToGetNextRow();
            builder.addRow(row);
        }
        return builder.getColumns();
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
     * Returns the next row as a list of its cell values. Missing cells are returned as empty strings.
     * The size of returned list is the size of the header list.
     * 
     * @return <code>null</code> if there are no more rows.
     */
    public List<String> tryToGetNextRow()
    {
        try
        {
            List<String> row = getRowCells(lineIterator.nextLine().trim());
            for (int i = row.size(); i < headers.size(); i++)
            {
                row.add("");
            }
            return row;
        } catch (NoSuchElementException ex)
        {
            return null;
        }

    }
    
    private List<String> getRowCells(String line)
    {
        String[] cells = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "\t");
        if (cells == null)
        {
            cells = new String[0];
        }
        List<String> row = new ArrayList<String>();
        row.addAll(Arrays.asList(cells));
        return row;
    }


}
