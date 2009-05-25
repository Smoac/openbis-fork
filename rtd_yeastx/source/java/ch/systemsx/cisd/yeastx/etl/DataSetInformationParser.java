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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyModel;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.TabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * A {@link AbstractParserObjectFactory} extension for creating {@link NewProperty}.
 * 
 * @author Tomasz Pylak
 */
final class DataSetInformationParser
{
    public static List<PlainDataSetInformation> parse(File indexFile)
    {
        TabFileLoader<PlainDataSetInformation> tabFileLoader =
                new TabFileLoader<PlainDataSetInformation>(
                        new IParserObjectFactoryFactory<PlainDataSetInformation>()
                            {

                                public IParserObjectFactory<PlainDataSetInformation> createFactory(
                                        IPropertyMapper propertyMapper) throws ParserException
                                {
                                    return new NewPropertyParserObjectFactory(propertyMapper);
                                }
                            });
        // TODO 2009-05-25, Tomasz Pylak: consider handling exception similar to BisTabFileLoader
        return tabFileLoader.load(indexFile);
    }

    private static final class NewPropertyParserObjectFactory extends
            AbstractParserObjectFactory<PlainDataSetInformation>
    {

        private NewPropertyParserObjectFactory(final IPropertyMapper propertyMapper)
        {
            super(PlainDataSetInformation.class, propertyMapper);
        }

        private final void setProperties(final PlainDataSetInformation dataset,
                final String[] lineTokens)
        {
            final List<NewProperty> properties = new ArrayList<NewProperty>();
            for (final String unmatchedProperty : getUnmatchedProperties())
            {
                final IPropertyModel propertyModel = tryGetPropertyModel(unmatchedProperty);
                final String propertyValue = getPropertyValue(lineTokens, propertyModel);
                if (StringUtils.isEmpty(propertyValue) == false)
                {
                    final NewProperty property = new NewProperty();
                    property.setPropertyCode(propertyModel.getCode());
                    property.setValue(propertyValue);
                    properties.add(property);
                }
            }
            dataset.setProperties(properties);
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
        public final PlainDataSetInformation createObject(final String[] lineTokens)
                throws ParserException
        {
            final PlainDataSetInformation dataset = super.createObject(lineTokens);
            setProperties(dataset, lineTokens);
            return dataset;
        }
    }
}