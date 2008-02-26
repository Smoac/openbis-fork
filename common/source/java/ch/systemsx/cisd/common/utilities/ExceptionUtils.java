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

package ch.systemsx.cisd.common.utilities;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.IndependentException;

/**
 * Provides utilities for manipulating and examining <code>Throwable</code> objects.
 * 
 * @author Christian Ribeaud
 */
public final class ExceptionUtils
{
    /**
     * Accepted packages for dependencies.
     */
    private static String[] ACCEPTED_DEPENDENCIES_PACKAGE_NAMES =
        { "java.lang", "ch.systemsx.cisd" };

    ExceptionUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Creates a new {@link IndependentException} from given <var>exception</var> only if it is needed ({@link #isIndependent(Exception)}
     * returns <code>false</code>). Otherwise returns given <var>exception</var>.
     */
    private final static Exception createIndependentException(final Exception exception)
    {
        final Exception rootException = CheckedExceptionTunnel.unwrapIfNecessary(exception);
        if (isIndependent(rootException) == false)
        {
            return new IndependentException(rootException);
        } else
        {
            return rootException;
        }
    }

    /** Recursively copies cause exception from <var>fromException</var> to <var>toException</var>. */
    private final static void copyCauseException(final Exception fromException, final Exception toException)
    {
        assert fromException != null : "Unspecified 'from' Exception.";
        assert toException != null : "Unspecified 'to' Exception.";
        final Exception fromCauseException =
                (Exception) org.apache.commons.lang.exception.ExceptionUtils.getCause(fromException);
        if (fromCauseException != null && fromCauseException != fromException)
        {
            final Exception toCauseException = createIndependentException(fromCauseException);
            if (toException.getCause() != toCauseException)
            {
                if (ClassUtils.setFieldValue(toException, "cause", toCauseException) == false)
                {
                    org.apache.commons.lang.exception.ExceptionUtils.setCause(toException, toCauseException);
                }
            }
            copyCauseException(fromCauseException, toCauseException);
        }
    }

    /**
     * Whether given <var>exception</var> is client-safe or not.
     */
    final static boolean isIndependent(final Exception exception)
    {
        assert exception != null : "Unspecified exception.";
        final String className = exception.getClass().getName();
        for (final String packageName : ACCEPTED_DEPENDENCIES_PACKAGE_NAMES)
        {
            if (className.startsWith(packageName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Analyzes given <var>exception</var> and makes it independent to packages outside the ones specified in
     * {@link #ACCEPTED_DEPENDENCIES_PACKAGE_NAMES}.
     */
    public final static Exception createIndependentExceptionIfNeeded(final Exception exception)
    {
        assert exception != null : "Unspecified SQL Exception.";
        final Exception clientSafeException = createIndependentException(exception);
        copyCauseException(exception, clientSafeException);
        return clientSafeException;
    }

}