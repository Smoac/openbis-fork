/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
@Test(groups =
{ "slow" })
public class OaipmhServletTest extends SystemTestCase
{

    private static final String GENERAL_INFORMATION_SERVICE_URL = TestInstanceHostUtils.getOpenBISUrl() + IGeneralInformationService.SERVICE_URL;

    private static final String OAIPMH_SERVLET_URL = TestInstanceHostUtils.getDSSUrl() + "/oaipmh/";

    private static final String USER_ID = "test";

    private static final String USER_PASSWORD = "password";

    private IGeneralInformationService generalInformationService;

    private DocumentBuilder xmlBuilder;

    private XPath xPath;

    @BeforeClass
    public void beforeClass() throws ParserConfigurationException
    {
        generalInformationService = HttpInvokerUtils.createServiceStub(IGeneralInformationService.class, GENERAL_INFORMATION_SERVICE_URL, 5000);
        xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xPath = XPathFactory.newInstance().newXPath();
    }

    @Test
    public void testWithoutAuthorizationHeader()
    {
        GetMethod method = sendRequest(null, OAIPMH_SERVLET_URL + "?verb=Identify");
        Assert.assertEquals(401, method.getStatusCode());
    }

    @Test
    public void testWithIncorrectAuthorizationHeader()
    {
        GetMethod method = sendRequest("This is an invalid header", OAIPMH_SERVLET_URL + "?verb=Identify");
        Assert.assertEquals(500, method.getStatusCode());
    }

    @Test
    public void testWithIncorrectCredentials()
    {
        GetMethod method = sendRequest("incorrect", USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=Identify");
        Assert.assertEquals(401, method.getStatusCode());
    }

    @Test
    public void testIdentify() throws InterruptedException
    {
        GetMethod method = sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=Identify");
        Assert.assertEquals(200, method.getStatusCode());
        Document document = parseResponse(method);
        Assert.assertEquals("TEST", evaluateToString(document, "/OAI-PMH/Identify/repositoryName"));
    }

    @Test
    public void testListMetadataformats()
    {
        GetMethod method = sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListMetadataFormats");
        Assert.assertEquals(200, method.getStatusCode());
        Document document = parseResponse(method);
        Assert.assertEquals("testPrefix", evaluateToString(document, "/OAI-PMH/ListMetadataFormats/metadataFormat/metadataPrefix"));
    }

    @Test
    public void testListSets()
    {
        GetMethod method = sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListSets");
        Assert.assertEquals(200, method.getStatusCode());
        Document document = parseResponse(method);
        Assert.assertEquals("This repository does not support sets", evaluateToString(document, "/OAI-PMH/error"));
    }

    @Test
    public void testListIdentifiers()
    {
        String sessionToken = generalInformationService.tryToAuthenticateForAllServices(USER_ID, USER_PASSWORD);
        List<DataSet> dataSets = generalInformationService.searchForDataSets(sessionToken, new SearchCriteria());

        String resumptionToken = null;
        int dataSetCount = 0;

        do
        {
            GetMethod method = null;
            if (resumptionToken == null)
            {
                method = sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListIdentifiers&metadataPrefix=testPrefix");
            } else
            {
                method = sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListIdentifiers&resumptionToken=" + resumptionToken);
            }
            Assert.assertEquals(200, method.getStatusCode());

            Document document = parseResponse(method);
            dataSetCount += evaluateToNodeList(document, "/OAI-PMH/ListIdentifiers/header").getLength();
            resumptionToken = evaluateToString(document, "/OAI-PMH/ListIdentifiers/resumptionToken");

        } while (resumptionToken != null && !resumptionToken.isEmpty());

        Assert.assertEquals(dataSets.size(), dataSetCount);
    }

    @Test
    public void testListRecords()
    {
        String sessionToken = generalInformationService.tryToAuthenticateForAllServices(USER_ID, USER_PASSWORD);
        List<DataSet> dataSets = generalInformationService.searchForDataSets(sessionToken, new SearchCriteria());

        String resumptionToken = null;
        int dataSetCount = 0;

        do
        {
            GetMethod method = null;
            if (resumptionToken == null)
            {
                method = sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListRecords&metadataPrefix=testPrefix");
            } else
            {
                method = sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=ListRecords&resumptionToken=" + resumptionToken);
            }
            Assert.assertEquals(200, method.getStatusCode());

            Document document = parseResponse(method);
            dataSetCount += evaluateToNodeList(document, "/OAI-PMH/ListRecords/record").getLength();
            resumptionToken = evaluateToString(document, "/OAI-PMH/ListRecords/resumptionToken");

        } while (resumptionToken != null && !resumptionToken.isEmpty());

        Assert.assertEquals(dataSets.size(), dataSetCount);
    }

    @Test
    public void testGetRecord()
    {
        GetMethod method =
                sendRequest(USER_ID, USER_PASSWORD, OAIPMH_SERVLET_URL + "?verb=GetRecord&metadataPrefix=testPrefix&identifier=20081105092159111-1");
        Assert.assertEquals(200, method.getStatusCode());

        Document document = parseResponse(method);
        Assert.assertEquals("20081105092159111-1", evaluateToString(document, "/OAI-PMH/GetRecord/record/header/identifier"));
        Assert.assertEquals("FEMALE", evaluateToString(document, "/OAI-PMH/GetRecord/record/metadata/properties/property[@code='GENDER']"));
    }

    private GetMethod sendRequest(String user, String password, String url)
    {
        String authorizationHeader = "Basic " + new String(Base64.encodeBase64(new String(user + ":" + password).getBytes()));
        return sendRequest(authorizationHeader, url);
    }

    private GetMethod sendRequest(String authorizationHeader, String url)
    {
        try
        {
            operationLog.info("Sending OAI-PMH request: " + url);

            HttpClient httpClient = new HttpClient();
            GetMethod method = new GetMethod(url);
            if (authorizationHeader != null)
            {
                method.setRequestHeader("Authorization", authorizationHeader);
            }
            httpClient.executeMethod(method);

            operationLog.info("Received OAI-PMH response: " + method.getResponseBodyAsString());

            return method;
        } catch (HttpException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private Document parseResponse(GetMethod method)
    {
        try
        {
            String body = method.getResponseBodyAsString();
            return xmlBuilder.parse(new ByteArrayInputStream(body.getBytes()));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (SAXException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private String evaluateToString(Document document, String xpath)
    {
        return (String) evaluate(document, xpath, XPathConstants.STRING);
    }

    private NodeList evaluateToNodeList(Document document, String xpath)
    {
        return (NodeList) evaluate(document, xpath, XPathConstants.NODESET);
    }

    private Object evaluate(Document document, String xpath, QName returnType)
    {
        try
        {
            return xPath.compile(xpath).evaluate(document, returnType);
        } catch (XPathExpressionException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
