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
 * A <code>ParserException</code> extension which signalizes missing of a mandatory property.
 * 
 * @author Christian Ribeaud
 */
public final class MandatoryPropertyMissingException extends ParserException
{

    static final String MESSAGE_FORMAT = "Field/Property name(s) '%s' is(are) mandatory.";

    private static final long serialVersionUID = 1L;

    /** The mandatory property names that could not be found in the parsed file. */
    private final Set<String> missingMandatoryProperties;

    /** The fields that are mandatory. */
    private final Set<String> mandatoryFields;

    public MandatoryPropertyMissingException(final Set<String> mandatoryFields,
            final Set<String> missingMandatoryProperties)
    {
        super(createMessage(missingMandatoryProperties));
        assert mandatoryFields != null && mandatoryFields.size() > 0 : "Unspecified mandatory fields.";
        this.mandatoryFields = mandatoryFields;
        this.missingMandatoryProperties = missingMandatoryProperties;
    }

    private final static String createMessage(final Set<String> missingMandatoryProperties)
    {
        assert missingMandatoryProperties != null : "Missing mandatory properties can not be null.";
        assert missingMandatoryProperties.size() > 0 : "No reason to throw this exception.";
        return String.format(MESSAGE_FORMAT, toString(missingMandatoryProperties));
    }

    final static String toString(final Set<String> set)
    {
        return CollectionUtils.abbreviate(set, -1, CollectionStyle.NO_BOUNDARY);
    }

    public final Set<String> getMissingMandatoryProperties()
    {
        return Collections.unmodifiableSet(missingMandatoryProperties);
    }

    public final Set<String> getMandatoryFields()
    {
        return Collections.unmodifiableSet(mandatoryFields);
    }
}
