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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;

/**
 * A <i>static</i> class for translating {@link InvalidationPE} into {@link Invalidation}.
 * 
 * @author Christian Ribeaud
 */
public final class InvalidationTranslator
{

    private InvalidationTranslator()
    {
        // Can not be instantiated.
    }

    public final static Invalidation translate(final InvalidationPE invalidation)
    {
        if (invalidation == null)
        {
            return null;
        }
        final Invalidation newInvalidation = new Invalidation();
        newInvalidation.setReason(StringEscapeUtils.escapeHtml(invalidation.getReason()));
        newInvalidation.setRegistrationDate(invalidation.getRegistrationDate());
        newInvalidation.setRegistrator(PersonTranslator.translate(invalidation.getRegistrator()));
        return newInvalidation;
    }
}
