/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.XMLInfraStructure;

/**
 * Utility methods for parsing and validating XML files.
 * 
 * @author Piotr Buczek
 */
public class XmlUtils
{
    public static String XML_SCHEMA_XSD_URL = "http://www.w3.org/2001/XMLSchema.xsd";

    public static String XSLT_XSD_URL = "http://www.w3.org/2007/schema-for-xslt20.xsd";

    public static String XML_SCHEMA_XSD_FILE_RESOURCE = "/XMLSchema.xsd";

    public static String XSLT_XSD_FILE_RESOURCE = "/schema-for-xslt20.xsd";

    /**
     * Parse given string as XML {@link Document}.
     * 
     * @throws UserFailureException if provided value is not a well-formed XML document
     */
    public static Document parseXmlDocument(String value)
    {
        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        dBF.setNamespaceAware(true);
        InputSource is = new InputSource(new StringReader(value));
        try
        {
            return dBF.newDocumentBuilder().parse(is);
        } catch (Exception e)
        {
            throw UserFailureException.fromTemplate(
                    "Provided value:\n\n%s\n\nisn't a well formed XML document. %s", value, e
                            .getMessage());
        }
    }

    /** validate given document against the specified schema */
    public static void validate(Document document, String xmlSchema) throws SAXException,
            IOException
    {
        validate(document, XMLInfraStructure.createSchema(new StreamSource(new StringReader(
                xmlSchema))));
    }

    /** validate given document against the schema specified by URL */
    public static void validate(Document document, URL schemaURL) throws SAXException, IOException
    {
        validate(document, XMLInfraStructure.createSchema(schemaURL));
    }

    /** validate given document against the schema specified in File */
    public static void validate(Document document, File schemaFile) throws SAXException,
            IOException
    {
        validate(document, XMLInfraStructure.createSchema(schemaFile));
    }

    /** validate given document against the specified schema */
    public static void validate(Document document, Schema schema) throws SAXException, IOException
    {
        // create a Validator instance, which can be used to validate an instance document
        Validator validator = schema.newValidator();
        // validate the DOM tree
        validator.validate(new DOMSource(document));
    }

}
