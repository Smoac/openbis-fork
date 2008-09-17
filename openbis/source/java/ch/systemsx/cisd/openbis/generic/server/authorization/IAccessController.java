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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.lang.reflect.Method;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;

/**
 * {@link IAuthSession} based <i>Access Controller</i> for manager methods.
 * 
 * @author Christian Ribeaud
 */
public interface IAccessController
{
    /**
     * Whether given <code>Session</code> has enough rights to access the calling <var>method</var>.
     * 
     * @param arguments the method arguments (minus the first one which is expected to be a
     *            {@link IAuthSession} object).
     * @throws UserFailureException if the authorization could not be checked for some reason.
     * @return a {@link Status} with {@link StatusFlag#OK} if given <var>session</var> is
     *         authorized to access the given <code>Method</code>. In case of
     *         {@link StatusFlag#ERROR} you might find more information by calling
     *         {@link Status#tryGetErrorMessage()}.
     */
    public Status isAuthorized(final IAuthSession session, final Method method,
            final Argument<?>[] arguments) throws UserFailureException;

    //
    // Helper class
    //

    /**
     * Small class encapsulating a method argument which could have been annotated with
     * {@link AuthorizationGuard}.
     * 
     * @author Christian Ribeaud
     */
    public final static class Argument<T>
    {
        public final static Argument<?>[] EMPTY_ARRAY = new Argument<?>[0];

        private Class<T> type;

        private final T argumentOrNull;

        private final AuthorizationGuard predicateCandidate;

        public Argument(final Class<T> type, final T argumentOrNull,
                final AuthorizationGuard predicateCandidate)
        {
            assert type != null : "Unspecified type";
            assert predicateCandidate != null : "Unspecified annotation";
            this.type = type;
            this.argumentOrNull = argumentOrNull;
            this.predicateCandidate = predicateCandidate;
        }

        public final Class<T> getType()
        {
            return type;
        }

        public final T tryGetArgument()
        {
            return argumentOrNull;
        }

        public final AuthorizationGuard getPredicateCandidate()
        {
            return predicateCandidate;
        }

        //
        // Object
        //

        @Override
        public final String toString()
        {
            return ToStringBuilder.reflectionToString(this,
                    ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        }
    }
}