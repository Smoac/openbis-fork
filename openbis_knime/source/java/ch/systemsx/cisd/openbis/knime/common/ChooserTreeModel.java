/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;

/**
 * The tree model for {@link OwnerChooser}.
 *
 * @author Franz-Josef Elmer
 */
public class ChooserTreeModel extends DefaultTreeModel
{
    static final String LOADING_TEXT = "Loading data...";

    private static final long serialVersionUID = 1L;

    private static interface ILoadingBuildingAction<T>
    {
        public T load();
        public void build(T data);
    }
    
    private static final class RootNode implements IChooserTreeNode<String>
    {
        static final RootNode ROOT = new RootNode();

        @Override
        public ChooserTreeNodeType getNodeType()
        {
            return ChooserTreeNodeType.ROOT;
        }

        @Override
        public String getNodeObject()
        {
            return "root";
        }

        @Override
        public String toString()
        {
            return getNodeObject();
        }
    }
    
    private static final class SpaceNode implements IChooserTreeNode<String>
    {
        private final String spaceCode;

        SpaceNode(String spaceCode)
        {
            this.spaceCode = spaceCode;
        }

        @Override
        public ChooserTreeNodeType getNodeType()
        {
            return ChooserTreeNodeType.SPACE;
        }

        @Override
        public String getNodeObject()
        {
            return spaceCode;
        }

        @Override
        public String toString()
        {
            return getNodeObject();
        }
    }
    
    private static final class ProjectNode implements IChooserTreeNode<Project>
    {
        private final Project project;

        ProjectNode(Project project)
        {
            this.project = project;
        }

        @Override
        public ChooserTreeNodeType getNodeType()
        {
            return ChooserTreeNodeType.PROJECT;
        }

        @Override
        public Project getNodeObject()
        {
            return project;
        }

        @Override
        public String toString()
        {
            return project.getCode();
        }
    }
    
    private static final class ExperimentNode implements IChooserTreeNode<Experiment>
    {
        private final Experiment experiment;

        ExperimentNode(Experiment experiment)
        {
            this.experiment = experiment;
        }

        @Override
        public ChooserTreeNodeType getNodeType()
        {
            return ChooserTreeNodeType.EXPERIMENT;
        }

        @Override
        public Experiment getNodeObject()
        {
            return experiment;
        }

        @Override
        public String toString()
        {
            return experiment.getCode();
        }
    }
    
    private static final class SampleNode implements IChooserTreeNode<Sample>
    {
        private final Sample sample;

        SampleNode(Sample sample)
        {
            this.sample = sample;
        }

        @Override
        public ChooserTreeNodeType getNodeType()
        {
            return ChooserTreeNodeType.SAMPLE;
        }

        @Override
        public Sample getNodeObject()
        {
            return sample;
        }

        @Override
        public String toString()
        {
            return sample.getIdentifier();
        }
    }
    
    private static final class DataSetNode implements IChooserTreeNode<DataSet>
    {
        private final DataSet dataSet;
        
        DataSetNode(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }
        
        @Override
        public ChooserTreeNodeType getNodeType()
        {
            return ChooserTreeNodeType.DATA_SET;
        }
        
        @Override
        public DataSet getNodeObject()
        {
            return dataSet;
        }
        
        @Override
        public String toString()
        {
            return dataSet.getCode();
        }
    }
    
    private static final class SamplesAndDataSets
    {
        private final List<Sample> samples;
        private final List<DataSet> dataSets;

        SamplesAndDataSets(List<Sample> samples, List<DataSet> dataSets)
        {
            this.samples = samples;
            this.dataSets = dataSets;
        }

        public List<Sample> getSamples()
        {
            return samples;
        }

        public List<DataSet> getDataSets()
        {
            return dataSets;
        }
    }

    private static final Comparator<Project> PROJECT_COMPARATOR = new Comparator<Project>()
        {
            @Override
            public int compare(Project p1, Project p2)
            {
                return p1.getIdentifier().compareTo(p2.getIdentifier());
            }
        };

    private static final Comparator<Experiment> EXPERIMENT_COMPARATOR =
            new Comparator<Experiment>()
                {
                    @Override
                    public int compare(Experiment e1, Experiment e2)
                    {
                        return e1.getCode().compareTo(e2.getCode());
                    }
                };

    private static final Comparator<Sample> SAMPLE_COMPARATOR = new Comparator<Sample>()
        {
            @Override
            public int compare(Sample s1, Sample s2)
            {
                return s1.getIdentifier().compareTo(s2.getIdentifier());
            }
        };

    private static final Comparator<DataSet> DATA_SET_COMPARATOR = new Comparator<DataSet>()
        {
            @Override
            public int compare(DataSet ds1, DataSet ds2)
            {
                return ds1.getCode().compareTo(ds2.getCode());
            }
        };
                        
    private final IGeneralInformationService service;
    private final DataSetOwnerType ownerType;
    private final String sessionToken;

    private List<SampleType> sampleTypes;

    ChooserTreeModel(DataSetOwnerType ownerType, String sessionToken,
            IGeneralInformationService service)
    {
        super(new DefaultMutableTreeNode());
        this.ownerType = ownerType;
        this.sessionToken = sessionToken;
        this.service = service;
        addSpaceAndProjectNodes();
        sampleTypes = service.listSampleTypes(sessionToken);
    }

    private void addSpaceAndProjectNodes()
    {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
        rootNode.setUserObject(RootNode.ROOT);
        TreeMap<String, List<Project>> spaceToProjectsMap = new TreeMap<String, List<Project>>();
        List<Project> projects = service.listProjects(sessionToken);
        Collections.sort(projects, PROJECT_COMPARATOR);
        for (Project project : projects)
        {
            List<Project> list = spaceToProjectsMap.get(project.getSpaceCode());
            if (list == null)
            {
                list = new ArrayList<Project>();
                spaceToProjectsMap.put(project.getSpaceCode(), list);
            }
            list.add(project);
        }
        for (Entry<String, List<Project>> entry : spaceToProjectsMap.entrySet())
        {
            String spaceCode = entry.getKey();
            DefaultMutableTreeNode spaceNode = new DefaultMutableTreeNode(new SpaceNode(spaceCode));
            rootNode.add(spaceNode);
            for (Project project : entry.getValue())
            {
                DefaultMutableTreeNode projectNode =
                        new DefaultMutableTreeNode(new ProjectNode(project));
                spaceNode.add(projectNode);
                projectNode.add(createLoadingNode());
            }
        }
    }

    public boolean isSelectable(TreePath path)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        if (userObject instanceof IChooserTreeNode == false)
        {
            return false;
        }
        IChooserTreeNode<?> treeNode = (IChooserTreeNode<?>) userObject;
        ChooserTreeNodeType nodeType = treeNode.getNodeType();
        switch (ownerType)
        {
            case EXPERIMENT: return nodeType == ChooserTreeNodeType.EXPERIMENT;
            case SAMPLE: return nodeType == ChooserTreeNodeType.SAMPLE;
            case DATA_SET: return nodeType == ChooserTreeNodeType.DATA_SET;
        }
        return false;
    }
    
    public void collapsNode(TreePath path)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        if (userObject instanceof IChooserTreeNode)
        {
            IChooserTreeNode<?> treeNode = (IChooserTreeNode<?>) userObject;
            if (EnumSet.of(ChooserTreeNodeType.PROJECT, ChooserTreeNodeType.EXPERIMENT,
                    ChooserTreeNodeType.SAMPLE).contains(treeNode.getNodeType()))
            {
                node.removeAllChildren();
                node.add(createLoadingNode());
                nodeStructureChanged(node);
            }
        }
    }
    
    public void expandNode(TreePath path, IAsyncNodeAction action)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        Project project = tryGetWrappedObject(Project.class, userObject);
        if (project != null)
        {
            expandProjectNode(node, project, action);
            return;
        }
        Experiment experiment = tryGetWrappedObject(Experiment.class, userObject);
        if (experiment != null)
        {
            expandExperimentNode(node, experiment, action);
            return;
        }
        Sample sample = tryGetWrappedObject(Sample.class, userObject);
        if (sample != null)
        {
            expandSampleNode(node, sample, action);
        }
    }

    private void expandProjectNode(final DefaultMutableTreeNode node, final Project project,
            IAsyncNodeAction action)
    {
        executeAsync(new ILoadingBuildingAction<List<Experiment>>()
            {
                @Override
                public List<Experiment> load()
                {
                    List<Experiment> experiments =
                            service.listExperiments(sessionToken, Arrays.asList(project), null);
                    Collections.sort(experiments, EXPERIMENT_COMPARATOR);
                    return experiments;
                }

                @Override
                public void build(List<Experiment> data)
                {
                    node.removeAllChildren();
                    for (Experiment experiment : data)
                    {
                        DefaultMutableTreeNode childNode =
                                new DefaultMutableTreeNode(new ExperimentNode(experiment));
                        node.add(childNode);
                        if (ownerType != DataSetOwnerType.EXPERIMENT)
                        {
                            childNode.add(createLoadingNode());
                        }
                    }
                    ChooserTreeModel.this.nodeStructureChanged(node);
                }
            }, action);
    }
    
    private void expandExperimentNode(final DefaultMutableTreeNode node, final Experiment experiment,
            IAsyncNodeAction action)
    {
        final String experimentPermId = experiment.getPermId();
        executeAsync(new ILoadingBuildingAction<SamplesAndDataSets>()
            {
                @Override
                public SamplesAndDataSets load()
                {
                    List<Sample> samples = new ArrayList<Sample>();
                    for (SampleType sampleType : sampleTypes)
                    {
                        if (sampleType.isListable())
                        {
                            SearchCriteria searchCriteria = new SearchCriteria();
                            searchCriteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
                            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                                    MatchClauseAttribute.TYPE,
                                    sampleType.getCode()));
                            SearchCriteria experimentCriteria = new SearchCriteria();
                            experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                                    MatchClauseAttribute.PERM_ID, experimentPermId));
                            searchCriteria.addSubCriteria(SearchSubCriteria
                                    .createExperimentCriteria(experimentCriteria));
                            samples.addAll(service.searchForSamples(sessionToken, searchCriteria,
                                    null));
                        }
                    }
                    Collections.sort(samples, SAMPLE_COMPARATOR);
                    List<DataSet> dataSets = new ArrayList<DataSet>();
                    if (ownerType == DataSetOwnerType.DATA_SET)
                    {
                        SearchCriteria searchCriteria = new SearchCriteria();
                        SearchCriteria experimentCriteria = new SearchCriteria();
                        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                                MatchClauseAttribute.PERM_ID, experimentPermId));
                        searchCriteria.addSubCriteria(SearchSubCriteria
                                .createExperimentCriteria(experimentCriteria));
                        dataSets = service.searchForDataSets(sessionToken, searchCriteria);
                        Collections.sort(dataSets, DATA_SET_COMPARATOR);
                    }
                    return new SamplesAndDataSets(samples, dataSets);
                }

                @Override
                public void build(SamplesAndDataSets data)
                {
                    addSampleAndDataSetNodes(node, data);
                }
            }, action);
    }

    private void expandSampleNode(final DefaultMutableTreeNode node, final Sample sample,
            IAsyncNodeAction action)
    {
        final String samplePermId = sample.getPermId();
        executeAsync(new ILoadingBuildingAction<SamplesAndDataSets>()
            {
                @Override
                public SamplesAndDataSets load()
                {
                    List<Sample> samples = new ArrayList<Sample>();
                    SearchCriteria sampleCriteria = new SearchCriteria();
                    sampleCriteria.addMatchClause(MatchClause.createAttributeMatch(
                            MatchClauseAttribute.PERM_ID, samplePermId));
                    List<Sample> sampleWithChildren =
                            service.searchForSamples(sessionToken, sampleCriteria,
                                    EnumSet.of(SampleFetchOption.CHILDREN));
                    if (sampleWithChildren.isEmpty() == false)
                    {
                        addFilteredSamples(samples, sampleWithChildren.get(0).getChildren());
                    }
                    SearchCriteria searchCriteria = new SearchCriteria();
                    searchCriteria.addSubCriteria(SearchSubCriteria
                            .createSampleContainerCriteria(sampleCriteria));
                    addFilteredSamples(samples,
                            service.searchForSamples(sessionToken, searchCriteria, null));
                    Collections.sort(samples, SAMPLE_COMPARATOR);
                    List<DataSet> dataSets = new ArrayList<DataSet>();
                    if (ownerType == DataSetOwnerType.DATA_SET)
                    {
                        searchCriteria = new SearchCriteria();
                        sampleCriteria = new SearchCriteria();
                        sampleCriteria.addMatchClause(MatchClause.createAttributeMatch(
                                MatchClauseAttribute.PERM_ID, samplePermId));
                        searchCriteria.addSubCriteria(SearchSubCriteria
                                .createSampleCriteria(sampleCriteria));
                        dataSets = service.searchForDataSets(sessionToken, searchCriteria);
                        Collections.sort(dataSets, DATA_SET_COMPARATOR);
                    }
                    return new SamplesAndDataSets(samples, dataSets);
                }

                @Override
                public void build(SamplesAndDataSets data)
                {
                    addSampleAndDataSetNodes(node, data);
                }
            }, action);
    }
    
    private void addFilteredSamples(List<Sample> samples, List<Sample> samplesToAdd)
    {
        boolean ownerIsSample = ownerType != DataSetOwnerType.SAMPLE;
        for (Sample sample : samplesToAdd)
        {
            if (ownerIsSample || sample.getExperimentIdentifierOrNull() != null)
            {
                samples.add(sample);
            }
        }
    }

    private void addSampleAndDataSetNodes(DefaultMutableTreeNode node, SamplesAndDataSets data)
    {
        node.removeAllChildren();
        for (DataSet dataSet : data.getDataSets())
        {
            node.add(new DefaultMutableTreeNode(new DataSetNode(dataSet)));
        }
        for (Sample sample : data.getSamples())
        {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new SampleNode(sample));
            node.add(childNode);
            childNode.add(createLoadingNode());
        }
        nodeStructureChanged(node);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T tryGetWrappedObject(Class<T> clazz, Object nodeObject)
    {
        if (nodeObject instanceof IChooserTreeNode == false)
        {
            return null;
        }
        Object treeNode = ((IChooserTreeNode<?>) nodeObject).getNodeObject();
        return clazz.isInstance(treeNode) ? (T) treeNode : null;
    }
    
    private DefaultMutableTreeNode createLoadingNode()
    {
        return new DefaultMutableTreeNode(LOADING_TEXT);
    }
    
    private <T> void executeAsync(final ILoadingBuildingAction<T> loadingBuildingAction,
            final IAsyncNodeAction action)
    {
        execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final T data = loadingBuildingAction.load();
                        action.execute(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    loadingBuildingAction.build(data);
                                }
                            });
                    } catch (Throwable ex)
                    {
                        action.handleException(ex);
                    }
                }
            });
    }

    void execute(Runnable runnable)
    {
        new Thread(runnable).start();
    }

}
