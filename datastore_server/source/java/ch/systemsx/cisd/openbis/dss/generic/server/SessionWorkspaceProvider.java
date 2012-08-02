/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISessionWorkspaceProvider;

/**
 * A provider for the session workspace directory.
 * 
 * @author Bernd Rinn
 */
public class SessionWorkspaceProvider implements ISessionWorkspaceProvider
{
    private final String sessionToken;

    private final File sessionWorkspace;

    public SessionWorkspaceProvider(File sessionWorkspaceRootDirectory, String userSessionToken)
    {
        this.sessionToken = userSessionToken;
        this.sessionWorkspace = new File(sessionWorkspaceRootDirectory, sessionToken);
    }

    @Override
    public File getSessionWorkspace()
    {
        if (sessionWorkspace.exists() == false)
        {
            sessionWorkspace.mkdirs();
        }
        return sessionWorkspace;
    }

}
