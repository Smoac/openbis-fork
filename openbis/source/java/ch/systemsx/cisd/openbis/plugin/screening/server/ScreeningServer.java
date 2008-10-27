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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.ISampleServerPlugin;
import ch.systemsx.cisd.openbis.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;

/**
 * The concrete {@link IScreeningServer} implementation.
 * 
 * @author Christian Ribeaud
 */
@Component(ResourceNames.SCREENING_SERVER)
public final class ScreeningServer extends AbstractServer<IScreeningServer> implements
        IScreeningServer
{
    public ScreeningServer()
    {
    }

    ScreeningServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final IGenericBusinessObjectFactory businessObjectFactory)
    {
        super(sessionManager, daoFactory, businessObjectFactory);
    }

    //
    // AbstractServer
    //

    @Override
    protected final Class<IScreeningServer> getProxyInterface()
    {
        return IScreeningServer.class;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final IScreeningServer createLogger(final boolean invocationSuccessful)
    {
        return new ScreeningServerLogger(getSessionManager(), invocationSuccessful);
    }

    //
    // IScreeningServer
    //

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = getBusinessObjectFactory().createSampleBO(session);
        sampleBO.loadBySampleIdentifier(identifier);
        final SamplePE sample = sampleBO.getSample();
        final ISampleServerPlugin plugin =
                SampleServerPluginRegistry.getPlugin(this, sample.getSampleType());
        return plugin.getSlaveServer().getSampleInfo(getDAOFactory(), session, sample);
    }
}
