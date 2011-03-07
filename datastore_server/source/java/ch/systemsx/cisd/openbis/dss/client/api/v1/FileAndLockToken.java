/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.io.File;

/**
 * Potentially locked file.
 *
 * @author Franz-Josef Elmer
 */
public class FileAndLockToken
{
    private final File file;
    private final String lockTokenOrNull;

    /**
     * Creates an instance for the specified file and an optional lock token if the file is locked.
     */
    public FileAndLockToken(File file, String lockTokenOrNull)
    {
        this.file = file;
        this.lockTokenOrNull = lockTokenOrNull;
    }
    
    /**
     * Returns the file.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Returns <code>true</code> if the lock token is not <code>null</code>.
     */
    public boolean isLocked()
    {
        return lockTokenOrNull != null;
    }
    
    /**
     * Returns the lock token or <code>null</code> if there is no lock on the file.
     */
    public String getLockToken()
    {
        return lockTokenOrNull;
    }
}
