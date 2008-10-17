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

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.Technology;
import ch.systemsx.cisd.openbis.plugin.screening.server.business.bo.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.business.bo.ScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;

/**
 * The concrete {@link IScreeningServer} implementation.
 * 
 * @author Christian Ribeaud
 */
@SuppressWarnings("unused")
public final class ScreeningServer extends AbstractServer<IScreeningServer> implements
        IScreeningServer
{
    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private static final Technology SCREENING_TECHNOLOGY = new Technology("SCREENING");

    public ScreeningServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory)
    {
        super(sessionManager, daoFactory);
        this.businessObjectFactory = new ScreeningBusinessObjectFactory(daoFactory);
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

    @Transactional
    public final SamplePE getSampleInfo(final String sessionToken, final SampleIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final SamplePE samplePE = new SamplePE();
        samplePE.setCode("CHOUBIDOU");
        // final ISampleServerPlugin plugin =
        // SampleServerPluginRegistry
        // .getPlugin(SCREENING_TECHNOLOGY, samplePE.getSampleType());
        return samplePE;
    }
}
