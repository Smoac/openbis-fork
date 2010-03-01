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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDataMergingReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin that concatenates rows of tabular files of all data sets (stripping the header
 * lines of all but the first file) and delivers the result back in the table model. Each row has
 * additional Data Set code column.
 * 
 * @author Tomasz Pylak
 */
// Possible extensions in future: allow different files to have different headers, merge the same
// columns and fill columns with empty values if they do not occur in some files.
public class ImageAnalysisMergedRowsReportingPlugin extends AbstractDataMergingReportingPlugin
{

    private static final long serialVersionUID = 1L;

    public ImageAnalysisMergedRowsReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, SEMICOLON_SEPARATOR);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("Data Set Code");
        builder.addHeader("Plate");
        if (datasets.isEmpty() == false)
        {
            final DatasetDescription firstDataset = datasets.get(0);
            final String[] titles = getHeaderTitles(firstDataset);
            for (String title : titles)
            {
                builder.addHeader(title);
            }
            for (DatasetDescription dataset : datasets)
            {
                final File dir = getDataSubDir(dataset);
                final DatasetFileLines lines = loadFromDirectory(dataset, dir);
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
                addDataRows(builder, dataset, lines, true);
            }
        }
        return builder.getTableModel();
    }

}
