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

    /** The property codes found in the parsed file. */
    private final Set<String> allPropertyCodes;

    /** The mandatory property codes found in the bean. */
    private final Set<String> mandatoryCodes;

    /** The optional property codes found in the bean. */
    private final Set<String> optionalCodes;

    /**
     * The property codes of {@link #allPropertyCodes} that can neither be found in
     * {@link #mandatoryCodes} nor in {@link #optionalCodes}.
     */
    private final Set<String> propertyCodes;

    public UnmatchedPropertiesException(final Class<?> beanClass,
            final Set<String> allPropertyCodes, final Set<String> mandatoryCodes,
            final Set<String> optionalCodes, final Set<String> propertyCodes)
    {
        super(createMessage(propertyCodes, mandatoryCodes, optionalCodes));
        assert allPropertyCodes != null : "All property codes can not be null.";
        assert mandatoryCodes != null : "Mandatory codes can not be null.";
        assert optionalCodes != null : "Optional codes can not be null.";
        this.beanClass = beanClass;
        this.allPropertyCodes = allPropertyCodes;
        this.mandatoryCodes = mandatoryCodes;
        this.optionalCodes = optionalCodes;
        this.propertyCodes = propertyCodes;
    }

    private final static String createMessage(Set<String> propertyCodes,
            Set<String> mandatoryNames, Set<String> optionalNames)
    {
        assert propertyCodes != null : "Property codes can not be null.";
        assert propertyCodes.size() > 0 : "There is no reason to throw this exception.";
        final StringBuilder builder = new StringBuilder();
        builder.append("Columns ").append(toString(propertyCodes)).append(
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

    public final Set<String> getAllPropertyCodes()
    {
        return Collections.unmodifiableSet(allPropertyCodes);
    }

    public final Set<String> getPropertyCodes()
    {
        return Collections.unmodifiableSet(propertyCodes);
    }

    public final Set<String> getMandatoryNames()
    {
        return Collections.unmodifiableSet(mandatoryCodes);
    }

    public final Set<String> getOptionalNames()
    {
        return Collections.unmodifiableSet(optionalCodes);
    }

}
