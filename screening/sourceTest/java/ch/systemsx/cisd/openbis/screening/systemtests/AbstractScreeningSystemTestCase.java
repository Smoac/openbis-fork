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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.io.File;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.server.IAnalysisSettingSetter;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;

/**
 * System test case for screening. Starts both AS and DSS.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractScreeningSystemTestCase extends SystemTestCase
{
    protected IScreeningClientService screeningClientService;
    protected IScreeningServer screeningServer;
    protected IScreeningApiServer screeningApiServer;
    protected IAnalysisSettingSetter analysisSettingServer;
    protected String sessionToken;
    protected IScreeningOpenbisServiceFacade screeningFacade;
    protected ICommonServer commonServer;

    @BeforeMethod
    public void setUpServices()
    {
        commonServer =
                (ICommonServer) applicationContext
                        .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        screeningClientService =
                (IScreeningClientService) applicationContext
                        .getBean(ResourceNames.SCREENING_PLUGIN_SERVICE);
        ((SpringRequestContextProvider) applicationContext.getBean("request-context-provider"))
                .setRequest(new MockHttpServletRequest());
        Object bean = applicationContext.getBean(ResourceNames.SCREENING_PLUGIN_SERVER);
        screeningServer = (IScreeningServer) bean;
        screeningApiServer = (IScreeningApiServer) bean;
        analysisSettingServer = (IAnalysisSettingSetter) bean;
        sessionToken = screeningClientService.tryToLogin("admin", "a").getSessionID();
        screeningFacade =
                ScreeningOpenbisServiceFacade.tryCreateForTest(sessionToken,
                        TestInstanceHostUtils.getOpenBISUrl(), screeningApiServer);
    }

    /**
     * Return the location of the openBIS application context config.
     */
    @Override
    protected String getApplicationContextLocation()
    {
        return "classpath:screening-applicationContext.xml";
    }

    /**
     * sets up the openbis database to be used by the tests.
     */
    @Override
    protected void setUpDatabaseProperties()
    {
        TestInitializer.initEmptyDbWithIndex();
    }

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-" + getClass().getSimpleName());
    }

}
