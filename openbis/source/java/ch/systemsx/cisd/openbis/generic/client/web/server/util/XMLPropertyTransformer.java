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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class XMLPropertyTransformer
{
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private final Map<String, Transformer> cachedTransformers = new HashMap<String, Transformer>();

    public <T> void transformXMLProperties(List<T> rows)
    {
        for (T row : rows)
        {
            if (row instanceof IEntityPropertiesHolder)
            {
                IEntityPropertiesHolder propertiesHolder = (IEntityPropertiesHolder) row;
                List<IEntityProperty> properties = propertiesHolder.getProperties();
                for (IEntityProperty property : properties)
                {
                    if (property instanceof GenericValueEntityProperty)
                    {
                        GenericValueEntityProperty entityProperty = (GenericValueEntityProperty) property;
                        PropertyType propertyType = entityProperty.getPropertyType();
                        if (propertyType.getDataType().getCode().equals(DataTypeCode.XML))
                        {
                            String transformation = propertyType.getTransformation();
                            if (transformation != null)
                            {
                                String xslt = StringEscapeUtils.unescapeHtml(transformation);
                                String v = StringEscapeUtils.unescapeHtml(entityProperty.getValue());
                                String renderedXMLString = eval(xslt, v);
                                entityProperty.setValue(renderedXMLString);
                                entityProperty.setOriginalValue(v);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private String eval(String xslt, String xmlString)
    {
        Transformer transformer = getTransformer(xslt);
        StringWriter writer = new StringWriter();
        try
        {
            transformer.transform(new StreamSource(new StringReader(xmlString)), new StreamResult(writer));
        } catch (TransformerException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return writer.toString();
    }
    
    private Transformer getTransformer(String xslt)
    {
        try
        {
            Transformer transformer = cachedTransformers.get(xslt);
            if (transformer == null)
            {
                long time = System.currentTimeMillis();
                transformer = TRANSFORMER_FACTORY.newTransformer(new StreamSource(new StringReader(xslt)));
                System.out.println((System.currentTimeMillis()-time)+ " msec create transformer");
                cachedTransformers.put(xslt, transformer);
            }
            return transformer;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }


}
