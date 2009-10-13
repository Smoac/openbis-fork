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

package ch.systemsx.cisd.yeastx.quant;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.utilities.XMLInfraStructure;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationsDTO;

/**
 * Loader of MS Quantifications (*.quantML) XML file.
 * 
 * @author Tomasz Pylak
 */
class QuantMLParser
{
    public static MSQuantificationsDTO parseQuantifications(File dataSet)
    {
        return new QuantMLParser().doParseMSQuantifications(dataSet);
    }

    private final Unmarshaller unmarshaller;

    private QuantMLParser()
    {
        this.unmarshaller = createUnmarshaller();
        unmarshaller.setSchema(XMLInfraStructure.createSchema("/"
                + QuantMLParser.class.getPackage().getName().replace('.', '/')
                + "/quantml-schema.xsd"));
    }

    private static Unmarshaller createUnmarshaller()
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(MSQuantificationsDTO.class);

            return context.createUnmarshaller();
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

    private MSQuantificationsDTO doParseMSQuantifications(File dataSet)
    {
        try
        {
            Object object = unmarshaller.unmarshal(dataSet);
            if (object instanceof MSQuantificationsDTO == false)
            {
                throw new IllegalArgumentException("Wrong type: " + object);
            }
            return (MSQuantificationsDTO) object;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
