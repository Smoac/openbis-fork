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
package ch.ethz.sis.openbis.generic.server;

import static ch.ethz.sis.openbis.generic.shared.entitygraph.Edge.CHILD;
import static ch.ethz.sis.openbis.generic.shared.entitygraph.Edge.COMPONENT;
import static ch.ethz.sis.openbis.generic.shared.entitygraph.Edge.CONNECTION;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IAttachmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.entitygraph.Edge;
import ch.ethz.sis.openbis.generic.shared.entitygraph.EntityGraph;
import ch.ethz.sis.openbis.generic.shared.entitygraph.Node;

public class EntityRetriever 
{
    private static final EntityGraph<Node<?>> graph = new EntityGraph<Node<?>>();

    private final IApplicationServerApi v3Api;

    private final String sessionToken;

    private static final int TIMEOUT = 10000;

    private EntityRetriever(IApplicationServerApi v3Api, String sessionToken)
    {
        this.v3Api = v3Api;
        this.sessionToken = sessionToken;
    }
    
    public static EntityRetriever createWithSessionToken(IApplicationServerApi v3Api, String sessionToken)
    {
        return new EntityRetriever(v3Api, sessionToken);
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
            findAndAddAttachments(prjNode);
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
            findAndAddAttachments(expNode);
            findAndAttachDataSetsForExperiment(expNode);
        }
    }

    private void findAndAddAttachments(Node<? extends IAttachmentsHolder> node)
    {
        List<Attachment> attachments = node.getEntity().getAttachments();
        for (Attachment attachment : attachments)
        {
            node.addBinaryData(attachment.getPermlink());
            // System.out.println("Attachment:" + attachment.getPermlink());
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
            findAndAddAttachments(sampleNode);
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
            findAndAddAttachments(sampleNode);
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
            findAndAddAttachments(subSampleNode);
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
            findAndAddAttachments(subSampleNode);
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

    public String fetchMetaDataAsXML() throws ParserConfigurationException, TransformerException
    {
        MasterData md = new MasterData();

        md.addSampleTypes(this.getSampleTypes());
        md.addExperimentTypes(this.getExperimentTypes());
        md.addDataSetTypes(this.getDataSetTypes());
        md.addMaterialTypes(this.getMaterialTypes());

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("masterData");
        doc.appendChild(rootElement);
        List<SampleType> sampleTypes = md.getSampleTypes();
        if (sampleTypes.size() > 0)
        {
            Element sampleTypesElement = doc.createElement("sampleTypes");
            rootElement.appendChild(sampleTypesElement);
            for (SampleType sampleType : sampleTypes)
            {
                Element sampleTypeElement = getEntityTypeXML(doc, sampleType, "sampleType");
                sampleTypesElement.appendChild(sampleTypeElement);
                Element propertyAssignmentsElement = getPropertyAssignmentXML(doc, sampleType.getPropertyAssignments());
                sampleTypeElement.appendChild(propertyAssignmentsElement);
            }
        }

        List<ExperimentType> experimentTypes = md.getExperimentTypes();
        if (experimentTypes.size() > 0)
        {
            Element expTypesElement = doc.createElement("experimentTypes");
            rootElement.appendChild(expTypesElement);
            for (ExperimentType expType : experimentTypes)
            {
                Element experimentTypeElement = getEntityTypeXML(doc, expType, "experimentType");
                expTypesElement.appendChild(experimentTypeElement);
                Element propertyAssignmentsElement = getPropertyAssignmentXML(doc, expType.getPropertyAssignments());
                experimentTypeElement.appendChild(propertyAssignmentsElement);
            }
        }

        List<DataSetType> dataSetTypes = md.getDataSetTypes();
        if (dataSetTypes.size() > 0)
        {
            Element dataSetTypesElement = doc.createElement("dataSetTypes");
            rootElement.appendChild(dataSetTypesElement);
            for (DataSetType dsType : dataSetTypes)
            {
                Element dsTypeElement = getEntityTypeXML(doc, dsType, "dataSetType");
                dataSetTypesElement.appendChild(dsTypeElement);
                Element propertyAssignmentsElement = getPropertyAssignmentXML(doc, dsType.getPropertyAssignments());
                dsTypeElement.appendChild(propertyAssignmentsElement);
            }
        }

        List<MaterialType> materialTypes = md.getMaterialTypes();
        if (materialTypes.size() > 0)
        {
            Element materialTypesElement = doc.createElement("materialTypes");
            rootElement.appendChild(materialTypesElement);
            for (MaterialType matType : materialTypes)
            {
                Element matTypeElement = getEntityTypeXML(doc, matType, "materialType");
                materialTypesElement.appendChild(matTypeElement);
                Element propertyAssignmentsElement = getPropertyAssignmentXML(doc, matType.getPropertyAssignments());
                matTypeElement.appendChild(propertyAssignmentsElement);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }

    private <E extends ICodeHolder> Element getEntityTypeXML(Document doc, E entityType,
            String elementName)
    {
        Element sampleTypeElement = doc.createElement(elementName);
        sampleTypeElement.setAttribute("code", entityType.getCode());
        return sampleTypeElement;
    }

    private Element getPropertyAssignmentXML(Document doc, List<PropertyAssignment> propertyAssignments)
    {
        Element propertyAssignmentsElement = doc.createElement("propertyAssignments");
        for (PropertyAssignment propAssignment : propertyAssignments)
        {
            Element propertyAssigmentElement = doc.createElement("propertyAssigment");
            propertyAssignmentsElement.appendChild(propertyAssigmentElement);
            propertyAssigmentElement.setAttribute("property_type_code", propAssignment.getPropertyType().getCode());
            propertyAssigmentElement.setAttribute("data_type_code", propAssignment.getPropertyType().getDataType().toString());
        }
        return propertyAssignmentsElement;
    }

    public void printSamples()
    {
        buildEntityGraph("SYNC");
    }

    // @XmlRootElement
    // @XmlAccessorType(XmlAccessType.FIELD)
    private static class MasterData
    {
        // @XmlElement(name = "sampleType")
        List<SampleType> sampleTypes = new ArrayList<SampleType>();

        List<ExperimentType> experimentTypes = new ArrayList<ExperimentType>();

        List<DataSetType> dataSetTypes = new ArrayList<DataSetType>();

        List<MaterialType> materialTypes = new ArrayList<MaterialType>();

        public List<SampleType> getSampleTypes()
        {
            return sampleTypes;
        }

        void addSampleTypes(Collection<SampleType> sampleTypes)
        {
            this.sampleTypes.addAll(sampleTypes);
        }

        public List<ExperimentType> getExperimentTypes()
        {
            return experimentTypes;
        }

        void addExperimentTypes(Collection<ExperimentType> expTypes)
        {
            this.experimentTypes.addAll(expTypes);
        }

        void addDataSetTypes(Collection<DataSetType> dsTypes)
        {
            this.dataSetTypes.addAll(dsTypes);
        }

        public List<DataSetType> getDataSetTypes()
        {
            return dataSetTypes;
        }

        void addMaterialTypes(Collection<MaterialType> matTypes)
        {
            this.materialTypes.addAll(matTypes);
        }

        public List<MaterialType> getMaterialTypes()
        {
            return materialTypes;
        }
    }

    private List<DataSetType> getDataSetTypes()
    {
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();
        // withPropertyAssignments.withPropertyType();

        SearchResult<DataSetType> searchResult = v3Api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
        return searchResult.getObjects();
    }

    private List<SampleType> getSampleTypes()
    {
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();

        SearchResult<SampleType> searchResult = v3Api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        return searchResult.getObjects();
    }

    private List<ExperimentType> getExperimentTypes()
    {
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();

        SearchResult<ExperimentType> searchResult = v3Api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
        return searchResult.getObjects();
    }

    private List<MaterialType> getMaterialTypes()
    {
        MaterialTypeSearchCriteria searchCriteria = new MaterialTypeSearchCriteria();
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();

        SearchResult<MaterialType> searchResult = v3Api.searchMaterialTypes(sessionToken, searchCriteria, fetchOptions);
        return searchResult.getObjects();
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