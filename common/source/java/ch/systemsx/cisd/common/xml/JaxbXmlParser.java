/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.utilities.XMLInfraStructure;

/**
 * Loads an XML file to the bean of the specified class. The bean should be annotated with JAXB
 * annotations.
 * 
 * @author Tomasz Pylak
 */
public class JaxbXmlParser<T>
{
    // Set to true to turn on debbuging help
    private static final boolean DEBUG = false;

    // Set to true have the parser explicitly use the LVData class
    private static final boolean EXPLICITLY_SET_DESTINATION = false;

    /**
     * @param validate if true the parsed xml will be validated with the XML Schema. It is expected
     *            that the "schema.xsd" file with XML Schema can be found in the same folder as the
     *            specified bean class.
     */
    // FIXME 2010-07-12, Piotr Buczek: creating unmarshaller for every file is very inefficient!!!
    public static <T> T parse(Class<T> beanClass, File dataSet, boolean validate)
    {
        JaxbXmlParser<T> parser = new JaxbXmlParser<T>(beanClass, validate);
        if (EXPLICITLY_SET_DESTINATION)
        {
            return parser.doParse(dataSet);
        } else
        {
            return parser.doParseSpecifyingDestinationClass(dataSet);
        }
    }

    private final Unmarshaller unmarshaller;

    private final Class<T> beanClass;

    private JaxbXmlParser(Class<T> beanClass, boolean validate)
    {
        this.beanClass = beanClass;
        this.unmarshaller = createUnmarshaller(beanClass);
        if (DEBUG)
            useDebugValidationEventHandler();

        if (validate)
        {
            unmarshaller.setSchema(XMLInfraStructure.createSchema("/"
                    + beanClass.getPackage().getName().replace('.', '/') + "/schema.xsd"));
        }
    }

    private void useDebugValidationEventHandler()
    {
        try
        {
            unmarshaller.setEventHandler(new DefaultValidationEventHandler());
        } catch (JAXBException ex)
        {
            ex.printStackTrace();
        }
    }

    private static <T> Unmarshaller createUnmarshaller(Class<T> beanClass)
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(beanClass);
            return context.createUnmarshaller();
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public static <T> void writeSchema(Class<T> beanClass)
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(beanClass);
            debugGenerateXmlSchema(context);
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public static void debugGenerateXmlSchema(JAXBContext context)
    {
        try
        {
            context.generateSchema(new SchemaOutputResolver()
                {
                    @Override
                    public Result createOutput(String namespaceUri, String schemaName)
                            throws IOException
                    {
                        return new StreamResult(new File(".", schemaName));
                    }
                });
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private T doParse(File file)
    {
        try
        {
            Object object = unmarshaller.unmarshal(file);
            if (beanClass.isAssignableFrom(object.getClass()) == false)
            {
                throw new IllegalArgumentException("Wrong type: " + object);
            }
            return beanClass.cast(object);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private T doParseSpecifyingDestinationClass(File file)
    {
        try
        {
            JAXBElement<T> object = unmarshaller.unmarshal(new StreamSource(file), beanClass);
            return object.getValue();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
