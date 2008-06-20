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

package ch.systemsx.cisd.common.highwatermark;

import java.io.IOException;

import org.apache.commons.io.FileSystemUtils;

import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.IFreeSpaceProvider;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * A simple <code>IFreeSpaceProvider</code> implementation based on {@link FileSystemUtils}.
 * 
 * @author Christian Ribeaud
 */
public final class SimpleFreeSpaceProvider implements IFreeSpaceProvider
{

    //
    // IFreeSpaceProvider
    //

    public final long freeSpaceKb(final HostAwareFile path) throws IOException
    {
        final String canonicalPath = FileUtilities.getCanonicalPath(path.getFile());
        return FileSystemUtils.freeSpaceKb(canonicalPath);
    }
}