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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.ISessionFactory;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session.ISessionCleaner;

/**
 * Factory of {@link Session} objects.
 * 
 * @author Franz-Josef Elmer
 */
public final class SessionFactory implements ISessionFactory<Session>
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SessionFactory.class);

    private final IDataStoreDAO datastoreDAO;

    private final IDataStoreServiceFactory dssFactory;

    public SessionFactory()
    {
        this(null, null);
    }

    public SessionFactory(IDAOFactory daoFactory, IDataStoreServiceFactory dssFactory)
    {
        this.datastoreDAO = (daoFactory != null) ? daoFactory.getDataStoreDAO() : null;
        this.dssFactory = dssFactory;
    }

    //
    // ISessionFactory
    //

    @Override
    public final Session create(final String sessionToken, final String userName,
            final Principal principal, final String remoteHost, final long sessionStart,
            final int expirationTime)
    {
        final Session session =
                new Session(userName, sessionToken, principal, remoteHost, sessionStart,
                        expirationTime);
        if (datastoreDAO != null && dssFactory != null)
        {
            session.addCleanupListener(new ISessionCleaner()
                {
                    @Override
                    public void cleanup()
                    {
                        cleanUpSessionOnDataStoreServers(sessionToken, datastoreDAO, dssFactory);
                    }
                });
        }
        return session;
    }

    public static void cleanUpSessionOnDataStoreServers(String sessionToken,
            IDataStoreDAO datastoreDAO, IDataStoreServiceFactory dssFactory)
    {
        for (DataStorePE datastore : datastoreDAO.listDataStores())
        {
            final String remoteUrl = datastore.getRemoteUrl();
            if (StringUtils.isBlank(remoteUrl) == false)
            {
                dssFactory.createMonitored(remoteUrl, LogLevel.WARN).cleanupSession(sessionToken);
            } else
            {
                operationLog.warn("datastore remoteUrl of datastore " + datastore.getCode()
                        + " is empty - skipping DSS session cleanup.");
            }
        }
    }
}
