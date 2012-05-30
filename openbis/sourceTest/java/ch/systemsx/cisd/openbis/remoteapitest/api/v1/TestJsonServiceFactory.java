/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.remoteapitest.api.v1;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.common.api.server.json.JsonReflectionsSubTypeResolver;
import ch.systemsx.cisd.common.api.server.json.JsonTypeAndClassAnnotationIntrospector;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * @author Kaloyan Enimanev
 */
public class TestJsonServiceFactory
{
    private static final String OPENBIS_SERVER_URL = "http://localhost:8888/openbis/";

    private static final String GENERAL_INFO_SERVICE_URL = OPENBIS_SERVER_URL
            + IGeneralInformationService.JSON_SERVICE_URL;

    private static final String GENERAL_INFO_CHANGING_SERVICE_URL = OPENBIS_SERVER_URL
            + IGeneralInformationChangingService.JSON_SERVICE_URL;

    static IGeneralInformationService createGeneralInfoService()
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setAnnotationIntrospector(new JsonTypeAndClassAnnotationIntrospector(null));
            mapper.setSubtypeResolver(JsonReflectionsSubTypeResolver.getDefaultInstance());
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(mapper, new URL(GENERAL_INFO_SERVICE_URL),
                            new HashMap<String, String>());
            return ProxyUtil.createProxy(TestJsonServiceFactory.class.getClassLoader(),
                    IGeneralInformationService.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

    static IGeneralInformationChangingService createGeneralInfoChangingService()
    {
        try
        {
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(new URL(GENERAL_INFO_CHANGING_SERVICE_URL));
            return ProxyUtil.createProxy(TestJsonServiceFactory.class.getClassLoader(),
                    IGeneralInformationChangingService.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

}
