/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
public class HttpTest
{

    private static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, HttpTest.class);

    private static DocumentBuilder xmlBuilder;

    private static XPath xPath;

    public static GetMethod sendRequest(String user, String password, String url)
    {
        String authorizationHeader = "Basic " + new String(Base64.encodeBase64(new String(user + ":" + password).getBytes()));
        return sendRequest(authorizationHeader, url);
    }

    public static GetMethod sendRequest(String authorizationHeader, String url)
    {
        try
        {
            operationLog.info("Sending HTTP request: " + url);

            HttpClient httpClient = new HttpClient();
            GetMethod method = new GetMethod(url);
            if (authorizationHeader != null)
            {
                method.setRequestHeader("Authorization", authorizationHeader);
            }
            httpClient.executeMethod(method);

            operationLog.info("Received HTTP response: " + method.getResponseBodyAsString());

            return method;
        } catch (HttpException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public static Document parseResponse(GetMethod method)
    {
        try
        {
            String body = method.getResponseBodyAsString();
            return getXmlBuilder().parse(new ByteArrayInputStream(body.getBytes()));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (SAXException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public static String evaluateToString(Document document, String xpath)
    {
        return (String) evaluate(document, xpath, XPathConstants.STRING);
    }

    public static NodeList evaluateToNodeList(Document document, String xpath)
    {
        return (NodeList) evaluate(document, xpath, XPathConstants.NODESET);
    }

    private static Object evaluate(Document document, String xpath, QName returnType)
    {
        try
        {
            return getXPath().compile(xpath).evaluate(document, returnType);
        } catch (XPathExpressionException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public static DocumentBuilder getXmlBuilder()
    {
        if (xmlBuilder != null)
        {
            return xmlBuilder;
        }
        try
        {
            xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return xmlBuilder;
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private static XPath getXPath()
    {
        if (xPath != null)
        {
            return xPath;
        }
        try
        {
            xPath = XPathFactory.newInstance().newXPath();
            return xPath;
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

}
