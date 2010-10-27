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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.api.v1;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * Example of usage of Proteomics Data API.
 *
 * @author Franz-Josef Elmer
 */
public class ProteomicsDataApiTest
{
    public static void main(String[] args)
    {
        if (args.length != 4)
        {
            System.err.println("Usage: <openbis-server-url> <login id> <password> <user id>");
            return;
        }

        String serverURL = args[0];
        String loginID = args[1];
        String password = args[2];
        String userID = args[3];
        IProteomicsDataApiFacade facade = FacadeFactory.create(serverURL, loginID, password);
        
        System.out.println("MS_INJECTION samples:");
        List<MsInjectionDataInfo> rawDataSamples = facade.listRawDataSamples(userID);
        for (MsInjectionDataInfo info : rawDataSamples)
        {
            Map<String, Date> latestDataSets = info.getLatestDataSetRegistrationDates();
            if (latestDataSets.isEmpty() == false)
            {
                System.out.println("   " + info.getMsInjectionSampleCode() + " -> "
                        + info.getBiologicalSampleIdentifier() + " -> "
                        + info.getBiologicalExperimentIdentifier());
                Experiment experiment = info.getBiologicalExperiment();
                if (experiment != null)
                {
                    System.out.println("   biological experiment: "
                            + experiment.getCode() + " "
                            + experiment.getProperties());
                }
                System.out.println("   latest data sets: " + info.getLatestDataSetRegistrationDates());
                Set<DataSet> dataSets = info.getDataSets();
                for (DataSet dataSet : dataSets)
                {
                    print(dataSet, "         ");
                }
            }
        }
        
        System.out.println("DSS processing plugins:");
        List<DataStoreServerProcessingPluginInfo> infos = facade.listDataStoreServerProcessingPluginInfos();
        String dataSetProcessingKey = null;
        for (DataStoreServerProcessingPluginInfo info : infos)
        {
            System.out.println("   key:" + info.getKey() + ", label:'" + info.getLabel()
                    + "', data set types:" + info.getDatasetTypeCodes());
            if (info.getDatasetTypeCodes().contains("PROT_RESULT"))
            {
                dataSetProcessingKey = info.getKey();
            }
        }
        
        System.out.println("Projects:");
        List<Project> projects = facade.listProjects(userID);
        for (Project project : projects)
        {
            System.out.println(project);
        }
        
        System.out.println("Search Experiments:");
        List<Experiment> experiments = facade.listSearchExperiments(userID);
        long[] ids = new long[experiments.size()];
        for (int i = 0; i < experiments.size(); i++)
        {
            Experiment experiment = experiments.get(i);
            System.out.println(experiment.getSpaceCode() + "/" + experiment.getProjectCode() + "/"
                    + experiment.getCode() + " [" + experiment.getId() + ", "
                    + experiment.getRegistrationDate() + "] " + experiment.getProperties());
            ids[i] = experiment.getId();
        }
        
        if (dataSetProcessingKey != null)
        {
            System.out.println("Process search data of " + ids.length + " experiments");
            facade.processSearchData(userID, dataSetProcessingKey , ids);
        }
        
        facade.logout();
    }
    
    private static void print(DataSet dataSet, String indentation)
    {
        System.out.println(indentation + dataSet.getCode() + " " + dataSet.getType() + " "
                + dataSet.getRegistrationDate() + " " + dataSet.getProperties());
        Set<DataSet> children = dataSet.getChildren();
        for (DataSet child : children)
        {
            print(child, indentation + "  ");
        }
    }
}
