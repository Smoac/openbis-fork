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

import java.util.Collections;
import java.util.Set;

/**
 * A <code>ParserException</code> extension which signalizes unmatched properties.
 * 
 * @author Christian Ribeaud
 */
public final class UnmatchedPropertiesException extends ParserException
{

    static final String MESSAGE_FORMAT = "The following header columns are not part of '%s': %s";

    private static final long serialVersionUID = 1L;

    /** The bean this is set during the parsing process. */
    private final Class<?> beanClass;

    /** The property names found in the parsed file. */
    private final Set<String> allPropertyNames;

    /** The property names found in the bean. */
    private final Set<String> descriptorNames;

    /** The difference of {@link #allPropertyNames} minus {@link #descriptorNames}. */
    private final Set<String> propertyNames;

    UnmatchedPropertiesException(final Class<?> beanClass, final Set<String> allPropertyNames,
            final Set<String> descriptorNames, final Set<String> propertyNames)
    {
        super(createMessage(beanClass, propertyNames));
        assert allPropertyNames != null : "All property names can not be null.";
        assert descriptorNames != null : "Descriptor names can not be null.";
        this.beanClass = beanClass;
        this.allPropertyNames = allPropertyNames;
        this.descriptorNames = descriptorNames;
        this.propertyNames = propertyNames;
    }

    private final static String createMessage(final Class<?> beanClass, final Set<String> propertyNames)
    {
        assert beanClass != null : "Bean class can not be null.";
        assert propertyNames != null : "Property names can not be null.";
        assert propertyNames.size() > 0 : "There is no reason to throw this exception.";
        return String.format(MESSAGE_FORMAT, beanClass.getSimpleName(), format(propertyNames));
    }

    private final static String format(final Set<String> set)
    {
        final StringBuilder builder = new StringBuilder();
        for (String s : set)
        {
            builder.append("'");
            builder.append(s);
            builder.append("', ");
        }
        // Remove trailing ", "
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    public final Class<?> getBeanClass()
    {
        return beanClass;
    }

    public final Set<String> getAllPropertyNames()
    {
        return allPropertyNames;
    }

    public final Set<String> getDescriptorNames()
    {
        return Collections.unmodifiableSet(descriptorNames);
    }

    public final Set<String> getPropertyNames()
    {
        return propertyNames;
    }

}
