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
 * The <code>RetriableEnvironmentFailureException</code> is the super class of all exceptions that have their cause in
 * the software or hardware environment of the system failing and where, at least in principle, retrying the operation
 * could help.
 * <p>
 * If retrying doesn't help, use an {@link EnvironmentFailureException} instead.
 * <p>
 * Note that the user does not count as part of the environment in this respect.
 * 
 * @author Bernd Rinn
 */
public class RetriableEnvironmentFailureException extends EnvironmentFailureException
{

    private static final long serialVersionUID = 1L;

    public RetriableEnvironmentFailureException(String msg)
    {
        super(msg);
    }

    public RetriableEnvironmentFailureException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates a {@link RetriableEnvironmentFailureException} using a {@link java.util.Formatter}.
     */
    public static RetriableEnvironmentFailureException fromTemplate(String messageTemplate,
            Object... args)
    {
        return new RetriableEnvironmentFailureException(String.format(messageTemplate, args));
    }

    /**
     * Creates a {@link RetriableEnvironmentFailureException} using a {@link java.util.Formatter}.
     */
    public static RetriableEnvironmentFailureException fromTemplate(Throwable cause,
            String messageTemplate, Object... args)
    {
        return new RetriableEnvironmentFailureException(String.format(messageTemplate, args), cause);
    }

    /**
     * Returns <code>true</code> to indicate that retrying the operation might help.
     * 
     * @see ch.systemsx.cisd.common.exceptions.EnvironmentFailureException#isRetriable()
     */
    @Override
    public boolean isRetriable()
    {
        return true;
    }

}
