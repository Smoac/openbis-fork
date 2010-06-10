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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyModel;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * A {@link AbstractParserObjectFactory} extension for creating {@link NewDataSet}.
 * 
 * @author Izabela Adamczyk
 */
public final class NewDataSetParserObjectFactory extends AbstractParserObjectFactory<NewDataSet>
{
    public NewDataSetParserObjectFactory(final IPropertyMapper propertyMapper)
    {
        super(NewDataSet.class, propertyMapper);
    }

    private final PropertyType createPropertyType(final String propertyTypeCode)
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        return propertyType;
    }

    private final void setProperties(final NewDataSet newObject, final String[] lineTokens)
    {
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        for (final String unmatchedProperty : getUnmatchedProperties())
        {
            final IPropertyModel propertyModel = tryGetPropertyModel(unmatchedProperty);
            final String propertyValue = getPropertyValue(lineTokens, propertyModel);
            if (StringUtils.isEmpty(propertyValue) == false)
            {
                final IEntityProperty property = new EntityProperty();
                property.setPropertyType(createPropertyType(unmatchedProperty));
                property.setValue(propertyValue);
                properties.add(property);
            }
        }
        newObject.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));
        newObject.setPropertiesToUpdate(getUnmatchedProperties());
    }

    @Override
    protected final boolean ignoreUnmatchedProperties()
    {
        return true;
    }

    @Override
    public final NewDataSet createObject(final String[] lineTokens) throws ParserException
    {
        final NewDataSet newObject = super.createObject(lineTokens);
        setProperties(newObject, lineTokens);
        return newObject;
    }
}
