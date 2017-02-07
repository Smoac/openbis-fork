/*
 * Copyright 2017 ETH Zuerich, SIS
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

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.INTERNAL_NAMESPACE_PREFIX;

import java.io.StringWriter;
import java.util.HashMap;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IEntityType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScriptImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTermImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * @author Ganime Betul Akin
 */
public class MasterDataExtractor
{
    private final IMasterDataRegistrationTransaction masterDataRegistrationTransaction;

    private final IApplicationServerApi v3Api;

    private final String sessionToken;

    /**
     * @param v3Api
     * @param sessionToken
     * @param masterDataRegistrationTransaction
     */
    public MasterDataExtractor(IApplicationServerApi v3Api, String sessionToken,
            IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        this.v3Api = v3Api;
        this.sessionToken = sessionToken;
        this.masterDataRegistrationTransaction = masterDataRegistrationTransaction;
    }

    public String fetchAsXmlString() throws ParserConfigurationException, TransformerException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        docFactory.setNamespaceAware(true);
        Element rootElement = doc.createElementNS("https://sis.id.ethz.ch/software/#openbis/xmdterms/", "xmd:masterData");
        rootElement.setAttribute("xmlns:xmd", "https://sis.id.ethz.ch/software/#openbis/xmdterms/");
        doc.appendChild(rootElement);

        // append scripts
        List<IScriptImmutable> scripts = masterDataRegistrationTransaction.listScripts();
        appendValidationPlugins(doc, rootElement, scripts);

        // append file format types
        List<IFileFormatTypeImmutable> fileFormatTypes = masterDataRegistrationTransaction.listFileFormatTypes();
        appendFileFormatTypes(doc, rootElement, fileFormatTypes);

        // append vocabularies
        List<IVocabularyImmutable> vocabularies = masterDataRegistrationTransaction.listVocabularies();
        appendVocabularies(doc, rootElement, vocabularies);

        // append property types
        List<IPropertyTypeImmutable> propertyTypes = masterDataRegistrationTransaction.listPropertyTypes();
        appendPropertyTypes(doc, rootElement, propertyTypes);

        // append sample types
        List<ISampleTypeImmutable> sampleTypes = masterDataRegistrationTransaction.listSampleTypes();
        appendSampleTypes(doc, rootElement, sampleTypes);

        // append experiment types
        List<IExperimentTypeImmutable> experimentTypes = masterDataRegistrationTransaction.listExperimentTypes();
        appendExperimentTypes(doc, rootElement, experimentTypes);

        // append data set types
        List<IDataSetTypeImmutable> dataSetTypes = masterDataRegistrationTransaction.listDataSetTypes();
        appendDataSetTypes(doc, rootElement, dataSetTypes);

        List<IMaterialTypeImmutable> materialTypes = masterDataRegistrationTransaction.listMaterialTypes();
        appendMaterialTypes(doc, rootElement, materialTypes);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                "validationPlugin");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }

    private void appendValidationPlugins(Document doc, Element rootElement, List<IScriptImmutable> scripts)
    {
        if (scripts.size() > 0)
        {
            Element pluginsElement = doc.createElement("xmd:validationPlugins");
            rootElement.appendChild(pluginsElement);
            for (IScriptImmutable script : scripts)
            {
                Element pluginElement = doc.createElement("xmd:validationPlugin");
                pluginElement.setAttribute("code", script.getCode());
                pluginElement.setAttribute("name", script.getName());
                pluginElement.setAttribute("description", script.getDescription());
                pluginElement.setAttribute("type", script.getScriptType());
                pluginElement.setAttribute("entityKind", script.getEntity());

                pluginElement.appendChild(doc.createCDATASection(script.getScript()));
                pluginsElement.appendChild(pluginElement);
            }
        }
    }

    private void appendFileFormatTypes(Document doc, Element rootElement, List<IFileFormatTypeImmutable> fileFormatTypes)
    {
        if (fileFormatTypes.size() > 0)
        {
            Element fileFormatTypesElement = doc.createElement("xmd:fileFormatTypes");
            rootElement.appendChild(fileFormatTypesElement);
            for (IFileFormatTypeImmutable fileFormatType : fileFormatTypes)
            {
                Element fileFormatTypeElement = doc.createElement("xmd:fileFormatType");
                fileFormatTypeElement.setAttribute("code", fileFormatType.getCode());
                fileFormatTypeElement.setAttribute("description", fileFormatType.getDescription());
                fileFormatTypesElement.appendChild(fileFormatTypeElement);
            }
        }
    }

    private void appendPropertyTypes(Document doc, Element rootElement, List<IPropertyTypeImmutable> propertyTypes)
    {
        if (propertyTypes.size() > 0)
        {
            Element propertyTypesElement = doc.createElement("xmd:propertyTypes");
            rootElement.appendChild(propertyTypesElement);
            for (IPropertyTypeImmutable propertyTypeImmutable : propertyTypes)
            {
                String code =
                        (propertyTypeImmutable.isInternalNamespace()
                        && propertyTypeImmutable.getCode().startsWith(INTERNAL_NAMESPACE_PREFIX)) ? CodeConverter.tryToDatabase(propertyTypeImmutable
                                .getCode()) : propertyTypeImmutable.getCode();
                Element propertyTypeElement = doc.createElement("xmd:propertyType");
                propertyTypeElement.setAttribute("code", code);
                propertyTypeElement.setAttribute("label", propertyTypeImmutable.getLabel());
                propertyTypeElement.setAttribute("dataType", propertyTypeImmutable.getDataType().name());
                propertyTypeElement.setAttribute("internalNamespace", String.valueOf(propertyTypeImmutable.isInternalNamespace()));
                propertyTypeElement.setAttribute("managedInternally", String.valueOf(propertyTypeImmutable.isManagedInternally()));
                propertyTypeElement.setAttribute("description", propertyTypeImmutable.getDescription());
                if (propertyTypeImmutable.getDataType().name().equals(DataType.CONTROLLEDVOCABULARY.name()))
                {
                    propertyTypeElement.setAttribute("vocabulary", propertyTypeImmutable.getVocabulary().getCode());
                }
                else if (propertyTypeImmutable.getDataType().name().equals(DataType.MATERIAL.name()))
                {
                    if (propertyTypeImmutable.getMaterialType() != null)
                    {
                        propertyTypeElement.setAttribute("material", propertyTypeImmutable.getMaterialType().getCode());
                    }
                    else
                    {
                        // for properties like "inhibitor_of" where it is of Material of Any Type
                        propertyTypeElement.setAttribute("material", "");
                    }
                }
                propertyTypesElement.appendChild(propertyTypeElement);
            }
        }
    }

    private void appendVocabularies(Document doc, Element rootElement, List<IVocabularyImmutable> vocabularies)
    {
        if (vocabularies.size() > 0)
        {
            Element vocabsElement = doc.createElement("xmd:controlledVocabularies");
            rootElement.appendChild(vocabsElement);
            for (IVocabularyImmutable vocabImmutable : vocabularies)
            {
                Element vocabElement = doc.createElement("xmd:controlledVocabulary");
                String code = vocabImmutable.isInternalNamespace()
                        && vocabImmutable.getCode().startsWith(INTERNAL_NAMESPACE_PREFIX) ? CodeConverter.tryToDatabase(vocabImmutable.getCode())
                        : vocabImmutable.getCode();
                vocabElement.setAttribute("code", code);
                vocabElement.setAttribute("description", vocabImmutable.getDescription());
                vocabElement.setAttribute("urlTemplate", vocabImmutable.getUrlTemplate());
                vocabElement.setAttribute("managedInternally", String.valueOf(vocabImmutable.isManagedInternally()));
                vocabElement.setAttribute("internalNamespace", String.valueOf(vocabImmutable.isInternalNamespace()));
                vocabElement.setAttribute("chosenFromList", String.valueOf(vocabImmutable.isChosenFromList()));
                vocabsElement.appendChild(vocabElement);

                List<IVocabularyTermImmutable> terms = vocabImmutable.getTerms();
                for (IVocabularyTermImmutable vocabTermImmutable : terms)
                {
                    Element termElement = doc.createElement("term");
                    termElement.setAttribute("code", vocabTermImmutable.getCode());
                    termElement.setAttribute("label", vocabTermImmutable.getLabel());
                    termElement.setAttribute("description", vocabTermImmutable.getDescription());
                    termElement.setAttribute("ordinal", String.valueOf(vocabTermImmutable.getOrdinal()));
                    termElement.setAttribute("url", vocabTermImmutable.getUrl());
                    vocabElement.appendChild(termElement);
                }
            }
        }
    }

    private void appendMaterialTypes(Document doc, Element rootElement, List<IMaterialTypeImmutable> materialTypes)
    {
        if (materialTypes.size() > 0)
        {
            Element materialTypesElement = doc.createElement("xmd:materialTypes");
            rootElement.appendChild(materialTypesElement);
            Map<String, List<PropertyAssignment>> materialTypeCodePropAssignmentMap = loadMaterialTypesUsingV3WithPropertyAssignments();
            for (IMaterialTypeImmutable matType : materialTypes)
            {
                Element matTypeElement = getEntityTypeXML(doc, matType, "xmd:materialType");
                matTypeElement.setAttribute("description", matType.getDescription());
                // TODO validation script
                materialTypesElement.appendChild(matTypeElement);
                Element propertyAssignmentsElement = getPropertyAssignmentXML(doc, materialTypeCodePropAssignmentMap.get(matType.getCode()));
                matTypeElement.appendChild(propertyAssignmentsElement);
            }
        }
    }

    private void appendDataSetTypes(Document doc, Element rootElement, List<IDataSetTypeImmutable> dataSetTypes)
    {
        if (dataSetTypes.size() > 0)
        {
            Element dataSetTypesElement = doc.createElement("xmd:dataSetTypes");
            rootElement.appendChild(dataSetTypesElement);
            Map<String, List<PropertyAssignment>> dsTypeCodePropAssignmentMap = loadDataSetTypesUsingV3WithPropertyAssignments();
            for (IDataSetTypeImmutable dsType : dataSetTypes)
            {
                Element dsTypeElement = getEntityTypeXML(doc, dsType, "xmd:dataSetType");
                dsTypeElement.setAttribute("description", dsType.getDescription());
                dsTypeElement.setAttribute("dataSetKind", dsType.getDataSetKind());
                dsTypeElement.setAttribute("mainDataSetPattern", dsType.getMainDataSetPattern());
                dsTypeElement.setAttribute("mainDataSetPath", dsType.getMainDataSetPath());
                dsTypeElement.setAttribute("deletionDisallowed", String.valueOf(dsType.isDeletionDisallowed()));
                // TODO validation script?
                dataSetTypesElement.appendChild(dsTypeElement);
                Element propertyAssignmentsElement = getPropertyAssignmentXML(doc, dsTypeCodePropAssignmentMap.get(dsType.getCode()));
                dsTypeElement.appendChild(propertyAssignmentsElement);
            }
        }
    }

    private void appendExperimentTypes(Document doc, Element rootElement, List<IExperimentTypeImmutable> experimentTypes)
    {
        if (experimentTypes.size() > 0)
        {
            Element experimentTypesElement = doc.createElement("xmd:collectionTypes");
            rootElement.appendChild(experimentTypesElement);
            Map<String, List<PropertyAssignment>> expTypeCodePropAssignmentMap = loadExperimentTypesUsingV3WithPropertyAssignments();
            for (IExperimentTypeImmutable expType : experimentTypes)
            {
                Element experimentTypeElement = getEntityTypeXML(doc, expType, "xmd:collectionType");
                experimentTypeElement.setAttribute("description", expType.getDescription());
                // TODO validation script?
                experimentTypesElement.appendChild(experimentTypeElement);
                Element propertyAssignmentsElement = getPropertyAssignmentXML(doc, expTypeCodePropAssignmentMap.get(expType.getCode()));
                experimentTypeElement.appendChild(propertyAssignmentsElement);
            }
        }
    }

    private void appendSampleTypes(Document doc, Element rootElement, List<ISampleTypeImmutable> sampleTypes)
    {
        if (sampleTypes.size() > 0)
        {
            Element sampleTypesElement = doc.createElement("xmd:objectTypes");
            rootElement.appendChild(sampleTypesElement);

            Map<String, List<PropertyAssignment>> sampleTypeCodePropAssignmentMap = loadSampleTypesUsingV3WithPropertyAssignments();
            for (ISampleTypeImmutable sampleType : sampleTypes)
            {
                Element sampleTypeElement = getEntityTypeXML(doc, sampleType, "xmd:objectType");
                sampleTypeElement.setAttribute("description", sampleType.getDescription());
                sampleTypeElement.setAttribute("listable", String.valueOf(sampleType.isListable()));
                sampleTypeElement.setAttribute("showContainer", String.valueOf(sampleType.isShowContainer()));
                sampleTypeElement.setAttribute("showParents", String.valueOf(sampleType.isShowParents()));
                sampleTypeElement.setAttribute("showParentMetadata", String.valueOf(sampleType.isShowParentMetadata()));
                sampleTypeElement.setAttribute("subcodeUnique", String.valueOf(sampleType.isSubcodeUnique()));
                sampleTypeElement.setAttribute("autoGeneratedCode", String.valueOf(sampleType.isAutoGeneratedCode()));
                sampleTypeElement.setAttribute("generatedCodePrefix", sampleType.getGeneratedCodePrefix());
                sampleTypesElement.appendChild(sampleTypeElement);
                Element propertyAssignmentsElement = getPropertyAssignmentXML(doc, sampleTypeCodePropAssignmentMap.get(sampleType.getCode()));
                sampleTypeElement.appendChild(propertyAssignmentsElement);
            }
        }
    }

    private <E extends IEntityType> Element getEntityTypeXML(Document doc, E entityType,
            String elementName)
    {
        Element typeElement = doc.createElement(elementName);
        typeElement.setAttribute("code", entityType.getCode());
        return typeElement;
    }

    private Element getPropertyAssignmentXML(Document doc, List<PropertyAssignment> propertyAssignments)
    {
        Element propertyAssignmentsElement = doc.createElement("xmd:propertyAssignments");
        for (PropertyAssignment propAssignment : propertyAssignments)
        {
            Element propertyAssignmentElement = doc.createElement("xmd:propertyAssignment");
            propertyAssignmentsElement.appendChild(propertyAssignmentElement);
            propertyAssignmentElement.setAttribute("propertyTypeCode", CodeConverter.tryToBusinessLayer(propAssignment.getPropertyType().getCode(),
                    propAssignment.getPropertyType().isInternalNameSpace()));
            propertyAssignmentElement.setAttribute("ordinal", String.valueOf(propAssignment.getOrdinal()));
            propertyAssignmentElement.setAttribute("section", propAssignment.getSection());
            propertyAssignmentElement.setAttribute("showInEdit", String.valueOf(propAssignment.isShowInEditView()));
            propertyAssignmentElement.setAttribute("mandatory", String.valueOf(propAssignment.isMandatory()));
            propertyAssignmentElement.setAttribute("showRawValueInForms", String.valueOf(propAssignment.isShowRawValueInForms()));
        }
        return propertyAssignmentsElement;
    }

    private Map<String, List<PropertyAssignment>> loadDataSetTypesUsingV3WithPropertyAssignments()
    {
        // We are mixing up v1 and v3 here because using v3 api to get property assignments is easier
        Map<String, List<PropertyAssignment>> dsTypeCodePropAssignmentMap = new HashMap<String, List<PropertyAssignment>>();
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();

        SearchResult<ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType> searchResult =
                v3Api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
        List<ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType> objects = searchResult.getObjects();
        for (ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType dataSetType : objects)
        {
            dsTypeCodePropAssignmentMap.put(dataSetType.getCode(), dataSetType.getPropertyAssignments());
        }
        return dsTypeCodePropAssignmentMap;
    }

    private Map<String, List<PropertyAssignment>> loadSampleTypesUsingV3WithPropertyAssignments()
    {
        // We are mixing up v1 and v3 here because using v3 api to get property assignments is easier
        Map<String, List<PropertyAssignment>> sampleTypeCodePropAssignmentMap = new HashMap<String, List<PropertyAssignment>>();
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();

        SearchResult<ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType> searchResult =
                v3Api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        List<ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType> objects = searchResult.getObjects();
        for (ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType sampleType : objects)
        {
            sampleTypeCodePropAssignmentMap.put(sampleType.getCode(), sampleType.getPropertyAssignments());
        }
        return sampleTypeCodePropAssignmentMap;
    }

    private Map<String, List<PropertyAssignment>> loadExperimentTypesUsingV3WithPropertyAssignments()
    {
        // We are mixing up v1 and v3 here because using v3 api to get property assignments is easier
        Map<String, List<PropertyAssignment>> expTypeCodePropAssignmentMap = new HashMap<String, List<PropertyAssignment>>();
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();

        SearchResult<ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType> searchResult =
                v3Api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
        List<ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType> objects = searchResult.getObjects();
        for (ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType experimentType : objects)
        {
            expTypeCodePropAssignmentMap.put(experimentType.getCode(), experimentType.getPropertyAssignments());
        }
        return expTypeCodePropAssignmentMap;
    }

    private Map<String, List<PropertyAssignment>> loadMaterialTypesUsingV3WithPropertyAssignments()
    {
        // We are mixing up v1 and v3 here because using v3 api to get property assignments is easier
        Map<String, List<PropertyAssignment>> matTypeCodePropAssignmentMap = new HashMap<String, List<PropertyAssignment>>();
        MaterialTypeSearchCriteria searchCriteria = new MaterialTypeSearchCriteria();
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();

        SearchResult<ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType> searchResult =
                v3Api.searchMaterialTypes(sessionToken, searchCriteria, fetchOptions);
        List<ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType> objects = searchResult.getObjects();
        for (ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType materialType : objects)
        {
            matTypeCodePropAssignmentMap.put(materialType.getCode(), materialType.getPropertyAssignments());
        }
        return matTypeCodePropAssignmentMap;
    }
}
