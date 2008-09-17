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

package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import java.io.Serializable;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.validator.Validator;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Check a specified location.
 * 
 * @author Christian Ribeaud
 */
public final class LocationValidator implements Validator<Location>, Serializable
{

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private boolean relative;

    //
    // Validator
    //

    public final void initialize(final Location location)
    {
        relative = location.relative();
    }

    public final boolean isValid(final Object value)
    {
        final String location = (String) value;
        final int prefixLength = FilenameUtils.getPrefixLength(location);
        if (relative)
        {
            return prefixLength == 0;
        } else
        {
            return prefixLength > 0;
        }
    }

}
