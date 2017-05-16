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

import org.apache.commons.collections4.map.MultiKeyMap;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;
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

    private Map<String, IncomingProject> projectsToProcess = new HashMap<String, ResourceListParserData.IncomingProject>();

    private Map<String, IncomingExperiment> experimentsToProcess = new HashMap<String, ResourceListParserData.IncomingExperiment>();

    private Map<String, IncomingSample> samplesToProcess = new HashMap<String, ResourceListParserData.IncomingSample>();

    private Map<String, IncomingDataSet> dataSetsToProcess = new HashMap<String, ResourceListParserData.IncomingDataSet>();

    private MultiKeyMap<String, MaterialWithLastModificationDate> materialsToProcess = new MultiKeyMap<String, MaterialWithLastModificationDate>();

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

    public Map<String, IncomingProject> getProjectsToProcess()
    {
        return projectsToProcess;
    }

    public Map<String, IncomingExperiment> getExperimentsToProcess()
    {
        return experimentsToProcess;
    }

    public Map<String, IncomingSample> getSamplesToProcess()
    {
        return samplesToProcess;
    }

    public Map<String, IncomingDataSet> getDataSetsToProcess()
    {
        return dataSetsToProcess;
    }

    public MultiKeyMap<String, MaterialWithLastModificationDate> getMaterialsToProcess()
    {
        return materialsToProcess;
    }

    public Map<String, IncomingDataSet> filterPhysicalDataSetsByLastModificationDate(Date lastSyncDate, Set<String> dataSetsCodesToRetry)
    {
        Map<String, IncomingDataSet> dsMap = new HashMap<String, ResourceListParserData.IncomingDataSet>();
        for (String permId : dataSetsToProcess.keySet())
        {
            IncomingDataSet ds = dataSetsToProcess.get(permId);
            if (ds.getKind() == DataSetKind.PHYSICAL
                    && (ds.lastModificationDate.after(lastSyncDate) || dataSetsCodesToRetry.contains(ds.getDataSet().getCode())))
            {
                dsMap.put(permId, ds);
            }
        }
        return dsMap;
    }

    public List<IncomingEntity<?>> filterAttachmentHoldersByLastModificationDate(Date lastSyncTimestamp, Set<String> attachmentHoldersToRetry)
    {
        List<IncomingEntity<?>> attachmentHoldersToProcess = new ArrayList<ResourceListParserData.IncomingEntity<?>>();
        // projects
        for (IncomingProject incomingProject : projectsToProcess.values())
        {
            if (syncAttachments(lastSyncTimestamp, attachmentHoldersToRetry, incomingProject) == true)
            {
                attachmentHoldersToProcess.add(incomingProject);
            }
        }
        // experiments
        for (IncomingExperiment incomingExperiment : experimentsToProcess.values())
        {
            if (syncAttachments(lastSyncTimestamp, attachmentHoldersToRetry, incomingExperiment) == true)
            {
                attachmentHoldersToProcess.add(incomingExperiment);
            }
        }
        // samples
        for (IncomingSample incomingSample : samplesToProcess.values())
        {
            if (syncAttachments(lastSyncTimestamp, attachmentHoldersToRetry, incomingSample) == true)
            {
                attachmentHoldersToProcess.add(incomingSample);
            }
        }
        return attachmentHoldersToProcess;
    }

    /**
     * Since we have no way of knowing if only the attachments have been in some way modified (more of a problem if the attachment has been deleted),
     * we need to process all modified entities and also retry any that have failed before. When the attachment holders are processed later, we check
     * if the entity has attachments to only query attachments for entities that has been marked as having attachments in the XML resource list.
     */
    private boolean syncAttachments(Date lastSyncTimestamp, Set<String> attachmentHoldersToRetry, IncomingEntity<?> incomingEntity)
    {
        Identifier<?> entity = incomingEntity.getEntity();
        if (incomingEntity.getLastModificationDate().after(lastSyncTimestamp)
                || attachmentHoldersToRetry.contains(incomingEntity.getEntityKind().getLabel() + "-" + entity.getPermID()))
        {
            return true;
        }
        return false;
    }

    public Map<String, IncomingDataSet> filterContainerDataSets()
    {
        Map<String, IncomingDataSet> dsMap = new HashMap<String, ResourceListParserData.IncomingDataSet>();
        for (String permId : dataSetsToProcess.keySet())
        {
            IncomingDataSet ds = dataSetsToProcess.get(permId);
            if (ds.getKind() == DataSetKind.CONTAINER)
            {
                dsMap.put(permId, ds);
            }
        }
        return dsMap;
    }

    public class IncomingEntity<T extends Identifier<T>>
    {
        private final Identifier<T> entity;

        private final SyncEntityKind entityKind;

        private List<Connection> connections = new ArrayList<Connection>();

        private boolean hasAttachments;

        public List<Connection> getConnections()
        {
            return connections;
        }

        void addConnection(Connection conn)
        {
            this.connections.add(conn);
        }

        public SyncEntityKind getEntityKind()
        {
            return entityKind;
        }

        public void setConnections(List<Connection> conns)
        {
            // TODO do this better
            this.connections = conns;
        }

        public boolean hasAttachments()
        {
            return hasAttachments;
        }

        public void setHasAttachments(boolean hasAttachments)
        {
            this.hasAttachments = hasAttachments;
        }

        public Identifier<T> getEntity()
        {
            return entity;
        }

        public String getIdentifer()
        {
            return getEntity().getIdentifier();
        }

        public String getPermID()
        {
            return getEntity().getPermID();
        }

        public Date getLastModificationDate()
        {
            return lastModificationDate;
        }

        private final Date lastModificationDate;

        IncomingEntity(Identifier<T> entity, SyncEntityKind entityKind, Date lastModDate)
        {
            this.entity = entity;
            this.entityKind = entityKind;
            this.lastModificationDate = lastModDate;
        }
    }

    class IncomingProject extends IncomingEntity<NewProject>
    {
        public NewProject getProject()
        {
            return (NewProject) getEntity();
        }

        IncomingProject(NewProject project, Date lastModDate)
        {
            super(project, SyncEntityKind.PROJECT, lastModDate);
        }
    }

    class IncomingExperiment extends IncomingEntity<NewExperiment>
    {
        public NewExperiment getExperiment()
        {
            return (NewExperiment) getEntity();
        }
        IncomingExperiment(NewExperiment exp, Date lastModDate)
        {
            super(exp, SyncEntityKind.EXPERIMENT, lastModDate);
        }
    }

    class IncomingSample extends IncomingEntity<NewSample>
    {
        public NewSample getSample()
        {
            return (NewSample) getEntity();
        }

        IncomingSample(NewSample sample, Date lastModDate)
        {
            super(sample, SyncEntityKind.SAMPLE, lastModDate);
        }
    }

    public class IncomingDataSet implements Serializable
    {
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

        IncomingDataSet(NewExternalData dataSet, Date lastModDate)
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