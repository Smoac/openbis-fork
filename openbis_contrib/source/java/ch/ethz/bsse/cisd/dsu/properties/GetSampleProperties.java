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

    private static final String END_TYPE = "END_TYPE";

    private static final String ELAND_CONFIG_FILE = "config.txt";

    private static final String BOWTIE_CONFIG_FILE = "bowtie.txt";

    private static final String BCL2FASTQ_CONFIG_FILE = "SampleSheet.csv";

    private static final String DEFAULT_FLOW_CELL_SPACE = "CISD:/BSSE_FLOWCELLS/";

    private static final String ORGANISM_PROPERTY = "NCBI_ORGANISM_TAXONOMY";

    private static final String BARCODE_PROPERTY = "BARCODE";
    
    private static final String EXTERNAL_SAMPLE_NAME = "EXTERNAL_SAMPLE_NAME";

    private static final String CYCLES = "CYCLES_REQUESTED_BY_CUSTOMER";
  
    private static final String SEPARATOR = ",";
    
    private static final String PHIX_NAME = "phiX";

    private static final String OPERATOR = "ETHZ_BSSE_DSU";
    
    private static final String HEADER = "FCID,Lane,SampleID,SampleRef,Index,Description,Control,Recipe,Operator,SampleProject\n";   
    
    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";
    
    private enum sampleProperties {ORGANISM_PROPERTY1, BARCODE_PROPERTY1, ISPHIX, EXTERNAL_SAMPLE_NAME1, CYCLES1}

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            GetSampleProperties.class);

    public static void main(String[] args) throws IOException
    {
        FileWriter writer = new FileWriter(ELAND_CONFIG_FILE);
        FileWriter bowtieWriter = new FileWriter(BOWTIE_CONFIG_FILE);
        Properties prop = PropertyUtils.loadProperties(SERVICE_PROPERTIES);
        Long techId = 0L;
        ArrayList <String> bcl2fastqList = new ArrayList <String>();
        List <ArrayList<String>> fullList = new ArrayList <ArrayList<String>>();
        String endType = "";
        String cycles = "";

        if (args.length < 2)
        {
            String programName = getProgramName();
            System.err.println("Usage: java -jar " + programName + " <server> <FlowCell>");
            System.exit(1);
        }

        String serverURL = args[0];
        String user = prop.getProperty(USERNAME);
        String password = prop.getProperty(PASSWORD);
        String sampleIdendtifier = DEFAULT_FLOW_CELL_SPACE + args[1];

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
            System.err.println("Unknown FlowCell!");
            System.exit(1);
        }

        System.out.println("Found "+ sample.getCode() + " with Id " + sample.getId());
        String flowCellId = extractFlowCellId(sample.getCode());
       
        // which end type: single read or paired end?
        List<IEntityProperty> p = sample.getProperties();
        for (IEntityProperty property : p)
        {
            if (property.getPropertyType().getCode().equals(END_TYPE))
            {
                endType = GenomeMap.getEndType(property.tryGetAsString());
            }
            if (property.getPropertyType().getCode().equals(CYCLES)) {
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
        }

        for (Entry<Integer, List<Sample>> entry : parentSamples.entrySet())
        {
            Integer laneNumber = entry.getKey();
            System.out.println("Processing Lane "+ laneNumber);
            String propertyString = "";
            String pathToGenome = "";
            String bowtieIndexName = "";
            List<Sample> samples = entry.getValue();
            
      
            // get the properties of the first parent
            List<IEntityProperty> properties = samples.get(0).getProperties();

            for (int i=0; i < samples.size(); i++ ) {
                List <IEntityProperty> properties1 = samples.get(i).getProperties();
                String sampleCode = samples.get(i).getCode();
                HashMap <sampleProperties, String> propertiesPerSample = new HashMap<sampleProperties, String>();
                
                
                for (IEntityProperty property : properties1)
                {
                    if (property.getPropertyType().getCode().equals(ORGANISM_PROPERTY))
                    {
                        propertyString = property.tryGetAsString();
                        bowtieIndexName = GenomeMap.getBowtieIndex(propertyString);
                        if (bowtieIndexName != null)
                        {
                          propertiesPerSample.put(sampleProperties.ORGANISM_PROPERTY1,bowtieIndexName);
                        }
                        if (bowtieIndexName.equals(PHIX_NAME)) {
                          propertiesPerSample.put(sampleProperties.ISPHIX,"Y");
                        }
                        else {
                            propertiesPerSample.put(sampleProperties.ISPHIX,"N");
                        }
                    }

                    if (property.getPropertyType().getCode().equals(EXTERNAL_SAMPLE_NAME)) {
                        propertiesPerSample.put(sampleProperties.EXTERNAL_SAMPLE_NAME1,property.tryGetAsString());
                    }

                    if (property.getPropertyType().getCode().equals(BARCODE_PROPERTY)) {
                        propertiesPerSample.put(sampleProperties.BARCODE_PROPERTY1, property.tryGetAsString());
                    }
                  
                 }
                
                 // when not barcoded
                 if (propertiesPerSample.get(sampleProperties.BARCODE_PROPERTY1) == null) {
                     // when it is a single sample in a single lane 
                     if (samples.size() < 2) {
                         bcl2fastqList.add(flowCellId + SEPARATOR + laneNumber + SEPARATOR + sampleCode + 
                                 SEPARATOR + propertiesPerSample.get(sampleProperties.ORGANISM_PROPERTY1) + SEPARATOR + 
                                 SEPARATOR + propertiesPerSample.get(sampleProperties.EXTERNAL_SAMPLE_NAME1) + 
                                 SEPARATOR + propertiesPerSample.get(sampleProperties.ISPHIX) +
                                 SEPARATOR + cycles + 
                                 SEPARATOR + OPERATOR +
                                 SEPARATOR + sample.getCode() + "_" + laneNumber + "\n");
                     }
                 }
                 else {
                     bcl2fastqList.add(flowCellId + SEPARATOR + laneNumber + SEPARATOR + sampleCode + 
                         SEPARATOR + propertiesPerSample.get(sampleProperties.ORGANISM_PROPERTY1) +
                         SEPARATOR + propertiesPerSample.get(sampleProperties.BARCODE_PROPERTY1) +
                         SEPARATOR + propertiesPerSample.get(sampleProperties.EXTERNAL_SAMPLE_NAME1) +
                         SEPARATOR + propertiesPerSample.get(sampleProperties.ISPHIX) +
                         SEPARATOR + cycles + 
                         SEPARATOR + OPERATOR +
                         SEPARATOR + sample.getCode() + "_" + laneNumber + "\n");
                 }
            }
            fullList.add(bcl2fastqList);
            bcl2fastqList = new ArrayList<String>();

            
            
            for (IEntityProperty property : properties)
            {
                
                if (property.getPropertyType().getCode().equals(BARCODE_PROPERTY)) {
                    propertyString = property.tryGetAsString();
                    //System.out.println("Barcode: " + propertyString);
                }
                
                if (property.getPropertyType().getCode().equals(ORGANISM_PROPERTY))
                {
                    try
                    {
                        propertyString = property.tryGetAsString();
                        pathToGenome = GenomeMap.getGenomePath(propertyString);
                        bowtieIndexName = GenomeMap.getBowtieIndex(propertyString);
                        if (pathToGenome != null)
                        {
                            writer.write(laneNumber + ":ELAND_GENOME " + pathToGenome + "\n");
                            writer.write(laneNumber + ":ANALYSIS " + endType + "\n");
                            bowtieWriter.write(bowtieIndexName + "\n");
                        }

                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        writer.write("ELAND_SET_SIZE 20\n" + "EMAIL_LIST manuel.kohler@bsse.ethz.ch");
        writer.close();
        bowtieWriter.close();
        operationLog.info("Writing " + ELAND_CONFIG_FILE);
        operationLog.info("Writing " + BOWTIE_CONFIG_FILE);
        
        writebcl2fastqList(fullList);
        service.logout(sessionToken);
    }

    private static void writebcl2fastqList(List<ArrayList<String>> fullList) throws IOException
    {
        FileWriter bcl2fastqWriter = new FileWriter(BCL2FASTQ_CONFIG_FILE);
        bcl2fastqWriter.write(HEADER);
        
        for (List<String> element  : fullList) {
            for (String e : element) {
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
    
    private static String extractFlowCellId(String flowCellName) {
        String flowCell [] = flowCellName.split("_");
        // substring removes the A or B which is the Flow Cell tray in the HiSeq
        return (flowCell[flowCell.length-1].substring(1));
    }

}