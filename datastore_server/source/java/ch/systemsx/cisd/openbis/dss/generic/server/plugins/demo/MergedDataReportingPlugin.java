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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.ParsingException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.TableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin that concatenates tabular files of all data sets (stripping the header lines of
 * all but the first file) and delivers the result back in the table model. Each row has additional
 * Data Set code column.
 * 
 * @author Piotr Buczek
 */
public class MergedDataReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    public MergedDataReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        TableModelBuilder builder = new TableModelBuilder();
        builder.addHeader("Data Set Code", TableModelColumnType.TEXT);
        if (datasets.isEmpty() == false)
        {
            final DatasetDescription firstDataset = datasets.get(0);
            final String[] titles = getHeaderTitles(firstDataset);
            for (String title : titles)
            {
                builder.addHeader(title, TableModelColumnType.TEXT);
            }
            for (DatasetDescription dataset : datasets)
            {
                final File dir = getOriginalDir(dataset);
                final DatasetFileLines lines = SimpleTabFileLoader.loadFromDirectory(dataset, dir);
                if (Arrays.equals(titles, lines.getHeaderTokens()) == false)
                {
                    throw UserFailureException.fromTemplate(
                            "All Data Set files should have the same headers, "
                                    + "but file header of '%s': \n\t '%s' "
                                    + "is different than file header of '%s': \n\t '%s'.",
                            firstDataset.getDatasetCode(), StringUtils.join(titles, "\t"), dataset
                                    .getDatasetCode(), StringUtils.join(lines.getHeaderTokens(),
                                    "\t"));
                }
                addDataRow(builder, dataset, lines.getDataTokens());
            }
        }

        return builder.getTableModel();
    }

    private String[] getHeaderTitles(DatasetDescription dataset)
    {
        File dir = getOriginalDir(dataset);
        DatasetFileLines lines = SimpleTabFileLoader.loadFromDirectory(dataset, dir);
        return lines.getHeaderTokens();
    }

    private static void addDataRow(TableModelBuilder builder, DatasetDescription dataset,
            String[] dataTokens)
    {
        String datasetCode = dataset.getDatasetCode();
        List<String> row = new ArrayList<String>();
        row.add(datasetCode);
        row.addAll(Arrays.asList(dataTokens));
        builder.addRow(row);
    }

    private static class SimpleTabFileLoader
    {
        /**
         * Loads {@link DatasetFileLines} from the file found in the specified directory.
         * 
         * @throws IOExceptionUnchecked if a {@link IOException} has occurred.
         */
        private static DatasetFileLines loadFromDirectory(DatasetDescription dataset, final File dir)
                throws ParserException, ParsingException, IllegalArgumentException,
                IOExceptionUnchecked
        {
            assert dir != null : "Given file must not be null";
            assert dir.isDirectory() : "Given file '" + dir.getAbsolutePath()
                    + "' is not a directory.";

            File[] datasetFiles = FileUtilities.listFiles(dir);
            assert datasetFiles.length == 1 : "Given directory '" + dir.getAbsolutePath()
                    + "should contain exactly 1 file instead of " + datasetFiles.length;

            return loadFromFile(dataset, datasetFiles[0]);
        }

        /**
         * Loads {@link DatasetFileLines} from the specified tab file.
         * 
         * @throws IOExceptionUnchecked if a {@link IOException} has occurred.
         */
        private static DatasetFileLines loadFromFile(DatasetDescription dataset, final File file)
                throws ParserException, ParsingException, IllegalArgumentException,
                IOExceptionUnchecked
        {
            assert file != null : "Given file must not be null";
            assert file.isFile() : "Given file '" + file.getAbsolutePath() + "' is not a file.";

            FileReader reader = null;
            try
            {
                reader = new FileReader(file);
                return load(dataset, reader);
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
        private static DatasetFileLines load(DatasetDescription dataset, final Reader reader)
                throws ParserException, ParsingException, IllegalArgumentException, IOException
        {
            assert reader != null : "Unspecified reader";

            final List<String> lines = IOUtils.readLines(reader);
            return new DatasetFileLines(dataset, lines);
        }
    }

    private static class DatasetFileLines
    {
        private final String[] headerTokens;

        private final String[] dataTokens;

        public DatasetFileLines(DatasetDescription dataset, List<String> lines)
        {
            super();
            if (lines.size() != 2)
            {
                throw UserFailureException.fromTemplate(
                        "Data Set '%s' file has has wrong number of lines (%s instead of 2).",
                        dataset.getDatasetCode(), lines.size());
            }
            this.headerTokens = parseLine(lines.get(0));
            this.dataTokens = parseLine(lines.get(1));
            if (headerTokens.length != dataTokens.length)
            {
                throw UserFailureException.fromTemplate(
                        "Number of columns in header (%s) does not match number of columns "
                                + "in data row (%s) in Data Set '%s' file.", headerTokens.length,
                        dataTokens.length, dataset.getDatasetCode());
            }
        }

        /** splits line with '\t' and strips quotes from every token */
        private String[] parseLine(String line)
        {
            String[] tokens = line.split("\t");
            return StringUtils.stripAll(tokens, "'\"");
        }

        public String[] getHeaderTokens()
        {
            return headerTokens;
        }

        public String[] getDataTokens()
        {
            return dataTokens;
        }

    }

}
