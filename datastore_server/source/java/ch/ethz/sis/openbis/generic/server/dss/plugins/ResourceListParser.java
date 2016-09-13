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
package ch.ethz.sis.openbis.generic.server.dss.plugins;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.Connection;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.DataSetWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.ExperimentWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.MaterialWithLastModificationDate;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.ProjectWithConnections;
import ch.ethz.sis.openbis.generic.server.dss.plugins.ResourceListParserData.SampleWithConnections;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Ganime Betul Akin
 */
public class ResourceListParser
{
    private final ResourceListParserData data;


    private final HashMap<String, String> spaceMappings;

    private ResourceListParser(HashMap<String, String> spaceMappings)
    {
        // TODO do the returning of parser data better
        this.data = new ResourceListParserData();
        this.spaceMappings = spaceMappings;
    }

    public static ResourceListParser create(HashMap<String, String> spaceMappings)
    {
        ResourceListParser parser = new ResourceListParser(spaceMappings);
        return parser;
    }

    public ResourceListParserData parseResourceListDocument(Document doc) throws XPathExpressionException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext()
            {
                public String getNamespaceURI(String prefix)
                {
                    if (prefix == null)
                        throw new NullPointerException("Null prefix");
                    else if ("s".equals(prefix))
                        return "http://www.sitemaps.org/schemas/sitemap/0.9";
                    else if ("rs".equals(prefix))
                        return "http://www.openarchives.org/rs/terms/";
                    else if ("x".equals(prefix))
                        return "https://sis.id.ethz.ch/software/#openbis/xdterms/";
                    else if ("xml".equals(prefix))
                        return XMLConstants.XML_NS_URI;
                    return XMLConstants.NULL_NS_URI;
                }

                // This method isn't necessary for XPath processing.
                public String getPrefix(String uri)
                {
                    throw new UnsupportedOperationException("Not implemented!!!");
                }

                // This method isn't necessary for XPath processing either.
                public Iterator getPrefixes(String uri)
                {
                    throw new UnsupportedOperationException("Not implemented!!!");
                }
            });
        List<String> uris = getResourceLocations(doc, xpath);
        for (String uri : uris)
        {

            parseUriMetaData(doc, xpath, uri);
        }

        return data;
    }

    private List<String> getResourceLocations(Document doc, XPath xpath) throws XPathExpressionException
    {
        XPathExpression expr = xpath.compile("/s:urlset/s:url/s:loc");// "//*[local-name()='loc']/text()"); //"//s:loc/text()"

        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            // System.out.print(nodes.item(i).getNodeName() + ":" + nodes.item(i).getAttributes().getNamedItem("href"));
            String uri = nodes.item(i).getTextContent();
            // if (uri.endsWith("MASTER_DATA/MASTER_DATA/M"))
            // {
            // // parseMasterData(doc, xpath, uri);
            // }
            // else
            if (uri.endsWith("/M"))
            {
                list.add(uri);
            }
        }
        return list;
    }

    private void parseMasterData(Document doc, XPath xpath, String uri) throws XPathExpressionException
    {
        XPathExpression expr =
                xpath.compile("//s:url/s:loc[normalize-space(.)='" + uri + "']//following-sibling::*[local-name() = 'masterData'][1]");
        Node xdNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (xdNode == null)
        {
            throw new XPathExpressionException("The master data resurce list should contain 1 master data element");
        }
        Element docElement = (Element) xdNode;
        NodeList sampleTypesNode = docElement.getElementsByTagName("sampleTypes");
        if (sampleTypesNode.getLength() == 1)
        {
            Element sampleTypesElement = (Element) sampleTypesNode.item(0);
            NodeList sampleTypeNodes = sampleTypesElement.getElementsByTagName("sampleType");
            for (int i = 0; i < sampleTypeNodes.getLength(); i++)
            {
                Element sampleTypeElement = (Element) sampleTypeNodes.item(i);
                // TODO proper error handling needed below in case the XML is not correct and item 0 does not exist
                String code = sampleTypeElement.getElementsByTagName("code").item(0).getTextContent();

            }
        }
    }

    private void parseUriMetaData(Document doc, XPath xpath, String uri) throws XPathExpressionException
    {
        Date lastModificationDate = extractLastModificationDate(doc, xpath, uri);

        Node xdNode = extractXdNode(doc, xpath, uri);
        String entityKind = xdNode.getAttributes().getNamedItem("kind").getTextContent();

        if ("PROJECT".equals(entityKind))
        {
            parseProjectMetaData(xpath, extractPermIdFromURI(uri), xdNode, lastModificationDate);
        }
        else if ("EXPERIMENT".equals(entityKind))
        {
            parseExperimentMetaData(xpath, extractPermIdFromURI(uri), xdNode, lastModificationDate);
        }
        else if ("SAMPLE".equals(entityKind))
        {
            parseSampleMetaData(xpath, extractPermIdFromURI(uri), xdNode, lastModificationDate);
        }
        else if ("DATA_SET".equals(entityKind))
        {
            parseDataSetMetaData(xpath, extractPermIdFromURI(uri), xdNode, lastModificationDate);
        }
        else if ("MATERIAL".equals(entityKind))
        {
            parseMaterialMetaData(xpath, extractMaterialCodeFromURI(uri), xdNode, lastModificationDate);
        }
        // expr = xpath.compile("//s:loc[normalize-space(.) ='" + uri + "']/../x:xd/x:properties/x:property"); //
        // "//*/text()[normalize-space(.)='" + loc_text + "']/parent::/rs:ln");// "/s:urlset/s:url[/s:loc/text() = '" +
        // for (int ji = 0; ji < url_node.getLength(); ji++)
        // {
        // System.out.print(url_node.item(ji).getNodeName() + ":" + url_node.item(ji).getAttributes().getNamedItem("kind"));
        // System.out.println();
        // }
    }

    private Date extractLastModificationDate(Document doc, XPath xpath, String uri) throws XPathExpressionException
    {
        XPathExpression expr = xpath.compile("//s:url/s:loc[normalize-space(.)='" + uri + "']//following-sibling::s:lastmod[1]");
        Node lastModNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (lastModNode == null)
        {
            throw new XPathExpressionException("The resource list should contain 1 lastmod element per resource");
        }

        String lastModDataStr = lastModNode.getTextContent().trim();
        // TODO data source servlet that generates the XML on the data source side MUST use the same format
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        // DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try
        {
            return df1.parse(lastModDataStr);
        } catch (ParseException e)
        {
            e.printStackTrace();
            throw new XPathExpressionException("Lastmod date cannot be parsed");
        }
    }

    private Node extractXdNode(Document doc, XPath xpath, String uri) throws XPathExpressionException
    {
        // alternative expression: //s:url/s:loc[normalize-space(.)='" + uri + "']/../x:xd");
        XPathExpression expr = xpath.compile("//s:url/s:loc[normalize-space(.)='" + uri + "']//following-sibling::x:xd[1]");
        Node xdNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (xdNode == null)
        {
            throw new XPathExpressionException("The resource list should contain 1 xd element per resource");
        }
        return xdNode;
    }

    private void parseDataSetMetaData(XPath xpath, String permId, Node xdNode, Date lastModificationDate)
    {
        String code = xdNode.getAttributes().getNamedItem("code").getTextContent();
        String sample = xdNode.getAttributes().getNamedItem("sample").getTextContent();
        String experiment = xdNode.getAttributes().getNamedItem("experiment").getTextContent();
        String type = xdNode.getAttributes().getNamedItem("type").getTextContent();
        String dsKind = xdNode.getAttributes().getNamedItem("dsKind").getTextContent();
        NewExternalData ds = new NewExternalData();
        if (dsKind.equals(DataSetKind.CONTAINER.toString()))
        {
            ds = new NewContainerDataSet();
            ds.setCode(code);
            ds.setDataSetType(new DataSetType(type));
            ds.setDataStoreCode("STANDARD");
            if (sample.trim().equals("") == false)
            {
                ds.setSampleIdentifierOrNull(getSampleIdentifier(sample));
            }
            if (experiment.trim().equals("") == false)
            {
                ds.setExperimentIdentifierOrNull(getExperimentIdentifier(experiment));
            }
        }
        else if (dsKind.equals(DataSetKind.PHYSICAL.toString()))
        {
            ds.setCode(code);
            ds.setDataSetType(new DataSetType(type));
            ds.setDataStoreCode("STANDARD");
            if (sample.trim().equals("") == false)
            {
                ds.setSampleIdentifierOrNull(getSampleIdentifier(sample));
            }
            if (experiment.trim().equals("") == false)
            {
                ds.setExperimentIdentifierOrNull(getExperimentIdentifier(experiment));
            }
        }
        DataSetWithConnections newDsWithConns = data.new DataSetWithConnections(ds, lastModificationDate);
        data.datasetsToProcess.put(permId, newDsWithConns);
        newDsWithConns.setConnections(parseConnections(xpath, xdNode));
        ds.setDataSetProperties(parseDataSetProperties(xpath, xdNode));
    }

    private SampleIdentifier getSampleIdentifier(String sampleIdentifierStr)
    {
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sampleIdentifierStr);
        SpaceIdentifier spaceLevel = sampleIdentifier.getSpaceLevel();
        String originalSpaceCode = spaceLevel.getSpaceCode();
        return new SampleIdentifier(new SpaceIdentifier(spaceMappings.get(originalSpaceCode)), sampleIdentifier.getSampleCode());
    }

    private ExperimentIdentifier getExperimentIdentifier(String experiment)
    {
        ExperimentIdentifier experimentIdentifier = ExperimentIdentifierFactory.parse(experiment);
        String originalSpaceCode = experimentIdentifier.getSpaceCode();
        String projectCode = experimentIdentifier.getProjectCode();
        String expCode = experimentIdentifier.getExperimentCode();
        return new ExperimentIdentifier(new ProjectIdentifier(spaceMappings.get(originalSpaceCode), projectCode), expCode);
    }

    private void parseProjectMetaData(XPath xpath, String permId, Node xdNode, Date lastModificationDate)
    {

        String code = xdNode.getAttributes().getNamedItem("code").getTextContent();
        String desc = xdNode.getAttributes().getNamedItem("desc").getTextContent();
        String space = xdNode.getAttributes().getNamedItem("space").getTextContent();
        // TODO is there a better way to create project identifier below?
        NewProject newProject = new NewProject("/" + spaceMappings.get(space) + "/" + code, desc);
        newProject.setPermID(permId);
        ProjectWithConnections newPrjWithConns =
                data.new ProjectWithConnections(newProject, lastModificationDate);
        data.projectsToProcess.put(permId, newPrjWithConns);
        newPrjWithConns.setConnections(parseConnections(xpath, xdNode));
    }

    private void parseMaterialMetaData(XPath xpath, String permId, Node xdNode, Date lastModificationDate)
    {
        String code = xdNode.getAttributes().getNamedItem("code").getTextContent();
        String type = xdNode.getAttributes().getNamedItem("type").getTextContent();
        NewMaterialWithType newMaterial = new NewMaterialWithType(code, type);
        MaterialWithLastModificationDate materialWithLastModDate =
                data.new MaterialWithLastModificationDate(newMaterial, lastModificationDate);
        data.materialsToProcess.add(materialWithLastModDate);
        newMaterial.setProperties(parseProperties(xpath, xdNode));
    }

    private List<Connection> parseConnections(XPath xpath, Node xdNode)
    {
        List<Connection> conns = new ArrayList<Connection>();
        Element docElement = (Element) xdNode;
        NodeList connsNode = docElement.getElementsByTagName("x:connections");
        if (connsNode.getLength() == 1)
        {
            Element connsElement = (Element) connsNode.item(0);
            NodeList connNodes = connsElement.getElementsByTagName("x:connection");
            for (int i = 0; i < connNodes.getLength(); i++)
            {
                String to = connNodes.item(i).getAttributes().getNamedItem("to").getTextContent();
                String type = connNodes.item(i).getAttributes().getNamedItem("type").getTextContent();
                conns.add(data.new Connection(to, type));
            }
        }
        return conns;
    }

    private List<NewProperty> parseDataSetProperties(XPath xpath, Node xdNode)
    {

        List<NewProperty> dsProperties = new ArrayList<NewProperty>();
        Element docElement = (Element) xdNode;
        NodeList propsNode = docElement.getElementsByTagName("x:properties");
        if (propsNode.getLength() == 1)
        {
            Element propsElement = (Element) propsNode.item(0);
            NodeList propertyNodes = propsElement.getElementsByTagName("x:property");
            for (int i = 0; i < propertyNodes.getLength(); i++)
            {
                Element propertyElement = (Element) propertyNodes.item(i);
                // TODO proper error handling needed below in case the XML is not correct and item 0 does not exist
                String code = propertyElement.getElementsByTagName("x:code").item(0).getTextContent();
                String val = propertyElement.getElementsByTagName("x:value").item(0).getTextContent();
                dsProperties.add(new NewProperty(code, val));
            }
        }
        return dsProperties;
    }

    private EntityProperty[] parseProperties(XPath xpath, Node xdNode)
    {

        List<EntityProperty> entityProperties = new ArrayList<EntityProperty>();
        Element docElement = (Element) xdNode;
        NodeList propsNode = docElement.getElementsByTagName("x:properties");
        if (propsNode.getLength() == 1)
        {
            Element propsElement = (Element) propsNode.item(0);
            NodeList propertyNodes = propsElement.getElementsByTagName("x:property");
            for (int i = 0; i < propertyNodes.getLength(); i++)
            {
                Element propertyElement = (Element) propertyNodes.item(i);
                // TODO proper error handling needed below in case the XML is not correct and item 0 does not exist
                String code = propertyElement.getElementsByTagName("x:code").item(0).getTextContent();
                String val = propertyElement.getElementsByTagName("x:value").item(0).getTextContent();
                EntityProperty property = createEntityProperty(code, val);
                entityProperties.add(property);
            }
        }
        return entityProperties.toArray(new EntityProperty[entityProperties.size()]);
    }

    private EntityProperty createEntityProperty(String code, String val)
    {
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        property.setPropertyType(propertyType);
        property.setValue(val);
        return property;
    }

    private void parseExperimentMetaData(XPath xpath, String permId, Node xdNode, Date lastModificationDate)
    {
        String code = xdNode.getAttributes().getNamedItem("code").getTextContent();
        String type = xdNode.getAttributes().getNamedItem("type").getTextContent();
        String project = xdNode.getAttributes().getNamedItem("project").getTextContent();
        String space = xdNode.getAttributes().getNamedItem("space").getTextContent();
        NewExperiment newExp = new NewExperiment("/" + spaceMappings.get(space) + "/" + project + "/" + code, type);
        newExp.setPermID(permId);
        ExperimentWithConnections newExpWithConns = data.new ExperimentWithConnections(newExp, lastModificationDate);
        data.experimentsToProcess.put(permId, newExpWithConns);
        newExpWithConns.setConnections(parseConnections(xpath, xdNode));
        newExp.setProperties(parseProperties(xpath, xdNode));
    }

    private void parseSampleMetaData(XPath xpath, String permId, Node xdNode, Date lastModificationDate)
    {
        String code = xdNode.getAttributes().getNamedItem("code").getTextContent();
        String type = xdNode.getAttributes().getNamedItem("type").getTextContent();
        String experiment = xdNode.getAttributes().getNamedItem("experiment").getTextContent();
        String space = xdNode.getAttributes().getNamedItem("space").getTextContent();
        SampleType sampleType = new SampleType();
        sampleType.setCode(type);

        NewSample newSample = new NewSample("/" + spaceMappings.get(space) + "/" + code, sampleType, null, null,
                experiment.trim().equals("") ? null : experiment, null, null, new IEntityProperty[0],
                new ArrayList<NewAttachment>());
        newSample.setPermID(permId);
        SampleWithConnections newSampleWithConns = data.new SampleWithConnections(newSample, lastModificationDate);
        data.samplesToProcess.put(permId, newSampleWithConns);
        newSampleWithConns.setConnections(parseConnections(xpath, xdNode));
        newSample.setProperties(parseProperties(xpath, xdNode));
    }

    private String extractPermIdFromURI(String uri) throws XPathExpressionException
    {
        Pattern pattern = Pattern.compile("([0-9\\-]{17,})");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        throw new XPathExpressionException("Malformed resource url");
    }

    private String extractMaterialCodeFromURI(String uri) throws XPathExpressionException
    {
        // TODO malformed uri handling
        String[] parts = uri.split("/");
        return parts[parts.length - 2];
    }
}
