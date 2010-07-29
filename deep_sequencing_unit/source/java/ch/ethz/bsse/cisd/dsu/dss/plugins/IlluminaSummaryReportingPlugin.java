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
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.xml.JaxbXmlParser;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

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

    private static final String DATA_INTENSITIES_BASE_CALLS_PATH = "/Data/Intensities/BaseCalls";

    private static final String GERALD_DIR = "GERALD";

    private static final String[] PROPERTIES =
        { "GENOME_ANALYZER", "END_TYPE", "ILLUMINA_PIPELINE_VERSION",
                "CYCLES_REQUESTED_BY_CUSTOMER" };

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
        builder.addHeader("Software");
        for (String property : PROPERTIES)
        {
            builder.addHeader(property);
        }
        builder.addHeader("PhiX: Clusters");
        builder.addHeader("PhiX: ClustersPF");
        builder.addHeader("PhiX: Yield (kbases)");
        builder.addHeader("PhiX: % Align (PF)");
        for (DatasetDescription dataset : datasets)
        {
            File originalData = getDataSubDir(dataset);

            // set the directory containing the Summary.xml
            File childDirectory =
                    new File(originalData, dataset.getSampleCode()
                            + DATA_INTENSITIES_BASE_CALLS_PATH);
            File[] files = childDirectory.listFiles(new FileFilter()
                {
                    public boolean accept(File file)
                    {
                        return file.isDirectory() && file.getName().startsWith(GERALD_DIR);
                    }
                });

            System.out.println(files[0]);
            if (files.length == 1)
            {
                File geraldDir = files[0];
                File summaryFile = new File(geraldDir, SUMMARY_FILE_NAME);
                describe(builder, dataset, summaryFile);
            } else
            {
                // throw new EnvironmentFailureException(String.format("More than one ..."));
            }
            // if (childDirectory.exists())
            // {
            // File summaryFile = extractSummaryFile(dataset, childDirectory);
            // describe(builder, dataset, summaryFile);
            // } else
            // {
            // File summaryFile = extractSummaryFile(dataset, originalData);
            // }
        }
        return builder.getTableModel();
    }

    // private static File extractSummaryFile(DatasetDescription dataset, File originalData)
    // {
    // List<File> files = new ArrayList<File>();
    // FileUtilities.findFiles(originalData, files, createIlluminaSummaryFileFilter());
    // int size = files.size();
    // if (size == 1)
    // {
    // return files.get(0);
    // } else
    // {
    // throw new EnvironmentFailureException(String.format(
    // "%s file was found for the dataset %s (%s).", (size == 0) ? "No summary"
    // : " More than one", dataset.getDatasetCode(), dataset.getSampleCode()));
    // }
    // }

    private static void describe(SimpleTableModelBuilder builder, DatasetDescription dataset,
            File summaryFile)
    {
        IlluminaSummary summary = IlluminaSummaryXMLLoader.readSummaryXML(summaryFile);
        describeSummary(builder, dataset, summary);
    }

    private static void describeSummary(SimpleTableModelBuilder builder,
            DatasetDescription dataset, IlluminaSummary summary)
    {
        ChipResultsSummary chipResultSummary = summary.getChipResultsSummary();

        String software_version = summary.getSoftware();
        if (software_version == null)
        {
            software_version = "Not available";
        }

        List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
        row.add(new StringTableCell(dataset.getSampleCode()));
        row.add(new IntegerTableCell(chipResultSummary.getClusterCountRaw()));
        row.add(new IntegerTableCell(chipResultSummary.getClusterCountPF()));
        row.add(new IntegerTableCell(chipResultSummary.getYield() / 1000));
        row.add(new StringTableCell(software_version));
        addPropertyColumnValues(dataset, row);
        // just dummies
        row.add(new IntegerTableCell(1));
        row.add(new IntegerTableCell(1));
        row.add(new IntegerTableCell(1));
        row.add(new DoubleTableCell(1.0));
        builder.addRow(row);
    }

    private static void addPropertyColumnValues(DatasetDescription dataset,
            List<ISerializableComparable> row)
    {
        Sample sample = getSample(dataset);
        for (String propertyCode : PROPERTIES)
        {
            boolean found = false;
            for (IEntityProperty property : sample.getProperties())
            {
                if (property.getPropertyType().getCode().equals(propertyCode))
                {
                    row.add(new StringTableCell(property.tryGetAsString()));
                    found = true;
                    break;
                }
            }
            if (found == false)
            {
                row.add(new StringTableCell(""));
            }
        }
    }

    private static Sample getSample(DatasetDescription dataset)
    {
        String spaceCode = dataset.getGroupCode();
        String sampleCode = dataset.getSampleCode();
        String databaseInstanceCode = dataset.getDatabaseInstanceCode();
        SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new SpaceIdentifier(databaseInstanceCode, spaceCode),
                        sampleCode);
        Sample sampleOrNull =
                ServiceProvider.getOpenBISService().tryGetSampleWithExperiment(sampleIdentifier);
        if (sampleOrNull == null)
        {
            throw new EnvironmentFailureException(String.format(
                    "Couldn't get sample %s for dataset %s.", dataset.getSampleCode(),
                    dataset.getDatasetCode()));
        }
        return sampleOrNull;
    }

    // private static FileFilter createIlluminaSummaryFileFilter()
    // {
    // return new FileFilter()
    // {
    // public boolean accept(File file)
    // {
    // return file.isFile() && file.getName().equals(SUMMARY_FILE_NAME);
    // }
    // };
    // }

    /**
     * Loader of Illumina summary XML file.
     * <p>
     * NOTE: This is not thread safe as it holds {@link JaxbXmlParser} singleton. As long as it is
     * used only by {@link IlluminaSummaryReportingPlugin} it will work correctly because we only
     * use a singleto of each reporting plugin.
     * 
     * @author Piotr Buczek
     */
    static class IlluminaSummaryXMLLoader
    {
        // we use one instance
        private static JaxbXmlParser<IlluminaSummary> PARSER_INSTANCE =
                new JaxbXmlParser<IlluminaSummary>(IlluminaSummary.class, false);

        public static IlluminaSummary readSummaryXML(File summaryXml)
        {
            return PARSER_INSTANCE.doParse(summaryXml);
        }

    }
}
