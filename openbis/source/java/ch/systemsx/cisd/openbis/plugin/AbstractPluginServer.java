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

package ch.systemsx.cisd.openbis.plugin;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * A {@link AbstractServer} extension for plugin specific server.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractPluginServer<T extends IServer> extends AbstractServer<T>
{
    // For testing purpose.
    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    protected AbstractPluginServer()
    {
    }

    // For testing purpose.
    protected AbstractPluginServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory);
        this.sampleTypeSlaveServerPlugin = sampleTypeSlaveServerPlugin;
    }

    protected final ISampleTypeSlaveServerPlugin getSampleTypeSlaveServerPlugin(
            final SampleTypePE sampleType)
    {
        if (sampleTypeSlaveServerPlugin != null)
        {
            return sampleTypeSlaveServerPlugin;
        }
        return SampleServerPluginRegistry.getPlugin(this, sampleType).getSlaveServer();
    }

}
