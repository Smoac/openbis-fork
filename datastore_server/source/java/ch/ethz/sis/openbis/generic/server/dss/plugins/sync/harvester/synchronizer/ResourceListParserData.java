/*
 * Copyright 2016 ETH Zuerich, SIS
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
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewLinkDataSet;

/**
 * @author Ganime Betul Akin
 */
public class ResourceListParserData
{
    // Introduced to store the timestamp of any still-running transaction on the data source at the time of
    // retrieving the resource list.
    private Date resourceListTimestamp;

    private Set<String> harvesterSpaceList = new HashSet<>();

    private MasterData masterData = new MasterData();

    private Map<String, ProjectWithConnections> projectsToProcess = new HashMap<String, ResourceListParserData.ProjectWithConnections>();

    private Map<String, ExperimentWithConnections> experimentsToProcess = new HashMap<String, ResourceListParserData.ExperimentWithConnections>();

    private Map<String, SampleWithConnections> samplesToProcess = new HashMap<String, ResourceListParserData.SampleWithConnections>();

    private Map<String, DataSetWithConnections> dataSetsToProcess = new HashMap<String, ResourceListParserData.DataSetWithConnections>();

    private Map<String, MaterialWithLastModificationDate> materialsToProcess = new HashMap<String, MaterialWithLastModificationDate>();

    public MasterData getMasterData()
    {
        return masterData;
    }

    public Date getResourceListTimestamp()
    {
        return resourceListTimestamp;
    }

    public void setResourceListTimestamp(Date resourceListTimestamp)
    {
        this.resourceListTimestamp = resourceListTimestamp;
    }

    public Set<String> getHarvesterSpaceList()
    {
        return harvesterSpaceList;
    }

    public Map<String, ProjectWithConnections> getProjectsToProcess()
    {
        return projectsToProcess;
    }

    public Map<String, ExperimentWithConnections> getExperimentsToProcess()
    {
        return experimentsToProcess;
    }

    public Map<String, SampleWithConnections> getSamplesToProcess()
    {
        return samplesToProcess;
    }

    public Map<String, DataSetWithConnections> getDataSetsToProcess()
    {
        return dataSetsToProcess;
    }

    public Map<String, MaterialWithLastModificationDate> getMaterialsToProcess()
    {
        return materialsToProcess;
    }

    public Map<String, DataSetWithConnections> filterPhysicalDataSetsByLastModificationDate(Date lastSyncDate, Set<String> dataSetsCodesToRetry)
    {
        Map<String, DataSetWithConnections> dsMap = new HashMap<String, ResourceListParserData.DataSetWithConnections>();
        for (String permId : dataSetsToProcess.keySet())
        {
            DataSetWithConnections ds = dataSetsToProcess.get(permId);
            if (ds.getKind() == DataSetKind.PHYSICAL
                    && (ds.lastModificationDate.after(lastSyncDate) || dataSetsCodesToRetry.contains(ds.getDataSet().getCode())))
            {
                dsMap.put(permId, ds);
            }
        }
        return dsMap;
    }

    public Map<String, DataSetWithConnections> filterContainerDataSets()
    {
        // List<NewDataSetWithConnections> dsList = new ArrayList<ResourceListParserData.NewDataSetWithConnections>();
        Map<String, DataSetWithConnections> dsMap = new HashMap<String, ResourceListParserData.DataSetWithConnections>();
        for (String permId : dataSetsToProcess.keySet())
        {
            DataSetWithConnections ds = dataSetsToProcess.get(permId);
            if (ds.getKind() == DataSetKind.CONTAINER)
            {
                dsMap.put(permId, ds);
            }
        }
        return dsMap;
    }

    class ProjectWithConnections
    {
        private final NewProject project;

        private final Date lastModificationDate;

        private boolean hasAttachments;

        public boolean hasAttachments()
        {
            return hasAttachments;
        }

        public void setHasAttachments(boolean hasAttachments)
        {
            this.hasAttachments = hasAttachments;
        }
        public NewProject getProject()
        {
            return project;
        }

        private List<Connection> connections = new ArrayList<Connection>();

        public List<Connection> getConnections()
        {
            return connections;
        }

        ProjectWithConnections(NewProject project, Date lastModDate)
        {
            this.project = project;
            this.lastModificationDate = lastModDate;
        }

        public Date getLastModificationDate()
        {
            return lastModificationDate;
        }

        void addConnection(Connection conn)
        {
            this.connections.add(conn);
        }

        public void setConnections(List<Connection> conns)
        {
            // TODO do this better
            this.connections = conns;
        }
    }

    class ExperimentWithConnections
    {
        private final NewExperiment experiment;

        private final Date lastModificationDate;

        private boolean hasAttachments;

        public boolean hasAttachments()
        {
            return hasAttachments;
        }

        public void setHasAttachments(boolean hasAttachments)
        {
            this.hasAttachments = hasAttachments;
        }

        public NewExperiment getExperiment()
        {
            return experiment;
        }

        public List<Connection> getConnections()
        {
            return connections;
        }

        private List<Connection> connections = new ArrayList<Connection>();

        ExperimentWithConnections(NewExperiment exp, Date lastModDate)
        {
            this.experiment = exp;
            this.lastModificationDate = lastModDate;
        }

        public Date getLastModificationDate()
        {
            return lastModificationDate;
        }

        public void setConnections(List<Connection> conns)
        {
            // TODO do this better
            this.connections = conns;
        }
    }

    class SampleWithConnections
    {
        private final NewSample sample;

        private final Date lastModificationDate;

        private boolean hasAttachments;

        public boolean hasAttachments()
        {
            return hasAttachments;
        }

        public void setHasAttachments(boolean hasAttachments)
        {
            this.hasAttachments = hasAttachments;
        }
        public Date getLastModificationDate()
        {
            return lastModificationDate;
        }

        public NewSample getSample()
        {
            return sample;
        }

        SampleWithConnections(NewSample sample, Date lastModDate)
        {
            super();
            this.sample = sample;
            this.lastModificationDate = lastModDate;
        }

        private List<Connection> connections = new ArrayList<Connection>();

        public List<Connection> getConnections()
        {
            return connections;
        }

        public void setConnections(List<Connection> conns)
        {
            // TODO do this better
            this.connections = conns;
        }
    }

    class DataSetWithConnections implements Serializable
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private final NewExternalData dataSet;

        private final Date lastModificationDate;

        public Date getLastModificationDate()
        {
            return lastModificationDate;
        }

        public DataSetKind getKind()
        {
            if (dataSet instanceof NewContainerDataSet)
                return DataSetKind.CONTAINER;
            else if (dataSet instanceof NewLinkDataSet)
                return DataSetKind.LINK;
            return DataSetKind.PHYSICAL;
        }

        public NewExternalData getDataSet()
        {
            return dataSet;
        }

        DataSetWithConnections(NewExternalData dataSet, Date lastModDate)
        {
            super();
            this.dataSet = dataSet;
            this.lastModificationDate = lastModDate;
        }

        private List<Connection> connections = new ArrayList<Connection>();

        public List<Connection> getConnections()
        {
            return connections;
        }

        public void setConnections(List<Connection> conns)
        {
            // TODO do this better
            this.connections = conns;
        }
    }

    class MasterData
    {
        private Map<String, FileFormatType> fileFormatTypesToProcess = new HashMap<String, FileFormatType>();

        private Map<String, Script> validationPluginsToProcess = new HashMap<String, Script>();

        private Map<String, NewVocabulary> vocabulariesToProcess = new HashMap<String, NewVocabulary>();

        private Map<String, PropertyType> propertyTypesToProcess = new HashMap<String, PropertyType>();

        private Map<String, SampleType> sampleTypesToProcess = new HashMap<String, SampleType>();

        private Map<String, DataSetType> dataSetTypesToProcess = new HashMap<String, DataSetType>();

        private Map<String, ExperimentType> experimentTypesToProcess = new HashMap<String, ExperimentType>();

        private Map<String, MaterialType> materialTypesToProcess = new HashMap<String, MaterialType>();

        private MultiKeyMap<String, List<NewETPTAssignment>> propertyAssignmentsToProcess = new MultiKeyMap<String, List<NewETPTAssignment>>();

        public MultiKeyMap<String, List<NewETPTAssignment>> getPropertyAssignmentsToProcess()
        {
            return propertyAssignmentsToProcess;
        }

        public Map<String, Script> getValidationPluginsToProcess()
        {
            return validationPluginsToProcess;
        }

        public void setValidationPluginsToProcess(Map<String, Script> validationPluginsToProcess)
        {
            this.validationPluginsToProcess = validationPluginsToProcess;
        }

        public Map<String, PropertyType> getPropertyTypesToProcess()
        {
            return propertyTypesToProcess;
        }

        public Map<String, DataSetType> getDataSetTypesToProcess()
        {
            return dataSetTypesToProcess;
        }

        public Map<String, ExperimentType> getExperimentTypesToProcess()
        {
            return experimentTypesToProcess;
        }

        public Map<String, MaterialType> getMaterialTypesToProcess()
        {
            return materialTypesToProcess;
        }

        public Map<String, SampleType> getSampleTypesToProcess()
        {
            return sampleTypesToProcess;
        }

        public Map<String, NewVocabulary> getVocabulariesToProcess()
        {
            return vocabulariesToProcess;
        }

        public Map<String, FileFormatType> getFileFormatTypesToProcess()
        {
            return fileFormatTypesToProcess;
        }

        public void setFileFormatTypesToProcess(Map<String, FileFormatType> fileFormatTypesToProcess)
        {
            this.fileFormatTypesToProcess = fileFormatTypesToProcess;
        }

        public void setVocabulariesToProcess(Map<String, NewVocabulary> vocabulariesToProcess)
        {
            this.vocabulariesToProcess = vocabulariesToProcess;
        }

        public void setPropertyTypesToProcess(Map<String, PropertyType> propertyTypesToProcess)
        {
            this.propertyTypesToProcess = propertyTypesToProcess;
        }

        public void setSampleTypesToProcess(Map<String, SampleType> sampleTypesToProcess)
        {
            this.sampleTypesToProcess = sampleTypesToProcess;
        }

        public void setDataSetTypesToProcess(Map<String, DataSetType> dataSetTypesToProcess)
        {
            this.dataSetTypesToProcess = dataSetTypesToProcess;
        }

        public void setExperimentTypesToProcess(Map<String, ExperimentType> experimentTypesToProcess)
        {
            this.experimentTypesToProcess = experimentTypesToProcess;
        }

        public void setMaterialTypesToProcess(Map<String, MaterialType> materialTypesToProcess)
        {
            this.materialTypesToProcess = materialTypesToProcess;
        }

        public void setPropertyAssignmentsToProcess(MultiKeyMap<String, List<NewETPTAssignment>> propertyAssignmentsToProcess)
        {
            this.propertyAssignmentsToProcess = propertyAssignmentsToProcess;
        }
    }

    class Connection
    {
        final String toPermId;

        final String connType;

        public String getType()
        {
            return connType;
        }

        Connection(String toPermId, String connType)
        {
            super();
            this.toPermId = toPermId;
            this.connType = connType;
        }

        public String getToPermId()
        {
            return toPermId;
        }
    }

    enum ConnectionType
    {
        SIMPLE_CONNECTION("Connection"),
        PARENT_CHILD_RELATIONSHIP("Child"),
        CONTAINER_COMPONENT_RELATIONSHIP("Component");

        private final String type;

        public String getType()
        {
            return type;
        }

        private ConnectionType(String type)
        {
            this.type = type;
        }
    }

    class MaterialWithLastModificationDate
    {
        private final NewMaterialWithType material;

        private final Date lastModificationDate;

        public NewMaterialWithType getMaterial()
        {
            return material;
        }

        MaterialWithLastModificationDate(NewMaterialWithType material, Date lastModDate)
        {
            this.material = material;
            this.lastModificationDate = lastModDate;
        }

        public Date getLastModificationDate()
        {
            return lastModificationDate;
        }
    }
}