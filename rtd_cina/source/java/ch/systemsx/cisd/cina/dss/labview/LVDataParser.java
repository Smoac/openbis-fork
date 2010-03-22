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

package ch.systemsx.cisd.cina.dss.labview;

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
 * A parser for the XML format used by LabVIEW.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class LVDataParser
{

    // Set to true to turn on debbuging help
    private static final boolean DEBUG = false;

    // Set to true have the parser explicitly use the LVData class
    private static final boolean EXPLICITLY_SET_DESTINATION = false;

    /**
     * Parse the file and return its representation as an LVData object
     */
    public static LVData parse(File dataSet)
    {
        LVDataParser newMe = new LVDataParser(false);
        LVData answer;
        if (EXPLICITLY_SET_DESTINATION)
        {
            answer = newMe.doParseSpecifyingDestinationClass(dataSet);
        } else
        {
            answer = newMe.doParse(dataSet);
        }
        
        return answer;
    }

    public static void writeSchema()
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(LVData.class);
            debugGenerateXmlSchema(context);
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private final Unmarshaller unmarshaller;

    private LVDataParser(boolean shouldValidate)
    {
        this.unmarshaller = createUnmarshaller(LVData.class);
        if (DEBUG)
            useDebugValidationEventHandler();

        if (shouldValidate)
        {
            unmarshaller.setSchema(XMLInfraStructure.createSchema("/"
                    + LVData.class.getPackage().getName().replace('.', '/') + "/schema.xsd"));
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

    private static void debugGenerateXmlSchema(JAXBContext context)
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

    private LVData doParse(File file)
    {
        try
        {
            Object object = unmarshaller.unmarshal(file);
            if (LVData.class.isAssignableFrom(object.getClass()) == false)
            {
                throw new IllegalArgumentException("Wrong type: " + object);
            }
            return LVData.class.cast(object);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private LVData doParseSpecifyingDestinationClass(File file)
    {
        try
        {
            JAXBElement<LVData> object =
                    unmarshaller.unmarshal(new StreamSource(file), LVData.class);
            return object.getValue();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
}
