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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session.ISessionCleaner;

/**
 * A cleaner for user session data on data store servers.
 * 
 * @author Bernd Rinn
 */
public class DataStoreUserSessionCleaner
{

    private final Map<String, List<IDataStoreService>> sessions =
            new HashMap<String, List<IDataStoreService>>();

    public void add(Session userSession, IDataStoreService dataStore)
    {
        final String userSessionToken = userSession.getSessionToken();
        final List<IDataStoreService> dataStores = getOrCreateDataStores(userSessionToken);
        dataStores.add(dataStore);
        userSession.addCleanupListener(new ISessionCleaner()
            {
                @Override
                public void cleanup()
                {
                    for (IDataStoreService service : dataStores)
                    {
                        service.cleanupSession(userSessionToken);
                    }
                    sessions.remove(userSessionToken);
                }
            });
    }

    private List<IDataStoreService> getOrCreateDataStores(String userSessionToken)
    {
        List<IDataStoreService> dataStores = sessions.get(userSessionToken);
        if (dataStores == null)
        {
            dataStores = new ArrayList<IDataStoreService>();
            sessions.put(userSessionToken, dataStores);
        }
        return dataStores;
    }

}
