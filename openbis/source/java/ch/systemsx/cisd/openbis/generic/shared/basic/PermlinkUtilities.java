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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Utility class to be used both on client and server side for permlink management.
 * 
 * @author Piotr Buczek
 */
public class PermlinkUtilities
{

    /** The HTTP URL parameter used to specify the entity identifier. */
    public static final String PERM_ID_PARAMETER_KEY = "permId";

    /** The HTTP URL parameter used to specify the entity kind. */
    public static final String ENTITY_KIND_PARAMETER_KEY = "entity";

    public final static String createPermlinkURL(final String baseIndexURL,
            final EntityKind entityKind, final String identifier)
    {
        URLMethodWithParameters ulrWithParameters = new URLMethodWithParameters(baseIndexURL);
        ulrWithParameters.addParameter(ENTITY_KIND_PARAMETER_KEY, entityKind.name());
        ulrWithParameters.addParameter(PERM_ID_PARAMETER_KEY, identifier);
        return ulrWithParameters.toString();
    }

}
