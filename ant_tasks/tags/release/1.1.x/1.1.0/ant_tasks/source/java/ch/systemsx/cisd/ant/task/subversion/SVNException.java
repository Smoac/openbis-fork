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

package ch.systemsx.cisd.ant.task.subversion;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * An exception indicating a problem performing a subversion command.
 *
 * @author Bernd Rinn
 */
public class SVNException extends EnvironmentFailureException
{

    private static final long serialVersionUID = 1L;

    public SVNException(String message)
    {
        super(message);
    }
    
    public SVNException(String messageTemplate, Throwable cause)
    {
        super(messageTemplate, cause);
    }

    /**
     * Creates a {@link SVNException} using a {@link java.util.Formatter}.
     */
    public static SVNException fromTemplate(String messageTemplate, Object... args)
    {
        return new SVNException(String.format(messageTemplate, args));
    }

    /**
     * Creates a {@link SVNException} using a {@link java.util.Formatter}.
     */
    public static SVNException fromTemplate(Throwable cause, String messageTemplate, Object... args)
    {
        return new SVNException(String.format(messageTemplate, args), cause);
    }

}
