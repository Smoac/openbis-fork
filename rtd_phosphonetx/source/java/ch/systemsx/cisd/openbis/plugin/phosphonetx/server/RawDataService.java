/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreServiceTranslator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;

/**
 * Imlementation of {@link IRawDataService}.
 *
 * @author Franz-Josef Elmer
 */
public class RawDataService extends AbstractServer<IRawDataService> implements IRawDataService
{
    private IRawDataServiceInternal service;

    public RawDataService()
    {
    }

    public RawDataService(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            IRawDataServiceInternal service)
    {
        super(sessionManager, daoFactory);
        this.service = service;
    }
    
    public IRawDataService createLogger(IInvocationLoggerContext context)
    {
        return new RawDataServiceLogger(getSessionManager(), context);
    }

    public List<Sample> listRawDataSamples(String sessionToken, String userID)
    {
        checkSession(sessionToken);
        SessionContextDTO session = login(userID);
        try
        {
            return service.listRawDataSamples(session.getSessionToken());
            
        } finally
        {
            service.logout(session.getSessionToken());
        }
    }

    public List<DatastoreServiceDescription> listDataStoreServices(String sessionToken)
    {
        checkSession(sessionToken);

        List<DatastoreServiceDescription> result = new ArrayList<DatastoreServiceDescription>();
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            Set<DataStoreServicePE> services = dataStore.getServices();
            for (DataStoreServicePE dataStoreService : services)
            {
                if (dataStoreService.getKind() == DataStoreServiceKind.PROCESSING)
                {
                    result.add(DataStoreServiceTranslator.translate(dataStoreService));
                }
            }
        }
        return result;
    }

    public void processingRawData(String sessionToken, String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs)
    {
        checkSession(sessionToken);
        SessionContextDTO session = login(userID);
        try
        {
            service.processRawData(session.getSessionToken(), dataSetProcessingKey,
                    rawDataSampleIDs);
        } finally
        {
            service.logout(session.getSessionToken());
        }
    }
    
    private SessionContextDTO login(String userID)
    {
        SessionContextDTO session = service.tryToAuthenticate(userID, "dummy-password");
        if (session == null)
        {
            throw new UserFailureException("Unknown user ID: " + userID);
        }
        return session;
    }
    
}
