/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.phosphonetx.server.plugins;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractTableModelReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Creates a report from proteins.csv
 *
 * @author Franz-Josef Elmer
 */
public class ProteinTableReport extends AbstractTableModelReportingPlugin implements IProcessingPluginTask
{
    private static final class Header
    {
        private final boolean abundanceHeader;
        private final String header;
        
        private String biologicalSample;
        private String protein;

        Header(String header, String newHeader)
        {
            abundanceHeader = newHeader != null;
            this.header = abundanceHeader ? newHeader : header;
        }

        public final String getBiologicalSample()
        {
            return biologicalSample;
        }

        public final void setBiologicalSample(String biologicalSample)
        {
            this.biologicalSample = biologicalSample;
        }

        public final String getProtein()
        {
            return protein;
        }

        public final void setProtein(String protein)
        {
            this.protein = protein;
        }

        public final String getHeader()
        {
            return header;
        }

        public final boolean isAbundanceHeader()
        {
            return abundanceHeader;
        }

        @Override
        public String toString()
        {
            return abundanceHeader ? header + " [" + biologicalSample + ", " + protein + ']' : header;
        }
    }
    
    private static final class Row
    {
        private final String[] values;
        private final Map<String, Integer> columnIndexMap;
        
        Row(String[] values, Map<String, Integer> columnIndexMap)
        {
            this.values = values;
            this.columnIndexMap = columnIndexMap;
        }
        
        public String getValue(String column)
        {
            Integer index = columnIndexMap.get(column);
            return index == null ? "" :values[index];
        }

        @Override
        public String toString()
        {
            return Arrays.asList(values).toString();
        }
    }
    
    private static final class Table
    {
        private final List<Header> headers;
        private final Map<String, Integer> columnIndexMap = new HashMap<String, Integer>();
        
        private final List<Row> rows = new ArrayList<Row>();

        Table(List<Header> headers)
        {
            this.headers = headers;
            for (int i = 0; i < headers.size(); i++)
            {
                columnIndexMap.put(headers.get(i).getHeader(), i);
            }
        }
        
        void add(String[] row)
        {
            rows.add(new Row(row, columnIndexMap));
        }

        public final List<Header> getHeaders()
        {
            return headers;
        }

        public final List<Row> getRows()
        {
            return rows;
        }
    }
    
    private static final long serialVersionUID = 1L;
    
    @Private static final String PROTEIN_FILE_NAME = "proteins.csv";
    private static final String PROTEIN_PROPERTY_CODE_KEY = "protein-property-code";
    private static final String DEFAULT_PROTEIN_PROPERTY_CODE = "PROTEIN";
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ProteinTableReport.class);

    private final String proteinPropertyCode;
    
    private IEncapsulatedOpenBISService service;

    public ProteinTableReport(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        proteinPropertyCode = properties.getProperty(PROTEIN_PROPERTY_CODE_KEY, DEFAULT_PROTEIN_PROPERTY_CODE);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        if (datasets.size() != 1)
        {
            throw new UserFailureException("Chosen plugin works with exactly one data set. "
                    + datasets.size() + " data sets selected.");
        }
        return createTableModel(datasets.get(0));
    }
    
    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        ProcessingStatus status = new ProcessingStatus();
        for (DatasetDescription datasetDescription : datasets)
        {
            try
            {
                process(datasetDescription, context);
                status.addDatasetStatus(datasetDescription, Status.OK);
            } catch (Exception ex)
            {
                status.addDatasetStatus(datasetDescription,
                        Status.createError("Exception occured: " + ex));
                operationLog.error("Exception occured while processing " + datasetDescription, ex);
            }
        }
        return status;
    }
    
    private void process(DatasetDescription datasetDescription, DataSetProcessingContext context)
    {
        try
        {
            TableModel tableModel = createTableModel(datasetDescription);
            StringWriter writer = new StringWriter();
            CsvWriter csvWriter = new CsvWriter(writer, ',');
            List<TableModelColumnHeader> headers = tableModel.getHeader();
            String[] stringArray = new String[headers.size()];
            for (int i = 0; i < stringArray.length; i++)
            {
                stringArray[i] = headers.get(i).getTitle();
            }
            csvWriter.writeRecord(stringArray);
            List<TableModelRow> rows = tableModel.getRows();
            for (TableModelRow row : rows)
            {
                List<ISerializableComparable> values = row.getValues();
                for (int i = 0; i < stringArray.length; i++)
                {
                    stringArray[i] = values.get(i).toString();
                }
                csvWriter.writeRecord(stringArray);
            }
            ByteArrayDataSource dataSource =
                    new ByteArrayDataSource(writer.toString(), "text/plain");
            context.getMailClient().sendEmailMessageWithAttachment("Protein APMS Report",
                    "Here is your report",
                    datasetDescription.getExperimentCode() + "-protein-report.txt",
                    new DataHandler(dataSource), null, null,
                    new EMailAddress(context.getUserEmailOrNull()));

        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private TableModel createTableModel(DatasetDescription datasetDescription)
    {
        File proteinFile = getProteinFile(datasetDescription);
        Table table = readTable(proteinFile);
        addHeaderMetaData(table);
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("Sample ID");
        builder.addHeader("Bait");
        builder.addHeader("Prey");
        builder.addHeader("num unique peptides");
        builder.addHeader("spec count");
        builder.addHeader("freq of obs");
        builder.addHeader("avg MS1 intensities (normalized for the bait)");
        builder.addHeader("STDV MS1 intensity");
        List<Entry<String, List<Header>>> groups = calculateAbundanceColumnGroups(table);
        List<Row> rows = table.getRows();
        for (int i = 0; i < rows.size(); i++)
        {
            Row row = rows.get(i);
            addRowsTo(builder, row, i, groups);
        }
        return builder.getTableModel();
    }

    private List<Entry<String, List<Header>>> calculateAbundanceColumnGroups(Table table)
    {
        List<Header> headers = table.getHeaders();
        Map<String, List<Header>> abundanceColumnHeaders = new HashMap<String, List<Header>>();
        for (Header header : headers)
        {
            if (header.isAbundanceHeader())
            {
                String biologicalSample = header.getBiologicalSample();
                List<Header> list = abundanceColumnHeaders.get(biologicalSample);
                if (list == null)
                {
                    list = new ArrayList<Header>();
                    abundanceColumnHeaders.put(biologicalSample, list);
                }
                list.add(header);
            }
        }
        Set<Entry<String, List<Header>>> entrySet = abundanceColumnHeaders.entrySet();
        List<Entry<String, List<Header>>> entryList =
                new ArrayList<Map.Entry<String, List<Header>>>(entrySet);
        Collections.sort(entryList, new Comparator<Entry<String, List<Header>>>()
            {
                public int compare(Entry<String, List<Header>> e1, Entry<String, List<Header>> e2)
                {
                    String key1 = e1.getKey();
                    String key2 = e2.getKey();
                    if (key1 != null && key2 != null)
                    {
                        return key1.compareTo(key2);
                    }
                    return key1 == null ? (key2 == null ? 0 : 1) : -1;
                }
            });
        return entryList;
    }

    private void addRowsTo(SimpleTableModelBuilder builder, Row row, int i,
            List<Entry<String, List<Header>>> groups)
    {
        String identifiedProtein = row.getValue("protein");
        for (Entry<String, List<Header>> group : groups)
        {
            String biologicalSample = group.getKey();
            List<Header> hs = group.getValue();
            String protein = null;
            double countNonZeroAbundances = 0;
            double sum = 0;
            double sum2 = 0;
            for (Header header : hs)
            {
                if (biologicalSample != null && protein == null)
                {
                    protein = header.getProtein();
                }
                String value = row.getValue(header.getHeader());
                try
                {
                    double abundance = Double.parseDouble(value);
                    sum += abundance;
                    sum2 += abundance * abundance;
                    if (abundance > 0)
                    {
                        countNonZeroAbundances++;
                    }
                } catch (NumberFormatException ex)
                {
                    throw new UserFailureException((i + 5) + ". row has an invalid value ("
                            + value + ") for column " + header.getHeader() + ": " + row);
                }
            }
            double averagedAbundance = sum / hs.size();
            double abundanceStandardDeviation =
                    Math.sqrt(averagedAbundance * averagedAbundance - sum2 / hs.size());
            double frequencyOfObservation = countNonZeroAbundances / hs.size();
            builder.addRow(Arrays.<ISerializableComparable> asList(new StringTableCell(
                    biologicalSample), new StringTableCell(protein), new StringTableCell(
                    identifiedProtein),
                    new IntegerTableCell(Long.parseLong(row.getValue("n_peptides"))),
                    new StringTableCell(""), new DoubleTableCell(frequencyOfObservation),
                    new DoubleTableCell(averagedAbundance), new DoubleTableCell(
                            abundanceStandardDeviation)));
        }
    }

    private void addHeaderMetaData(Table table)
    {
        List<Header> headers = table.getHeaders();
        for (Header header : headers)
        {
            if (header.isAbundanceHeader())
            {
                SampleIdentifier sampleIdentifier =
                        SampleIdentifierFactory.parse("/MS_DATA/"
                                + header.getHeader().toUpperCase());
                Sample sample = getService().tryGetSampleWithExperiment(sampleIdentifier);
                if (sample == null)
                {
                    throw new UserFailureException("Unknown sample: " + sampleIdentifier);
                }
                List<Sample> parents =
                    getService().listSamples(
                            ListSampleCriteria.createForChild(new TechId(sample.getId())));
                if (parents.size() != 1)
                {
                    throw new UserFailureException("Exactly one parent sample expected for "
                            + sample.getIdentifier() + " instead of " + parents.size());
                }
                Sample biologicalSample = parents.get(0);
                header.setBiologicalSample(biologicalSample.getCode());
                String protein = tryToFindProteinProperty(biologicalSample);
                if (protein != null)
                {
                    header.setProtein(protein);
                }
            }
        }
    }
    
    private String tryToFindProteinProperty(Sample sample)
    {
        List<IEntityProperty> sampleProperties = sample.getProperties();
        for (IEntityProperty property : sampleProperties)
        {
            if (property.getPropertyType().getCode().equalsIgnoreCase(proteinPropertyCode))
            {
                Material material = property.getMaterial();
                if (material != null)
                {
                    return material.getCode();
                }
                return property.tryGetAsString();
            }
        }
        List<Sample> parents =
                getService().listSamples(
                        ListSampleCriteria.createForChild(new TechId(sample.getId())));
        for (Sample parent : parents)
        {
            String proteinProperty = tryToFindProteinProperty(parent);
            if (proteinProperty != null)
            {
                return proteinProperty;
            }
        }
        return null;
    }

    private Table readTable(File file)
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(file);
            CsvReader csvReader = new CsvReader(reader);
            csvReader.readRecord();
            csvReader.readRecord();
            csvReader.readRecord();
            Map<String, String> map = createAbundanceColumnMap(csvReader.getRawRecord());
            csvReader.readRecord();
            String[] columnHeaders = csvReader.getValues();
            List<Header> headers = new ArrayList<Header>();
            for (int i = 0; i < columnHeaders.length; i++)
            {
                String header = columnHeaders[i];
                String newHeader = map.get(header);
                headers.add(new Header(header, newHeader));
            }
            Table table = new Table(headers);
            while (csvReader.readRecord())
            {
                table.add(csvReader.getValues());
            }
            return table;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    private Map<String, String> createAbundanceColumnMap(String abundanceColumnMappingDescription)
    {
        int indexOfFirstColon = abundanceColumnMappingDescription.indexOf(':');
        if (indexOfFirstColon < 0)
        {
            throw new UserFailureException("Missing ':' in third line: "
                    + abundanceColumnMappingDescription);
        }
        String[] terms =
                abundanceColumnMappingDescription.substring(indexOfFirstColon + 1).split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String term : terms)
        {
            int indexOfColon = term.indexOf(':');
            if (indexOfColon < 0)
            {
                throw new UserFailureException("Missing ':' in mapping definition: " + term);
            }
            String abundanceColumn = "abundance_" + term.substring(0, indexOfColon).trim();
            String fileName = term.substring(indexOfColon + 1).trim();
            int lastIndexOfSlash = fileName.lastIndexOf('/');
            if (lastIndexOfSlash >= 0)
            {
                fileName = fileName.substring(lastIndexOfSlash + 1);
            }
            int lastIndexOfDot = fileName.lastIndexOf('.');
            if (lastIndexOfDot >= 0)
            {
                fileName = fileName.substring(0, lastIndexOfDot);
            }
            map.put(abundanceColumn, fileName);
        }
        return map;
    }

    private File getProteinFile(DatasetDescription datasetDescription)
    {
        File dataSetDir = getDataSubDir(datasetDescription);
        if (dataSetDir.isDirectory() == false)
        {
            throw new EnvironmentFailureException("Data set folder is not a directory: " + dataSetDir);
        }
        File[] files = dataSetDir.listFiles();
        if (files.length == 0)
        {
            throw new EnvironmentFailureException("Empty data set folder: " + dataSetDir);
        }
        File proteinFile = new File(files[0], PROTEIN_FILE_NAME);
        if (proteinFile.exists() == false)
        {
            throw new UserFailureException("File " + PROTEIN_FILE_NAME + " missing.");
        }
        if (proteinFile.isFile() == false)
        {
            throw new UserFailureException("File " + PROTEIN_FILE_NAME + " is a directory.");
        }
        return proteinFile;
    }

    private IEncapsulatedOpenBISService getService()
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return service;
    }

    // for tests
    void setService(IEncapsulatedOpenBISService service)
    {
        this.service = service;
    }
}
