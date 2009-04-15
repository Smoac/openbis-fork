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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyModel;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;

/**
 * A {@link AbstractParserObjectFactory} extension for creating {@link NewSample}.
 * 
 * @author Christian Ribeaud
 */
final class NewSampleParserObjectFactory extends AbstractParserObjectFactory<NewSample>
{
    private final SampleType sampleType;

    private final boolean identifierExpectedInFile;

    NewSampleParserObjectFactory(final SampleType sampleType, final IPropertyMapper propertyMapper,
            boolean identifierExpectedInFile)
    {
        super(NewSample.class, propertyMapper);
        this.sampleType = sampleType;
        this.identifierExpectedInFile = identifierExpectedInFile;
    }

    private final SampleTypePropertyType createSampleTypePropertyType(final String propertyTypeCode)
    {
        final SampleTypePropertyType sampleTypePropertyType = new SampleTypePropertyType();
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        sampleTypePropertyType.setPropertyType(propertyType);
        sampleTypePropertyType.setEntityType(sampleType);
        return sampleTypePropertyType;
    }

    private final void setProperties(final NewSample newSample, final String[] lineTokens)
    {
        final List<SampleProperty> properties = new ArrayList<SampleProperty>();
        for (final String unmatchedProperty : getUnmatchedProperties())
        {
            final IPropertyModel propertyModel = tryGetPropertyModel(unmatchedProperty);
            final String propertyValue = getPropertyValue(lineTokens, propertyModel);
            if (StringUtils.isEmpty(propertyValue) == false)
            {
                final SampleProperty property = new SampleProperty();
                property.setEntityTypePropertyType(createSampleTypePropertyType(unmatchedProperty));
                property.setValue(propertyValue);
                properties.add(property);
            }
        }
        newSample.setProperties(properties.toArray(SampleProperty.EMPTY_ARRAY));
    }

    //
    // AbstractParserObjectFactory
    //

    @Override
    protected final boolean ignoreUnmatchedProperties()
    {
        return true;
    }

    @Override
    public final NewSample createObject(final String[] lineTokens) throws ParserException
    {
        final NewSample newSample = super.createObject(lineTokens);
        if (identifierExpectedInFile && newSample.getIdentifier() == null)
        {
            throw new ParserException("Mandatory column '" + NewSample.IDENTIFIER_COLUMN
                    + "' is missing.");
        }
        if (identifierExpectedInFile == false && newSample.getIdentifier() != null)
        {
            throw new ParserException("Requested automatical generation of codes. Column '"
                    + NewSample.IDENTIFIER_COLUMN + "' should be removed from the file.");
        }
        newSample.setSampleType(sampleType);
        setProperties(newSample, lineTokens);
        newSample
                .setContainerIdentifier(StringUtils.trimToNull(newSample.getContainerIdentifier()));
        newSample.setParentIdentifier(StringUtils.trimToNull(newSample.getParentIdentifier()));
        return newSample;
    }
}
