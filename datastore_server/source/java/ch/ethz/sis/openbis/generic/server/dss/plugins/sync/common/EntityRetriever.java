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
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common;

import static ch.ethz.sis.openbis.generic.shared.entitygraph.Edge.CHILD;
import static ch.ethz.sis.openbis.generic.shared.entitygraph.Edge.COMPONENT;
import static ch.ethz.sis.openbis.generic.shared.entitygraph.Edge.CONNECTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.entitygraph.Edge;
import ch.ethz.sis.openbis.generic.shared.entitygraph.EntityGraph;
import ch.ethz.sis.openbis.generic.shared.entitygraph.Node;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;

public class EntityRetriever 
{
    private static final EntityGraph<Node<?>> graph = new EntityGraph<Node<?>>();

    private final IApplicationServerApi v3Api;

    private final IMasterDataRegistrationTransaction masterDataRegistrationTransaction;

    private final String sessionToken;

    private EntityRetriever(IApplicationServerApi v3Api, String sessionToken, IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        this.v3Api = v3Api;
        this.sessionToken = sessionToken;
        this.masterDataRegistrationTransaction = masterDataRegistrationTransaction;
    }
    
    public static EntityRetriever createWithMasterDataRegistationTransaction(IApplicationServerApi v3Api, String sessionToken,
            IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        return new EntityRetriever(v3Api, sessionToken, masterDataRegistrationTransaction);
    }

    public static EntityRetriever createWithSessionToken(IApplicationServerApi v3Api, String sessionToken)
    {
        return new EntityRetriever(v3Api, sessionToken, null);
    }

    public EntityGraph<Node<?>> getEntityGraph(String spaceId)
    {
        // TODO fix this
        buildEntityGraph(spaceId);
        // graph.printGraph(spaceId);
        return graph;
    }

    public boolean spaceExists(String spaceId)
    {
        SpacePermId spacePermId = new SpacePermId(spaceId);
        Map<ISpaceId, Space> map =
                v3Api.getSpaces(sessionToken,
                        Arrays.asList(spacePermId),
                        new SpaceFetchOptions());
        return map.get(spacePermId) != null;
    }

    /**
     * Returns spaces the logged in user is allowed to see
     */
    public List<Space> getSpaces()
    {
        return v3Api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();
    }

    public void buildEntityGraph(String spaceId)
    {
        // TODO add experiment datasets.
        graph.clear();
        ProjectSearchCriteria prjCriteria = new ProjectSearchCriteria();
        prjCriteria.withSpace().withCode().thatEquals(spaceId);

        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withSpace();
        projectFetchOptions.withAttachments();

        List<Project> projects = v3Api.searchProjects(sessionToken, prjCriteria, projectFetchOptions).getObjects();
        for (Project project : projects)
        {
            Node<Project> prjNode = new Node<Project>(project);
            graph.addNode(prjNode);
            findExperiments(prjNode);
        }

        findSpaceSamples(spaceId);

        // TODO move the logout to the handler
        // v3.logout(sessionToken);
    }

    private void findExperiments(Node<Project> prjNode)
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withCode().thatEquals(prjNode.getCode());
        criteria.withProject().withSpace().withCode().thatEquals(prjNode.getEntity().getSpace().getCode());
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withProject().withSpace();
        fetchOptions.withType();
        fetchOptions.withAttachments();

        List<Experiment> experiments = v3Api.searchExperiments(sessionToken, criteria, fetchOptions).getObjects();
        for (Experiment exp : experiments)
        {
            Node<Experiment> expNode = new Node<Experiment>(exp);
            graph.addEdge(prjNode, expNode, new Edge(CONNECTION));
            findSamplesForExperiment(expNode);
            findAndAttachDataSetsForExperiment(expNode);
        }
    }

    private void findSpaceSamples(String spaceCode)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode().thatEquals(spaceCode);
        criteria.withoutExperiment();
        criteria.withAndOperator();

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withSpace();
        fetchOptions.withAttachments();

        List<Sample> samples = v3Api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();
        for (Sample sample : samples)
        {
            Node<Sample> sampleNode = new Node<Sample>(sample);
            graph.addNode(sampleNode);
            findChildAndComponentSamples(sampleNode);
            findAndAttachDataSets(sampleNode);
        }
    }

    private void findSamplesForExperiment(Node<Experiment> expNode)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withId().thatEquals(expNode.getEntity().getPermId());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withDataSets();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withSpace();
        fetchOptions.withAttachments();

        List<Sample> samples = v3Api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();
        for (Sample sample : samples)
        {
            Node<Sample> sampleNode = new Node<Sample>(sample);
            graph.addEdge(expNode, sampleNode, new Edge(CONNECTION));

            findAndAttachDataSets(sampleNode);
            findChildAndComponentSamples(sampleNode);
        }
    }

    private void findAndAttachDataSetsForExperiment(Node<Experiment> expNode)
    {
        DataSetSearchCriteria dsCriteria = new DataSetSearchCriteria();
        dsCriteria.withExperiment().withId().thatEquals(expNode.getEntity().getIdentifier());

        DataSetFetchOptions dsFetchOptions = new DataSetFetchOptions();
        dsFetchOptions.withType();
        dsFetchOptions.withSample();
        dsFetchOptions.withExperiment();
        dsFetchOptions.withProperties();

        List<DataSet> dataSets = v3Api.searchDataSets(sessionToken, dsCriteria, dsFetchOptions).getObjects();
        for (DataSet dataSet : dataSets)
        {
            Node<DataSet> dataSetNode = new Node<DataSet>(dataSet);
            graph.addEdge(expNode, dataSetNode, new Edge(CONNECTION));
            findChildAndContainedDataSets(dataSetNode);
        }
    }

    private void findAndAttachDataSets(Node<Sample> sampleNode)
    {
        DataSetSearchCriteria dsCriteria = new DataSetSearchCriteria();
        dsCriteria.withSample().withId().thatEquals(sampleNode.getEntity().getIdentifier());

        DataSetFetchOptions dsFetchOptions = new DataSetFetchOptions();
        dsFetchOptions.withType();
        dsFetchOptions.withSample();
        dsFetchOptions.withExperiment();
        dsFetchOptions.withProperties();

        List<DataSet> dataSets = v3Api.searchDataSets(sessionToken, dsCriteria, dsFetchOptions).getObjects();
        for (DataSet dataSet : dataSets)
        {
            Node<DataSet> dataSetNode = new Node<DataSet>(dataSet);
            graph.addEdge(sampleNode, dataSetNode, new Edge(CONNECTION));
            findChildAndContainedDataSets(dataSetNode);
        }
    }

    private void findChildAndComponentSamples(Node<Sample> sampleNode)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withType();
        fetchOptions.withDataSets();
        fetchOptions.withExperiment();
        fetchOptions.withSpace();
        fetchOptions.withAttachments();

        // first find the children
        if (graph.isVisitedAsParent(sampleNode.getIdentifier()) == false)
        {
            graph.markAsVisitedAsParent(sampleNode.getIdentifier());
            findChildSamples(sampleNode, fetchOptions);
        }

        // then find contained samples
        if (graph.isVisitedAsContainer(sampleNode.getIdentifier()) == false)
        {
            graph.markAsVisitedAsContainer(sampleNode.getIdentifier());
            findComponentSamples(sampleNode, fetchOptions);
        }
    }

    private void findComponentSamples(Node<Sample> sampleNode, SampleFetchOptions fetchOptions)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withId().thatEquals(sampleNode.getEntity().getPermId());
        List<Sample> components = v3Api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();
        for (Sample sample : components)
        {
            Node<Sample> subSampleNode = new Node<Sample>(sample);
            graph.addEdge(sampleNode, subSampleNode, new Edge(COMPONENT));

            findAndAttachDataSets(subSampleNode);
            findChildAndComponentSamples(subSampleNode);
        }
    }

    private void findChildSamples(Node<Sample> sampleNode, SampleFetchOptions fetchOptions)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withId().thatEquals(sampleNode.getEntity().getPermId());
        List<Sample> children = v3Api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();
        for (Sample sample : children)
        {
            Node<Sample> subSampleNode = new Node<Sample>(sample);
            graph.addEdge(sampleNode, subSampleNode, new Edge(CHILD));
   
            findAndAttachDataSets(subSampleNode);
            findChildAndComponentSamples(subSampleNode);
        }
    }

    private void findChildAndContainedDataSets(Node<DataSet> dsNode)
    {
        DataSetFetchOptions dsFetchOptions = new DataSetFetchOptions();
        dsFetchOptions.withType();
        dsFetchOptions.withSample();
        dsFetchOptions.withExperiment();
        dsFetchOptions.withProperties();

        // first find the children
        if (graph.isVisitedAsParent(dsNode.getIdentifier()) == false)
        {
            graph.markAsVisitedAsParent(dsNode.getIdentifier());
            findChildDataSets(dsNode, dsFetchOptions);
        }

        // then find contained data sets
        if (graph.isVisitedAsContainer(dsNode.getIdentifier()) == false)
        {
            graph.markAsVisitedAsContainer(dsNode.getIdentifier());
            findComponentDataSets(dsNode, dsFetchOptions);
        }
    }

    private void findComponentDataSets(Node<DataSet> dsNode, DataSetFetchOptions dsFetchOptions)
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withContainer().withId().thatEquals(dsNode.getEntity().getPermId());
        List<DataSet> components = v3Api.searchDataSets(sessionToken, criteria, dsFetchOptions).getObjects();
        for (DataSet ds : components)
        {
            Node<DataSet> containedDsNode = new Node<DataSet>(ds);
            graph.addEdge(dsNode, containedDsNode, new Edge(COMPONENT));
            findChildAndContainedDataSets(containedDsNode);
        }
    }

    private void findChildDataSets(Node<DataSet> dsNode, DataSetFetchOptions dsFetchOptions)
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withParents().withId().thatEquals(dsNode.getEntity().getPermId());
        List<DataSet> children = v3Api.searchDataSets(sessionToken, criteria, dsFetchOptions).getObjects();
        for (DataSet ds : children)
        {
            Node<DataSet> childDsNode = new Node<DataSet>(ds);
            graph.addEdge(dsNode, childDsNode, new Edge(CHILD));

            findChildAndContainedDataSets(childDsNode);
        }
    }

    public List<Material> fetchMaterials()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();

        final MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProperties();
        
        SearchResult<Material> searchResult =
                v3Api.searchMaterials(sessionToken, criteria, fetchOptions);

        return searchResult.getObjects();
    }

    public String fetchMasterDataAsXML() throws ParserConfigurationException, TransformerException
    {
        MasterDataExtractor masterDataExtractor = new MasterDataExtractor(v3Api, sessionToken, masterDataRegistrationTransaction);
        return masterDataExtractor.fetchAsXmlString();
    }

    protected List<String> extractCodes(List<? extends ICodeHolder> codeHolders)
    {
        List<String> codes = new ArrayList<>();
        for (ICodeHolder codeHolder : codeHolders)
        {
            codes.add(codeHolder.getCode());
        }
        return codes;
    }
}