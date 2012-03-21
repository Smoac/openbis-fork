package ch.ethz.bsse.cisd.dsu.properties;

/*

 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 * @author Manuel Kohler
 */
public class GetSampleProperties
{
    private static final String SERVICE_PROPERTIES = "etc/service.properties";

    private static final String ELAND_CONFIG_FILE = "eland_config_file";

    private static final String BOWTIE_CONFIG_FILE = "bowtie_config_file";

    private static final String BCL2FASTQ_CONFIG_FILE = "bcl2fastq_config_file";

    private static final String DEFAULT_FLOW_CELL_SPACE = "default_flow_cell_space";

    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";

    private static final String BCL2FASTQ_SEPARATOR = "bcl2fastq_separator";

    private static final String PHIX_NAME = "phix_name";

    private static final String OPERATOR = "operator";

    private static final String MAIL = "mail";

    private static final String HEADER = "header";

    // Flow Cell properties read out
    private static final String END_TYPE = "END_TYPE";

    private static final String CYCLES = "CYCLES_REQUESTED_BY_CUSTOMER";

    // Biological Sample properties read out ?
    private static final String ORGANISM_PROPERTY = "NCBI_ORGANISM_TAXONOMY";

    private static final String BARCODE_PROPERTY = "BARCODE";

    private static final String EXTERNAL_SAMPLE_NAME = "EXTERNAL_SAMPLE_NAME";

    private enum sampleProperties
    {
        ORGANISM_PROPERTY1, BARCODE_PROPERTY1, ISPHIX, EXTERNAL_SAMPLE_NAME1, CYCLES1
    }

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            GetSampleProperties.class);

    public static void main(String[] args) throws IOException
    {
        Properties prop = PropertyUtils.loadProperties(SERVICE_PROPERTIES);

        // get all values from service.properties
        FileWriter elandWriter = new FileWriter(prop.getProperty(ELAND_CONFIG_FILE));
        FileWriter bowtieWriter = new FileWriter(prop.getProperty(BOWTIE_CONFIG_FILE));
        FileWriter bcl2fastqWriter = new FileWriter(prop.getProperty(BCL2FASTQ_CONFIG_FILE));
        String user = prop.getProperty(USERNAME);
        String password = prop.getProperty(PASSWORD);
        String default_flow_cell_space = prop.getProperty(DEFAULT_FLOW_CELL_SPACE);
        String bcl2fastqSeparator = prop.getProperty(BCL2FASTQ_SEPARATOR);
        String phixName = prop.getProperty(PHIX_NAME);
        String operator = prop.getProperty(OPERATOR);
        String mail = prop.getProperty(MAIL);
        String header = prop.getProperty(HEADER);

        Long techId = 0L;
        ArrayList<String> bcl2fastqList = new ArrayList<String>();
        List<ArrayList<String>> fullList = new ArrayList<ArrayList<String>>();
        String endType = "";
        String cycles = "";

        if (args.length < 2)
        {
            String programName = getProgramName();
            System.err.println("Usage: java -jar " + programName + " <server> <FlowCell>");
            System.exit(1);
        }

        String serverURL = args[0];
        String sampleIdendtifier = default_flow_cell_space + args[1];

        IETLLIMSService service =
                HttpInvokerUtils.createServiceStub(IETLLIMSService.class, serverURL + "/rmi-etl",
                        5000);

        String sessionToken = service.tryToAuthenticate(user, password).getSessionToken();

        Sample sample =
                service.tryGetSampleWithExperiment(sessionToken,
                        SampleIdentifierFactory.parse(sampleIdendtifier));
        try
        {
            techId = sample.getId();

        } catch (Exception ex)
        {
            System.err.println("Unknown FlowCell " + sampleIdendtifier);
            System.exit(1);
        }

        operationLog.info("Found Sample " + sample.getCode() + " with Id " + sample.getId());
        String flowCellId = extractFlowCellId(sample.getCode());

        // which end type: single read or paired end?
        List<IEntityProperty> p = sample.getProperties();
        for (IEntityProperty property : p)
        {
            if (property.getPropertyType().getCode().equals(END_TYPE))
            {
                endType = GenomeMap.getEndType(property.tryGetAsString());
            }
            if (property.getPropertyType().getCode().equals(CYCLES))
            {
                cycles = GenomeMap.getNumberOfCycles(Integer.parseInt(property.tryGetAsString()));
            }

        }

        List<Sample> flowLaneSample =
                service.listSamples(sessionToken,
                        ListSampleCriteria.createForContainer(new TechId(techId)));

        Map<Integer, List<Sample>> parentSamples = new TreeMap<Integer, List<Sample>>();

        for (Sample fl : flowLaneSample)
        {
            // Just extract the lane number

            Integer ii = Integer.parseInt(fl.getIdentifier().split(":")[1]);
            parentSamples.put(
                    ii,
                    service.listSamples(sessionToken,
                            ListSampleCriteria.createForChild(new TechId(fl.getId()))));

            for (Map.Entry<Integer, List<Sample>> entry : parentSamples.entrySet())
            {

                // System.out.println(entry.getKey() + " " + entry.getValue());
                // System.out.println(entry.getValue().size());

                for (int j = 0; j <= entry.getValue().size() - 1; ++j)
                {
                    // System.out.println(entry.getValue().get(0).getParents());
                    Long s = entry.getValue().get(j).getId();
                    // System.out.println(s);
                    List<Sample> setParents =
                            service.listSamples(sessionToken,
                                    ListSampleCriteria.createForChild(new TechId(s)));
                    // System.out.println(setParents);
                }
                // System.out.println(service.listSamples(sessionToken,
                // ListSampleCriteria.createForChild(new TechId(entry.getKey()))));

            }

        }

        for (Entry<Integer, List<Sample>> entry : parentSamples.entrySet())
        {
            Integer laneNumber = entry.getKey();
            String propertyString = "";
            String pathToGenome = "";
            String bowtieIndexName = "";
            List<Sample> samples = entry.getValue();

            // get the properties of the first parent
            List<IEntityProperty> properties = samples.get(0).getProperties();

            for (int i = 0; i < samples.size(); i++)
            {

                List<IEntityProperty> properties1 = samples.get(i).getProperties();
                String sampleCode = samples.get(i).getCode();
                HashMap<sampleProperties, String> propertiesPerSample =
                        new HashMap<sampleProperties, String>();

                for (IEntityProperty property : properties1)
                {
                    if (property.getPropertyType().getCode().equals(ORGANISM_PROPERTY))
                    {
                        propertyString = property.tryGetAsString();
                        bowtieIndexName = GenomeMap.getBowtieIndex(propertyString);
                        if (bowtieIndexName != null)
                        {
                            propertiesPerSample.put(sampleProperties.ORGANISM_PROPERTY1,
                                    bowtieIndexName);
                            if (bowtieIndexName.equals(phixName))
                            {
                                propertiesPerSample.put(sampleProperties.ISPHIX, "Y");
                            } else
                            {
                                propertiesPerSample.put(sampleProperties.ISPHIX, "N");
                            }
                        } else
                        {
                            propertiesPerSample.put(sampleProperties.ORGANISM_PROPERTY1,
                                    "NO_REFERENCE_GENOME_AVAILABLE");
                            propertiesPerSample.put(sampleProperties.ORGANISM_PROPERTY1,
                                    bowtieIndexName);
                            propertiesPerSample.put(sampleProperties.ISPHIX, "N");

                        }

                    }

                    if (property.getPropertyType().getCode().equals(EXTERNAL_SAMPLE_NAME))
                    {
                        propertiesPerSample.put(sampleProperties.EXTERNAL_SAMPLE_NAME1,
                                property.tryGetAsString());
                    }

                    if (property.getPropertyType().getCode().equals(BARCODE_PROPERTY))
                    {
                        // only take the first six nucleotides of the barcode
                        String barcode = property.tryGetAsString();
                        String strippedBarcode = barcode.substring(0, barcode.length() - 1);
                        propertiesPerSample
                                .put(sampleProperties.BARCODE_PROPERTY1, strippedBarcode);
                    }

                }

                // when not barcoded
                if (propertiesPerSample.get(sampleProperties.BARCODE_PROPERTY1) == null)
                {
                    // when it is a single sample in a single lane
                    if (samples.size() < 2)
                    {
                        bcl2fastqList.add(flowCellId
                                + bcl2fastqSeparator
                                + laneNumber
                                + bcl2fastqSeparator
                                + sampleCode
                                + "_"
                                + flowCellId
                                + bcl2fastqSeparator
                                + propertiesPerSample.get(sampleProperties.ORGANISM_PROPERTY1)
                                + bcl2fastqSeparator
                                + bcl2fastqSeparator
                                + cleanString(propertiesPerSample
                                        .get(sampleProperties.EXTERNAL_SAMPLE_NAME1))
                                + bcl2fastqSeparator
                                + propertiesPerSample.get(sampleProperties.ISPHIX)
                                + bcl2fastqSeparator + cycles + bcl2fastqSeparator + operator
                                + bcl2fastqSeparator + sample.getCode() + "_" + laneNumber + "\n");
                    }
                } else
                {
                    bcl2fastqList.add(flowCellId
                            + bcl2fastqSeparator
                            + laneNumber
                            + bcl2fastqSeparator
                            + sampleCode
                            + "_"
                            + flowCellId
                            + bcl2fastqSeparator
                            + propertiesPerSample.get(sampleProperties.ORGANISM_PROPERTY1)
                            + bcl2fastqSeparator
                            + propertiesPerSample.get(sampleProperties.BARCODE_PROPERTY1)
                            + bcl2fastqSeparator
                            + cleanString(propertiesPerSample
                                    .get(sampleProperties.EXTERNAL_SAMPLE_NAME1))
                            + bcl2fastqSeparator + propertiesPerSample.get(sampleProperties.ISPHIX)
                            + bcl2fastqSeparator + cycles + bcl2fastqSeparator + operator
                            + bcl2fastqSeparator + sample.getCode() + "_" + laneNumber + "\n");
                }
            }
            fullList.add(bcl2fastqList);
            bcl2fastqList = new ArrayList<String>();

            for (IEntityProperty property : properties)
            {
                if (property.getPropertyType().getCode().equals(BARCODE_PROPERTY))
                {
                    propertyString = property.tryGetAsString();
                }

                if (property.getPropertyType().getCode().equals(ORGANISM_PROPERTY))
                {
                    try
                    {
                        propertyString = property.tryGetAsString();
                        pathToGenome = GenomeMap.getGenomePath(propertyString);
                        bowtieIndexName = GenomeMap.getBowtieIndex(propertyString);
                        if (pathToGenome != null)
                        // TODO: if null then handle properly
                        {
                            elandWriter.write(laneNumber + ":ELAND_GENOME " + pathToGenome + "\n");
                            elandWriter.write(laneNumber + ":ANALYSIS " + endType + "\n");
                            bowtieWriter.write(bowtieIndexName + "\n");
                        }

                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        elandWriter.write("ELAND_FASTQ_FILES_PER_PROCESS 8\n" + "EMAIL_LIST " + mail);
        elandWriter.close();
        bowtieWriter.close();
        writebcl2fastqList(bcl2fastqWriter, fullList, header);
        operationLog.info("Writing " + prop.getProperty(BCL2FASTQ_CONFIG_FILE)
                + " for BCL to FASTQ conversion");
        operationLog
                .info("Writing " + prop.getProperty(ELAND_CONFIG_FILE) + " for Eland Alignment");
        operationLog.info("Writing " + prop.getProperty(BOWTIE_CONFIG_FILE)
                + " for Bowtie Alignment");
        service.logout(sessionToken);
    }

    private static void writebcl2fastqList(FileWriter bcl2fastqWriter,
            List<ArrayList<String>> fullList, String header) throws IOException
    {
        bcl2fastqWriter.write(header + "\n");

        for (List<String> element : fullList)
        {
            for (String e : element)
            {
                operationLog.debug(e);
                bcl2fastqWriter.write(e);
            }
        }
        bcl2fastqWriter.close();
    }

    private static String getProgramName()
    {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement main = stack[stack.length - 1];
        String mainClass = main.getClassName();

        // workaround, because the split does not work with a '.'
        mainClass = mainClass.replace(".", " ");
        String[] tokens = mainClass.split(" ");

        String jarName = tokens[tokens.length - 1] + ".jar";
        return jarName;
    }

    private static String extractFlowCellId(String flowCellName)
    {
        String flowCell[] = flowCellName.split("_");
        // this is a GA
        if (flowCell.length == 2)
        {
            return (flowCell[flowCell.length - 1]);
        } else
        {
            // substring removes the A or B which is the Flow Cell tray in the HiSeq
            return (flowCell[flowCell.length - 1].substring(1));
        }
    }

    public static String cleanString(String dirtyString)
    /*
     * Replace all (Unicode) characters that are neither letters nor numbers with a "_"
     */
    {
        String cleanString = dirtyString.replaceAll("[^\\p{L}\\p{N}]", "_");
        return cleanString;

    }

}