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
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
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
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * Reporting plugin which shows numbers of the Summary.xml file generated from the Illumina
 * Sequencer. The structure of the Summary file has changed from Casava 1.6 to 1.7 so some XML
 * elements are not available in the old files.
 * 
 * @author Manuel Kohler
 */
public class IlluminaSummaryReportingPlugin extends AbstractTableModelReportingPlugin
{
    private static final String UNALIGNED_PATH = "/Unaligned_no_mismatch";

    private static final int MEGA = 1000000;

    private static final int KILO = 1000;

    private static final long serialVersionUID = 1L;

    private static final String SUMMARY_FILE_NAME = "Summary.xml";

    private static final String BUSTARD_SUMMARY_FILE_NAME = "BustardSummary.xml";

    private static final String DATA_INTENSITIES_BASE_CALLS_PATH = "/Data/Intensities/BaseCalls";

    private static final String GERALD_DIR = "GERALD";

    private static final String BASECALL_DIR = "Basecall";

    private static final String[] PROPERTIES =
        { "GENOME_ANALYZER", "END_TYPE", "ILLUMINA_PIPELINE_VERSION",
                "CYCLES_REQUESTED_BY_CUSTOMER" };

    private static final String[] COLUMNS =
        { "Sample Code", "Clusters", "Clusters (PF)", "Yield (Mbases)", "Density Ratio",
                "PhiX: Clusters", "PhiX: ClustersPF", "PhiX: Yield (Mbases)", "PhiX: % Align (PF)",
                "Software", "Eland finished" };

    public IlluminaSummaryReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    private File[] findFile(DataSetProcessingContext context, DatasetDescription dataset,
            final String summaryFilePath, String path)
    {

        File originalData = getDataSubDir(context.getDirectoryProvider(), dataset);

        File childDirectory = new File(originalData, path);

        File[] files = childDirectory.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isDirectory() && file.getName().startsWith(summaryFilePath);
                }
            });
        return files;
    }

    private static void createRows(File[] f, String fileName, SimpleTableModelBuilder b,
            DatasetDescription dataset)
    {
        File summaryFile = new File(f[0], fileName);
        describe(b, dataset, summaryFile);

    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();

        for (String column : COLUMNS)
        {
            builder.addHeader(column);
        }

        for (String property : PROPERTIES)
        {
            builder.addHeader(property);
        }

        for (DatasetDescription dataset : datasets)
        {

            File[] f =
                    findFile(context, dataset, GERALD_DIR, dataset.getSampleCode()
                            + DATA_INTENSITIES_BASE_CALLS_PATH);

            if (f != null && f.length > 0)
            {
                createRows(f, SUMMARY_FILE_NAME, builder, dataset);
            } else
            {
                File[] fileNewCasava = findFile(context, dataset, BASECALL_DIR, UNALIGNED_PATH);
                try
                {
                    createRows(fileNewCasava, BUSTARD_SUMMARY_FILE_NAME, builder, dataset);
                } catch (RuntimeException exc)
                {
                    List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
                    row.add(new StringTableCell(dataset.getSampleCode()));
                    for (int i = 0; i <= COLUMNS.length + PROPERTIES.length - 2; ++i)
                    {
                        row.add(new StringTableCell(""));
                    }
                    builder.addRow(row);
                }
            }

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

        List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
        row.add(new StringTableCell(dataset.getSampleCode()));

        addTableRow(chipResultSummary.getClusterCountRaw(), row);
        addTableRow(chipResultSummary.getClusterCountPF(), row);
        addTableRow(chipResultSummary.getYield() / MEGA, row);
        addTableRow(chipResultSummary.getDensityRatio(), row);

        // PhiX Lane
        addTableRow(laneResultSummary.getRead().getLanes().get(4).getClusterCountRaw().getMean(),
                row);
        addTableRow(laneResultSummary.getRead().getLanes().get(4).getClusterCountPF().getMean(),
                row);
        addTableRow(laneResultSummary.getRead().getLanes().get(4).getLaneYield() / KILO, row);

        try
        {
            addTableRow(laneResultSummary.getRead().getLanes().get(4).getPercentUniquelyAlignedPF()
                    .getMean(), row);
        } catch (RuntimeException exc)
        {
            row.add(new StringTableCell(""));

        }

        addTableRow(summary.getSoftware(), row);
        addTableRow(summary.getDate(), row);
        addPropertyColumnValues(dataset, row);

        builder.addRow(row);
    }

    private static void addTableRow(Long number, List<ISerializableComparable> row)
    {
        try
        {
            row.add(new IntegerTableCell(number));

        } catch (RuntimeException exc)
        {
            row.add(new StringTableCell(""));

        }
    }

    private static void addTableRow(Double number, List<ISerializableComparable> row)
    {
        try
        {
            row.add(new DoubleTableCell(number));

        } catch (RuntimeException exc)
        {
            row.add(new StringTableCell(""));

        }
    }

    private static void addTableRow(String s, List<ISerializableComparable> row)
    {
        try
        {
            row.add(new StringTableCell(s));

        } catch (RuntimeException exc)
        {
            row.add(new StringTableCell(""));

        }
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
                    dataset.getDataSetCode()));
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
