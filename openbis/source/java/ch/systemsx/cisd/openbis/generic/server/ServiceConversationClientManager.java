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

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.conversation.manager.BaseServiceConversationClientManager;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.conversation.ServiceConversationApplicationServerClientId;
import ch.systemsx.cisd.openbis.generic.shared.conversation.ServiceConversationDataStoreUrl;

/**
 * The service conversation client manager used by AS.
 * 
 * @author pkupczyk
 */
public class ServiceConversationClientManager extends BaseServiceConversationClientManager
        implements IServiceConversationClientManagerLocal
{

    private Map<String, Integer> dataStoreUrlToDataStoreTimeoutMap = new HashMap<String, Integer>();

    @Override
    public IDataStoreService getDataStoreService(String dataStoreUrl, String sessionToken)
    {
        String dataStoreServerUrl =
                new ServiceConversationDataStoreUrl(dataStoreUrl).getServerUrl();
        Object applicationServerClientId = new ServiceConversationApplicationServerClientId();
        int dataStoreTimeoutInMillis = dataStoreUrlToDataStoreTimeoutMap.get(dataStoreUrl);

        return getService(dataStoreServerUrl, IDataStoreService.class, sessionToken,
                applicationServerClientId, dataStoreTimeoutInMillis);
    }

    @Override
    public void setDataStoreInformation(String dataStoreUrl, int dataStoreTimeoutInMinutes)
    {
        dataStoreUrlToDataStoreTimeoutMap.put(dataStoreUrl, dataStoreTimeoutInMinutes * 60 * 1000);
    }

}
