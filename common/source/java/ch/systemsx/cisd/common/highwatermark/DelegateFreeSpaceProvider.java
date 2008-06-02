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

import java.io.File;
import java.io.IOException;

import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.IFreeSpaceProvider;

/**
 * A <code>IFreeSpaceProvider</code> implementation which delegates its job to the encapsulated
 * {@link IFreeSpaceProvider} implementation.
 * 
 * @author Christian Ribeaud
 */
public class DelegateFreeSpaceProvider implements IFreeSpaceProvider
{

    private final IFreeSpaceProvider freeSpaceProvider;

    public DelegateFreeSpaceProvider(final IFreeSpaceProvider freeSpaceProvider)
    {
        this.freeSpaceProvider = freeSpaceProvider;
    }

    protected final IFreeSpaceProvider getFreeSpaceProvider()
    {
        return freeSpaceProvider;
    }

    //
    // IFreeSpaceProvider
    //

    public long freeSpaceKb(final File path) throws IOException
    {
        return freeSpaceProvider.freeSpaceKb(path);
    }
}
