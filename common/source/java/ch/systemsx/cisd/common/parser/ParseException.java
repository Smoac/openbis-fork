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

package ch.systemsx.cisd.common.parser;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Signals that an error has been reached unexpectedly while parsing.
 * 
 * @author Christian Ribeaud
 */
public final class ParseException extends UserFailureException
{

    private static final long serialVersionUID = 1L;

    private final int lineNumber;

    public ParseException(String message, int lineNumber)
    {
        super(message);
        this.lineNumber = lineNumber;
    }

    public ParseException(String message, Throwable cause, int lineNumber)
    {
        super(message, cause);
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the line where the error was found.
     */
    public final int getLineNumber()
    {
        return lineNumber;
    }
}
