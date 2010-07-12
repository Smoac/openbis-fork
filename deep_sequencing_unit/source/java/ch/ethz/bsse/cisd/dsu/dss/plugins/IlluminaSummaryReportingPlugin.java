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

package ch.ethz.bsse.cisd.dsu.dss.plugins;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin which shows numbers of the Summary file generated from the Illumina Sequencer.
 * 
 * @author Manuel Kohler
 */
public class IlluminaSummaryReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final long serialVersionUID = 1L;

    private static final String SUMMARY_FILE_NAME = "Summary.xml";

    public IlluminaSummaryReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("Sample Code");
        builder.addHeader("Clusters");
        builder.addHeader("Clusters (PF)");
        builder.addHeader("Yield (kbases)");
        for (DatasetDescription dataset : datasets)
        {
            File originalData = getDataSubDir(dataset);
            File summaryFile = extractSummaryFile(dataset, originalData);
            describe(builder, dataset, summaryFile);
        }
        return builder.getTableModel();
    }

    private static File extractSummaryFile(DatasetDescription dataset, File originalData)
    {
        List<File> files = new ArrayList<File>();
        FileUtilities.findFiles(originalData, files, createIlluminaSummaryFileFilter());
        int size = files.size();
        if (size == 1)
        {
            return files.get(0);
        } else
        {
            throw new EnvironmentFailureException(String.format(
                    "%s file was found for the dataset %s (%s).", (size == 0) ? "No summary"
                            : " More than one", dataset.getDatasetCode(), dataset.getSampleCode()));
        }
    }

    private static void describe(SimpleTableModelBuilder builder, DatasetDescription dataset,
            File summaryFile)
    {
        IlluminaSummary summary = IlluminaSummaryXMLLoader.readSummaryXML(summaryFile, false);
        describeSummary(builder, dataset, summary);
    }

    private static void describeSummary(SimpleTableModelBuilder builder,
            DatasetDescription dataset, IlluminaSummary summary)
    {
        ChipResultsSummary chipResultSummary = summary.getChipResultsSummary();
        List<ISerializableComparable> row =
                Arrays.<ISerializableComparable> asList(
                        new StringTableCell(dataset.getSampleCode()), new IntegerTableCell(
                                chipResultSummary.getClusterCountPF()), new IntegerTableCell(
                                chipResultSummary.getClusterCountRaw()), new IntegerTableCell(
                                chipResultSummary.getYield()));
        builder.addRow(row);
    }

    private static FileFilter createIlluminaSummaryFileFilter()
    {
        return new FileFilter()
            {
                public boolean accept(File file)
                {
                    return file.isFile() && file.getName().equals(SUMMARY_FILE_NAME);
                }
            };
    }

}
