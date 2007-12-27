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

package ch.systemsx.cisd.bds.storage.filesystem;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.IPathImmutableCopier;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.common.utilities.RecursiveHardLinkMaker;

/**
 * A provider of {@link IPathImmutableCopier} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class LinkMakerProvider
{

    private static final String NO_HARD_LINK_EXECUTABLE = "No hard link executable has been found.";

    private static final String WINDOWS_NOT_SUPPORTED = "Windows is not supported.";

    private LinkMakerProvider()
    {
        // This class can not be instantiated.
    }

    /**
     * Returns a default <code>IPathImmutableCopier</code> implementation.
     */
    public final static IPathImmutableCopier getDefaultLinkMaker()
    {
        if (OSUtilities.isWindows() == false)
        {
            final IPathImmutableCopier copier = RecursiveHardLinkMaker.tryCreate();
            if (copier != null)
            {
                return copier;
            } else
            {
                throw new EnvironmentFailureException(NO_HARD_LINK_EXECUTABLE);
            }
        }
        throw new EnvironmentFailureException(WINDOWS_NOT_SUPPORTED);
    }

}
