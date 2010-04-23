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

package ch.systemsx.cisd.openbis.dss.genedata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class FeatureStorageProcessor extends DefaultStorageProcessor
{
    private static final char DELIMITER = ';';

    private static final String LAYER_PREFIX = "<Layer=";

    private File originalFile;

    private final class ColumnsBuilder
    {
        private final List<Column> columns = new ArrayList<Column>();

        private final List<String> rowLetters = new ArrayList<String>();

        private TableBuilder tableBuilder;

        private int numberOfColumns;

        public void startNewColumn(String columnName)
        {
            addColumn();
            tableBuilder = new TableBuilder(columnName);
        }

        public void checkColumnsLine(int lineIndex, String line)
        {
            int columnCount = new StringTokenizer(line).countTokens();
            if (numberOfColumns == 0)
            {
                numberOfColumns = columnCount;
            }
            if (numberOfColumns != columnCount)
            {
                throw error(lineIndex, line, "Inconsistent number of columns: Expected "
                        + numberOfColumns + " but was " + columnCount);
            }

        }

        public void addRow(int lineIndex, String line)
        {
            StringTokenizer tokenizer = new StringTokenizer(line);
            int countTokens = tokenizer.countTokens();
            if (countTokens != numberOfColumns + 1)
            {
                throw error(lineIndex, line, "Inconsistent number of columns: Expected "
                        + numberOfColumns + " but was " + (countTokens - 1));
            }
            String rowLetter = tokenizer.nextToken();
            if (rowLetters.contains(rowLetter) == false)
            {
                rowLetters.add(rowLetter);
            }
            while (tokenizer.hasMoreTokens())
            {
                if (tableBuilder != null)
                {
                    tableBuilder.addRow(tokenizer.nextToken());
                }
            }
        }

        public void finish()
        {
            addColumn();
        }

        public int getNumberOfWells()
        {
            return numberOfColumns * rowLetters.size();
        }

        public String getRowLetter(int index)
        {
            return rowLetters.get((index / numberOfColumns) % rowLetters.size());
        }

        public int getColumnNumber(int index)
        {
            return index % numberOfColumns + 1;
        }

        public List<Column> getColumns()
        {
            return columns;
        }

        private void addColumn()
        {
            if (tableBuilder != null)
            {
                columns.addAll(tableBuilder.getColumns());
            }
        }

    }

    public FeatureStorageProcessor(Properties properties)
    {
        super(properties);
    }

    @Override
    protected void transform(File originalDataSet, File targetFolderForTransformedDataSet)
    {
        originalFile = originalDataSet;
        List<String> lines = FileUtilities.loadToStringList(originalDataSet);
        if (lines.isEmpty())
        {
            throw new UserFailureException("Empty file: " + originalDataSet.getName());
        }
        String barCode = extractBarCode(lines.get(0));
        ColumnsBuilder columnsBuilder = new ColumnsBuilder();
        for (int i = 1; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();
            if (line.length() == 0)
            {
                continue;
            }
            if (line.startsWith(LAYER_PREFIX))
            {
                columnsBuilder.startNewColumn(extractLayer(line, i));
            } else if (line.startsWith("1"))
            {
                columnsBuilder.checkColumnsLine(i, line);
            } else
            {
                columnsBuilder.addRow(i, line);
            }
        }
        columnsBuilder.finish();
        List<Column> columns = columnsBuilder.getColumns();
        StringBuilder builder = new StringBuilder();
        builder.append("barcode").append(DELIMITER).append("row").append(DELIMITER).append("col");
        for (Column column : columns)
        {
            builder.append(DELIMITER).append(column.getHeader());
        }
        for (int i = 0, n = columnsBuilder.getNumberOfWells(); i < n; i++)
        {
            builder.append('\n').append(barCode);
            builder.append(DELIMITER).append(columnsBuilder.getRowLetter(i));
            builder.append(DELIMITER).append(columnsBuilder.getColumnNumber(i));
            for (Column column : columns)
            {
                builder.append(DELIMITER).append(column.getValues().get(i));
            }
        }
        File originalDirectory = getOriginalDirectory(targetFolderForTransformedDataSet);
        File file = new File(originalDirectory, originalDataSet.getName() + ".txt");
        FileUtilities.writeToFile(file, builder.toString());
    }

    @Override
    public void commit()
    {
        if (originalFile != null && originalFile.exists())
        {
            originalFile.delete();
        }
    }

    private String extractBarCode(String firstLine)
    {
        int indexOfEqual = firstLine.indexOf('=');
        if (indexOfEqual < 0)
        {
            throw error(0, firstLine, "Missing '='");
        }
        return firstLine.substring(indexOfEqual + 1).trim();
    }

    private String extractLayer(String line, int lineIndex)
    {
        String layer = line.substring(LAYER_PREFIX.length());
        if (layer.endsWith(">") == false)
        {
            throw error(lineIndex, line, "Missing '>' at the end");
        }
        return layer.substring(0, layer.length() - 1);
    }

    private UserFailureException error(int lineIndex, String line, String reason)
    {
        return new UserFailureException("Error in line " + lineIndex + 1 + ": " + reason + ": "
                + line);
    }

}
