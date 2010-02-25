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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Stores the lines of a tsv file.
 * 
 * @author Bernd Rinn
 */
public class DatasetFileLines
{
    private final String[] headerTokens;

    private final List<String[]> dataLines;

    private final String separator;

    private final File file;

    public DatasetFileLines(File file, DatasetDescription dataset, List<String> lines,
            String separator)
    {
        this.file = file;
        this.separator = separator;
        if (lines.size() < 2)
        {
            throw UserFailureException.fromTemplate(
                    "Data Set '%s' file should have at least 2 lines instead of %s.", dataset
                            .getDatasetCode(), lines.size());
        }
        this.headerTokens = parseLine(lines.get(0));
        dataLines = new ArrayList<String[]>(lines.size());
        for (int i = 1; i < lines.size(); i++)
        {
            if (StringUtils.isNotBlank(lines.get(i)))
            {
                String[] dataTokens = parseLine(lines.get(i));
                if (headerTokens.length != dataTokens.length)
                {
                    throw UserFailureException.fromTemplate(
                            "Number of columns in header (%s) does not match number of columns "
                                    + "in %d data row (%s) in Data Set '%s' file.",
                            headerTokens.length, i, dataTokens.length, dataset.getDatasetCode());
                }
                dataLines.add(dataTokens);
            }
        }
    }

    /** splits line with '\t' and strips quotes from every token */
    private String[] parseLine(String line)
    {
        String[] tokens = line.split(separator, -1);
        return StringUtils.stripAll(tokens, "'\"");
    }

    public final File getFile()
    {
        return file;
    }

    public String[] getHeaderTokens()
    {
        return headerTokens;
    }

    public List<String[]> getDataLines()
    {
        return dataLines;
    }

}