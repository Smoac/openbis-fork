/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.io.IOException;
import java.net.URL;

import javax.xml.validation.Schema;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.XMLInfraStructure;

/**
 * Tests of {@link XmlUtils}.
 * <p>
 * NOTE: some tests in this class fail if they are run offline as online namespaces are used
 * 
 * @author Piotr Buczek
 */
public class XmlUtilsTest extends AssertJUnit
{
    private static String EXAMPLE_XML =
            "<?xml version='1.0'?>\n                                            "
                    + "<note xmlns='http://www.w3schools.com'>\n                "
                    + "  <to>Tove</to>\n                                        "
                    + "  <from>Jani</from>\n                                    "
                    + "  <heading>Reminder</heading>\n                          "
                    + "  <body>Don't forget me this weekend!</body>\n           "
                    + "</note>                                                  ";

    public static String EXAMPLE_SCHEMA =
            "<?xml version='1.0'?>\n                                            "
                    + "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'   "
                    + "targetNamespace='http://www.w3schools.com'               "
                    + "xmlns='http://www.w3schools.com'                         "
                    + "elementFormDefault='qualified'>\n                        "
                    + "<xs:element name='note'>\n                               "
                    + "  <xs:complexType>\n                                     "
                    + "    <xs:sequence>\n                                      "
                    + "      <xs:element name='to' type='xs:string'/>\n         "
                    + "      <xs:element name='from' type='xs:string'/>\n       "
                    + "      <xs:element name='heading' type='xs:string'/>\n    "
                    + "      <xs:element name='body' type='xs:string'/>\n       "
                    + "    </xs:sequence>\n                                     "
                    + "  </xs:complexType>\n                                    "
                    + "</xs:element>\n                                          "
                    + "</xs:schema>                                             ";

    public static String EXAMPLE_XSLT =
            "<?xml version='1.0'?>\n"
                    + "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
                    + "<xsl:output method='html'/>\n                            "
                    + "<xsl:template match='child::person'>\n                   "
                    + " <html>\n                                                "
                    + "  <head>\n                                               "
                    + "   <title>\n                                             "
                    + "    <xsl:value-of select='descendant::firstname' />\n    "
                    + "    <xsl:text> </xsl:text>\n                             "
                    + "    <xsl:value-of select='descendant::lastname' />\n     "
                    + "   </title>\n                                            "
                    + "  </head>\n                                              "
                    + "  <body>\n                                               "
                    + "   <xsl:value-of select='descendant::firstname' />\n     "
                    + "   <xsl:text> </xsl:text>\n                              "
                    + "   <xsl:value-of select='descendant::lastname' />\n      "
                    + "  </body>\n                                              "
                    + " </html>\n                                               "
                    + "</xsl:template>\n                                        "
                    + "</xsl:stylesheet>";

    @Test
    public void testParseXmlDocument()
    {
        XmlUtils.parseXmlDocument("<xml>simple<b>XML</b> file</xml>");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testParseXmlDocumentFails()
    {
        XmlUtils.parseXmlDocument("<xml>no end tag");
    }

    @Test
    public void testParseAndValidateXmlDocumentWithGivenSchema() throws SAXException, IOException
    {
        // this test doesn't work offline! online namespaces are needed
        Document document = XmlUtils.parseXmlDocument(EXAMPLE_XML);
        XmlUtils.validate(document, EXAMPLE_SCHEMA);
    }

    // FIXME 2010-13-09, Piotr Buczek: fix on hudson
    @Test(groups = "broken")
    public void testParseAndValidateXmlSchemaDocument() throws SAXException, IOException
    {
        // this test doesn't work offline! online namespaces are needed
        Document document = XmlUtils.parseXmlDocument(EXAMPLE_SCHEMA);
        // get schema from a URL resource
        XmlUtils.validate(document, new URL(XmlUtils.XML_SCHEMA_XSD_URL));
        // get schema from a file resource
        Schema schema = XMLInfraStructure.createSchema(XmlUtils.XML_SCHEMA_XSD_FILE_RESOURCE);
        XmlUtils.validate(document, schema);
    }

    @Test
    public void testParseAndValidateXsltXmlDocument() throws SAXException, IOException
    {
        // this test doesn't work offline! online namespaces are needed
        Document document = XmlUtils.parseXmlDocument(EXAMPLE_XSLT);
        // get schema from a URL resource
        XmlUtils.validate(document, new URL(XmlUtils.XSLT_XSD_URL));
        // get schema from a file resource
        Schema schema = XMLInfraStructure.createSchema(XmlUtils.XSLT_XSD_FILE_RESOURCE);
        XmlUtils.validate(document, schema);
    }
}
