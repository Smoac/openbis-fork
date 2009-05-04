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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * The view model.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericViewModel
{
    private ApplicationInfo applicationInfo;

    private SessionContext sessionContext;
    
    /**
     * The URL parameters.
     * <p>
     * Is never <code>null</code> but could be empty.
     * </p>
     */
    private Map<String, String> urlParams = new HashMap<String, String>();

    public final ApplicationInfo getApplicationInfo()
    {
        return applicationInfo;
    }

    public final void setApplicationInfo(final ApplicationInfo applicationInfo)
    {
        this.applicationInfo = applicationInfo;
    }

    public final SessionContext getSessionContext()
    {
        return sessionContext;
    }

    public final void setSessionContext(final SessionContext sessionContext)
    {
        this.sessionContext = sessionContext;
    }

    public final Map<String, String> getUrlParams()
    {
        return urlParams;
    }

    public final void setUrlParams(final Map<String, String> urlParams)
    {
        assert urlParams != null : "URL params can not be null.";
        this.urlParams = urlParams;
    }

}
