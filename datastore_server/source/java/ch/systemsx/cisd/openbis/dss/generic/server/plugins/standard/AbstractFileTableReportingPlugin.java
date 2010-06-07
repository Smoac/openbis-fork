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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.ParsingException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.TableCellUtil;

/**
 * Abstract class for reporting plugins which read from CSV or TSV files.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractFileTableReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    protected static final char TAB_SEPARATOR = '\t';

    protected static final char SEMICOLON_SEPARATOR = ';';

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR_PROPERTY_KEY = "separator";

    private static final String IGNORE_COMMENTS_PROPERTY_KEY = "ignore-comments";
    
    private static final String IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY = "ignore-trailing-empty-cells";

    // if the line starts with this character and comments should be ignored, the line is ignored
    private static final char COMMENT = '#';

    private final char separator;

    private final boolean ignoreComments;

    private final boolean ignoreTrailingEmptyCells;

    protected AbstractFileTableReportingPlugin(Properties properties, File storeRoot,
            char defaultSeparator)
    {
        super(properties, storeRoot);
        this.separator =
                PropertyUtils.getChar(properties, SEPARATOR_PROPERTY_KEY, defaultSeparator);
        this.ignoreComments =
                PropertyUtils.getBoolean(properties, IGNORE_COMMENTS_PROPERTY_KEY, true);
        ignoreTrailingEmptyCells =
                PropertyUtils.getBoolean(properties, IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY,
                        false);

    }

    /**
     * Loads {@link DatasetFileLines} from the specified tab file.
     * 
     * @throws IOExceptionUnchecked if a {@link IOException} has occurred.
     */
    protected DatasetFileLines loadFromFile(DatasetDescription dataset, final File file)
            throws ParserException, ParsingException, IllegalArgumentException,
            IOExceptionUnchecked
    {
        assert file != null : "Given file must not be null";
        assert file.isFile() : "Given file '" + file.getAbsolutePath() + "' is not a file.";

        CsvReader reader = null;
        try
        {
            reader = readFile(file, ignoreComments, separator);
            return load(dataset, reader, file);
        } catch (final IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        } finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    private static CsvReader readFile(File file, boolean ignoreComments, char separator)
            throws IOException
    {
        if (file.isFile() == false)
        {
            throw new UserFailureException(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(separator);
        csvReader.setSkipEmptyRecords(true);
        if (ignoreComments)
        {
            csvReader.setUseComments(true);
            csvReader.setComment(COMMENT);
        }
        return csvReader;
    }

    /**
     * Loads data from the specified reader.
     * 
     * @throws IOException
     */
    protected DatasetFileLines load(final DatasetDescription dataset, final CsvReader reader,
            final File file) throws ParserException, ParsingException, IllegalArgumentException,
            IOException
    {
        assert reader != null : "Unspecified reader";

        List<String[]> lines = new ArrayList<String[]>();
        while (reader.readRecord())
        {
            lines.add(reader.getValues());
        }
        return new DatasetFileLines(file, dataset, lines);
    }

    protected TableModel createTableModel(DatasetFileLines lines)
    {
        SimpleTableModelBuilder tableBuilder = new SimpleTableModelBuilder();
        String[] headerTokens = lines.getHeaderTokens();
        for (String title : headerTokens)
        {
            tableBuilder.addHeader(title);
        }
        for (String[] line : lines.getDataLines())
        {
            List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
            for (String token : line)
            {
                row.add(TableCellUtil.createTableCell(token));
            }
            if (ignoreTrailingEmptyCells)
            {
                while (row.size() > headerTokens.length)
                {
                    ISerializableComparable cell = row.get(row.size() - 1);
                    if (cell.toString().length() > 0)
                    {
                        break;
                    }
                    row.remove(row.size() - 1);
                }
            }
            tableBuilder.addRow(row);
        }
        return tableBuilder.getTableModel();
    }

    protected static TableModel createTransposedTableModel(DatasetFileLines lines)
    {
        int columns = lines.getHeaderTokens().length;
        int rows = lines.getDataLines().size() + 1;
        String[][] all = new String[columns][rows];
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < columns; c++)
            {
                all[c][r] =
                        (r == 0) ? lines.getHeaderTokens()[c] : lines.getDataLines().get(r - 1)[c];
            }
        }
        SimpleTableModelBuilder tableBuilder = new SimpleTableModelBuilder();
        for (int r = 0; r < rows; r++)
        {
            tableBuilder.addHeader(all[0][r]);
        }
        for (int c = 1; c < columns; c++)
        {
            List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
            for (int r = 0; r < rows; r++)
            {
                row.add(TableCellUtil.createTableCell(all[c][r]));
            }
            tableBuilder.addRow(row);
        }
        return tableBuilder.getTableModel();
    }
}
