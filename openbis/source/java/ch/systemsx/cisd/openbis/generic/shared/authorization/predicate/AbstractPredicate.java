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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <i>abstract</i> <code>IPredicate</code> implementation which mainly checks method
 * parameters before doing the real work.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractPredicate<T> implements IPredicate<T>
{
    /**
     * Returns a simple description for the exception to be thrown.
     */
    abstract String getCandidateDescription();

    /**
     * Does the validation after parameters have been checked.
     * <p>
     * Must be implemented by subclasses.
     * </p>
     */
    abstract Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T value);

    //
    // IPredicate
    //

    public final Status evaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T valueOrNull)
    {
        assert person != null : "Unspecified person";
        assert allowedRoles != null : "Unspecified allowed roles";
        if (valueOrNull == null)
        {
            throw UserFailureException.fromTemplate("No %s specified.", getCandidateDescription());
        }
        return doEvaluation(person, allowedRoles, valueOrNull);
    }
}
