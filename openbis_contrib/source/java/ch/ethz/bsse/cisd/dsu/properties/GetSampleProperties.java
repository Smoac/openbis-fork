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

    private static final String DEFAULT_FLOW_CELL_SPACE = "CISD:/BSSE_FLOWCELLS/";

    // private static final String DEFAULT_FLOW_CELL_SPACE = "default_flow_cell_space";

    private static final String PROPERTY = "NCBI_ORGANISM_TAXONOMY";

    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            GetSampleProperties.class);

    public static void main(String[] args) throws IOException
    {
        FileWriter writer = new FileWriter(ELAND_CONFIG_FILE);
        FileWriter bowtieWriter = new FileWriter(BOWTIE_CONFIG_FILE);
        Properties prop = PropertyUtils.loadProperties(SERVICE_PROPERTIES);
        Long techId = 0L;
        String endType = "";

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
                HttpInvokerUtils
                        .createServiceStub(IETLLIMSService.class, serverURL + "/rmi-etl", 5);

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

        // which end type: single read or paired end?
        List<IEntityProperty> p = sample.getProperties();
        for (IEntityProperty property : p)
        {
            if (property.getPropertyType().getCode().matches(END_TYPE))
            {
                endType = GenomeMap.getEndType(property.tryGetAsString());
            }
        }

        List<Sample> flowLaneSample =
                service.listSamples(sessionToken,
                        ListSampleCriteria.createForContainer(new TechId(techId)));

        Map<Integer, List<Sample>> parentSamples = new TreeMap<Integer, List<Sample>>();

        for (Sample fl : flowLaneSample)
        {
            // Just extract the lane number
            Integer ii = Integer.parseInt(fl.getIdentifier().split(":")[2]);
            parentSamples.put(
                    ii,
                    service.listSamples(sessionToken,
                            ListSampleCriteria.createForChild(new TechId(fl.getId()))));
        }

        for (Entry<Integer, List<Sample>> entry : parentSamples.entrySet())
        {
            Integer key = entry.getKey();
            String propertyString = "";
            String pathToGenome = "";
            String bowtieIndexName = "";
            List<Sample> samples = entry.getValue();
            // get the properties of the first parent
            List<IEntityProperty> properties = samples.get(0).getProperties();
            for (IEntityProperty property : properties)
            {
                if (property.getPropertyType().getCode().matches(PROPERTY))
                {
                    try
                    {
                        propertyString = property.tryGetAsString();
                        pathToGenome = GenomeMap.getGenomePath(propertyString);
                        bowtieIndexName = GenomeMap.getBowtieIndex(propertyString);
                        if (pathToGenome != null)
                        {
                            writer.write(key + ":ELAND_GENOME " + pathToGenome + "\n");
                            writer.write(key + ":ANALYSIS " + endType + "\n");
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
        service.logout(sessionToken);
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

}