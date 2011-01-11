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
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractTableModelReportingPlugin;
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
 * Reporting plugin which shows numbers of the Summary.xml file generated from the Illumina
 * Sequencer. The structure of the Summary file has changed from Casava 1.6 to 1.7 so some XML
 * elements are not available in the old files.
 * 
 * @author Manuel Kohler
 */
public class IlluminaSummaryReportingPlugin extends AbstractTableModelReportingPlugin
{
    private static final int MEGA = 1000000;

    private static final int KILO = 1000;

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
        builder.addHeader("Yield (Mbases)");
        builder.addHeader("Density Ratio");
        builder.addHeader("PhiX: Clusters");
        builder.addHeader("PhiX: ClustersPF");
        builder.addHeader("PhiX: Yield (Mbases)");
        builder.addHeader("PhiX: % Align (PF)");

        builder.addHeader("Software");
        builder.addHeader("Eland finished");
        for (String property : PROPERTIES)
        {
            builder.addHeader(property);
        }

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

            // just take the first GERALD folder which was found
            File geraldDir = files[0];
            File summaryFile = new File(geraldDir, SUMMARY_FILE_NAME);
            describe(builder, dataset, summaryFile);
        }
        return builder.getTableModel();
    }

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
        LaneResultsSummary laneResultSummary = summary.getLaneResultsSummary();
        // ChipSummary chipSummary = summary.getChipSummary();

        String software_version = summary.getSoftware();
        if (software_version == null)
        {
            software_version = "Not available";
        }

        // TODO 2010-10-20, Manuel Kohler : Cover Paired end runs

        List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
        row.add(new StringTableCell(dataset.getSampleCode()));
        row.add(new IntegerTableCell(chipResultSummary.getClusterCountRaw()));
        row.add(new IntegerTableCell(chipResultSummary.getClusterCountPF()));
        row.add(new IntegerTableCell(chipResultSummary.getYield() / MEGA));
        row.add(new DoubleTableCell(chipResultSummary.getDensityRatio()));

        // PhiX Lane
        row.add(new IntegerTableCell(laneResultSummary.getRead().getLanes().get(4)
                .getClusterCountRaw().getMean()));
        row.add(new IntegerTableCell(laneResultSummary.getRead().getLanes().get(4)
                .getClusterCountPF().getMean()));
        row.add(new IntegerTableCell(laneResultSummary.getRead().getLanes().get(4).getLaneYield()
                / KILO));
        row.add(new DoubleTableCell(laneResultSummary.getRead().getLanes().get(4)
                .getPercentUniquelyAlignedPF().getMean()));

        row.add(new StringTableCell(software_version));
        row.add(new StringTableCell(summary.getDate()));
        addPropertyColumnValues(dataset, row);

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
        String spaceCode = dataset.getSpaceCode();
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

    /**
     * Loader of Illumina summary XML file.
     * <p>
     * NOTE: This is not thread safe as it holds {@link JaxbXmlParser} singleton. As long as it is
     * used only by {@link IlluminaSummaryReportingPlugin} it will work correctly because we only
     * use a singleton of each reporting plugin.
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
