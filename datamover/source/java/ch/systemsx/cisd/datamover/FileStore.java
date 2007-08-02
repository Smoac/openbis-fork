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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.IOException;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * A class to holds the information about a file store.
 * 
 * @author Bernd Rinn
 */
public class FileStore
{
    private final File path;

    private final String canonicalPath;

    private final String kind;

    private final String host;

    private final boolean remote;

    FileStore(File path, String kind, String host, boolean remote)
    {
        this.path = path;
        this.kind = kind;
        if (path != null)
        {
            this.canonicalPath = getCanonicalPath(path, host);
        } else
        {
            this.canonicalPath = null;
        }
        this.host = host;
        this.remote = remote;
    }

    private static String getCanonicalPath(File path, String host)
    {
        if (host != null)
        {
            return path.getPath();
        }
        try
        {
            return path.getCanonicalPath() + File.separator;
        } catch (IOException e)
        {
            throw EnvironmentFailureException.fromTemplate(e, "Cannot determine canonical form of path '%s'", path
                    .getPath());
        }
    }

    /**
     * Returns the remote host, if copying is done via an ssh tunnel, or <code>null</code> otherwise.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Returns the path of the file store. If {@link #getHost()} is <code>null</code>, and only then, it may be
     * interpreted to be on the local host.
     */
    public File getPath()
    {
        return path;
    }

    /**
     * Returns the canonical form of the path as a string, if {@link #getHost()} is <code>null</code>. Note that the
     * canonical name is only available if {@link #getHost()} is <code>null</code>, otherwise the normal path (as
     * returned by {@link File#getPath()}) will be returned by this method.
     */
    public String getCanonicalPath()
    {
        return canonicalPath;
    }

    /**
     * Returns the kind of file store (used for error and log messages).
     */
    public String getKind()
    {
        return kind;
    }

    /**
     * Returns <code>true</code>, if the file store resides on a remote computer and <code>false</code> otherwise.
     * <p>
     * Note that this method can return <code>true</code> despite {@link #getHost()} returning <code>null</code>.
     * In this case the file store is on a remote share mounted on local host via NFS or CIFS. 
     */
    public boolean isRemote()
    {
        return remote;
    }

}