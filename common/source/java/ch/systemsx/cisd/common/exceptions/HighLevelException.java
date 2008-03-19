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
 * An exception that is "high-level" in the sense that we have a pretty good understanding what the failure means in the
 * context where the exception has been thrown.
 * 
 * @author Bernd Rinn
 */
public abstract class HighLevelException extends RuntimeException
{

    protected HighLevelException(String message)
    {
        super(message);
    }

    protected HighLevelException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
