/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;

/**
 * Implementation of {@link IDataStoreService} which will be accessed remotely by the opneBIS
 * server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreService extends AbstractServiceWithLogger<IDataStoreService> implements IDataStoreService
{
    private final SessionTokenManager sessionTokenManager;

    public DataStoreService(SessionTokenManager sessionTokenManager)
    {
        this.sessionTokenManager = sessionTokenManager;
    }
    
    @Override
    protected Class<IDataStoreService> getProxyInterface()
    {
        return IDataStoreService.class;
    }

    public IDataStoreService createLogger(boolean invocationSuccessful)
    {
        return new DataStoreServiceLogger(operationLog, invocationSuccessful);
    }

    public int getVersion(String sessionToken)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        
        return IDataStoreService.VERSION;
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        
        for (String location : dataSetLocations)
        {
            System.out.println(location);
        }
        
    }

}
