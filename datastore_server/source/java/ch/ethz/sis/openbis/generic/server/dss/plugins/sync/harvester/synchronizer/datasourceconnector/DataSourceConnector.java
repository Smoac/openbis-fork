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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.datasourceconnector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.B64Code;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.BasicAuthCredentials;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author Ganime Betul Akin
 */
public class DataSourceConnector implements IDataSourceConnector
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DataSourceConnector.class);

    final String ENTRY_START_TAG = "<url>";

    final String ENTRY_FINISH_TAG = "</url>";

    private final String dataSourceUrl;

    private final BasicAuthCredentials authCredentials;

    public DataSourceConnector(String url, BasicAuthCredentials authCredentials)
    {
        this.dataSourceUrl = url;
        this.authCredentials = authCredentials;
    }

    @Override
    public Document getResourceListAsXMLDoc(List<String> spaceBlackList) throws Exception
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        Request requestEntity = createResourceListRequest(client, spaceBlackList);

        operationLog.info("Start loading a resource list from " + requestEntity.getURI());

        ContentResponse contentResponse = getResponse(requestEntity);
        Document document = parse(contentResponse.getContent());

        if (isResourceListIndex(document))
        {
            operationLog.info("Received a resource list index (the resource list was too big and was split into parts).");
            List<String> locations = getResourceListPartLocations(document);
            List<String> parts = loadResourceListParts(client, locations);
            return mergeResourceListParts(parts);
        } else
        {
            operationLog.info("Received the resource list.");
            return document;
        }
    }

    private boolean isResourceListIndex(Document document)
    {
        return document != null && document.hasChildNodes() && document.getFirstChild().getNodeName().equals("sitemapindex");
    }

    private List<String> getResourceListPartLocations(Document document)
    {
        Node sitemapindex = document.getFirstChild();
        List<String> locations = new ArrayList<String>();

        for (int i = 0; i < sitemapindex.getChildNodes().getLength(); i++)
        {
            Node child = sitemapindex.getChildNodes().item(i);
            if (child.getNodeName().equals("sitemap"))
            {
                for (int j = 0; j < child.getChildNodes().getLength(); j++)
                {
                    Node grandChild = child.getChildNodes().item(j);
                    if (grandChild.getNodeName().equals("loc"))
                    {
                        String location = grandChild.getTextContent().trim();
                        operationLog.info("Resource list part location: " + location);
                        locations.add(location);
                    }
                }
            }
        }

        if (locations.isEmpty())
        {
            operationLog.info("No locations of the resource list parts were found in the index.");
        }

        return locations;
    }

    private List<String> loadResourceListParts(HttpClient client, List<String> locations) throws Exception
    {
        List<String> parts = new ArrayList<String>();

        for (String location : locations)
        {
            Request request = createRequest(client, location);
            operationLog.info("Start loading a resource list part from " + location);
            ContentResponse response = getResponse(request);
            operationLog.info("Received the resource list part.");
            parts.add(response.getContentAsString());
        }

        return parts;
    }

    private Document mergeResourceListParts(List<String> parts) throws Exception
    {
        StringBuilder merged = new StringBuilder();

        if (parts.size() > 0)
        {
            merged.append(parts.get(0).substring(0, parts.get(0).indexOf(ENTRY_START_TAG)));

            for (String part : parts)
            {
                int firstEntryIndex = part.indexOf(ENTRY_START_TAG);
                int lastEntryIndex = part.lastIndexOf(ENTRY_FINISH_TAG);

                if (firstEntryIndex != -1 && lastEntryIndex != -1 && firstEntryIndex < lastEntryIndex)
                {
                    merged.append(part.substring(firstEntryIndex, lastEntryIndex + ENTRY_FINISH_TAG.length()));
                }
            }

            merged.append("</urlset>");
        }

        operationLog.info("Merged the resource list parts.");

        return parse(merged.toString().getBytes());
    }

    private Document parse(byte[] content) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(bis);
        return doc;
    }

    private ContentResponse getResponse(Request requestEntity) throws InterruptedException, TimeoutException, ExecutionException, IOException
    {
        ContentResponse contentResponse;
        contentResponse = requestEntity.send();
        int statusCode = contentResponse.getStatus();

        if (statusCode != HttpStatus.Code.OK.getCode())
        {
            throw new IOException("Resource List could not be retrieved: " + contentResponse.getContentAsString());
        }
        return contentResponse;
    }

    private Request createResourceListRequest(HttpClient client, List<String> spaceBlackList)
    {
        StringBuffer sb = new StringBuffer();

        for (String dataSourceSpace : spaceBlackList)
        {
            sb.append(dataSourceSpace + ",");
        }

        String url = dataSourceUrl + "?verb=resourcelist.xml";

        if (sb.length() != 0)
        {
            String str = sb.toString();
            str = str.substring(0, str.length() - 1);
            url += "&black_list=" + str;
        }

        return createRequest(client, url);
    }

    private Request createRequest(HttpClient client, String url)
    {
        Request requestEntity = client.newRequest(url).method("GET");
        requestEntity.header(HttpHeader.AUTHORIZATION, "Basic " + B64Code.encode(authCredentials.getUser() + ":" + authCredentials.getPassword()));
        return requestEntity;
    }

}
