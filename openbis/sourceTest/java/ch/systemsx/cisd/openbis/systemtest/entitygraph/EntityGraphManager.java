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

package ch.systemsx.cisd.openbis.systemtest.entitygraph;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistrationAndModificationDate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Helper class which creates a graph of entities of kind experiment, sample, and data set based on a
 * textual description. A simple language defines the entities (experiments, samples, and data sets) and its relations. 
 * From the textual definition of the graph real entities are created in the database. The actual graph of
 * entities as stored in the data base can be rendered in the same language used for parsing. This allows to
 * checked expected changes after some (re-)assignment between the entities has been performed. It is also
 * possible to checked whether the entities have been modified (i.e. modifier and modification date have been changed)
 * or not. Here is an example:
 * <pre>
 * EntityGraphGenerator g = entityGraphManager.parseAndCreateGraph("E1, data sets: DS4\n"
                + "S1, data sets: DS1[NECT] DS2[NECT]\n"
                + "S2\n"
                + "S3, data sets: DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT] DS3[NET]\n"
                + "DS2[NECT], components: DS4");
        
        reassignToSample(g.ds(1), g.s(2));
        
        assertEquals("E1, data sets: DS4\n"
                + "S2, data sets: DS1[NECT] DS2[NECT]\n"
                + "S3, data sets: DS3[NET]\n"
                + "DS1[NECT], components: DS2[NECT] DS3[NET]\n"
                + "DS2[NECT], components: DS4\n", entityGraphManager.renderGraph(g));
        entityGraphManager.assertModified(g.s(1), g.s(2));
        entityGraphManager.assertModified(g.ds(1), g.ds(2));
        entityGraphManager.assertUnmodified(g);
 * </pre>
 * 
 *
 * @author Franz-Josef Elmer
 */
public class EntityGraphManager
{
    private final IServiceForDataStoreServer service;
    private final ICommonServer commonService;
    private final String sessionToken;
    
    private Space defaultSpace;
    private Project defaultProject;
    private ExperimentType defaultExperimentType;
    private SampleType defaultSampleType;
    
    private EntityRepository repository;
    
    public EntityGraphManager(IServiceForDataStoreServer service, ICommonServer commonService, String sessionToken)
    {
        this.service = service;
        this.commonService = commonService;
        this.sessionToken = sessionToken;
    }
    
    public EntityGraphGenerator parseAndCreateGraph(String graphDefinition)
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse(graphDefinition);
        createGraph(g);
        assertEquals(removeSolitaryNodes(graphDefinition), renderGraph(g, true));
        repository.gatherModifictaionInfos();
        return g;
    }
    
    public void addToRepository(ExperimentNode experimentNode, Experiment experiment)
    {
        repository.put(experimentNode, experiment);
    }

    public void assertModified(ExperimentNode[] experimentNodes)
    {
        repository.assertModified(experimentNodes);
    }

    public void assertModified(SampleNode[] sampleNodes)
    {
        repository.assertModified(sampleNodes);
    }

    public void assertModified(DataSetNode[] dataSetNodes)
    {
        repository.assertModified(dataSetNodes);
    }

    public void assertUnmodified(EntityGraphGenerator g)
    {
        repository.assertUnmodified(g);
    }

    public String getIdentifierOfDefaultProject()
    {
        return defaultProject.getIdentifier();
    }

    public String getExperimentIdentifierOrNull(ExperimentNode experimentNodeOrNull)
    {
        return repository.getExperimentIdentifierOrNull(experimentNodeOrNull);
    }
    
    public Sample getSample(SampleNode sampleNode)
    {
        return repository.getSample(sampleNode);
    }
    
    public Sample tryGetSample(SampleNode sampleNode)
    {
        return repository.tryGetSample(sampleNode);
    }

    public String getSamplePermIdOrNull(SampleNode sampleNodeOrNull)
    {
        return repository.getSamplePermIdOrNull(sampleNodeOrNull);
    }
    
    public AbstractExternalData getDataSet(DataSetNode dataSetNode)
    {
        return repository.getDataSet(dataSetNode);
    }
    
    public AbstractExternalData tryGetDataSet(DataSetNode dataSetNode)
    {
        return repository.tryGetDataSet(dataSetNode);
    }

    public String getDataSetCodeOrNull(DataSetNode dataSetNodeOrNull)
    {
        return repository.getDataSetCodeOrNull(dataSetNodeOrNull);
    }
    
    public String renderGraph(EntityGraphGenerator g)
    {
        return renderGraph(g, false);
    }
    
    public String renderGraph(EntityGraphGenerator g, boolean showWhereIAm)
    {
        repository.refreshGraph(showWhereIAm);
        StringBuilder builder = new StringBuilder();
        for (ExperimentNode experimentNode : g.getExperiments().values())
        {
            StringBuilder builder2 = new StringBuilder();
            render(builder2, "samples", repository.getSampleNodes(experimentNode));
            render(builder2, "data sets", repository.getDataSetNodes(experimentNode));
            appendNodeTo(builder, getIdentifierAndType(experimentNode), builder2);
        }
        for (SampleNode sampleNode : g.getSamples().values())
        {
            StringBuilder builder2 = new StringBuilder();
            render(builder2, "components", repository.getComponentSampleNodes(sampleNode));
            render(builder2, "children", repository.getChildrenSampleNodes(sampleNode));
            render(builder2, "data sets", repository.getDataSetNodes(sampleNode));
            appendNodeTo(builder, getIdentifierAndType(sampleNode), builder2);
        }
        for (DataSetNode dataSetNode : g.getDataSets().values())
        {
            StringBuilder builder2 = new StringBuilder();
            render(builder2, "components", repository.getComponentDataSetNodes(dataSetNode));
            render(builder2, "parents", repository.getParentDataSetNodes(dataSetNode));
            appendNodeTo(builder, dataSetNode.getIdentifierAndType(), builder2);
        }
        return builder.toString();
    }

    private String getIdentifierAndType(ExperimentNode experimentNode)
    {
        String projectIdentifier = repository.getExperiment(experimentNode).getProject().getIdentifier();
        if (experimentNode.getProject() != null || defaultProject.getIdentifier().equals(projectIdentifier) == false)
        {
            return projectIdentifier + "/" + experimentNode.getCodeAndType();
        }
        return experimentNode.getIdentifierAndType();
    }

    private String getIdentifierAndType(SampleNode sampleNode)
    {
        Sample sample = repository.getSample(sampleNode);
        if (sampleNode.isShared() || sampleNode.getSpace() != null || defaultSpace.equals(sample.getSpace()) == false)
        {
            return (sample.getSpace() == null ? "" : sample.getSpace().getIdentifier()) + "/" + sampleNode.getCodeAndType();
        }
        return sampleNode.getIdentifierAndType();
    }
    
    private String removeSolitaryNodes(String graphDefinition)
    {
        StringBuilder builder = new StringBuilder();
        String[] lines = graphDefinition.split("\n");
        for (String line : lines)
        {
            if (line.contains(","))
            {
                builder.append(line).append("\n");
            }
        }
        return builder.toString();
    }
    
    private void appendNodeTo(StringBuilder builder, String identifierAndType, StringBuilder builder2)
    {
        if (builder2.length() > 0)
        {
            builder.append(identifierAndType);
            builder.append(builder2).append("\n");
        }
    }
    
    private void render(StringBuilder builder, String name, Collection<? extends EntityNode> nodes)
    {
        render(builder, name, getIdentifiers(nodes));
    }
    
    private List<String> getIdentifiers(Collection<? extends EntityNode> nodes)
    {
        List<String> identifiers = new ArrayList<String>();
        for (EntityNode node : nodes)
        {
            String identifierAndType = node.getIdentifierAndType();
            if (node instanceof SampleNode)
            {
                identifierAndType = getIdentifierAndType((SampleNode) node);
            }
            identifiers.add(identifierAndType);
        }
        return identifiers;
    }
    
    private void render(StringBuilder builder, String name, List<String> identifiers)
    {
        if (identifiers.isEmpty())
        {
            return;
        }
        builder.append(", ").append(name).append(":");
        for (String identifier : identifiers)
        {
            builder.append(' ').append(identifier);
        }
    }
    
    private Space createSpace(String spaceCode)
    {
        SpaceIdentifier spaceIdentifier = new SpaceIdentifier(spaceCode);
        Space space = service.tryGetSpace(sessionToken, spaceIdentifier);
        if (space != null)
        {
            return space;
        }
        commonService.registerSpace(sessionToken, spaceCode, null);
        return service.tryGetSpace(sessionToken, spaceIdentifier);
    }
    
    private Project createProject(Space space, String projectCode)
    {
        ProjectIdentifier projectIdentifier = new ProjectIdentifier(space.getCode(), projectCode);
        Project project = service.tryGetProject(sessionToken, projectIdentifier);
        if (project != null)
        {
            return project;
        }
        commonService.registerProject(sessionToken, projectIdentifier, null, null, 
                Collections.<NewAttachment>emptyList());
        return commonService.getProjectInfo(sessionToken, projectIdentifier);
    }
    
    private ExperimentType createExperimentType(String typeCode)
    {
        for (ExperimentType experimentType : commonService.listExperimentTypes(sessionToken))
        {
            if (experimentType.getCode().equals(typeCode))
            {
                return experimentType;
            }
        }
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(typeCode);
        commonService.registerExperimentType(sessionToken, experimentType);
        return service.getExperimentType(sessionToken, typeCode);
    }
    
    private SampleType createSampleType(String typeCode)
    {
        for (SampleType sampleType : commonService.listSampleTypes(sessionToken))
        {
            if (sampleType.getCode().equals(typeCode))
            {
                return sampleType;
            }
        }
        SampleType sampleType = new SampleType();
        sampleType.setCode(typeCode);
        sampleType.setGeneratedCodePrefix("prefix");
        commonService.registerSampleType(sessionToken, sampleType);
        return service.getSampleType(sessionToken, typeCode);
    }
    
    private DataSetType createDataSetType(String typeCode, DataSetKind dataSetKind)
    {
        for (DataSetType dataSetType : commonService.listDataSetTypes(sessionToken))
        {
            if (dataSetType.getCode().equals(typeCode))
            {
                return dataSetType;
            }
        }
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(typeCode);
        dataSetType.setDataSetKind(dataSetKind);
        commonService.registerDataSetType(sessionToken, dataSetType);
        dataSetType = service.getDataSetType(sessionToken, typeCode).getDataSetType();
        return dataSetType;
    }
    
    private Experiment createExperiment(ExperimentNode experimentNode, Project project)
    {
        try
        {
            String projectIdentifier = project.getIdentifier();
            if (experimentNode.getProject() != null)
            {
                Space space = createSpace(experimentNode.getSpace());
                projectIdentifier = createProject(space, experimentNode.getProject()).getIdentifier();
            }
            String identifier = projectIdentifier + "/" + experimentNode.getCode() + generateUniqueId();
            service.registerExperiment(sessionToken, new NewExperiment(identifier, defaultExperimentType.getCode()));
            return service.tryGetExperiment(sessionToken, ExperimentIdentifierFactory.parse(identifier));
        } catch (Exception ex)
        {
            throw new RuntimeException("Error while creating experiment for node " + experimentNode.getCode() 
                    + ": " + ex.getMessage(), ex);
        }
    }

    private Experiment refresh(Experiment experiment)
    {
        return commonService.getExperimentInfo(sessionToken, new TechId(experiment.getId()));
    }

    private Sample createSample(SampleNode sampleNode, Space space)
    {
        try
        {
            NewSample sample = new NewSample();
            sample.setIdentifier(createSampleIdentifier(space, sampleNode));
            sample.setSampleType(defaultSampleType);
            ExperimentNode experimentNode = sampleNode.getExperiment();
            if (experimentNode != null)
            {
                sample.setExperimentIdentifier(repository.tryGetExperiment(experimentNode).getIdentifier());
            }
            List<String> parentIdentifiers = getParentIdentifiers(sampleNode);
            sample.setParentsOrNull(parentIdentifiers.toArray(new String[0]));
            SampleNode container = sampleNode.getContainer();
            if (container != null)
            {
                Sample containerSample = repository.tryGetSample(container);
                if (containerSample == null)
                {
                    throw new IllegalStateException("Sample " + container.getCode() 
                            + " is specified as container of " + sampleNode.getCode() 
                            + " but hasn't yet created.");
                }
                sample.setContainerIdentifier(containerSample.getIdentifier());
            }
            sample.setProperties(new IEntityProperty[0]);
            sample.setAttachments(Collections.<NewAttachment>emptyList());
            service.registerSample(sessionToken, sample, null);
            return service.tryGetSampleWithExperiment(sessionToken, SampleIdentifierFactory.parse(sample));
        } catch (Exception ex)
        {
            throw new RuntimeException("Error while creating sample for node " + sampleNode.getCode() 
                    + ": " + ex.getMessage(), ex);
        }
    }

    private List<String> getParentIdentifiers(SampleNode sampleNode)
    {
        List<String> parentIdentifiers = new ArrayList<String>();
        for (SampleNode parent : sampleNode.getParents())
        {
            Sample parentSample = repository.tryGetSample(parent);
            if (parentSample == null)
            {
                throw new IllegalStateException("Sample " + parent.getCode() 
                        + " is specified as parent of " + sampleNode.getCode() 
                        + " but hasn't yet created.");
            }
            parentIdentifiers.add(parentSample.getIdentifier());
        }
        return parentIdentifiers;
    }

    private String createSampleIdentifier(Space space, SampleNode sampleNode)
    {
        String prefix = space == null ? "/" : space.getIdentifier() + "/";
        if (sampleNode.isShared())
        {
            prefix = "/";
        } else if (sampleNode.getSpace() != null)
        {
            prefix = createSpace(sampleNode.getSpace()).getIdentifier() + "/";
        }
        return prefix + sampleNode.getCode() + generateUniqueId();
    }
    
    private Sample refresh(Sample sample)
    {
        SampleParentWithDerived result = commonService.getSampleInfo(sessionToken, new TechId(sample.getId()));
        return result.getParent();
    }
    
    private AbstractExternalData createDataSet(DataSetNode dataSetNode)
    {
        try
        {
            NewExternalData dataSet = new NewExternalData();
            DataSetKind dataSetKind = DataSetKind.PHYSICAL;
            List<DataSetNode> components = dataSetNode.getComponents();
            if (components.isEmpty() == false)
            {
                dataSetKind = DataSetKind.CONTAINER;
                NewContainerDataSet cont = new NewContainerDataSet();
                cont.setContainedDataSetCodes(getRelatedDataSetIdentifiers(dataSetNode, "component", components));
                dataSet = cont;
            }
            dataSet.setCode(dataSetNode.getCode() + generateUniqueId());
            String dataSetTypeCode = dataSetNode.getType();
            if (dataSetTypeCode == null)
            {
                dataSetTypeCode = "UNKNOWN-" + generateUniqueId();
            }
            dataSet.setDataSetType(createDataSetType(dataSetTypeCode, dataSetKind));
            dataSet.setFileFormatType(new FileFormatType(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE));
            dataSet.setLocatorType(new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE));
            dataSet.setLocation(UUID.randomUUID().toString());
            dataSet.setStorageFormat(StorageFormat.PROPRIETARY);
            dataSet.setDataStoreCode("STANDARD");
            addSampleOrExperimentIdentifier(dataSet, dataSetNode);
            dataSet.setParentDataSetCodes(getRelatedDataSetIdentifiers(dataSetNode, "parent", dataSetNode.getParents()));
            if (dataSet.getSampleIdentifierOrNull() != null)
            {
                service.registerDataSet(sessionToken, dataSet.getSampleIdentifierOrNull(), dataSet);
            } else
            {
                service.registerDataSet(sessionToken, dataSet.getExperimentIdentifierOrNull(), dataSet);
            }
            return service.tryGetDataSet(sessionToken, dataSet.getCode());
        } catch (Exception ex)
        {
            throw new RuntimeException("Error while creating data set for node " + dataSetNode.getIdentifierAndType() 
                    + ": " + ex.getMessage(), ex);
        }
    }

    private void addSampleOrExperimentIdentifier(NewExternalData dataSet, DataSetNode dataSetNode)
    {
        if (dataSetNode.getSample() != null)
        {
            Sample sample = repository.tryGetSample(dataSetNode.getSample());
            dataSet.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample));
        } else if (dataSetNode.getExperiment() != null)
        {
            String identifier = repository.tryGetExperiment(dataSetNode.getExperiment()).getIdentifier();
            dataSet.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(identifier));
        }
    }

    private List<String> getRelatedDataSetIdentifiers(DataSetNode dataSetNode, String relationType, List<DataSetNode> dataSetNodes)
    {
        List<String> parentCodes = new ArrayList<String>();
        for (DataSetNode parent : dataSetNodes)
        {
            AbstractExternalData parentDataSet = repository.tryGetDataSet(parent);
            if (parentDataSet == null)
            {
                throw new IllegalStateException("Data set " + parent.getCode() 
                        + " is specified as " + relationType + " of " + dataSetNode.getCode() 
                        + " but hasn't yet created.");
            }
            parentCodes.add(parentDataSet.getCode());
        }
        return parentCodes;
    }

    private AbstractExternalData refresh(AbstractExternalData data)
    {
        return service.tryGetDataSet(sessionToken, data.getCode());
    }

    private String generateUniqueId()
    {
        return "-" + service.createPermId(sessionToken);
    }
    
    private void createGraph(EntityGraphGenerator g)
    {
        repository = new EntityRepository();
        defaultSpace = createSpace("S0");
        defaultProject = createProject(defaultSpace, "P0");
        defaultExperimentType = createExperimentType("ET");
        defaultSampleType = createSampleType("ST");
        createExperiments(defaultProject, g);
        createSamples(defaultSpace, g);
        createDataSets(g);
    }

    private void createDataSets(EntityGraphGenerator g)
    {
        List<DataSetNode> dataSetNodes = new ArrayList<DataSetNode>(g.getDataSets().values());
        Collections.reverse(dataSetNodes);
        for (DataSetNode dataSetNode : dataSetNodes)
        {
            repository.put(dataSetNode, createDataSet(dataSetNode));
        }
    }

    private void createSamples(Space space, EntityGraphGenerator g)
    {
        ArrayList<SampleNode> sampleNodes = new ArrayList<SampleNode>(g.getSamples().values());
//        Collections.reverse(sampleNodes);
        for (SampleNode sampleNode : sampleNodes)
        {
            repository.put(sampleNode, createSample(sampleNode, space));
        }
    }
    
    private void createExperiments(Project project, EntityGraphGenerator g)
    {
        for (ExperimentNode experimentNode : g.getExperiments().values())
        {
            repository.put(experimentNode, createExperiment(experimentNode, project));
        }
    }
    
    private final class EntityRepository
    {
        private Map<Long, Experiment> experimentsNodeToDtoMap = new TreeMap<Long, Experiment>();
        private Map<Long, ExperimentNode> experimentDtoToNodeMap = new TreeMap<Long, ExperimentNode>();
        private Map<Long, ModificationInfo> experimentModificationInfoByNodeId = new HashMap<Long, ModificationInfo>();
        private Set<ExperimentNode> modifiedExperimentNodes = new HashSet<ExperimentNode>();
        
        private Map<Long, Sample> samplesNodeToDtoMap = new TreeMap<Long, Sample>();
        private Map<Long, SampleNode> samplesDtoToNodeMap = new TreeMap<Long, SampleNode>();
        private Map<Long, ModificationInfo> sampleModificationInfoByNodeId = new HashMap<Long, ModificationInfo>();
        private Set<SampleNode> modifiedSampleNodes = new HashSet<SampleNode>();

        private Map<Long, AbstractExternalData> dataSetsNodeToDtoMap = new TreeMap<Long, AbstractExternalData>();
        private Map<Long, DataSetNode> dataSetsDtoToNodeMap = new TreeMap<Long, DataSetNode>();
        private Map<Long, ModificationInfo> dataSetModificationInfoByNodeId = new HashMap<Long, ModificationInfo>();
        private Set<DataSetNode> modifiedDataSetNodes = new HashSet<DataSetNode>();

        private Map<Long, Set<Long>> experimentSamplesMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> childrenSamplesMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> componentSamplesMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> experimentDataSetsMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> sampleDataSetsMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> componentDataSetsMap = new HashMap<Long, Set<Long>>();
        private Map<Long, Set<Long>> parentDataSetsMap = new HashMap<Long, Set<Long>>();
        
        public String renderNodeToDtoMapping()
        {
            StringBuilder builder = new StringBuilder();
            for (Entry<Long, Experiment> entry : experimentsNodeToDtoMap.entrySet())
            {
                render(builder, "E", entry.getKey(), entry.getValue());
            }
            for (Entry<Long, Sample> entry : samplesNodeToDtoMap.entrySet())
            {
                render(builder, "S", entry.getKey(), entry.getValue());
            }
            for (Entry<Long, AbstractExternalData> entry : dataSetsNodeToDtoMap.entrySet())
            {
                render(builder, "DS", entry.getKey(), entry.getValue());
            }
            return builder.toString();
        }
        
        private void render(StringBuilder builder, String prefix, Long id, 
                CodeWithRegistrationAndModificationDate<?> entity)
        {
            builder.append(prefix).append(id).append(" -> ").append(entity.getCode()).append(" (");
            builder.append(entity.getModifier().getUserId()).append(", ");
            builder.append(entity.getModificationDate()).append(")\n");
        }
        
        public String getExperimentIdentifierOrNull(ExperimentNode experimentNodeOrNull)
        {
            return experimentNodeOrNull == null ? null : getExperiment(experimentNodeOrNull).getIdentifier();
        }
        
        public Experiment getExperiment(ExperimentNode experimentNode)
        {
            Experiment experiment = tryGetExperiment(experimentNode);
            if (experiment == null)
            {
                throw new IllegalArgumentException("Unknown experiment " + experimentNode.getCode());
            }
            return experiment;
        }
        
        public Experiment tryGetExperiment(ExperimentNode experimentNode)
        {
            return experimentsNodeToDtoMap.get(experimentNode.getId());
        }
        
        public String getSamplePermIdOrNull(SampleNode sampleNodeOrNull)
        {
            return sampleNodeOrNull == null ? null : getSample(sampleNodeOrNull).getPermId();
        }
        
        public Sample getSample(SampleNode sampleNode)
        {
            Sample sample = tryGetSample(sampleNode);
            if (sample == null)
            {
                throw new IllegalArgumentException("Unknown sample " + sampleNode.getCode());
            }
            return sample;
        }
        
        public Sample tryGetSample(SampleNode sampleNode)
        {
            return samplesNodeToDtoMap.get(sampleNode.getId());
        }
        
        public String getDataSetCodeOrNull(DataSetNode dataSetNodeOrNull)
        {
            return dataSetNodeOrNull == null ? null : getDataSet(dataSetNodeOrNull).getCode();
        }
        
        public AbstractExternalData getDataSet(DataSetNode dataSetNode)
        {
            AbstractExternalData dataSet = tryGetDataSet(dataSetNode);
            if (dataSet == null)
            {
                throw new IllegalArgumentException("Unknown data set " + dataSetNode.getCode());
            }
            return dataSet;
        }
        
        public AbstractExternalData tryGetDataSet(DataSetNode dataSetNode)
        {
            return dataSetsNodeToDtoMap.get(dataSetNode.getId());
        }
        
        void refreshGraph(boolean showWhereIAm)
        {
            refreshEntityDtos();
            refreshSamplesMap();
            refreshDataSetsMap();
            if (showWhereIAm)
            {
                printWhereIAm();
            }
            System.out.println("Entities mapping:\n" + renderNodeToDtoMapping());
        }

        private void refreshEntityDtos()
        {
            for (Long id : experimentsNodeToDtoMap.keySet())
            {
                experimentsNodeToDtoMap.put(id, refresh(experimentsNodeToDtoMap.get(id)));
            }
            for (Long id : samplesNodeToDtoMap.keySet())
            {
                samplesNodeToDtoMap.put(id, refresh(samplesNodeToDtoMap.get(id)));
            }
            for (Long id : dataSetsNodeToDtoMap.keySet())
            {
                dataSetsNodeToDtoMap.put(id, refresh(dataSetsNodeToDtoMap.get(id)));
            }
        }

        private void refreshSamplesMap()
        {
            experimentSamplesMap.clear();
            childrenSamplesMap.clear();
            componentSamplesMap.clear();
            for (Sample sample : samplesNodeToDtoMap.values())
            {
                Experiment experiment = sample.getExperiment();
                addToEntitiesMap(experimentSamplesMap, experiment, sample);
                for (Sample parentSample : sample.getParents())
                {
                    addToEntitiesMap(childrenSamplesMap, parentSample, sample);
                }
                Sample container = sample.getContainer();
                if (container != null)
                {
                    addToEntitiesMap(componentSamplesMap, container, sample);
                }
            }
        }

        private void refreshDataSetsMap()
        {
            experimentDataSetsMap.clear();
            sampleDataSetsMap.clear();
            componentDataSetsMap.clear();
            parentDataSetsMap.clear();
            for (AbstractExternalData dataSet : dataSetsNodeToDtoMap.values())
            {
                addToEntitiesMap(experimentDataSetsMap, dataSet.getExperiment(), dataSet);
                addToEntitiesMap(sampleDataSetsMap, dataSet.getSample(), dataSet);
                for (ContainerDataSet containerDataSet : dataSet.getContainerDataSets())
                {
                    addToEntitiesMap(componentDataSetsMap, containerDataSet, dataSet);
                }
                for (AbstractExternalData parentDataSet : dataSet.getParents())
                {
                    addToEntitiesMap(parentDataSetsMap, dataSet, parentDataSet);
                }
            }
        }

        private void printWhereIAm()
        {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 5, n = Math.min(stackTrace.length, 9); i < n; i++)
            {
                StackTraceElement element = stackTrace[i];
                System.out.print(element.getMethodName() + " <- ");
            }
            System.out.println("...");
        }
        
        private void addToEntitiesMap(Map<Long, Set<Long>> entityToEntitiesMap, IIdHolder keyEntity, IIdHolder valueEntity)
        {
            if (keyEntity != null)
            {
                Long id = keyEntity.getId();
                Set<Long> entityIds = entityToEntitiesMap.get(id);
                if (entityIds == null)
                {
                    entityIds = new TreeSet<Long>();
                    entityToEntitiesMap.put(id, entityIds);
                }
                entityIds.add(valueEntity.getId());
            }
        }
        
        void gatherModifictaionInfos()
        {
            for (Entry<Long, Experiment> entry : experimentsNodeToDtoMap.entrySet())
            {
                experimentModificationInfoByNodeId.put(entry.getKey(), new ModificationInfo(entry.getValue()));
            }
            for (Entry<Long, Sample> entry : samplesNodeToDtoMap.entrySet())
            {
                sampleModificationInfoByNodeId.put(entry.getKey(), new ModificationInfo(entry.getValue()));
            }
            for (Entry<Long, AbstractExternalData> entry : dataSetsNodeToDtoMap.entrySet())
            {
                dataSetModificationInfoByNodeId.put(entry.getKey(), new ModificationInfo(entry.getValue()));
            }
        }
        
        void put(ExperimentNode experimentNode, Experiment experiment)
        {
            experimentsNodeToDtoMap.put(experimentNode.getId(), experiment);
            experimentDtoToNodeMap.put(experiment.getId(), experimentNode);
        }
        
        public void assertModified(ExperimentNode...experimentNodes)
        {
            assertModificationInfo(true, experimentModificationInfoByNodeId, experimentsNodeToDtoMap, 
                    Arrays.<EntityNode>asList(experimentNodes));
            modifiedExperimentNodes.addAll(Arrays.asList(experimentNodes));
        }
        
        void put(SampleNode sampleNode, Sample sample)
        {
            samplesNodeToDtoMap.put(sampleNode.getId(), sample);
            samplesDtoToNodeMap.put(sample.getId(), sampleNode);
        }

        public void assertModified(SampleNode...sampleNodes)
        {
            assertModificationInfo(true, sampleModificationInfoByNodeId, samplesNodeToDtoMap, 
                    Arrays.<EntityNode>asList(sampleNodes));
            modifiedSampleNodes.addAll(Arrays.asList(sampleNodes));
        }
        
        void put(DataSetNode dataSetNode, AbstractExternalData dataSet)
        {
            dataSetsNodeToDtoMap.put(dataSetNode.getId(), dataSet);
            dataSetsDtoToNodeMap.put(dataSet.getId(), dataSetNode);
        }
        
        public void assertModified(DataSetNode...dataSetNodes)
        {
            assertModificationInfo(true, dataSetModificationInfoByNodeId, dataSetsNodeToDtoMap, 
                    Arrays.<EntityNode>asList(dataSetNodes));
            modifiedDataSetNodes.addAll(Arrays.asList(dataSetNodes));
        }
        
        public void assertUnmodified(EntityGraphGenerator g)
        {
            Set<EntityNode> unmodifiedExperimentNodes = new HashSet<EntityNode>(g.getExperiments().values());
            unmodifiedExperimentNodes.removeAll(modifiedExperimentNodes);
            assertModificationInfo(false, experimentModificationInfoByNodeId, experimentsNodeToDtoMap, unmodifiedExperimentNodes);
            Set<EntityNode> unmodifiedSampleNodes = new HashSet<EntityNode>(g.getSamples().values());
            unmodifiedSampleNodes.removeAll(modifiedSampleNodes);
            assertModificationInfo(false, sampleModificationInfoByNodeId, samplesNodeToDtoMap, unmodifiedSampleNodes);
            Set<EntityNode> unmodifiedDataSetNodes = new HashSet<EntityNode>(g.getDataSets().values());
            unmodifiedDataSetNodes.removeAll(modifiedDataSetNodes);
            assertModificationInfo(false, dataSetModificationInfoByNodeId, dataSetsNodeToDtoMap, unmodifiedDataSetNodes);
        }
        
        private void assertModificationInfo(boolean modified, Map<Long, ModificationInfo> previousInfos,
                Map<Long, ? extends CodeWithRegistrationAndModificationDate<?>> nodeToDtoMap,
                Collection<EntityNode> entityNodes)
        {
            for (EntityNode node : entityNodes)
            {
                ModificationInfo previous = previousInfos.get(node.getId());
                if (previous == null)
                {
                    if (modified == false)
                    {
                        continue;
                    } else
                    {
                        throw new AssertionError(node.getCode() + " no previous modification info");
                    }
                }
                assertNotNull(node.getCode() + " no previous modification info", previous);
                CodeWithRegistrationAndModificationDate<?> entity = nodeToDtoMap.get(node.getId());
                assertNotNull(node.getCode() + " unknown", entity);
                ModificationInfo current = new ModificationInfo(entity);
                if (modified)
                {
                    assertEquals(node.getCode() + " has unexpectedly still the old modifier: " + current.modifier, 
                            false, current.modifier.equals(previous.modifier));
                    assertEquals(node.getCode() + " has unexpectedly still the old modification date: " + current.modificationDate, 
                            true, current.modificationDate.getTime() > previous.modificationDate.getTime());
                } else
                {
                    assertEquals(node.getCode() + " has unexpectedly a new modifier: ",
                            previous.modifier, current.modifier);
                    assertEquals(node.getCode() + " has unexpectedly a new modification date:", 
                            previous.modificationDate, current.modificationDate);
                }
            }
        }

        Set<SampleNode> getSampleNodes(ExperimentNode experimentNode)
        {
            return getEntityNodes(experimentSamplesMap, experimentsNodeToDtoMap.get(experimentNode.getId()), 
                    samplesDtoToNodeMap);
        }
        
        Set<SampleNode> getChildrenSampleNodes(SampleNode sampleNode)
        {
            return getEntityNodes(childrenSamplesMap, samplesNodeToDtoMap.get(sampleNode.getId()), 
                    samplesDtoToNodeMap);
        }
        
        Set<SampleNode> getComponentSampleNodes(SampleNode sampleNode)
        {
            return getEntityNodes(componentSamplesMap, samplesNodeToDtoMap.get(sampleNode.getId()), 
                    samplesDtoToNodeMap);
        }
        
        Set<DataSetNode> getDataSetNodes(ExperimentNode experimentNode)
        {
            return getDataSetNodes(experimentDataSetsMap, experimentsNodeToDtoMap.get(experimentNode.getId()));
        }

        Set<DataSetNode> getDataSetNodes(SampleNode sampleNode)
        {
            return getDataSetNodes(sampleDataSetsMap, samplesNodeToDtoMap.get(sampleNode.getId()));
        }
        
        Set<DataSetNode> getComponentDataSetNodes(DataSetNode containerDataSetNode)
        {
            return getDataSetNodes(componentDataSetsMap, dataSetsNodeToDtoMap.get(containerDataSetNode.getId()));
        }
        
        Set<DataSetNode> getParentDataSetNodes(DataSetNode childDataSetNode)
        {
            return getDataSetNodes(parentDataSetsMap, dataSetsNodeToDtoMap.get(childDataSetNode.getId()));
        }
        
        private Set<DataSetNode> getDataSetNodes(Map<Long, Set<Long>> idHolderDataSetsMap, IIdHolder idHolder)
        {
            return getEntityNodes(idHolderDataSetsMap, idHolder, dataSetsDtoToNodeMap);
        }

        private <T extends EntityNode> Set<T> getEntityNodes(Map<Long, Set<Long>> idHolderEntitiesMap, IIdHolder idHolder, 
                Map<Long, T> dtoToNodeMap)
        {
            Set<T> result = new TreeSet<T>();
            if (idHolder != null)
            {
                Set<Long> entityDtoIds = idHolderEntitiesMap.get(idHolder.getId());
                if (entityDtoIds != null)
                {
                    for (Long dtoId : entityDtoIds)
                    {
                        result.add(dtoToNodeMap.get(dtoId));
                    }
                }
            }
            return result;
        }
    }
    
    private static final class ModificationInfo
    {
        private final String modifier;
        private final Date modificationDate;

        ModificationInfo(CodeWithRegistrationAndModificationDate<?> entity)
        {
            modifier = entity.getModifier().getUserId();
            modificationDate = entity.getModificationDate();
        }
    }

}
