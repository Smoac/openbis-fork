/*
 * Copyright 2010 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Utility class for the format of URLs that imply searches.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchlinkUtilities
{
    /** The HTTP URL parameter used to specify the entity identifier. */
    public static final String CODE_PARAMETER_KEY = "code";

    public final static String createSearchlinkURL(final String baseIndexURL,
            final EntityKind entityKind, final String searchString)
    {
        URLMethodWithParameters ulrWithParameters = new URLMethodWithParameters(baseIndexURL);
        ulrWithParameters.addParameter(ViewLocator.ACTION_PARAMETER, ViewLocator.SEARCH_ACTION);
        ulrWithParameters.addParameter(ViewLocator.ENTITY_PARAMETER, entityKind.name());
        ulrWithParameters.addParameter(CODE_PARAMETER_KEY, searchString);
        return ulrWithParameters.toString();
    }
}
