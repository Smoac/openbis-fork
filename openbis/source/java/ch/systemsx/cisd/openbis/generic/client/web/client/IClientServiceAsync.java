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

package ch.systemsx.cisd.openbis.generic.client.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * @author Christian Ribeaud
 */
public interface IClientServiceAsync
{

    /** @see IClientService#getApplicationInfo() */
    public void getApplicationInfo(AsyncCallback<ApplicationInfo> callback);

    /** @see IClientService#tryToGetCurrentSessionContext() */
    public void tryToGetCurrentSessionContext(AsyncCallback<SessionContext> callback);

    /** @see IClientService#tryToLogin(String, String) */
    public void tryToLogin(String userID, String password, AsyncCallback<SessionContext> callback);

    /** @see IClientService#setBaseURL(String) */
    public void setBaseURL(String baseURL, AsyncCallback<SessionContext> callback);

    /** @see IClientService#updateDisplaySettings(DisplaySettings) */
    public void updateDisplaySettings(DisplaySettings displaySettings, AsyncCallback<Void> callback);

    /** @see IClientService#resetDisplaySettings() */
    public void resetDisplaySettings(AsyncCallback<DisplaySettings> resetUserSettingsCallback);

    /** @see IClientService#changeUserHomeGroup(TechId) */
    public void changeUserHomeGroup(TechId groupIdOrNull, AsyncCallback<Void> callback);

    /** @see IClientService#logout(DisplaySettings) */
    public void logout(DisplaySettings displaySettings, AsyncCallback<Void> callback);

}
