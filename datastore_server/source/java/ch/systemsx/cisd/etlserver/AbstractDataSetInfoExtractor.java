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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.TabFileLoader;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * An abstract <code>ICodeExtractor</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractDataSetInfoExtractor implements IDataSetInfoExtractor
{

    /** The name of the property to get the experiment separator from. */
    @Private
    static final String ENTITY_SEPARATOR_PROPERTY_NAME = "entity-separator";

    /** The default entity separator. */
    protected static final char DEFAULT_ENTITY_SEPARATOR = '.';

    @Private
    static final String STRIP_EXTENSION = "strip-file-extension";

    @Private
    static final String GROUP_CODE = "group-code";

    protected final Properties properties;

    /** Separator character that divides entities in a data set name. */
    protected final char entitySeparator;

    protected final boolean stripExtension;

    protected AbstractDataSetInfoExtractor(final Properties globalProperties)
    {
        assert globalProperties != null : "Global properties can not be null.";
        properties = ExtendedProperties.getSubset(globalProperties, EXTRACTOR_KEY + '.', true);
        stripExtension = PropertyUtils.getBoolean(properties, STRIP_EXTENSION, false);
        entitySeparator =
                PropertyUtils.getChar(properties, ENTITY_SEPARATOR_PROPERTY_NAME,
                        DEFAULT_ENTITY_SEPARATOR);
        if (Character.isWhitespace(entitySeparator))
        {
            throw new ConfigurationFailureException("Entity separator is a whitespace character.");
        }
    }

    protected String getGroupCode()
    {
        return properties.getProperty(GROUP_CODE);
    }

    /**
     * Extracts the data set properties from a tsv file in <var>incomingDataSetPath</var> with name
     * <var>fileNameOrNull</var>.
     */
    public static List<NewProperty> extractDataSetProperties(File incomingDataSetPath,
            String fileNameOrNull)
    {
        List<NewProperty> result = new ArrayList<NewProperty>();
        if (fileNameOrNull != null && incomingDataSetPath.isDirectory())
        {
            final File propertiesFile = new File(incomingDataSetPath, fileNameOrNull);
            if (propertiesFile.exists() == false)
            {
                return result;
            }
            if (propertiesFile.isFile())
            {
                TabFileLoader<NewProperty> tabFileLoader =
                        new TabFileLoader<NewProperty>(
                                new IParserObjectFactoryFactory<NewProperty>()
                                    {

                                        public IParserObjectFactory<NewProperty> createFactory(
                                                IPropertyMapper propertyMapper)
                                                throws ParserException
                                        {
                                            return new AbstractParserObjectFactory<NewProperty>(
                                                    NewProperty.class, propertyMapper)
                                                {
                                                };
                                        }
                                    });
                result.addAll(tabFileLoader.load(propertiesFile));

            } else
            {
                throw new UserFailureException("Data set properties file '" + propertiesFile
                        + "' does not exist or is not a 'normal' file.");
            }
        }
        return result;

    }

}
