/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.bsse.cisd.dsu.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;

/**
 * @author Manuel Kohler
 */

public class create_metadata
{
    private static final String TSV_ENDING = ".tsv";

    private static final String FLOW_CELL_PROPERTIES_NAME = "FLOW_CELL_PROPERTIES";

    private static final String EXPERIMENT_NAME = "EXPERIMENT";

    private static final String SAMPLE_TYPE_ILLUMINA_SEQUENCING = "ILLUMINA_SEQUENCING";

    private static final String INDEX1_NOINDEX_VALUE = "NOINDEX";

    private static final String DATASET_TYPE_CODE_FASTQ_GZ = "FASTQ_GZ";

    private static final String INDEX1_PROPERTY_CODE = "BARCODE";

    private static final String TSV_FLOWCELL_PROPERTIES = "FLOWCELL PROPERTIES";

    private static final String TSV_FASTQ_FILES = "FASTQ_FILES";

    private static final String CL_PARAMETER_OUTPUT_FOLDER = "output";

    private static final String CL_PARAMETER_SAMPLE_LIST = "samples";

    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    private static final char[] HEX_CHARACTERS =
    { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };

    public static void main(String[] args)
    {
        try
        {
            HashMap<String, String[]> commandLineMap = parseCommandLine(args);
            extract(commandLineMap);
        } catch (EnvironmentFailureException ex)
        {
            System.err.println(ex);
        } catch (Throwable ex)
        {
            System.err.println(ex);
        }
    }

    private static void extract(HashMap<String, String[]> commandLineMap)
    {
        LogInitializer.init();
        Properties props = PropertyIOUtils.loadProperties(SERVICE_PROPERTIES_FILE);
        Parameters params = new Parameters(props);

        ServiceFinder serviceFinder = new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService infoService =
                serviceFinder.createService(IGeneralInformationService.class, params.getOpenbisServerURL());

        String sessionToken = infoService.tryToAuthenticateForAllServices(params.getOpenbisUser(), params.getOpenbisPassword());
        if (sessionToken == null)
        {
            System.out.println("Wrong username/password!");
            System.exit(0);
        }
        ArrayList<String> sampleCodeList = new ArrayList<String>();

        String outputFolder = commandLineMap.get(CL_PARAMETER_OUTPUT_FOLDER)[0];
        String[] clSampleCodeList = commandLineMap.get(CL_PARAMETER_SAMPLE_LIST);
        for (String sampleCode : clSampleCodeList)
        {
            sampleCodeList.add(sampleCode);
        }

        EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.CHILDREN, SampleFetchOption.PROPERTIES);
        EnumSet<SampleFetchOption> flowcellFetchOptions = EnumSet.of(SampleFetchOption.PROPERTIES);
        Connection connection = DbAccess.connectToDB(params);

        for (String sampleCode : sampleCodeList)
        {
            List<Sample> sampleList = searchSample(infoService, sessionToken, sampleCode, fetchOptions);
            SortedMap<String, SortedMap<String, String>> sampleMap = getProperties(sampleList);
            SortedMap<String, SortedMap<String, String>> flowcellMap = null;
            String flowcellCode = "";

            for (Sample sample : sampleList)
            {
                List<Sample> children = sample.getChildren();
                String permId = extractDataSets(infoService, sessionToken, children);
                HashMap<String, Integer> dbResult = DbAccess.doQuery(connection, permId);
                for (Sample child : children)
                {
                    if (child.getSampleTypeCode().equals("ILLUMINA_FLOW_LANE"))
                    {
                        flowcellCode = child.getCode().split(":")[0];
                        List<Sample> flowcellList = searchSample(infoService, sessionToken, flowcellCode, flowcellFetchOptions);
                        flowcellMap = getProperties(flowcellList);
                    }
                }
                writeTSVFile(sampleMap, flowcellMap, dbResult, outputFolder);
            }
        }
        DbAccess.closeDBConnection(connection);
    }

    private static SortedMap<String, SortedMap<String, String>> getProperties(List<Sample> sampleList)
    {
        SortedMap<String, String> sortedProperties = new TreeMap<String, String>();
        SortedMap<String, SortedMap<String, String>> sampleMap = new TreeMap<String, SortedMap<String, String>>();

        for (Sample sample : sampleList)
        {
            Map<String, String> sampleProperties = sample.getProperties();
            for (String key : sampleProperties.keySet())
            {
                if (!key.equals(FLOW_CELL_PROPERTIES_NAME))
                {
                    sortedProperties.put(key, cleanString(sampleProperties.get(key).toString()));
                }
            }
            if (sample.getSampleTypeCode().equals(SAMPLE_TYPE_ILLUMINA_SEQUENCING))
            {
                sortedProperties.put(EXPERIMENT_NAME, sample.getExperimentIdentifierOrNull());
            }
            sampleMap.put(sample.getCode(), sortedProperties);
        }
        return sampleMap;
    }

    private static void writeTSVFile(SortedMap<String, SortedMap<String, String>> sampleMap,
            SortedMap<String, SortedMap<String, String>> flowcellMap, HashMap<String, Integer> dbResult,
            String outputFolder)
    {
        assert flowcellMap.size() == 1;
        SortedMap<String, String> flowcellProperties = flowcellMap.get(flowcellMap.firstKey());

        for (String key : sampleMap.keySet())
        {
            SortedMap<String, String> currentSample = sampleMap.get(key);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(key);
            stringBuilder.append("_");
            stringBuilder.append(flowcellProperties.get("RUN_NAME_FOLDER"));
            stringBuilder.append("_metadata");
            stringBuilder.append(TSV_ENDING);

            Path path = Paths.get(outputFolder, stringBuilder.toString());
            File metaDataFile = new File(path.toUri());
            metaDataFile.getParentFile().mkdirs();
            try
            {
                metaDataFile.createNewFile();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }

            try
            {
                BufferedWriter fOut = new BufferedWriter(new FileWriter(metaDataFile));
                for (String propertyKey : currentSample.keySet())
                {
                    fOut.write(propertyKey + "\t" + currentSample.get(propertyKey) + "\n");
                }
                fOut.write("\n" + TSV_FLOWCELL_PROPERTIES + "\n");

                for (String flowcellPropertyKey : flowcellProperties.keySet())
                {
                    fOut.write(flowcellPropertyKey + "\t" + flowcellProperties.get(flowcellPropertyKey) + "\n");
                }
                fOut.write("\n" + TSV_FASTQ_FILES + "\n");

                for (String fileName : dbResult.keySet())
                {
                    fOut.write(fileName + "\t" + crc32ToString(dbResult.get(fileName)) + "\n");
                }

                fOut.close();
                System.out.println("Written " + metaDataFile);

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static List<Sample> searchSample(IGeneralInformationService infoService, String sessionToken,
            String sampleCode, EnumSet<SampleFetchOption> fetchOptions)
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode));
        List<Sample> sampleList = infoService.searchForSamples(sessionToken, sc, fetchOptions);
        return sampleList;
    }

    private static String extractDataSets(IGeneralInformationService infoService, String sessionToken, List<Sample> children)
    {
        List<DataSet> flowLaneDatasets = infoService.listDataSets(sessionToken, children);
        String permId = "";

        for (DataSet ds : flowLaneDatasets)
        {
            HashMap<String, String> dsProperties = ds.getProperties();
            String barcode = dsProperties.get(INDEX1_PROPERTY_CODE);
            if (ds.getDataSetTypeCode().equals(DATASET_TYPE_CODE_FASTQ_GZ) && barcode.equals(INDEX1_NOINDEX_VALUE))
            {
                permId = ds.getCode();
                return permId;
            }
        }
        return permId;
    }

    private static String cleanString(String s)
    {
        return s.replaceAll("\n", " ");
    }

    /**
     * Converts a CRC32 checksum to a string representation.
     */
    public static String crc32ToString(final int checksum)
    {
        final char buf[] = new char[8];
        int w = checksum;
        for (int i = 0, x = 7; i < 4; i++)
        {
            buf[x--] = HEX_CHARACTERS[w & 0xf];
            buf[x--] = HEX_CHARACTERS[(w >>> 4) & 0xf];
            w >>= 8;
        }
        return new String(buf);
    }

    private static HashMap<String, String[]> parseCommandLine(String[] args)
    {
        HashMap<String, String[]> commandLineMap = new HashMap<String, String[]>();
        CommandLineParser parser = new GnuParser();

        Options options = new Options();

        OptionBuilder.withArgName(CL_PARAMETER_SAMPLE_LIST);
        OptionBuilder.hasArgs();
        OptionBuilder.withDescription("list of samples");
        Option samples = OptionBuilder.create(CL_PARAMETER_SAMPLE_LIST);
        samples.setArgs(Option.UNLIMITED_VALUES);
        samples.isRequired();
        options.addOption(samples);

        OptionBuilder.withArgName(CL_PARAMETER_OUTPUT_FOLDER);
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("output folder");

        Option outputFolder = OptionBuilder.create(CL_PARAMETER_OUTPUT_FOLDER);
        outputFolder.setArgs(1);
        options.addOption(outputFolder);

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        if (args.length < 2)
        {
            formatter.printHelp("help", options);
            System.exit(0);
        }

        try
        {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(CL_PARAMETER_SAMPLE_LIST))
            {
                String[] sampleArray = line.getOptionValues(CL_PARAMETER_SAMPLE_LIST);
                commandLineMap.put(CL_PARAMETER_SAMPLE_LIST, sampleArray);
            }
            if (line.hasOption(CL_PARAMETER_OUTPUT_FOLDER))
            {
                String[] outputArray = line.getOptionValues(CL_PARAMETER_OUTPUT_FOLDER);
                commandLineMap.put(CL_PARAMETER_OUTPUT_FOLDER, outputArray);
            }
            else
            {
                String cwd = System.getProperty("user.dir");
                String[] arrayCwd = cwd.split("@"); // just use a split with a not valid char to convert the String into String []
                System.out.println("No outout folder specified! Will use: " + cwd);
                commandLineMap.put(CL_PARAMETER_OUTPUT_FOLDER, arrayCwd);
            }
        } catch (ParseException exp)
        {
            System.out.println("Parsing of command line parameters failed.\n" + exp.getMessage());
            formatter.printHelp("help", options);
            System.exit(0);
        }
        return commandLineMap;
    }
}
