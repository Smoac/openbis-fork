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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.ParsingException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Common super class of all tsv-based data merging reporting plugins.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractDataMergingReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final String FILE_INCLUDE_PATTERN = "file-include-pattern";

    private static final String FILE_EXCLUDE_PATTERN = "file-exclude-pattern";

    /** pattern for files that should be excluded (e.g. data set properties files) */
    public final static String DEFAULT_EXCLUDED_FILE_NAMES_PATTERN = ".*\\.tsv";

    private final String excludePattern;

    private final String includePatternOrNull;

    protected AbstractDataMergingReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        final String excludePatternOrNull = properties.getProperty(FILE_EXCLUDE_PATTERN);
        if (excludePatternOrNull == null)
        {
            this.excludePattern = DEFAULT_EXCLUDED_FILE_NAMES_PATTERN;
        } else
        {
            this.excludePattern = excludePatternOrNull;
        }
        this.includePatternOrNull = properties.getProperty(FILE_INCLUDE_PATTERN);
    }

    protected String[] getHeaderTitles(DatasetDescription dataset)
    {
        File dir = getDataSubDir(dataset);
        final DatasetFileLines lines = loadFromDirectory(dataset, dir);
        return lines.getHeaderTokens();
    }

    protected static void addDataRows(SimpleTableModelBuilder builder, DatasetDescription dataset,
            List<String[]> dataLines)
    {
        String datasetCode = dataset.getDatasetCode();
        for (String[] dataTokens : dataLines)
        {
            addDataRow(builder, datasetCode, dataTokens);
        }
    }

    protected static void addDataRow(SimpleTableModelBuilder builder, String datasetCode,
            String[] dataTokens)
    {
        List<String> row = new ArrayList<String>();
        row.add(datasetCode);
        row.addAll(Arrays.asList(dataTokens));
        builder.addRow(row);
    }

    /**
     * Loads {@link DatasetFileLines} from the file found in the specified directory.
     * 
     * @throws IOExceptionUnchecked if a {@link IOException} has occurred.
     */
    protected DatasetFileLines loadFromDirectory(DatasetDescription dataset, final File dir)
            throws ParserException, ParsingException, IllegalArgumentException,
            IOExceptionUnchecked
    {
        assert dir != null : "Given file must not be null";
        assert dir.isDirectory() : "Given file '" + dir.getAbsolutePath() + "' is not a directory.";

        File[] datasetFiles = FileUtilities.listFiles(dir);
        List<File> datasetFilesToMerge = new ArrayList<File>();
        for (File datasetFile : datasetFiles)
        {
            if (datasetFile.isDirectory())
            {
                // recursively go down the directories
                return loadFromDirectory(dataset, datasetFile);
            } else
            {
                // exclude files with properties
                if (isFileExcluded(datasetFile) == false)
                {
                    datasetFilesToMerge.add(datasetFile);
                }
            }

        }
        if (datasetFilesToMerge.size() != 1)
        {
            throw UserFailureException
                    .fromTemplate(
                            "Directory with Data Set '%s' data ('%s') should contain exactly 1 file with data but %s files were found.",
                            dataset.getDatasetCode(), dir.getAbsolutePath(), datasetFilesToMerge
                                    .size());
        } else
        {
            return loadFromFile(dataset, datasetFilesToMerge.get(0));
        }
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

        final List<String> lines = IOUtils.readLines(reader);
        return new DatasetFileLines(file, dataset, lines);
    }

    protected boolean isFileExcluded(File file)
    {
        if (includePatternOrNull != null)
        {
            return file.getName().matches(includePatternOrNull) == false;
        } else
        {
            return file.getName().matches(excludePattern);
        }
    }

    private static final long serialVersionUID = 1L;

}
