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

import org.apache.commons.lang.exception.ExceptionUtils;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * A <code>RuntimeException</code> extension which is client safe, meaning that it does not contain any third-party
 * specific or proprietary <code>Exception</code> extension that the client does not know about and does not
 * understand.
 * <p>
 * Note that this class can only be instantiated via {@link #createClientSafeExceptionIfNeeded(Exception)}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ClientSafeException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    /**
     * The class name of the root exception.
     * <p>
     * Can not be <code>null</code>.
     * </p>
     */
    private final String rootClassName;

    private ClientSafeException(final Exception rootException)
    {
        super(rootException.getMessage());
        setStackTrace(rootException.getStackTrace());
        rootClassName = rootException.getClass().getName();
    }

    /**
     * Creates a new <code>ClientSafeException</code> from given <var>exception</var> only if it is needed ({@link #isClientSafe(Exception)}
     * returns <code>false</code>). Otherwise returns given <var>exception</var>.
     */
    private final static Exception createClientSafeException(final Exception exception)
    {
        if (isClientSafe(exception) == false)
        {
            return new ClientSafeException(exception);
        } else
        {
            return exception;
        }
    }

    /**
     * Whether given <var>exception</var> is client-safe or not.
     */
    private final static boolean isClientSafe(final Exception exception)
    {
        assert exception != null : "Unspecified exception.";
        final String className = exception.getClass().getName();
        return className.startsWith("java.lang") || className.startsWith("ch.systemsx.cisd");
    }

    /** Recursively copies cause exception from <var>fromException</var> to <var>toException</var>. */
    private final static void copyCauseException(final Exception fromException, final Exception toException)
    {
        assert fromException != null : "Unspecified 'from' Exception.";
        assert toException != null : "Unspecified 'to' Exception.";
        final Exception fromCauseException =
                CheckedExceptionTunnel.unwrapIfNecessary((Exception) ExceptionUtils.getCause(fromException));
        if (fromCauseException != null && fromCauseException != fromException)
        {
            final Exception toCauseException = createClientSafeException(fromCauseException);
            if (toException.getCause() != toCauseException)
            {
                ClassUtils.setFieldValue(toException, "cause", toCauseException);
            }
            copyCauseException(fromCauseException, toCauseException);
        }
    }

    /**
     * Analyzes given <var>exception</var> and makes it client-safe.
     */
    public final static Exception createClientSafeExceptionIfNeeded(final Exception exception)
    {
        assert exception != null : "Unspecified SQL Exception.";
        final Exception rootException = createClientSafeException(exception);
        copyCauseException(rootException, rootException);
        return rootException;
    }

    //
    // RuntimeException
    //

    @Override
    public final String toString()
    {
        final String s = rootClassName;
        final String message = getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}