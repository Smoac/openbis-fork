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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.DefaultNameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.INameTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class MasterDataParser
{
    private final INameTranslator nameTranslator;
    
    private Map<String, Script> validationPlugins = new HashMap<String, Script>();

    private Map<String, ExternalDms> externalDataManagementSystems = new HashMap<>();
    private Map<String, FileFormatType> fileFormatTypes = new HashMap<String, FileFormatType>();

    private Map<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>();

    private Map<String, SampleType> sampleTypes = new HashMap<String, SampleType>();

    private Map<String, DataSetType> dataSetTypes = new HashMap<String, DataSetType>();

    private Map<String, ExperimentType> experimentTypes = new HashMap<String, ExperimentType>();

    private Map<String, MaterialType> materialTypes = new HashMap<String, MaterialType>();

    private Map<String, NewVocabulary> vocabularies = new HashMap<String, NewVocabulary>();

    MultiKeyMap<String, List<NewETPTAssignment>> entityPropertyAssignments = new MultiKeyMap<String, List<NewETPTAssignment>>();

    private MasterDataParser(INameTranslator nameTranslator)
    {
        this.nameTranslator = nameTranslator;
    }

    public static MasterDataParser create(INameTranslator nameTranslator)
    {
        if (nameTranslator == null)
        {
            return create();
        }
        return new MasterDataParser(nameTranslator);
    }

    public static MasterDataParser create()
    {
        return create(new DefaultNameTranslator());
    }

    public void parseMasterData(Document doc, XPath xpath, String uri) throws XPathExpressionException
    {
        XPathExpression expr =
                xpath.compile("//s:url/s:loc[normalize-space(.)='" + uri + "']//following-sibling::*[local-name() = 'masterData'][1]");
        Node xdNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (xdNode == null)
        {
            throw new XPathExpressionException("The master data resurce list should contain 1 master data element");
        }
        parseMasterDataElements((Element) xdNode);
    }

    private void parseMasterDataElements(Element docElement) throws XPathExpressionException
    {
        parseFileFormatTypes(docElement.getElementsByTagName("xmd:fileFormatTypes"));
        parseValidationPlugins(docElement.getElementsByTagName("xmd:validationPlugins"));
        parseVocabularies(docElement.getElementsByTagName("xmd:controlledVocabularies"));
        parseMaterialTypes(docElement.getElementsByTagName("xmd:materialTypes"));
        parsePropertyTypes(docElement.getElementsByTagName("xmd:propertyTypes"));
        parseSampleTypes(docElement.getElementsByTagName("xmd:objectTypes"));
        parseDataSetTypes(docElement.getElementsByTagName("xmd:dataSetTypes"));
        parseExperimentTypes(docElement.getElementsByTagName("xmd:collectionTypes"));
        parseExternalDataManagementSystems(docElement.getElementsByTagName("xmd:externalDataManagementSystems"));
    }


    public Map<String, FileFormatType> getFileFormatTypes()
    {
        return fileFormatTypes;
    }

    public Map<String, Script> getValidationPlugins()
    {
        return validationPlugins;
    }

    public Map<String, NewVocabulary> getVocabularies()
    {
        return vocabularies;
    }

    public Map<String, PropertyType> getPropertyTypes()
    {
        return propertyTypes;
    }

    public MultiKeyMap<String, List<NewETPTAssignment>> getEntityPropertyAssignments()
    {
        return entityPropertyAssignments;
    }

    public Map<String, SampleType> getSampleTypes()
    {
        return sampleTypes;
    }

    public Map<String, DataSetType> getDataSetTypes()
    {
        return dataSetTypes;
    }

    public Map<String, ExperimentType> getExperimentTypes()
    {
        return experimentTypes;
    }

    public Map<String, MaterialType> getMaterialTypes()
    {
        return materialTypes;
    }

    public Map<String, ExternalDms> getExternalDataManagementSystems()
    {
        return externalDataManagementSystems;
    }

    private void parseValidationPlugins(NodeList validationPluginsNode) throws XPathExpressionException
    {
        if (validationPluginsNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(validationPluginsNode, "validationPlugins");

        Element validationPluginsElement = (Element) validationPluginsNode.item(0);
        NodeList pluginNodes = validationPluginsElement.getElementsByTagName("xmd:validationPlugin");

        for (int i = 0; i < pluginNodes.getLength(); i++)
        {
            Element pluginElement = (Element) pluginNodes.item(i);
            Script plugin = new Script();
            plugin.setName(nameTranslator.translate(getAttribute(pluginElement, "name")));
            plugin.setDescription(getAttribute(pluginElement, "description"));
            String entityKind = getAttribute(pluginElement, "entityKind").trim();
            plugin.setScriptType(ScriptType.valueOf(getAttribute(pluginElement, "type")));
            plugin.setPluginType(PluginType.JYTHON);
            if (entityKind.equals("") == false &&  entityKind.equals("All") == false)
            {
                String[] splittedEntityKinds = entityKind.split(",");
                EntityKind[] entityKinds = new EntityKind[splittedEntityKinds.length];
                for (int j = 0; j < splittedEntityKinds.length; j++)
                {
                    entityKinds[j] = EntityKind.valueOf(splittedEntityKinds[j].trim());
                }
                plugin.setEntityKind(entityKinds);
            }
            plugin.setScript(pluginElement.getTextContent());
            validationPlugins.put(plugin.getName(), plugin);
        }
    }

    private void parseExternalDataManagementSystems(NodeList edmsNode) throws XPathExpressionException
    {
        if (edmsNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(edmsNode, "externalDataManagementSystems");

        NodeList edmsNodes = ((Element) edmsNode.item(0)).getElementsByTagName("xmd:externalDataManagementSystem");

        for (int i = 0; i < edmsNodes.getLength(); i++)
        {
            Element element = (Element) edmsNodes.item(i);
            ExternalDms edms = new ExternalDms();
            String code = nameTranslator.translate(getAttribute(element, "code"));
            edms.setCode(code);
            edms.setPermId(new ExternalDmsPermId(code));
            edms.setLabel(getAttribute(element, "label"));
            edms.setAddressType(ExternalDmsAddressType.valueOf(getAttribute(element, "addressType")));
            edms.setAddress(getAttribute(element, "address"));
            externalDataManagementSystems.put(code, edms);
        }
    }

    private void parseFileFormatTypes(NodeList fileFormatTypesNode) throws XPathExpressionException
    {
        if (fileFormatTypesNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(fileFormatTypesNode, "fileFormatTypes");
        
        Element fileFormatTypesElement = (Element) fileFormatTypesNode.item(0);
        NodeList fileFormatTypeNodes = fileFormatTypesElement.getElementsByTagName("xmd:fileFormatType");
        
        for (int i = 0; i < fileFormatTypeNodes.getLength(); i++)
        {
            Element typeElement = (Element) fileFormatTypeNodes.item(i);
            
            FileFormatType type = new FileFormatType();
            String code = getAttribute(typeElement, "code");
            type.setCode(code);
            type.setDescription(getAttribute(typeElement, "description"));
            
            fileFormatTypes.put(code, type);
        }
    }
    
    private void validateElementNode(NodeList nodeList, String tagName) throws XPathExpressionException
    {
        if (nodeList.getLength() != 1)
        {
            throw new XPathExpressionException("Resource List should contain a single ' " + tagName + "' node");
        }
    }

    private void parseVocabularies(NodeList vocabulariesNode) throws XPathExpressionException
    {
        if (vocabulariesNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(vocabulariesNode, "controlledVocabularies");

        Element vocabsElement = (Element) vocabulariesNode.item(0);
        NodeList vocabNodes = vocabsElement.getElementsByTagName("xmd:controlledVocabulary");

        for (int i = 0; i < vocabNodes.getLength(); i++)
        {
            Element vocabElement = (Element) vocabNodes.item(i);
            String code = nameTranslator.translate(getAttribute(vocabElement, "code"));
            NewVocabulary newVocabulary = new NewVocabulary();
            newVocabulary.setCode(code);
            newVocabulary.setDescription(getAttribute(vocabElement, "description"));
            newVocabulary.setURLTemplate(getAttribute(vocabElement, "urlTemplate"));
            newVocabulary.setManagedInternally(Boolean.valueOf(getAttribute(vocabElement, "managedInternally")));
            newVocabulary.setInternalNamespace(Boolean.valueOf(getAttribute(vocabElement, "internalNamespace")));
            newVocabulary.setChosenFromList(Boolean.valueOf(getAttribute(vocabElement, "chosenFromList")));
            vocabularies.put(CodeConverter.tryToBusinessLayer(newVocabulary.getCode(), newVocabulary.isInternalNamespace()), newVocabulary);
            parseVocabularyTerms(vocabElement, newVocabulary);
        }
    }

    private void parseVocabularyTerms(Element vocabElement, NewVocabulary newVocabulary)
    {
        NodeList termNodes = vocabElement.getElementsByTagName("xmd:term");
        for (int i = 0; i < termNodes.getLength(); i++)
        {
            Element termElement = (Element) termNodes.item(i);
            VocabularyTerm newVocabularyTerm = new VocabularyTerm();
            newVocabularyTerm.setCode(getAttribute(termElement, "code"));
            newVocabularyTerm.setLabel(getAttribute(termElement, "label"));
            newVocabularyTerm.setDescription(getAttribute(termElement, "description"));
            newVocabularyTerm.setOrdinal(Long.valueOf(getAttribute(termElement, "ordinal")));
            newVocabularyTerm.setUrl(getAttribute(termElement, "url"));
            newVocabulary.getTerms().add(newVocabularyTerm);
        }
    }

    private String getAttribute(Element termElement, String attr)
    {
        Node node = termElement.getAttributes().getNamedItem(attr);
        return node != null ? node.getTextContent() : null;
    }

    private void parseMaterialTypes(NodeList matTypesNode) throws XPathExpressionException
    {
        if (matTypesNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(matTypesNode, "materialTypes");

        Element matTypesElement = (Element) matTypesNode.item(0);
        NodeList matTypeNodes = matTypesElement.getElementsByTagName("xmd:materialType");
        for (int i = 0; i < matTypeNodes.getLength(); i++)
        {
            Element materialTypeElement = (Element) matTypeNodes.item(i);
            MaterialType materialType = new MaterialType();
            materialType.setCode(nameTranslator.translate(getAttribute(materialTypeElement, "code")));
            materialType.setDescription(getAttribute(materialTypeElement, "description"));
            materialType.setValidationScript(getValidationPlugin(materialTypeElement));
            materialTypes.put(materialType.getCode(), materialType);

            parsePropertyAssignments(EntityKind.MATERIAL, materialType, materialTypeElement);
        }
    }

    private void parseExperimentTypes(NodeList expTypesNode) throws XPathExpressionException
    {
        if (expTypesNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(expTypesNode, "collectionTypes");

        Element expTypesElement = (Element) expTypesNode.item(0);
        NodeList expTypeNodes = expTypesElement.getElementsByTagName("xmd:collectionType");
        for (int i = 0; i < expTypeNodes.getLength(); i++)
        {
            Element expTypeElement = (Element) expTypeNodes.item(i);
            ExperimentType expType = new ExperimentType();
            expType.setCode(nameTranslator.translate(getAttribute(expTypeElement, "code")));
            expType.setDescription(getAttribute(expTypeElement, "description"));
            expType.setValidationScript(getValidationPlugin(expTypeElement));
            experimentTypes.put(expType.getCode(), expType);

            parsePropertyAssignments(EntityKind.EXPERIMENT, expType, expTypeElement);
        }
    }

    private void parseSampleTypes(NodeList sampleTypesNode) throws XPathExpressionException
    {
        if (sampleTypesNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(sampleTypesNode, "objectTypes");

        Element sampleTypesElement = (Element) sampleTypesNode.item(0);
        NodeList sampleTypeNodes = sampleTypesElement.getElementsByTagName("xmd:objectType");
        for (int i = 0; i < sampleTypeNodes.getLength(); i++)
        {
            Element sampleTypeElement = (Element) sampleTypeNodes.item(i);
            SampleType sampleType = new SampleType();
            sampleType.setCode(nameTranslator.translate(getAttribute(sampleTypeElement, "code")));
            sampleType.setDescription(getAttribute(sampleTypeElement, "description"));
            sampleType.setListable(Boolean.valueOf(getAttribute(sampleTypeElement, "listable")));
            sampleType.setShowContainer(Boolean.valueOf(getAttribute(sampleTypeElement, "showContainer")));
            sampleType.setShowParents(Boolean.valueOf(getAttribute(sampleTypeElement, "showParents")));
            sampleType.setShowParentMetadata(Boolean.valueOf(getAttribute(sampleTypeElement, "showParentMetadata")));
            sampleType.setSubcodeUnique(Boolean.valueOf(getAttribute(sampleTypeElement, "subcodeUnique")));
            sampleType.setAutoGeneratedCode(Boolean.valueOf(getAttribute(sampleTypeElement, "autoGeneratedCode")));
            sampleType.setGeneratedCodePrefix(getAttribute(sampleTypeElement, "generatedCodePrefix"));
            sampleType.setValidationScript(getValidationPlugin(sampleTypeElement));
            sampleTypes.put(sampleType.getCode(), sampleType);

            parsePropertyAssignments(EntityKind.SAMPLE, sampleType, sampleTypeElement);
        }
    }

    private void parseDataSetTypes(NodeList dataSetTypesNode) throws XPathExpressionException
    {
        if (dataSetTypesNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(dataSetTypesNode, "dataSetTypes");

        Element dataSetTypesElement = (Element) dataSetTypesNode.item(0);
        NodeList dataSetTypeNodes = dataSetTypesElement.getElementsByTagName("xmd:dataSetType");
        for (int i = 0; i < dataSetTypeNodes.getLength(); i++)
        {
            Element dataSetTypeElement = (Element) dataSetTypeNodes.item(i);
            DataSetType dataSetType = new DataSetType();
            dataSetType.setCode(nameTranslator.translate(getAttribute(dataSetTypeElement, "code")));
            dataSetType.setDescription(getAttribute(dataSetTypeElement, "description"));
            String mainDataSetPattern = getAttribute(dataSetTypeElement, "mainDataSetPattern");
            if (StringUtils.isNotBlank(mainDataSetPattern))
            {
                dataSetType.setMainDataSetPattern(mainDataSetPattern);
            }
            String mainDataSetPath = getAttribute(dataSetTypeElement, "mainDataSetPath");
            if (StringUtils.isNotBlank(mainDataSetPath))
            {
                dataSetType.setMainDataSetPath(mainDataSetPath);
            }
            dataSetType.setDeletionDisallow(Boolean.valueOf(getAttribute(dataSetTypeElement, "deletionDisallowed")));
            dataSetType.setValidationScript(getValidationPlugin(dataSetTypeElement));
            dataSetTypes.put(dataSetType.getCode(), dataSetType);

            parsePropertyAssignments(EntityKind.DATA_SET, dataSetType, dataSetTypeElement);
        }
    }
    
    private Script getValidationPlugin(Element element)
    {
        String name = getAttribute(element, "validationPlugin");
        if (StringUtils.isBlank(name))
        {
            return null;
        }
        return validationPlugins.get(nameTranslator.translate(name));
    }

    private void parsePropertyAssignments(EntityKind entityKind, EntityType entityType, Element entityTypeElement)
            throws XPathExpressionException
    {
        NodeList propertyAssignmentsNode = entityTypeElement.getElementsByTagName("xmd:propertyAssignments");
        if (propertyAssignmentsNode.getLength() == 0)
        {
            return;
        }

        validateElementNode(propertyAssignmentsNode, "propertyAssignments");

        List<NewETPTAssignment> list = new ArrayList<NewETPTAssignment>();
        Element propertyAssignmentsElement = (Element) propertyAssignmentsNode.item(0);
        NodeList propertyAssignmentNodes = propertyAssignmentsElement.getElementsByTagName("xmd:propertyAssignment");
        for (int i = 0; i < propertyAssignmentNodes.getLength(); i++)
        {
            Element propertyAssignmentElement = (Element) propertyAssignmentNodes.item(i);
            String propertyTypeCode = getAttribute(propertyAssignmentElement, "propertyTypeCode");
            propertyTypeCode = nameTranslator.translate(propertyTypeCode);
            NewETPTAssignment assignment = new NewETPTAssignment();
            assignment.setPropertyTypeCode(propertyTypeCode);
            assignment.setEntityKind(entityType.getEntityKind());
            assignment.setEntityTypeCode(entityType.getCode());
            assignment.setMandatory(Boolean.valueOf(getAttribute(propertyAssignmentElement, "mandatory")));
            assignment.setSection(getAttribute(propertyAssignmentElement, "section"));
            // ch.systemsx.cisd.openbis.generic.server.business.bo.EntityTypePropertyTypeBO.createAssignment() increases
            // the provided ordinal by one. Thus, we have to subtract 1 in order to get the same ordinal.
            assignment.setOrdinal(Long.valueOf(getAttribute(propertyAssignmentElement, "ordinal")) - 1);
            assignment.setShownInEditView(Boolean.valueOf(getAttribute(propertyAssignmentElement, "showInEdit")));
            String pluginId = getAttribute(propertyAssignmentElement, "plugin");
            if (pluginId != null)
            {
                assignment.setScriptName(nameTranslator.translate(pluginId));
                String pluginType = getAttribute(propertyAssignmentElement, "pluginType");
                assignment.setDynamic(ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType.DYNAMIC_PROPERTY.toString().equals(pluginType));
                assignment.setManaged(ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType.MANAGED_PROPERTY.toString().equals(pluginType));
            }
            list.add(assignment);
        }
        entityPropertyAssignments.put(entityType.getEntityKind().name(), entityType.getCode(), list);
    }

    private void parsePropertyTypes(NodeList propertyTypesNode) throws XPathExpressionException
    {
        if (propertyTypesNode.getLength() == 0)
        {
            return;
        }
        validateElementNode(propertyTypesNode, "propertyTypes");

        Element propertyTypesElement = (Element) propertyTypesNode.item(0);
        NodeList propertyTypeNodes = propertyTypesElement.getElementsByTagName("xmd:propertyType");
        for (int i = 0; i < propertyTypeNodes.getLength(); i++)
        {
            Element propertyTypeElement = (Element) propertyTypeNodes.item(i);

            PropertyType newPropertyType = new PropertyType();
            Boolean isInternalNameSpace = Boolean.valueOf(getAttribute(propertyTypeElement, "internalNamespace"));
            String code = nameTranslator.translate(getAttribute(propertyTypeElement, "code"));
            newPropertyType.setCode(CodeConverter.tryToBusinessLayer(code, isInternalNameSpace));
            newPropertyType.setLabel(getAttribute(propertyTypeElement, "label"));
            DataTypeCode dataTypeCode = DataTypeCode.valueOf(getAttribute(propertyTypeElement, "dataType"));
            newPropertyType.setDataType(new DataType(dataTypeCode));
            newPropertyType.setDescription(getAttribute(propertyTypeElement, "description"));
            newPropertyType.setInternalNamespace(isInternalNameSpace);
            newPropertyType.setManagedInternally(Boolean.valueOf(getAttribute(propertyTypeElement, "managedInternally")));

            propertyTypes.put(newPropertyType.getCode(), newPropertyType);
            if (dataTypeCode.equals(DataTypeCode.CONTROLLEDVOCABULARY))
            {
                String vocabularyCode = nameTranslator.translate((getAttribute(propertyTypeElement, "vocabulary")));
                newPropertyType.setVocabulary(vocabularies.get(vocabularyCode));
            }
            else if (dataTypeCode.equals(DataTypeCode.MATERIAL))
            {
                String materialCode = getAttribute(propertyTypeElement, "material");
                if (StringUtils.isBlank(materialCode))
                {
                    newPropertyType.setMaterialType(null); // material of any type
                }
                else
                {
                    newPropertyType.setMaterialType(materialTypes.get(nameTranslator.translate(materialCode)));
                }
            }
        }
    }
}
