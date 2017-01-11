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
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTerm;

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
                String code = getAttribute(vocabElement, "code");
                if (code.startsWith("$"))
                    continue;
                //TODO complete other attributes
                IVocabulary newVocabulary = masterDataRegistrationTransaction.getOrCreateNewVocabulary(code);
                newVocabulary.setDescription(getAttribute(vocabElement, "description"));
                newVocabulary.setUrlTemplate(getAttribute(vocabElement, "urlTemplate"));
                newVocabulary.setInternalNamespace(Boolean.valueOf(getAttribute(vocabElement, "internalNamespace")));
                newVocabulary.setManagedInternally(Boolean.valueOf(getAttribute(vocabElement, "managedInternally")));
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
            // TODO setUrl?
            newVocabulary.addTerm(newVocabularyTerm);
        }
    }

    private String getAttribute(Element termElement, String attr)
    {
        return termElement.getAttributes().getNamedItem(attr).getTextContent();
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
                String property_type_code = getAttribute(propertyAssignmentElement, "property_type_code");
                String data_type_code = getAttribute(propertyAssignmentElement, "data_type_code");
                if (property_type_code.startsWith("$"))
                    continue;
                boolean mandatory = Boolean.valueOf(getAttribute(propertyAssignmentElement, "mandatory"));
                long ordinal = Long.valueOf(getAttribute(propertyAssignmentElement, "ordinal"));
                String section = getAttribute(propertyAssignmentElement, "section");

                if (propertyTypeMap.get(property_type_code) != null)
                {
                    IPropertyAssignment assignment =
                            masterDataRegistrationTransaction.assignPropertyType(newSampleType, propertyTypeMap.get(property_type_code));
                    assignment.setMandatory(mandatory);
                    assignment.setSection(section);
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
