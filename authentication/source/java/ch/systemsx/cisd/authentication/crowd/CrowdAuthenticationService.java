/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.crowd;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.crowd.CrowdSoapElements.SOAPAttribute;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * This <code>IAuthenticationService</code> implementation first registers the application on the <i>Crowd</i>
 * server, then authenticates the user.
 * 
 * @author Franz-Josef Elmer
 */
public class CrowdAuthenticationService implements IAuthenticationService
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CrowdAuthenticationService.class);

    /** The template to authenticate the application. */
    private static final MessageFormat AUTHENTICATE_APPL =
            new MessageFormat("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                    + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + " <soap:Body>\n"
                    + "   <authenticateApplication xmlns=\"urn:SecurityServer\">\n" + "     <in0>\n"
                    + "       <credential xmlns=\"http://authentication.integration.crowd.atlassian.com\">\n"
                    + "         <credential>{1}</credential>\n" + "       </credential>\n"
                    + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</name>\n"
                    + "       <validationFactors xmlns=\"http://authentication.integration.crowd.atlassian.com\" "
                    + "                          xsi:nil=\"true\" />\n" + "     </in0>\n"
                    + "   </authenticateApplication>\n" + " </soap:Body>\n" + "</soap:Envelope>\n");

    /** The template to authenticate the user. */
    private static final MessageFormat AUTHENTICATE_USER =
            new MessageFormat(
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                            + " <soap:Body>\n"
                            + "   <authenticatePrincipal xmlns=\"urn:SecurityServer\">\n"
                            + "     <in0>\n"
                            + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</name>\n"
                            + "       <token xmlns=\"http://authentication.integration.crowd.atlassian.com\">{1}</token>\n"
                            + "     </in0>\n"
                            + "     <in1>\n"
                            + "       <application xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</application>\n"
                            + "       <credential xmlns=\"http://authentication.integration.crowd.atlassian.com\">\n"
                            + "         <credential>{3}</credential>\n"
                            + "       </credential>\n"
                            + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{2}</name>\n"
                            + "       <validationFactors xmlns=\"http://authentication.integration.crowd.atlassian.com\" />\n"
                            + "     </in1>\n" + "   </authenticatePrincipal>\n" + " </soap:Body>\n"
                            + "</soap:Envelope>\n");

    /** The template to find a principal by token or by name. */
    private static final MessageFormat FIND_PRINCIPAL_BY_NAME =
            new MessageFormat(
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                            + "   <soap:Body>\n"
                            + "       <findPrincipalByName xmlns=\"urn:SecurityServer\">\n"
                            + "           <in0>\n"
                            + "               <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</name>\n"
                            + "               <token xmlns=\"http://authentication.integration.crowd.atlassian.com\">{1}</token>\n"
                            + "           </in0>\n"
                            + "           <in1>{2}</in1>\n"
                            + "       </findPrincipalByName>\n"
                            + "   </soap:Body>\n" + "</soap:Envelope>\n");

    private final String url;

    private final String application;

    private final String applicationPassword;

    public CrowdAuthenticationService(String host, int port, String application, String applicationPassword)
    {
        this("https://" + host + ":" + port + "/crowd/services/SecurityServer", application, applicationPassword);
    }

    public CrowdAuthenticationService(String url, String application, String applicationPassword)
    {
        this.url = url;
        this.application = application;
        this.applicationPassword = applicationPassword;
        if (operationLog.isDebugEnabled())
        {
            final String msg =
                    "A new CrowdAuthenticationService instance has been created for [" + "url=" + url
                            + ", application=" + application + "]";
            operationLog.debug(msg);
        }
    }

    public final void checkAvailability()
    {
        try
        {
            String response = execute(AUTHENTICATE_APPL, application, applicationPassword);
            if (pickElementContent(response, CrowdSoapElements.TOKEN) == null)
            {
                throw new EnvironmentFailureException("Application '" + application + "' couldn't be authenticated: "
                        + response);
            }
        } catch (EnvironmentFailureException ex)
        {
            throw ex;
        } catch (CheckedExceptionTunnel ex)
        {
            throw new EnvironmentFailureException(ex.getMessage(), ex.getCause());
        } catch (RuntimeException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage(), ex);
        }
    }

    public final Principal authenticate(String user, String password)
    {
        assert user != null;
        // Application login
        final String applicationToken =
                StringEscapeUtils.unescapeXml(execute(CrowdSoapElements.TOKEN, AUTHENTICATE_APPL, application,
                        applicationPassword));
        if (applicationToken == null)
        {
            operationLog.error("CROWD: application '" + application + "' failed to authenticate.");
            return null;
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("CROWD: application '" + application + "' successfully authenticated.");
            }
        }
        final String userToken =
                StringEscapeUtils.unescapeXml(execute("out", AUTHENTICATE_USER, application, applicationToken, user,
                        password));
        if (operationLog.isInfoEnabled())
        {
            final String msg = "CROWD: authentication of user '" + user + "', application '" + application + "': ";
            if (userToken == null)
            {
                operationLog.info(msg + "FAILED.");
            } else
            {
                operationLog.info(msg + "SUCCESS.");
            }
        }
        if (userToken == null)
        {
            return null;
        }
        // Find principal by name. Obviously we do not need to make an user authentication to get
        // these informations.
        String xmlResponse = null;
        try
        {
            xmlResponse = execute(FIND_PRINCIPAL_BY_NAME, application, applicationToken, user);
            Map<SOAPAttribute, String> parseXmlResponse = parseXmlResponse(xmlResponse);
            Principal principal;
            if (parseXmlResponse.size() < 1)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("No SOAPAttribute element could be found in the SOAP XML response.");
                }
                principal = null;
            } else
            {
                principal = createPrincipal(user, parseXmlResponse);
            }
            if (principal == null)
            {
                throw new EnvironmentFailureException("CROWD: Principal information for user '" + user
                        + "' could not be obtained.");
            }
            return principal;
            // SAXException, IOException
        } catch (Exception ex)
        {
            String message = "Parsing XML response '" + xmlResponse + "' throws an Exception.";
            throw new EnvironmentFailureException(message, ex);
        }
    }

    /**
     * Parses given <i>Crowd</i> XML response and returns a map of found <code>SOAPAttribute</code>s.
     * <p>
     * Never returns <code>null</code> but could returns an empty <code>Map</code>.
     * </p>
     */
    private final static Map<CrowdSoapElements.SOAPAttribute, String> parseXmlResponse(String xmlResponse)
            throws SAXException, IOException
    {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        SOAPAttributeContentHandler contentHandler = new SOAPAttributeContentHandler();
        xmlReader.setContentHandler(contentHandler);
        StringReader stringReader = new StringReader(xmlResponse);
        xmlReader.parse(new InputSource(stringReader));
        stringReader.close();
        return contentHandler.getSoapAttributes();
    }

    /** Creates a <code>Principal</code> with found SOAP attributes. */
    private final static Principal createPrincipal(String user,
            Map<CrowdSoapElements.SOAPAttribute, String> soapAttributes)
    {
        String firstName = soapAttributes.get(CrowdSoapElements.SOAPAttribute.givenName);
        String lastName = soapAttributes.get(CrowdSoapElements.SOAPAttribute.sn);
        String email = soapAttributes.get(CrowdSoapElements.SOAPAttribute.mail);
        Principal principal = new Principal(user, firstName, lastName, email);
        principal.setProperty(CrowdSoapElements.SOAPAttribute.invalidPasswordAttempts.name(), Integer
                .valueOf(soapAttributes.get(CrowdSoapElements.SOAPAttribute.invalidPasswordAttempts)));
        principal.setProperty(CrowdSoapElements.SOAPAttribute.requiresPasswordChange.name(), Boolean
                .valueOf(soapAttributes.get(CrowdSoapElements.SOAPAttribute.requiresPasswordChange)));
        principal.setProperty(CrowdSoapElements.SOAPAttribute.lastAuthenticated.name(), new Date(Long
                .valueOf(soapAttributes.get(CrowdSoapElements.SOAPAttribute.lastAuthenticated))));
        principal.setProperty(CrowdSoapElements.SOAPAttribute.passwordLastChanged.name(), new Date(Long
                .valueOf(soapAttributes.get(CrowdSoapElements.SOAPAttribute.passwordLastChanged))));
        return principal;
    }

    /**
     * Constructs the POST message, does the HTTP request and picks the given <code>responseElement</code> in the
     * server's response.
     * 
     * @return The <var>responseElement</var> in the server's response.
     */
    private final String execute(String responseElement, MessageFormat template, String... args)
    {
        final String response = execute(template, args);
        return pickElementContent(response, responseElement);
    }

    private final String execute(MessageFormat template, String... args)
    {
        final Object[] decodedArguments = new Object[args.length];
        for (int i = 0; i < args.length; i++)
        {
            decodedArguments[i] = StringEscapeUtils.escapeXml(args[i]);
        }
        return execute(template.format(decodedArguments));
    }

    /**
     * Tries to find given <code>element</code> in <code>xmlString</code>.
     * <p>
     * Note that this is a special-perpose method not suitable for putting it into general utility classes. For example
     * it does not find empty elements.
     * 
     * @return The requested element, or <code>null</code> if it could not be found.
     */
    private static final String pickElementContent(String xmlString, String element)
    {
        if (xmlString == null)
        {
            operationLog.error("Response of web service is invalid (null). We were looking for element '" + element
                    + "'.");
            return null;
        }

        int index = xmlString.indexOf("<" + element);
        if (index < 0)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Element '" + element + "' could not be found in '"
                        + StringUtils.abbreviate(xmlString, 50) + "'.");
            }
            return null;
        }
        index = xmlString.indexOf(">", index);
        if (index < 0)
        {
            operationLog.error("Element '" + element + "' seems to be present but XML is invalid: '"
                    + StringUtils.abbreviate(xmlString, 50) + "'.");
            return null;
        }
        int endIndex = xmlString.indexOf("</" + element + ">", index);
        if (endIndex < 0)
        {
            operationLog.error("Start tag of element '" + element + "' is present but end tag is missing: '"
                    + StringUtils.abbreviate(xmlString, 50) + "'.");
            return null;
        }
        return xmlString.substring(index + 1, endIndex);
    }

    /**
     * Makes a POST request with "application/soap+xml" as content type.
     * 
     * @return The server's response to the request.
     */
    private final String execute(String xmlMessage)
    {
        try
        {
            HttpClient client = new HttpClient();
            PostMethod post = new PostMethod(url);
            StringRequestEntity entity = new StringRequestEntity(xmlMessage, "application/soap+xml", "utf-8");
            post.setRequestEntity(entity);
            String response = null;
            try
            {
                client.executeMethod(post);
                response = post.getResponseBodyAsString();
            } finally
            {
                post.releaseConnection();
            }
            return response;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
