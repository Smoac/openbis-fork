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

package ch.systemsx.cisd.openbis.remoteapitest.api.v1;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.remoteapitest.RemoteApiTestCase;
import ch.systemsx.cisd.openbis.remoteapitest.api.v1.GeneralInformationServiceAbstractTestCases.IGeneralInformationServiceFactory;

/**
 * Verifies that an instance of {@link IGeneralInformationService} is published via JSON-RPC and
 * that it is correctly functioning with external clients.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups =
    { "remote api" })
public class GeneralInformationServiceJsonApiTest extends RemoteApiTestCase implements
        IGeneralInformationServiceFactory
{
    private static final String SERVICE_URL = "http://localhost:8888/openbis/"
            + IGeneralInformationService.JSON_SERVICE_URL;

    @Factory
    public Object[] createTestCases()
    {
        return new Object[]
            { new GeneralInformationServiceAbstractTestCases(this) };
    }

    public IGeneralInformationService createService()
    {
        try
        {
            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(SERVICE_URL));
            return ProxyUtil.createProxy(getClass().getClassLoader(),
                    IGeneralInformationService.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

}
