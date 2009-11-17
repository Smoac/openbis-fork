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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public final class TrackingServer extends AbstractServer<ITrackingServer> implements
        ITrackingServer
{
    private final ICommonBusinessObjectFactory businessObjectFactory;

    public TrackingServer(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory, final ICommonBusinessObjectFactory businessObjectFactory)
    {
        super(sessionManager, daoFactory);
        this.businessObjectFactory = businessObjectFactory;
    }

    ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return businessObjectFactory;
    }

    // private static UserFailureException createUserFailureException(final DataAccessException ex)
    // {
    // return new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
    // }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final ITrackingServer createLogger(final boolean invocationSuccessful,
            final long elapsedTime)
    {
        return new TrackingServerLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    //
    // ITrackingServer
    //

    public List<ExternalData> listDataSets(String sessionToken, TrackingDataSetCriteria criteria)
    {
        // TODO 2009-11-06, Piotr Buczek: implement
        return null;
    }

    public List<Sample> listSamples(String sessionToken, TrackingSampleCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return sampleLister.list(new ListOrSearchSampleCriteria(criteria));
    }
}
