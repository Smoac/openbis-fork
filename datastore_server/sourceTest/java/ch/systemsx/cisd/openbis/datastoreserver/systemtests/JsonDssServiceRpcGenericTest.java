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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.api.json.GenericObjectMapper;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.util.TestInstanceHostUtils;

/**
 * Verifies that the functionality of {@link IDssServiceRpcGeneric} is accessible over JSON-RPC.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups =
    { "slow" })
public class JsonDssServiceRpcGenericTest extends SystemTestCase
{
    private static final String OPENBIS_URL = TestInstanceHostUtils.getOpenBISUrl()
            + IGeneralInformationService.JSON_SERVICE_URL;

    // TODO KE: put the suffix in a constant
    private static final String DSS_URL = TestInstanceHostUtils.getDSSUrl()
            + "/datastore_server/rmi-dss-api-v1.json";

    private IGeneralInformationService openbisService;

    private IDssServiceRpcGeneric dssRpcService;

    private String sessionToken;

    @BeforeClass
    public void beforeClass() throws IOException
    {
        openbisService = createOpenbisService();
        dssRpcService = createDssRpcService();

        sessionToken = openbisService.tryToAuthenticateForAllServices("test", "1");

        File resourceDir =
                new File("../datastore_server/resource/test-data/" + getClass().getSimpleName());
        FileUtils.copyDirectory(resourceDir, rootDir);
    }

    @AfterClass
    public void afterClass()
    {
        openbisService.logout(sessionToken);
    }

    @Test
    public void testListDataSetContents()
    {

        String validationScript = dssRpcService.getValidationScript(sessionToken, "HCS_IMAGE");
        System.out.println(validationScript);

        FileInfoDssDTO[] result =
                dssRpcService.listFilesForDataSet(sessionToken, "20081105092159111-1", "", true);

        for (FileInfoDssDTO fileInfo : result)
        {
            System.out.println(fileInfo);
        }

    }

    public static IGeneralInformationService createOpenbisService()
    {
        try
        {
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(new GenericObjectMapper(), new URL(OPENBIS_URL),
                            new HashMap<String, String>());
            return ProxyUtil.createProxy(JsonDssServiceRpcGenericTest.class.getClassLoader(),
                    IGeneralInformationService.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

    public static IDssServiceRpcGeneric createDssRpcService()
    {
        try
        {
            JsonRpcHttpClient client =
                    new JsonRpcHttpClient(new GenericObjectMapper(), new URL(DSS_URL),
                            new HashMap<String, String>());
            return ProxyUtil.createProxy(JsonDssServiceRpcGenericTest.class.getClassLoader(),
                    IDssServiceRpcGeneric.class, client);
        } catch (MalformedURLException ex)
        {
            throw new RuntimeException("Failed to initialize json-rpc client: " + ex.getMessage(),
                    ex);
        }
    }

}
