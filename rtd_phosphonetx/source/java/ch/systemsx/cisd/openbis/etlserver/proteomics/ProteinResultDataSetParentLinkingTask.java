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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinResultDataSetParentLinkingTask implements IMaintenanceTask
{
    private static final String PARENT_DATA_SET_CODES_KEY =
            DataSetInfoExtractorForProteinResults.PARENT_DATA_SET_CODES.toUpperCase();

    private static final String BASE_EXPERIMENT_KEY =
            DataSetInfoExtractorForProteinResults.EXPERIMENT_IDENTIFIER_KEY.toUpperCase();

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ProteinResultDataSetParentLinkingTask.class);

    private final IEncapsulatedOpenBISService service;

    public ProteinResultDataSetParentLinkingTask()
    {
        this(ServiceProvider.getOpenBISService());
    }

    ProteinResultDataSetParentLinkingTask(IEncapsulatedOpenBISService service)
    {
        this.service = service;

    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
    }

    @Override
    public void execute()
    {
        List<DataSetBatchUpdatesDTO> dataSetUpdates = new ArrayList<DataSetBatchUpdatesDTO>();
        List<Project> projects = service.listProjects();
        for (Project project : projects)
        {
            List<Experiment> experiments =
                    service.listExperiments(new ProjectIdentifier(project.getSpace().getCode(),
                            project.getCode()));
            for (Experiment experiment : experiments)
            {
                Map<String, IEntityProperty> propertiesMap = getPropertiesMap(experiment);
                String baseExperimentIdentifier =
                        tryGetProperty(propertiesMap, BASE_EXPERIMENT_KEY);
                String parentDataSetCodes =
                        tryGetProperty(propertiesMap, PARENT_DATA_SET_CODES_KEY);
                List<String> codes =
                        DataSetInfoExtractorForProteinResults.getParentDataSetCodes(
                                parentDataSetCodes, baseExperimentIdentifier, service)
                                .getDataSetCodes();
                if (codes.isEmpty())
                {
                    continue;
                }
                List<ExternalData> dataSets =
                        service.listDataSetsByExperimentID(experiment.getId());
                if (dataSets.isEmpty())
                {
                    continue;
                }
                for (ExternalData ds : dataSets)
                {
                    if (ds instanceof DataSet == false)
                    {
                        continue;
                    }
                    DataSet dataSet = (DataSet) ds;
                    DataSetBatchUpdatesDTO update = new DataSetBatchUpdatesDTO();
                    update.setDatasetId(new TechId(dataSet.getId()));
                    update.setVersion(dataSet.getVersion());
                    update.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(dataSet
                            .getExperiment().getIdentifier()));
                    update.setFileFormatTypeCode(dataSet.getFileFormatType().getCode());
                    update.setProperties(dataSet.getProperties());

                    // All we want to do is update the parents
                    update.setDatasetCode(dataSet.getCode());
                    update.setModifiedParentDatasetCodesOrNull(codes.toArray(new String[0]));
                    DataSetBatchUpdateDetails details = new DataSetBatchUpdateDetails();
                    details.setParentsUpdateRequested(true);

                    operationLog.info("Parent data set links of data set " + dataSet.getCode()
                            + " from experiment " + experiment.getIdentifier()
                            + " will be updated.");
                    dataSetUpdates.add(update);
                }
            }
        }
        service.performEntityOperations(new AtomicEntityOperationDetails(null, null, Collections
                .<NewSpace> emptyList(), Collections.<NewProject> emptyList(), Collections
                .<NewExperiment> emptyList(), Collections.<ExperimentUpdatesDTO> emptyList(),
                Collections.<SampleUpdatesDTO> emptyList(), Collections.<NewSample> emptyList(),
                Collections.<String, List<NewMaterial>> emptyMap(), Collections
                        .<MaterialUpdateDTO> emptyList(),
                Collections.<NewExternalData> emptyList(), dataSetUpdates, Collections
                        .<NewMetaproject> emptyList(), Collections
                        .<MetaprojectUpdatesDTO> emptyList(), Collections
                        .<VocabularyUpdatesDTO> emptyList()));
        operationLog.info("Parent data set links for " + dataSetUpdates.size()
                + " data sets have been updated.");
    }

    private Map<String, IEntityProperty> getPropertiesMap(Experiment experiment)
    {
        List<IEntityProperty> properties = experiment.getProperties();
        Map<String, IEntityProperty> propertiesMap = new HashMap<String, IEntityProperty>();
        for (IEntityProperty property : properties)
        {
            propertiesMap.put(property.getPropertyType().getCode(), property);
        }
        return propertiesMap;
    }

    private String tryGetProperty(Map<String, IEntityProperty> propertiesMap, String key)
    {
        IEntityProperty property = propertiesMap.get(key);
        return property == null ? null : property.tryGetAsString();
    }

}
