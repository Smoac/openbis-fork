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
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AutoResolveUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Shows the data as a table if one 'main data set' file exists, matches the selected dataset
 * pattern and is a 'tsv' file.
 * 
 * @author Izabela Adamczyk
 */
public class TSVViewReportingPlugin extends AbstractFileTableReportingPlugin
{
    private static final long serialVersionUID = 1L;

    public TSVViewReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, TAB_SEPARATOR);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        assureOnlyOneDataSetSelected(datasets);
        DatasetDescription dataset = datasets.get(0);
        File root = getDatasetDir(dataset);
        File fileToOpenOrNull =
                tryFindFileToOpen(dataset.getMainDataSetPattern(), dataset.getMainDataSetPath(),
                        root);
        if (fileToOpenOrNull != null && fileToOpenOrNull.isFile() && fileToOpenOrNull.exists())
        {
            DatasetFileLines lines = loadFromFile(dataset, fileToOpenOrNull);
            return createTableModel(lines);
        }
        throw UserFailureException.fromTemplate("Main TSV file could not be found.");
    }

    private File tryFindFileToOpen(String pattern, String path, File root)
    {
        List<File> patternMatchinFiles =
                AutoResolveUtils.findSomeMatchingFiles(root, path, pattern);
        File mainDataSetFile = null;
        if (patternMatchinFiles.size() == 1)
        {
            mainDataSetFile = patternMatchinFiles.get(0);
        }
        return mainDataSetFile;
    }

    private void assureOnlyOneDataSetSelected(List<DatasetDescription> datasets)
    {
        if (datasets.size() != 1)
        {
            throw UserFailureException.fromTemplate(
                    "Chosen plugin works with exactly one data set. %s data sets selected.",
                    datasets.size());
        }
    }
}
