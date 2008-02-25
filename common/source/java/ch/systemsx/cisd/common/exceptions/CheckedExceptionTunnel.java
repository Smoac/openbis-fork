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

package ch.systemsx.cisd.common.exceptions;

/**
 * An exception for tunneling checked exception through code that doesn't expect it.
 * 
 * @author Bernd Rinn
 */
public final class CheckedExceptionTunnel extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    /**
     * Returns an unchecked exception from a <var>checkedException</var>.
     * 
     * @param checkedException The checked exception to tunnel.
     */
    public CheckedExceptionTunnel(final Exception checkedException)
    {
        super(checkedException);

        assert (checkedException != null && checkedException instanceof RuntimeException) == false;
    }

    /**
     * Returns a {@link RuntimeException} from an <var>exception</var>. If <var>exception</var> is already a
     * {@link RuntimeException}, itself is returned, otherwise a {@link CheckedExceptionTunnel} with <var>exception</var>
     * as checked exception argument.
     * 
     * @param exception The exception to represent by the return value.
     * @return A {@link RuntimeException} representing the <var>exception</var>.
     */
    public final static RuntimeException wrapIfNecessary(final Exception exception)
    {
        if (exception instanceof RuntimeException)
        {
            return (RuntimeException) exception;
        } else
        {
            return new CheckedExceptionTunnel(exception);
        }
    }

    /**
     * Returns the original exception before being wrapped, if the exception has been wrapped, or <var>exception</var>
     * otherwise.
     */
    public final static Exception unwrapIfNecessary(final Exception exception)
    {
        assert exception != null : "Exception not specified.";
        if (exception instanceof CheckedExceptionTunnel)
        {
            // We are sur that the wrapped exception is an 'Exception'.
            return (Exception) exception.getCause();
        } else
        {
            return exception;
        }
    }

}
