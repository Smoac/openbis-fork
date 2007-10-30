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

package ch.systemsx.cisd.common.parser;

import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Creates new object by setting its properties.
 * 
 * @author Tomasz Pylak on Oct 26, 2007
 */
public class PropertiesParserObjectFactory<E> implements IParserObjectFactory<E>
{
    /** The <code>IPropertyMapper</code> implementation. */
    private final IPropertyMapper propertyMapper;

    private final IPropertyObjectFactory<E> objectFactory;

    public PropertiesParserObjectFactory(IPropertyMapper propertyMapper, IPropertyObjectFactory<E> objectFactory)
    {
        this.propertyMapper = propertyMapper;
        this.objectFactory = objectFactory;
    }

    public E createObject(String[] lineTokens) throws UserFailureException
    {
        IPropertiesSetter<E> setter = objectFactory.createObjectSetter();
        Set<String> propertyNames = propertyMapper.getAllPropertyNames();
        for (String name : propertyNames)
        {
            final IPropertyModel propertyModel = propertyMapper.getProperty(name);
            String propertyValue = getPropertyValue(lineTokens, propertyModel);
            setter.setProperty(name, propertyValue);
        }
        return setter.done();
    }

    private String getPropertyValue(final String[] lineTokens, final IPropertyModel propertyModel)
    {
        int column = propertyModel.getColumn();
        if (column >= lineTokens.length)
        {
            throw UserFailureException.fromTemplate("Not enough tokens are available (index: %d, available: %d)",
                    column, lineTokens.length);
        }
        return lineTokens[column];
    }

}
