/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.oai_pmh.systemtests;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * @author pkupczyk
 */
public class PublishLogicTest extends OAIPMHSystemTest
{

    private static final String GENERAL_INFORMATION_SERVICE_URL = TestInstanceHostUtils.getOpenBISUrl() + IGeneralInformationService.SERVICE_URL;

    private static final String DSS_SERVICE_RPC_GENERIC_URL = TestInstanceHostUtils.getDSSUrl() + "/datastore_server/rmi-dss-api-v1";

    private static final String USER_ID = "test";

    private static final String USER_PASSWORD = "password";

    private IGeneralInformationService generalInformationService;

    private IDssServiceRpcGeneric dssServiceRpcGeneric;

    private String sessionToken;

    @BeforeClass
    public void beforeClass() throws ParserConfigurationException, InterruptedException
    {
        generalInformationService = HttpInvokerUtils.createServiceStub(IGeneralInformationService.class, GENERAL_INFORMATION_SERVICE_URL, 5000);
        dssServiceRpcGeneric = HttpInvokerUtils.createServiceStub(IDssServiceRpcGeneric.class, DSS_SERVICE_RPC_GENERIC_URL, 5000);
        sessionToken = generalInformationService.tryToAuthenticateForAllServices(USER_ID, USER_PASSWORD);
    }

    @Test
    public void test()
    {
        QueryTableModel result =
                dssServiceRpcGeneric.createReportFromAggregationService(sessionToken, "publish-logic", new HashMap<String, Object>());
        System.out.println("RESULT: " + result);
    }

}