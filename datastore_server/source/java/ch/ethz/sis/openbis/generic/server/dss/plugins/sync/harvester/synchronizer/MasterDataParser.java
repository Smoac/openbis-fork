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

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IEntityType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetKind;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class MasterDataParser
{
    private final IMasterDataRegistrationTransaction masterDataRegistrationTransaction;
    
    private Map<String, IPropertyType> propertyTypeMap = new HashMap<String, IPropertyType>();

    private Map<String, IVocabulary> vocabularyMap = new HashMap<String, IVocabulary>();

    private Map<String, IMaterialType> materialTypeMap = new HashMap<String, IMaterialType>();

    private Map<String, NodeList> materialTypePropertyAssignmentsMap = new HashMap<String, NodeList>();

    /**
     * @param masterDataRegistrationTransaction
     */
    public MasterDataParser(IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        this.masterDataRegistrationTransaction = masterDataRegistrationTransaction;
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
        Element docElement = (Element) xdNode;

        parseVocabularies(docElement.getElementsByTagName("vocabularies"));
        parseMaterialTypes(docElement.getElementsByTagName("materialTypes"));
        parsePropertyTypes(docElement.getElementsByTagName("propertyTypes"));
        parseSampleTypes(docElement.getElementsByTagName("sampleTypes"));
        parseDataSetTypes(docElement.getElementsByTagName("dataSetTypes"));
        parseExperimentTypes(docElement.getElementsByTagName("experimentTypes"));
        handleMaterialPropertyAssignments();

    }

    private void handleMaterialPropertyAssignments()
    {
        for (IMaterialType materialType : materialTypeMap.values())
        {
            handlePropertyAssignments(materialType, materialTypePropertyAssignmentsMap.get(materialType.getCode()));
        }
    }

    private void parseVocabularies(NodeList vocabulariesNode)
    {
        if (vocabulariesNode.getLength() == 1)
        {
            Element vocabsElement = (Element) vocabulariesNode.item(0);
            NodeList vocabNodes = vocabsElement.getElementsByTagName("vocabulary");
            for (int i = 0; i < vocabNodes.getLength(); i++)
            {
                Element vocabElement = (Element) vocabNodes.item(i);
                String code = getAttribute(vocabElement, "code");
                if (code.startsWith("$"))
                    continue;
                IVocabulary newVocabulary = masterDataRegistrationTransaction.getOrCreateNewVocabulary(code);
                newVocabulary.setDescription(getAttribute(vocabElement, "description"));
                newVocabulary.setUrlTemplate(getAttribute(vocabElement, "urlTemplate"));
                newVocabulary.setManagedInternally(Boolean.valueOf(getAttribute(vocabElement, "managedInternally")));
                newVocabulary.setInternalNamespace(Boolean.valueOf(getAttribute(vocabElement, "internalNamespace")));
                newVocabulary.setChosenFromList(Boolean.valueOf(getAttribute(vocabElement, "chosenFromList")));
                vocabularyMap.put(code, newVocabulary);
                parseVocabularyTerms(vocabElement, newVocabulary);
            }
        }
    }

    private void parseVocabularyTerms(Element vocabElement, IVocabulary newVocabulary)
    {
        NodeList termNodes = vocabElement.getElementsByTagName("term");
        for (int i = 0; i < termNodes.getLength(); i++)
        {
            Element termElement = (Element) termNodes.item(i);
            String code = getAttribute(termElement, "code");
            IVocabularyTerm newVocabularyTerm = masterDataRegistrationTransaction.createNewVocabularyTerm(code);
            newVocabularyTerm.setLabel(getAttribute(termElement, "label"));
            newVocabularyTerm.setDescription(getAttribute(termElement, "description"));
            newVocabularyTerm.setOrdinal(Long.valueOf(getAttribute(termElement, "ordinal")));
            // TODO setUrl? There is no way to set it
            newVocabulary.addTerm(newVocabularyTerm);
        }
    }

    private String getAttribute(Element termElement, String attr)
    {
        return termElement.getAttributes().getNamedItem(attr).getTextContent();
    }

    private void parseMaterialTypes(NodeList matTypesNode)
    {
        if (matTypesNode.getLength() == 1)
        {
            Element matTypesElement = (Element) matTypesNode.item(0);
            NodeList matTypeNodes = matTypesElement.getElementsByTagName("materialType");
            for (int i = 0; i < matTypeNodes.getLength(); i++)
            {
                Element materialTypeElement = (Element) matTypeNodes.item(i);
                String code = getAttribute(materialTypeElement, "code");
                IMaterialType materialType = masterDataRegistrationTransaction.getOrCreateNewMaterialType(code);
                materialType.setDescription(getAttribute(materialTypeElement, "description"));
                materialTypeMap.put(code, materialType);
                // defer material property assignments until after property types are parsed
                materialTypePropertyAssignmentsMap.put(code, materialTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void parseExperimentTypes(NodeList expTypesNode)
    {
        if (expTypesNode.getLength() == 1)
        {
            Element expTypesElement = (Element) expTypesNode.item(0);
            NodeList expTypeNodes = expTypesElement.getElementsByTagName("experimentType");
            for (int i = 0; i < expTypeNodes.getLength(); i++)
            {
                Element expTypeElement = (Element) expTypeNodes.item(i);
                String code = getAttribute(expTypeElement, "code");
                IExperimentType expType = masterDataRegistrationTransaction.getOrCreateNewExperimentType(code);
                expType.setDescription(getAttribute(expTypeElement, "description"));

                handlePropertyAssignments(expType, expTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void parseSampleTypes(NodeList sampleTypesNode)
    {
        if (sampleTypesNode.getLength() == 1)
        {
            Element sampleTypesElement = (Element) sampleTypesNode.item(0);
            NodeList sampleTypeNodes = sampleTypesElement.getElementsByTagName("sampleType");
            for (int i = 0; i < sampleTypeNodes.getLength(); i++)
            {
                Element sampleTypeElement = (Element) sampleTypeNodes.item(i);
                String code = getAttribute(sampleTypeElement, "code");
                ISampleType sampleType = masterDataRegistrationTransaction.getOrCreateNewSampleType(code);
                sampleType.setDescription(getAttribute(sampleTypeElement, "description"));
                sampleType.setListable(Boolean.valueOf(getAttribute(sampleTypeElement, "listable")));
                sampleType.setShowContainer(Boolean.valueOf(getAttribute(sampleTypeElement, "showContainer")));
                sampleType.setShowParents(Boolean.valueOf(getAttribute(sampleTypeElement, "showParents")));
                sampleType.setShowParentMetadata(Boolean.valueOf(getAttribute(sampleTypeElement, "showParentMetadata")));
                sampleType.setSubcodeUnique(Boolean.valueOf(getAttribute(sampleTypeElement, "subcodeUnique")));
                sampleType.setAutoGeneratedCode(Boolean.valueOf(getAttribute(sampleTypeElement, "autoGeneratedCode")));
                sampleType.setGeneratedCodePrefix(getAttribute(sampleTypeElement, "generatedCodePrefix"));

                handlePropertyAssignments(sampleType, sampleTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void parseDataSetTypes(NodeList dataSetTypesNode)
    {
        if (dataSetTypesNode.getLength() == 1)
        {
            Element dataSetTypesElement = (Element) dataSetTypesNode.item(0);
            NodeList dataSetTypeNodes = dataSetTypesElement.getElementsByTagName("dataSetType");
            for (int i = 0; i < dataSetTypeNodes.getLength(); i++)
            {
                Element dataSetTypeElement = (Element) dataSetTypeNodes.item(i);
                String code = getAttribute(dataSetTypeElement, "code");
                IDataSetType dataSetType = masterDataRegistrationTransaction.getOrCreateNewDataSetType(code);
                dataSetType.setDescription(getAttribute(dataSetTypeElement, "description"));
                dataSetType.setDataSetKind(DataSetKind.valueOf(getAttribute(dataSetTypeElement, "dataSetKind")).toString());
                String mainDataSetPattern = getAttribute(dataSetTypeElement, "mainDataSetPattern");
                if (mainDataSetPattern.length() < 1)
                {
                    dataSetType.setMainDataSetPattern(null);
                }
                else
                {
                    dataSetType.setMainDataSetPattern(mainDataSetPattern);
                }
                if (mainDataSetPattern.length() < 1)
                {
                    dataSetType.setMainDataSetPath(null);
                }
                else
                {
                    dataSetType.setMainDataSetPath(mainDataSetPattern);
                }
                dataSetType.setDeletionDisallowed(Boolean.valueOf(getAttribute(dataSetTypeElement, "deletionDisallowed")));

                handlePropertyAssignments(dataSetType, dataSetTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void handlePropertyAssignments(IEntityType entityType, NodeList propertyAssignmentsNode)
    {
        if (propertyAssignmentsNode.getLength() == 1)
        {
            Element propertyAssignmentsElement = (Element) propertyAssignmentsNode.item(0);
            NodeList propertyAssignmentNodes = propertyAssignmentsElement.getElementsByTagName("propertyAssignment");
            for (int i = 0; i < propertyAssignmentNodes.getLength(); i++)
            {
                Element propertyAssignmentElement = (Element) propertyAssignmentNodes.item(i);
                String property_type_code = getAttribute(propertyAssignmentElement, "property_type_code");
                // TODO handle $ types
                if (property_type_code.startsWith("$"))
                    continue;
                if (propertyTypeMap.get(property_type_code) != null)
                {
                    IPropertyAssignment assignment =
                            masterDataRegistrationTransaction.assignPropertyType(entityType, propertyTypeMap.get(property_type_code));
                    assignment.setMandatory(Boolean.valueOf(getAttribute(propertyAssignmentElement, "mandatory")));
                    assignment.setSection(getAttribute(propertyAssignmentElement, "section"));
                    assignment.setPositionInForms(Long.valueOf(getAttribute(propertyAssignmentElement, "ordinal")));
                    assignment.setShownEdit(Boolean.valueOf(getAttribute(propertyAssignmentElement, "showInEdit")));
                }
            }
        }
    }

    private void parsePropertyTypes(NodeList propertyTypesNode)
    {
        if (propertyTypesNode.getLength() == 1)
        {
            Element propertyTypesElement = (Element) propertyTypesNode.item(0);
            NodeList propertyTypeNodes = propertyTypesElement.getElementsByTagName("propertyType");
            for (int i = 0; i < propertyTypeNodes.getLength(); i++)
            {
                Element propertyTypeElement = (Element) propertyTypeNodes.item(i);
                String code = getAttribute(propertyTypeElement, "code");
                // TODO handle internal properties
                if (code.startsWith("$"))
                    continue;
                String label = getAttribute(propertyTypeElement, "label");
                String dataType = getAttribute(propertyTypeElement, "dataType");
                String description = getAttribute(propertyTypeElement, "description");
                boolean internalNamespace = Boolean.valueOf(getAttribute(propertyTypeElement, "internalNamespace"));
                boolean managedInternally = Boolean.valueOf(getAttribute(propertyTypeElement, "managedInternally"));
                
                IPropertyType newPropertyType = masterDataRegistrationTransaction.getOrCreateNewPropertyType(code, DataType.valueOf(dataType));
                propertyTypeMap.put(code, newPropertyType);
                newPropertyType.setInternalNamespace(internalNamespace);
                newPropertyType.setManagedInternally(managedInternally);
                newPropertyType.setLabel(label);
                newPropertyType.setDescription(description);
                if (dataType.equals(DataType.CONTROLLEDVOCABULARY.name()))
                {
                    String vocabularyCode = getAttribute(propertyTypeElement, "vocabulary");
                    newPropertyType.setVocabulary(vocabularyMap.get(vocabularyCode));
                }
                else if (dataType.equals(DataType.MATERIAL.name()))
                {
                    String materialCode = getAttribute(propertyTypeElement, "material");
                    if (materialCode.trim().length() < 1)
                    {
                        newPropertyType.setMaterialType(null); // material of any type
                    }
                    else
                    {
                        newPropertyType.setMaterialType(materialTypeMap.get(materialCode));
                    }
                }
            }
        }
    }
}
