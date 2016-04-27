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

package eu.basynthec.cisd.client.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;

/**
 * A class that, given a live connection to openBIS, searches for data sets where one of the specified strains appear, downloads the data set and
 * prints information about the data set and experiment.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetSearch extends AbstractBaSynthecClient
{
    private static final String STRAIN_NAMES_PROPERTY = "STRAIN_NAMES";

    private static final String STRAINS_HEADER = "Strains";

    private static final String REGISTERED_BY_HEADER = "Contact Person";

    private static final String DATA_SET_HEADER = "Data Set";

    private static final String PATH_HEADER = "Data Path";

    /**
     * An enum for keeping track of the metadata fields we are interested in, in the order we want to display them.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private enum MetadataField
    {
        EXPERIMENT_DATE("Date"), DESCRIPTION("Description"), MEDIUM("Medium"), TEMPERATURE(
                "Temperature"), MISC_GROWTH_CONDITIONS("Growth Conditions");

        MetadataField(String headerText)
        {
            this.headerText = headerText;
        }

        private final String headerText;
    }

    // Where to store the data we download
    private final File downloadFolder;

    // State we compute as part of the operation of this class

    // The data sets we are interested in
    private List<DataSet> matchingDataSets;

    // The experiments that contain the data sets we are interested in
    private List<Experiment> experiments;

    // A mapping from experiments to data sets
    private HashMap<String, ArrayList<DataSet>> experimentDataSets;

    // A mapping from data sets to paths where the data is stored
    private HashMap<DataSet, File> dataSetFiles;

    /**
     * The public constructor
     * 
     * @param facade The facade for interacting with openBIS.
     */
    public DataSetSearch(IOpenbisServiceFacade facade, File downloadFolder)
    {
        super(facade);
        this.downloadFolder = downloadFolder;
    }

    /**
     * Using the connection to openBIS, get search for data sets that contain data on the specified strains. Download the data set and print
     * information about the data set and its experiment.<br>
     * <ul>
     * <li>Experiment Date</li>
     * <li>Description</li>
     * <li>Medium</li>
     * <li>Temperature</li>
     * <li>Growth Conditions</li>
     * <li>Data Set Strains</li>
     * <li>Data Set Path</li>
     * </ul>
     */
    public void run(List<String> strainNames)
    {
        retrieveInformationFromOpenBis(strainNames);
        downloadDataSets();

        printHeader();
        for (Experiment experiment : experiments)
        {
            printExperiment(experiment);
        }
    }

    /**
     * Connect to openBis to get the information we need.
     */
    private void retrieveInformationFromOpenBis(List<String> strainNames)
    {
        // Find all data sets that match the strain names
        matchingDataSets = retrieveDataSetsReferencingStrains(strainNames);

        // Get the experiments for the specified data sets
        List<String> experimentIdentifiers = new ArrayList<String>();
        for (DataSet dataSet : matchingDataSets)
        {
            experimentIdentifiers.add(dataSet.getExperimentIdentifier());
        }
        if (experimentIdentifiers.isEmpty())
        {
            experiments = new ArrayList<Experiment>();
        } else
        {
            experiments = openBis.getExperiments(experimentIdentifiers);
        }

        // Create a map from experiment to data set
        experimentDataSets = new HashMap<String, ArrayList<DataSet>>();
        for (DataSet dataSet : matchingDataSets)
        {
            ArrayList<DataSet> dataSetsForExperiment =
                    experimentDataSets.get(dataSet.getExperimentIdentifier());
            if (null == dataSetsForExperiment)
            {
                dataSetsForExperiment = new ArrayList<DataSet>();
                experimentDataSets.put(dataSet.getExperimentIdentifier(), dataSetsForExperiment);
            }
            dataSetsForExperiment.add(dataSet);
        }
    }

    /**
     * Connect to openBis to download the data sets
     */
    private void downloadDataSets()
    {
        dataSetFiles = new HashMap<DataSet, File>();
        for (DataSet dataSet : matchingDataSets)
        {
            // This method retrieves a link to the data set if the file system it is on can be
            // mounted locally. In this case, it cannot be mounted locally, so don't even try.
            File location = dataSet.getLinkOrCopyOfContent(null, downloadFolder, "original/tsv");
            dataSetFiles.put(dataSet, location);
        }
    }

    /**
     * Find data sets that contain data for the specified strains.
     */
    private List<DataSet> retrieveDataSetsReferencingStrains(List<String> strainNames)
    {
        // Construct a search criteria that matches any of the strains specified.
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        for (String strainName : strainNames)
        {
            searchCriteria.addMatchClause(MatchClause.createPropertyMatch(STRAIN_NAMES_PROPERTY,
                    strainName));
        }
        return openBis.searchForDataSets(searchCriteria);
    }

    private void printHeader()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Experiment");
        for (MetadataField field : MetadataField.values())
        {
            sb.append("\t");
            sb.append(field.headerText);
        }
        sb.append("\t");
        sb.append(REGISTERED_BY_HEADER);
        sb.append("\t");
        sb.append(DATA_SET_HEADER);
        sb.append("\t");
        sb.append(STRAINS_HEADER);
        sb.append("\t");
        sb.append(PATH_HEADER);
        println(sb.toString());
    }

    private void printEmptyExperimentInformationOn(StringBuffer sb)
    {
        // Experiment Identifier
        sb.append("\t");
        for (@SuppressWarnings("unused")
        MetadataField field : MetadataField.values())
        {
            sb.append("\t");
        }
        // REGISTERED_BY_HEADER
        sb.append("\t");

        // We have now tabbed to allign with the data set information
    }

    private void printExperiment(Experiment experiment)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(experiment.getIdentifier());
        printExperimentPropertiesOn(experiment.getProperties(), sb);
        printRegisteredByOn(experiment.getRegistrationDetails(), sb);
        printDataSetsOn(experiment, sb);
        println(sb.toString());
    }

    private void printExperimentPropertiesOn(Map<String, String> properties, StringBuffer sb)
    {
        for (MetadataField field : MetadataField.values())
        {
            sb.append("\t");
            String value = properties.get(field.toString());
            if (null != value)
            {
                sb.append(value);
            }
        }
    }

    /**
     * Get the strains for the experiment and print them to the string buffer.
     * 
     * @throws IOException
     */
    private void printDataSetsOn(Experiment experiment, StringBuffer sb)
    {
        sb.append("\t");

        // dataSets cannot be null, because we only have experiments that have data sets here
        ArrayList<DataSet> dataSets = experimentDataSets.get(experiment.getIdentifier());

        boolean isFirstLine = true;
        for (DataSet dataSet : dataSets)
        {
            String dataSetCode = dataSet.getCode();
            String strainNames = dataSet.getProperties().get(STRAIN_NAMES_PROPERTY);
            String path;
            try
            {
                path = dataSetFiles.get(dataSet).getCanonicalPath();
            } catch (IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
            if (false == isFirstLine)
            {
                sb.append("\n");
                printEmptyExperimentInformationOn(sb);
            }
            sb.append(dataSetCode);
            sb.append("\t");
            sb.append(strainNames);
            sb.append("\t");
            sb.append(path);
            isFirstLine = false;
        }
    }

    private void printRegisteredByOn(EntityRegistrationDetails registrationDetails, StringBuffer sb)
    {
        sb.append("\t");
        sb.append(registrationDetails.getUserEmail());
    }
}
