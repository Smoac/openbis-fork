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

import java.util.EnumMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.systemsx.cisd.authentication.crowd.CrowdSoapElements.SOAPAttribute;

/**
 * Looks for <code>SOAPAttribute</code> elements in a XML file and tries to digest their 'name-values' children.
 * <p>
 * When its job is finished, result is returned using {@link #getSoapAttributes()}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class SOAPAttributeContentHandler extends DefaultHandler
{

    private final Map<CrowdSoapElements.SOAPAttribute, String> soapAttributes;

    private static enum Child
    {
        soapAttributeName, soapAttributeValues;
    }

    private boolean inSoapAttribute;

    private Child currentChild;

    private SOAPAttribute soapAttribute;

    //
    // DefaultHandler
    //

    SOAPAttributeContentHandler()
    {
        soapAttributes = new EnumMap<CrowdSoapElements.SOAPAttribute, String>(CrowdSoapElements.SOAPAttribute.class);
    }

    /** Returns the digested 'name-values' children of <code>SOAPAttribute</code>. */
    final Map<CrowdSoapElements.SOAPAttribute, String> getSoapAttributes()
    {
        return soapAttributes;
    }

    //
    // DefaultHandler
    //

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
    {
        if (CrowdSoapElements.SOAP_ATTRIBUTE.equals(localName))
        {
            inSoapAttribute = true;
            return;
        }
        if (inSoapAttribute)
        {
            if (CrowdSoapElements.NAME.equals(localName))
            {
                currentChild = Child.soapAttributeName;
            } else if (CrowdSoapElements.VALUES.equals(localName))
            {
                currentChild = Child.soapAttributeValues;
            }
        }
    }

    @Override
    public final void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (CrowdSoapElements.SOAP_ATTRIBUTE.equals(localName))
        {
            inSoapAttribute = false;
        }
    }

    @Override
    public final void characters(char[] ch, int start, int length) throws SAXException
    {
        if (inSoapAttribute)
        {
            String string = String.valueOf(ch, start, length);
            if (currentChild == Child.soapAttributeName)
            {
                SOAPAttribute name = null;
                try
                {
                    name = CrowdSoapElements.SOAPAttribute.valueOf(string);
                } catch (IllegalArgumentException ex)
                {
                    throw new SAXException("Given '" + string + "' is not an allowed SOAPAttribute name.");
                }
                soapAttributes.put(name, (String) null);
                soapAttribute = name;
            } else if (currentChild == Child.soapAttributeValues)
            {
                soapAttributes.put(soapAttribute, string);
            }
        }
    }
}
