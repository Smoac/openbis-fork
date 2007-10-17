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

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Most simplest implementation of {@link IFormattedData}. It is associated with {@link UnknownFormat1_0}.
 * It can be subclassed provided {@link #getFormat()} will be overridden.
 *
 * @author Franz-Josef Elmer
 */
public class NoFormattedData implements IFormattedData
{
    /**
     * Root directory of formated data.
     */
    protected final IDirectory dataDirectory;

    /**
     * Creates an instance for the specified data directory.
     */
    public NoFormattedData(IDirectory dataDirectory)
    {
        this.dataDirectory = dataDirectory;
    }
    
    /**
     * Returns {@link UnknownFormat1_0#UNKNOWN_1_0}.
     */
    public Format getFormat()
    {
        return UnknownFormat1_0.UNKNOWN_1_0;
    }

}
