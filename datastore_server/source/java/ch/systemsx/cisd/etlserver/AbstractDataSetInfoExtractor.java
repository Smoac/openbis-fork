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

import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * An abstract <code>ICodeExtractor</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractDataSetInfoExtractor implements IDataSetInfoExtractor
{

    /** The name of the property to get the experiment separator from. */
    @Private static final String ENTITY_SEPARATOR_PROPERTY_NAME = "entity-separator";

    /** The default entity separator. */
    protected static final char DEFAULT_ENTITY_SEPARATOR = '.';

    @Private static final String STRIP_EXTENSION = "strip-file-extension";
    
    @Private static final String GROUP_CODE = "group-code";

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
    }
    
    protected String getGroupCode()
    {
        return properties.getProperty(GROUP_CODE);
    }

}
