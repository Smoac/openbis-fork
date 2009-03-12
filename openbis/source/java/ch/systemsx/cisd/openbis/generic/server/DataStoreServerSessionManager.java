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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerSession;

/**
 * Class managing Data Store Server (DSS) sessions. It is assumed that the URL of the DSS is unique
 * in order to allow more than one DSS.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServerSessionManager
{
    private final Map<String, DataStoreServerSession> sessions = new HashMap<String, DataStoreServerSession>();
    
    /**
     * Registers the specified DSS session.
     */
    public void registerDataStoreServer(DataStoreServerSession session)
    {
        synchronized (sessions)
        {
            sessions.put(session.getDataStoreServerURL(), session);
        }
    }
    
    /**
     * Tries to get the DSS session identified by the specified Data Store Server URL.
     * 
     * @return <code>null</code> if no session has been registered for the specified URL.
     */
    public DataStoreServerSession tryToGetSession(String dssURL)
    {
        synchronized (sessions)
        {
            return sessions.get(dssURL);
        }
    }

    /**
     * Returns all registered DSS sessions.
     */
    public Collection<DataStoreServerSession> getSessions()
    {
        synchronized (sessions)
        {
            return new ArrayList<DataStoreServerSession>(sessions.values());
        }
    }
}
