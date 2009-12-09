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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Utility class to parse properties.
 * <p>
 * Includes utilities to parse properties section. It's assumed that all properties in one section
 * begins with the common prefix.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class PropertyParametersUtil
{
    @Private
    static final String DSS_CODE_KEY = "data-store-server-code";

    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    public static final String ITEMS_DELIMITER = ",";

    /** loads server configuration */
    public static ExtendedProperties loadServiceProperties()
    {
        return loadProperties(SERVICE_PROPERTIES_FILE);
    }

    public static ExtendedProperties loadProperties(String filePath)
    {
        Properties properties = PropertyUtils.loadProperties(filePath);
        return ExtendedProperties.createWith(properties);
    }

    public static String getDataStoreCode(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, DSS_CODE_KEY);
    }

    // a section is a set of properties which start with the common prefix
    public static class SectionProperties
    {
        // the key which was the prefix of all the properties in one section
        private final String key;

        private final Properties sectionProperties;

        public SectionProperties(String key, Properties sectionProperties)
        {
            this.key = key;
            this.sectionProperties = sectionProperties;
        }

        public String getKey()
        {
            return key;
        }

        public Properties getProperties()
        {
            return sectionProperties;
        }
    }

    /**
     * Extracts properties of the specified sections.
     * 
     * @param properties list of all properties
     * @param sectionNamestPropertyName property name, its value should contain a list of section
     *            names
     * @param attachGeneralProperties if true, each section will have also global properties
     *            attached (those which do not belong to any section)
     */
    public static SectionProperties[] extractSectionProperties(final Properties properties,
            String sectionNamestPropertyName, boolean attachGeneralProperties)
    {
        final String[] names = tryParseItemisedProperty(properties, sectionNamestPropertyName);
        if (names == null)
        {
            return new SectionProperties[0];
        }
        SectionProperties[] sectionProperties = extractSectionProperties(names, properties);
        if (attachGeneralProperties)
        {
            attachGeneralProperties(properties, names, sectionProperties);
        }
        return sectionProperties;
    }

    /**
     * takes a value of a specified property and treats it as a list of tokens separated by a
     * delimiter.
     */
    private static String[] tryParseItemisedProperty(final Properties properties,
            String itemsListPropertyName)
    {
        final String namesText = properties.getProperty(itemsListPropertyName);
        if (namesText == null)
        {
            return null;
        }
        return parseItemisedProperty(namesText, itemsListPropertyName);
    }

    /**
     * Parses a list of items (e.g. "item1, item2, item3"). Checks that they are unique and
     * non-empty.
     */
    public static String[] parseItemisedProperty(String itemsList, String itemsListPropertyName)
    {
        String[] names = itemsList.split(ITEMS_DELIMITER);
        names = trim(names);
        validateUniqueNonemptyNames(names, itemsListPropertyName);
        return names;
    }

    private static String[] trim(String[] names)
    {
        String[] trimmedNames = new String[names.length];
        for (int i = 0; i < trimmedNames.length; i++)
        {
            trimmedNames[i] = names[i].trim();
        }
        return trimmedNames;
    }

    private static void attachGeneralProperties(final Properties properties, final String[] names,
            SectionProperties[] sectionProperties)
    {
        final Properties generalProperties = extractGeneralProperties(names, properties);
        for (SectionProperties section : sectionProperties)
        {
            section.getProperties().putAll(generalProperties);
        }
    }

    private static SectionProperties[] extractSectionProperties(final String[] sectionNames,
            final Properties properties)
    {
        final SectionProperties[] sections = new SectionProperties[sectionNames.length];
        for (int i = 0; i < sectionNames.length; i++)
        {
            final String name = sectionNames[i].trim();
            // extract thread specific properties, remove prefix
            final ExtendedProperties sectionProperties =
                    ExtendedProperties.getSubset(properties, getPropertyPrefix(name), true);
            sections[i] = new SectionProperties(name, sectionProperties);
        }
        return sections;
    }

    // extracts properties that do not begin with the given names
    private static ExtendedProperties extractGeneralProperties(final String[] names,
            final Properties properties)
    {
        final ExtendedProperties generalProperties = ExtendedProperties.createWith(properties);
        for (final String name : names)
        {
            generalProperties.removeSubset(getPropertyPrefix(name));
        }
        return generalProperties;
    }

    private static String getPropertyPrefix(final String name)
    {
        return name + ".";
    }

    private static void validateUniqueNonemptyNames(String[] names, String propertyName)
    {
        final Set<String> processed = new HashSet<String>();
        for (final String name : names)
        {
            if (processed.contains(name))
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Duplicated name '%s' in '%s' property.", name, propertyName);
            }
            if (name.length() == 0)
            {
                throw ConfigurationFailureException.fromTemplate("Empty name in '%s' property.",
                        propertyName);
            }
            processed.add(name);
        }
    }

}
