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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;
import java.io.Serializable;

import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * A {@link java.io.File} that is aware of the host it is located on.
 * 
 * @author Christian Ribeaud
 */
public class HostAwareFile extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final char HOST_FILE_SEP = ':';

    private final String hostOrNull;

    private final File path;

    private final String rsyncModuleOrNull;

    public HostAwareFile(final File path)
    {
        this(null, path, null);
    }

    public HostAwareFile(final String hostOrNull, final File path, final String rsyncModuleOrNull)
    {
        this.hostOrNull = hostOrNull;
        this.path = path;
        this.rsyncModuleOrNull = rsyncModuleOrNull;
    }

    /**
     * @return the host on which {@link #getFile()} is located on or <code>null</code>.
     */
    public final String tryGetHost()
    {
        return hostOrNull;
    }

    /**
     * Returns the file path.
     */
    public final File getFile()
    {
        return path;
    }

    /**
     * Returns the rsync module on the rsync server to use to access this file, or <code>null</code>
     * , if no rsync server but rather an ssh tunnel should be used.
     */
    public final String tryGetRsyncModule()
    {
        return rsyncModuleOrNull;
    }

    /** Return the canonical path of the encapsulated <code>path</code>. */
    public final String getCanonicalPath()
    {
        if (tryGetHost() == null)
        {
            return FileUtilities.getCanonicalPath(getFile());
        } else
        {
            if (tryGetRsyncModule() == null)
            {
                return tryGetHost() + HOST_FILE_SEP + getFile();
            } else
            {
                return tryGetHost() + HOST_FILE_SEP + tryGetRsyncModule() + HOST_FILE_SEP
                        + getFile();
            }
        }
    }
}
