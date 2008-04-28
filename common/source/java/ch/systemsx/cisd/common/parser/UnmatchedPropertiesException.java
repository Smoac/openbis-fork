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

import ch.systemsx.cisd.common.collections.CollectionStyle;
import ch.systemsx.cisd.common.collections.CollectionUtils;

/**
 * A <code>ParserException</code> extension which signalizes unmatched properties.
 * 
 * @author Christian Ribeaud
 */
public final class UnmatchedPropertiesException extends ParserException
{
    private static final long serialVersionUID = 1L;

    /** The bean this is set during the parsing process. */
    private final Class<?> beanClass;

    /** The property names found in the parsed file. */
    private final Set<String> allPropertyNames;

    /** The mandatory property names found in the bean. */
    private final Set<String> mandatoryNames;

    /** The optional property names found in the bean. */
    private final Set<String> optionalNames;

    /**
     * The property names of {@link #allPropertyNames} that can neither be found in
     * {@link #mandatoryNames} nor in {@link #optionalNames}.
     */
    private final Set<String> propertyNames;

    public UnmatchedPropertiesException(final Class<?> beanClass,
            final Set<String> allPropertyNames, final Set<String> mandatoryNames,
            final Set<String> optionalNames, final Set<String> propertyNames)
    {
        super(createMessage(propertyNames, mandatoryNames, optionalNames));
        assert allPropertyNames != null : "All property names can not be null.";
        assert mandatoryNames != null : "Mandatory names can not be null.";
        assert optionalNames != null : "Optional names can not be null.";
        this.beanClass = beanClass;
        this.allPropertyNames = allPropertyNames;
        this.mandatoryNames = mandatoryNames;
        this.optionalNames = optionalNames;
        this.propertyNames = propertyNames;
    }

    private final static String createMessage(Set<String> propertyNames,
            Set<String> mandatoryNames, Set<String> optionalNames)
    {
        assert propertyNames != null : "Property names can not be null.";
        assert propertyNames.size() > 0 : "There is no reason to throw this exception.";
        final StringBuilder builder = new StringBuilder();
        builder.append("Columns ").append(toString(propertyNames)).append(
                " specified in the header are not expected (");
        final boolean hasMandatory = mandatoryNames.size() > 0;
        if (hasMandatory)
        {
            builder.append("mandatory colums are ");
            builder.append(toString(mandatoryNames));
        }
        if (optionalNames.size() > 0)
        {
            if (hasMandatory)
            {
                builder.append(", ");
            }
            builder.append("optional colums are ");
            builder.append(toString(optionalNames));
        }
        builder.append(")");
        return builder.toString();

    }

    private final static String toString(final Set<String> set)
    {
        return CollectionUtils.abbreviate(set, -1, CollectionStyle.SINGLE_QUOTE_BOUNDARY);
    }

    public final Class<?> getBeanClass()
    {
        return beanClass;
    }

    public final Set<String> getAllPropertyNames()
    {
        return Collections.unmodifiableSet(allPropertyNames);
    }

    public final Set<String> getPropertyNames()
    {
        return Collections.unmodifiableSet(propertyNames);
    }

    public final Set<String> getMandatoryNames()
    {
        return Collections.unmodifiableSet(mandatoryNames);
    }

    public final Set<String> getOptionalNames()
    {
        return Collections.unmodifiableSet(optionalNames);
    }

}
