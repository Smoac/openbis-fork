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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.storage.StorageException;

/**
 * Exception thrown by manipulations of BDS data structures which can not be classified as {@link StorageException}.
 *
 * @author Franz-Josef Elmer
 */
public class DataStructureException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    /**
     *  Creates an instance with the specified message.
     */
    public DataStructureException(String message)
    {
        super(message);
    }

    /**
     * Creates an instance with the specified message and throwable causing this exception.
     */
    public DataStructureException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
