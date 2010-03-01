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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
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
    protected static final String TAB_SEPARATOR = "\t";

    protected static final String SEMICOLON_SEPARATOR = "\t";

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR_PROPERTY_KEY = "separator";

    private static final String IGNORE_COMMENTS_PROPERTY_KEY = "ignore-comments";

    // if the line starts with this character and comments should be ignored, the line is ignored
    private static final String COMMENT = "#";

    private final String separator;

    private final boolean ignoreComments;

    protected AbstractFileTableReportingPlugin(Properties properties, File storeRoot,
            String defaultSeparator)
    {
        super(properties, storeRoot);
        this.separator =
                PropertyUtils.getProperty(properties, SEPARATOR_PROPERTY_KEY, defaultSeparator);
        this.ignoreComments =
                PropertyUtils.getBoolean(properties, IGNORE_COMMENTS_PROPERTY_KEY, true);

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

        FileReader reader = null;
        try
        {
            reader = new FileReader(file);
            return load(dataset, reader, file);
        } catch (final IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Loads data from the specified reader.
     * 
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected DatasetFileLines load(final DatasetDescription dataset, final Reader reader,
            final File file) throws ParserException, ParsingException, IllegalArgumentException,
            IOException
    {
        assert reader != null : "Unspecified reader";

        List<String> lines = IOUtils.readLines(reader);
        if (ignoreComments)
        {
            lines = filterCommentedLines(lines);
        }
        return new DatasetFileLines(file, dataset, lines, separator);
    }

    private static List<String> filterCommentedLines(List<String> lines)
    {
        List<String> result = new ArrayList<String>();
        for (String line : lines)
        {
            if (line.trim().startsWith(COMMENT) == false)
            {
                result.add(line);
            }
        }
        return result;
    }

    protected static TableModel createTableModel(DatasetFileLines lines)
    {
        SimpleTableModelBuilder tableBuilder = new SimpleTableModelBuilder();
        for (String title : lines.getHeaderTokens())
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
            tableBuilder.addRow(row);
        }
        return tableBuilder.getTableModel();
    }
}
