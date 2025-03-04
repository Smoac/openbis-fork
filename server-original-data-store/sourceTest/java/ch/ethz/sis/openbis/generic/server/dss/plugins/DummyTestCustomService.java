/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.dssapi.v3.plugin.service.ICustomDSSServiceExecutor;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

public class DummyTestCustomService implements ICustomDSSServiceExecutor
{
    public DummyTestCustomService(Properties properties) {}
    @Override
    public Serializable executeService(String sessionToken, ICustomDSSServiceId serviceId,
            CustomDSSServiceExecutionOptions options)
    {
        Map<String, Object> params = options.getParameters();
        if(params.containsKey("key") && params.get("key").toString().equalsIgnoreCase("PING"))
        {
            return "PONG";
        }
        throw new IllegalArgumentException("Missing Ping parameter");
    }
}
