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
import ch.systemsx.cisd.common.exceptions.MasqueradingException;

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
    private final static String[] ACCEPTED_PACKAGE_NAME_DEPENDENCIES =
        { "java.lang", "ch.systemsx.cisd" };

    ExceptionUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Creates a new {@link MasqueradingException} from given <var>exception</var> only if it is
     * needed ({@link #isCandidateForMasquerading(Exception)} returns <code>true</code>).
     * Otherwise returns given <var>exception</var>.
     */
    private final static Exception createMasqueradingException(final Exception exception)
    {
        final Exception rootException = CheckedExceptionTunnel.unwrapIfNecessary(exception);
        if (isCandidateForMasquerading(rootException))
        {
            return new MasqueradingException(rootException);
        } else
        {
            return rootException;
        }
    }

    /** Recursively copies cause exception from <var>fromException</var> to <var>toException</var>. */
    private final static void copyCauseException(final Exception fromException,
            final Exception toException)
    {
        assert fromException != null : "Unspecified 'from' Exception.";
        assert toException != null : "Unspecified 'to' Exception.";
        final Exception fromCauseException =
                (Exception) org.apache.commons.lang.exception.ExceptionUtils
                        .getCause(fromException);
        if (fromCauseException != null && fromCauseException != fromException)
        {
            final Exception toCauseException = createMasqueradingException(fromCauseException);
            if (toException.getCause() != toCauseException)
            {
                if (ClassUtils.setFieldValue(toException, "cause", toCauseException) == false)
                {
                    org.apache.commons.lang.exception.ExceptionUtils.setCause(toException,
                            toCauseException);
                }
            }
            copyCauseException(fromCauseException, toCauseException);
        }
    }

    /**
     * Whether given <var>exception</var> is a candidate for masquerading or not.
     */
    final static boolean isCandidateForMasquerading(final Exception exception)
    {
        assert exception != null : "Unspecified exception.";
        final String className = exception.getClass().getName();
        for (final String packageName : ACCEPTED_PACKAGE_NAME_DEPENDENCIES)
        {
            if (className.startsWith(packageName))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Analyzes given <var>exception</var> and makes it independent to packages outside the ones
     * specified in an internal list, <code>ACCEPTED_PACKAGE_NAME_DEPENDENCIES</code>.
     */
    public final static Exception createMasqueradingExceptionIfNeeded(final Exception exception)
    {
        assert exception != null : "Unspecified SQL Exception.";
        final Exception clientSafeException = createMasqueradingException(exception);
        copyCauseException(exception, clientSafeException);
        return clientSafeException;
    }

}