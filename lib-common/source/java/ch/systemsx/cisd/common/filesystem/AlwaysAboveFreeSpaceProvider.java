/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.filesystem;

/**
 * An <code>IFreeSpaceProvider</code> implementation which returns {@link Long#MAX_VALUE} as free space value.
 * <p>
 * Therefore the free space available will never be below the <i>high water mark</i>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class AlwaysAboveFreeSpaceProvider implements IFreeSpaceProvider
{

    /** The only instance of this class. */
    public final static IFreeSpaceProvider INSTANCE = new AlwaysAboveFreeSpaceProvider();

    private AlwaysAboveFreeSpaceProvider()
    {
        // Can not be instantiated.
    }

    //
    // IFreeSpaceProvider
    //

    @Override
    public final long freeSpaceKb(final HostAwareFile path)
    {
        return Long.MAX_VALUE;
    }

}
