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

package ch.systemsx.cisd.cifex.client.application;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.cifex.client.dto.Configuration;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * Some <code>static</code> data that are available in the whole application context.
 * 
 * @author Franz-Josef Elmer
 */
public final class Model
{
    private UserInfoDTO user;

    /**
     * The URL parameters.
     * <p>
     * Is never <code>null</code> but could be empty.
     * </p>
     */
    private Map urlParams = new HashMap();

    private Configuration configuration;

    public final Map getUrlParams()
    {
        return urlParams;
    }

    public final void setUrlParams(final Map urlParams)
    {
        assert urlParams != null : "URL params can not be null.";
        this.urlParams = urlParams;
    }

    public final void setUser(final UserInfoDTO user)
    {
        assert user != null : "User must not be null.";
        this.user = user;
    }

    public final UserInfoDTO getUser()
    {
        return user;
    }

    public final Configuration getConfiguration()
    {
        return configuration;
    }

    public final void setConfiguration(Configuration configuration)
    {
        assert configuration != null : "Configuration must not be null.";

        this.configuration = configuration;
    }
}
