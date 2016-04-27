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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * A class that, given a live connection to openBIS, prints a listing of experiments with metadata.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ExperimentLister extends AbstractBaSynthecClient
{
    private static final String STRAIN_NAMES_PROPERTY = "STRAIN_NAMES";

    private static final String STRAINS_HEADER = "Strains";

    private static final String REGISTERED_BY_HEADER = "Contact Person";

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

    // State we compute as part of the operation of this class

    // The experiments we are interested in
    private List<Experiment> experiments;

    // A mapping from experiments to data sets
    private HashMap<String, ArrayList<DataSet>> experimentDataSets;

    /**
     * The public constructor
     * 
     * @param facade The facade for interacting with openBIS.
     */
    public ExperimentLister(IOpenbisServiceFacade facade)
    {
        super(facade);
    }

    /**
     * Using the connection to openBIS, get all experiments in the specified projects and print the following information about the experiment:<br>
     * <ul>
     * <li>Experiment Date</li>
     * <li>Description</li>
     * <li>Medium</li>
     * <li>Temperature</li>
     * <li>Growth Conditions</li>
     * <li>Strains</li>
     * </ul>
     * 
     * @param projectIdentifiers The projects in which the experiments we are interested in are housed.
     */
    public void run(List<String> projectIdentifiers)
    {
        retrieveInformationFromOpenBis(projectIdentifiers);

        printHeader();
        for (Experiment experiment : experiments)
        {
            printExperiment(experiment);
        }
    }

    /**
     * Connect to openBis to get the information we need.
     */
    private void retrieveInformationFromOpenBis(List<String> projectIdentifiers)
    {
        // Get the experiments for the specified project
        experiments = openBis.listExperimentsForProjects(projectIdentifiers);

        // Get the data sets
        List<String> experimentIdentifiers = new ArrayList<String>();
        for (Experiment experiment : experiments)
        {
            experimentIdentifiers.add(experiment.getIdentifier());
        }

        List<DataSet> dataSets = openBis.listDataSetsForExperiments(experimentIdentifiers);

        experimentDataSets = new HashMap<String, ArrayList<DataSet>>();
        for (DataSet dataSet : dataSets)
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
        sb.append(STRAINS_HEADER);
        sb.append("\t");
        sb.append(REGISTERED_BY_HEADER);
        println(sb.toString());
    }

    private void printExperiment(Experiment experiment)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(experiment.getIdentifier());
        printExperimentPropertiesOn(experiment.getProperties(), sb);
        printExperimentStrainsOn(experiment, sb);
        printRegisteredByOn(experiment.getRegistrationDetails(), sb);
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
     */
    private void printExperimentStrainsOn(Experiment experiment, StringBuffer sb)
    {
        sb.append("\t");

        ArrayList<DataSet> dataSets = experimentDataSets.get(experiment.getIdentifier());
        if (null == dataSets)
        {
            return;
        }

        // collect all the strain names
        ArrayList<String> strains = new ArrayList<String>();
        for (DataSet dataSet : dataSets)
        {
            String strainNames = dataSet.getProperties().get(STRAIN_NAMES_PROPERTY);
            if (null != strainNames)
            {
                strains.add(strainNames);
            }
        }

        if (strains.size() < 1)
        {
            return;
        }

        // Append them to the StringBuffer
        sb.append(strains.get(0));
        for (int i = 1; i < strains.size(); ++i)
        {
            sb.append(",");
            sb.append(strains.get(i));
        }
    }

    private void printRegisteredByOn(EntityRegistrationDetails registrationDetails, StringBuffer sb)
    {
        sb.append("\t");
        if (null != registrationDetails)
        {
            sb.append(registrationDetails.getUserEmail());
        }
    }
}
