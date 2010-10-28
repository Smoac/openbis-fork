/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;

/**
 * @author Franz-Josef Elmer
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public abstract class SystemTestCase extends AbstractTestNGSpringContextTests
{
    protected ICommonServerForInternalUse commonServer;

    protected ICommonClientService commonClientService;

    protected IGenericClientService genericClientService;

    protected MockHttpServletRequest request;

    @BeforeSuite
    public void beforeSuite()
    {
        TestInitializer.init();
    }

    /**
     * Sets a {@link MockHttpServletRequest} for the specified context provider
     */
    @Autowired
    public final void setRequestContextProvider(final SpringRequestContextProvider contextProvider)
    {
        request = new MockHttpServletRequest();
        contextProvider.setRequest(request);
    }

    /**
     * Sets <code>commonServer</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setCommonServer(final ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

    /**
     * Sets <code>commonClientService</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setCommonClientService(final ICommonClientService commonClientService)
    {
        this.commonClientService = commonClientService;
    }

    /**
     * Sets <code>genericClientService</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setGenericClientService(final IGenericClientService genericClientService)
    {
        this.genericClientService = genericClientService;
    }

    protected SessionContext logIntoCommonClientService()
    {
        SessionContext context = commonClientService.tryToLogin("test", "a");
        AssertJUnit.assertNotNull(context);
        return context;
    }

    protected void logOutFromCommonClientService()
    {
        commonClientService.logout(new DisplaySettings());
    }

    protected void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

}
