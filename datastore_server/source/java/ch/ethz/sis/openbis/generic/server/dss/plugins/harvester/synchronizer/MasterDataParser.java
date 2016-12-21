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

package ch.ethz.sis.openbis.generic.server.dss.plugins.harvester.synchronizer;

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
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;

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
        parsePropertyTypes(docElement.getElementsByTagName("propertyTypes"));
        parseSampleTypes(docElement.getElementsByTagName("sampleTypes"));
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
                String code = vocabElement.getAttributes().getNamedItem("code").getTextContent();
                if (code.startsWith("$"))
                    continue;
                //TODO complete other attributes
                IVocabulary newVocabulary = masterDataRegistrationTransaction.getOrCreateNewVocabulary(code);
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
            // TODO set other attributes
            String code = termElement.getAttributes().getNamedItem("code").getTextContent();
            newVocabulary.addTerm(masterDataRegistrationTransaction.createNewVocabularyTerm(code));
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
                String code = sampleTypeElement.getAttributes().getNamedItem("code").getTextContent();
                ISampleType newSampleType = masterDataRegistrationTransaction.getOrCreateNewSampleType(code);
                newSampleType.setGeneratedCodePrefix("S");

                handlePropertyAssignments(newSampleType, sampleTypeElement.getElementsByTagName("propertyAssignments"));
            }
        }
    }

    private void handlePropertyAssignments(ISampleType newSampleType, NodeList propertyAssignmentsNode)
    {
        if (propertyAssignmentsNode.getLength() == 1)
        {
            Element propertyAssignmentsElement = (Element) propertyAssignmentsNode.item(0);
            NodeList propertyAssignmentNodes = propertyAssignmentsElement.getElementsByTagName("propertyAssigment");
            for (int i = 0; i < propertyAssignmentNodes.getLength(); i++)
            {
                Element propertyAssignmentElement = (Element) propertyAssignmentNodes.item(i);
                // TODO set other attributes
                String property_type_code = propertyAssignmentElement.getAttributes().getNamedItem("property_type_code").getTextContent();
                String data_type_code = propertyAssignmentElement.getAttributes().getNamedItem("data_type_code").getTextContent();
                if (property_type_code.startsWith("$"))
                    continue;
                boolean mandatory = Boolean.valueOf(propertyAssignmentElement.getAttributes().getNamedItem("mandatory").getTextContent());
                int ordinal = Integer.valueOf(propertyAssignmentElement.getAttributes().getNamedItem("ordinal").getTextContent());

                if (propertyTypeMap.get(property_type_code) != null)
                {
                    IPropertyAssignment assignment =
                            masterDataRegistrationTransaction.assignPropertyType(newSampleType, propertyTypeMap.get(property_type_code));
                    assignment.setMandatory(mandatory);
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
                String code = propertyTypeElement.getAttributes().getNamedItem("code").getTextContent();
                // TODO handle internal properties
                if (code.startsWith("$"))
                    continue;
                String label = propertyTypeElement.getAttributes().getNamedItem("label").getTextContent();
                String dataType = propertyTypeElement.getAttributes().getNamedItem("dataType").getTextContent();
                String description = propertyTypeElement.getAttributes().getNamedItem("description").getTextContent();
                boolean internalNamespace = Boolean.valueOf(propertyTypeElement.getAttributes().getNamedItem("internalNamespace").getTextContent());
                boolean managedInternally = Boolean.valueOf(propertyTypeElement.getAttributes().getNamedItem("managedInternally").getTextContent());
                String vocabulary = null;
                Node namedItem = propertyTypeElement.getAttributes().getNamedItem("vocabulary");
                if (namedItem != null)
                {
                    vocabulary = namedItem.getTextContent();
                }
                
                IPropertyType newPropertyType = masterDataRegistrationTransaction.getOrCreateNewPropertyType(code, DataType.valueOf(dataType));
                propertyTypeMap.put(code, newPropertyType);
                newPropertyType.setInternalNamespace(internalNamespace);
                newPropertyType.setManagedInternally(managedInternally);
                newPropertyType.setLabel(label);
                newPropertyType.setDescription(description);
                if (vocabulary != null)
                {
                    newPropertyType.setVocabulary(vocabularyMap.get(vocabulary));
                }
                // TODO handle the case for property types that are of data type material
            }
        }
    }
}
