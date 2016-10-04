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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpStatus;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.ethz.sis.openbis.generic.server.dss.plugins.harvester.config.BasicAuthCredentials;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class DataSourceConnector
{
    private final String dataSourceUrl;

    private final BasicAuthCredentials authCredentials;

    public DataSourceConnector(String url, BasicAuthCredentials authCredentials)
    {
        this.dataSourceUrl = url;
        this.authCredentials = authCredentials;
    }

    public Document getResourceListAsXMLDoc(List<String> spaceBlackList) throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException,
            URISyntaxException,
            InterruptedException, TimeoutException, ExecutionException
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        addAuthenticationCredentials(client);
        Request requestEntity = createNewHttpRequest(client, spaceBlackList);
        ContentResponse contentResponse = getResponse(requestEntity);
        return parseResponse(contentResponse);
    }

    private Document parseResponse(ContentResponse contentResponse) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        byte[] content = contentResponse.getContent();
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

    private Request createNewHttpRequest(HttpClient client, List<String> spaceBlackList)
    {
        StringBuffer sb = new StringBuffer();
        for (String dataSourceSpace : spaceBlackList)
        {
            sb.append(dataSourceSpace + ",");
        }
        String req = dataSourceUrl + "?verb=resourcelist.xml";
        if (sb.length() != 0)
        {
            String str = sb.toString();
            str = str.substring(0, str.length() - 1);
            req += "&black_list=" + str;
        }
        Request requestEntity = client.newRequest(req).method("GET");
        return requestEntity;
    }

    private void addAuthenticationCredentials(HttpClient client) throws URISyntaxException
    {
        AuthenticationStore auth = client.getAuthenticationStore();
        auth.addAuthentication(new BasicAuthentication(new URI(dataSourceUrl), authCredentials.getRealm(), authCredentials.getUser(), authCredentials
                .getPassword()));
    }
}
