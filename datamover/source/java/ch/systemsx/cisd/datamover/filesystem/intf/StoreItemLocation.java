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

package ch.systemsx.cisd.datamover.filesystem.intf;

import ch.systemsx.cisd.common.utilities.StoreItem;

/**
 * Bean for the location of a {@link StoreItem} on a local or remote file system.
 * 
 * @author Franz-Josef Elmer
 */
public final class StoreItemLocation
{
    private final String host;

    private final String absolutePath;

    /**
     * Creates a new instance for the specified host and absolute path.
     * 
     * @param hostOrNull If not <code>null</code> the file is on a remote file system.
     */
    public StoreItemLocation(final String hostOrNull, final String absolutePath)
    {
        if (absolutePath == null)
        {
            throw new IllegalArgumentException("Absolute path not specified.");
        }
        host = hostOrNull;
        this.absolutePath = absolutePath;
    }

    /**
     * Returns the host if this location is on a remote file system. Otherwise <code>null</code>
     * is returned.
     */
    public final String getHost()
    {
        return host;
    }

    public final String getAbsolutePath()
    {
        return absolutePath;
    }
}
