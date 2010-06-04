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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataStoreApiUrlUtilities
{
    /**
     * Converts the download url to a server url.
     */
    public static String getDataStoreUrlFromDataStore(DataStore dataStore)
    {
        String datastoreUrl = dataStore.getDownloadUrl();
        System.out.println("Data store url : " + datastoreUrl);
        // The url objained form a DataStore object is the *download* url. Convert this to the
        // datastore URL
        if (datastoreUrl.endsWith(DATA_STORE_SERVER_WEB_APPLICATION_NAME))
        {
            datastoreUrl =
                    datastoreUrl.substring(0, datastoreUrl.length()
                            - DATA_STORE_SERVER_WEB_APPLICATION_NAME.length());
        }
        if (datastoreUrl.endsWith("/"))
        {
            datastoreUrl = datastoreUrl.substring(0, datastoreUrl.length() - 1);
        }

        return datastoreUrl;
    }
}
