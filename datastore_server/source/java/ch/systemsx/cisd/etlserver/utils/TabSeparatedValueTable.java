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
import java.util.Iterator;
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
    private static final class RowLineIterator implements Iterator<String>
    {
        private final LineIterator lineIterator;
        private final boolean ignoreEmptyLines;
        
        private String currentLine;

        RowLineIterator(LineIterator lineIterator, boolean ignoreEmptyLines)
        {
            this.lineIterator = lineIterator;
            this.ignoreEmptyLines = ignoreEmptyLines;
            
        }
        public boolean hasNext()
        {
            if (currentLine == null)
            {
                currentLine = getNextLine();
            }
            return currentLine != null;
        }

        public String next()
        {
            if (currentLine == null)
            {
                currentLine = getNextLine();
            }
            try
            {
                return currentLine;
            } finally
            {
                currentLine = null;
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
        
        private String getNextLine()
        {
            while (true)
            {
                if (lineIterator.hasNext() == false)
                {
                    return null;
                }
                String line = lineIterator.nextLine();
                if (ignoreEmptyLines == false || line == null || StringUtils.isNotBlank(line))
                {
                    return line;
                }
            }
        }
    }
    
    private final RowLineIterator rowLineIterator;
    private final List<String> headers;

    /**
     * Creates an instance for the specified reader. The constructor already reads the header line.
     * It will be immediately available via {@link #getHeaders()}.
     * 
     * @param reader Reader pointing to the header line of the table.
     * @param nameOfReadingSource Source (usually file name) from which the table will be read. This
     *            is used for error messages only.
     * @param ignoreEmptyLines If <code>true</code> lines with only white spaces will be ignored.
     */
    public TabSeparatedValueTable(Reader reader, String nameOfReadingSource, boolean ignoreEmptyLines)
    {
        rowLineIterator = new RowLineIterator(IOUtils.lineIterator(reader), ignoreEmptyLines);
        if (rowLineIterator.hasNext() == false)
        {
            throw new IllegalArgumentException("Empty file '" + nameOfReadingSource + "'.");
        }
        headers = getRowCells(rowLineIterator.next().trim());
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
        return rowLineIterator.hasNext();
    }
    
    /**
     * Returns the next row as a list of its cell values. Missing cells are returned as empty strings.
     * The size of returned list is the size of the header list.
     * 
     * @return <code>null</code> if there are no more rows.
     */
    public List<String> tryToGetNextRow()
    {
        String line = rowLineIterator.next();
        if (line == null)
        {
            return null;
        }
        List<String> row = getRowCells(line);
        for (int i = row.size(); i < headers.size(); i++)
        {
            row.add("");
        }
        return row;
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
