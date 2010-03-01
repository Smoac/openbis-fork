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

package ch.systemsx.cisd.openbis.generic.client.web.clientnonjs;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorHandlerRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorHandlerRegistry.AbstractViewLocatorHandler;

/**
 * A test of the ViewLocatorHandlerRegistry functionality from the Java side. The JavaScript side is
 * tested by
 * {@link ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorHandlerRegistryTest}
 * .
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ViewLocatorHandlerRegistryTest extends AssertJUnit
{

    private static class DummyViewLocatorHandler extends AbstractViewLocatorHandler
    {
        boolean wasCalled = false;

        public DummyViewLocatorHandler(String handledAction)
        {
            super(handledAction);
        }

        public void invoke(ViewLocator locator)
        {
            wasCalled = true;
        }
    }

    private ViewLocatorHandlerRegistry registry;

    @BeforeMethod
    public void setUp()
    {
        registry = new ViewLocatorHandlerRegistry();
    }

    @AfterMethod
    public void tearDown()
    {

    }

    @Test
    public void testHandlerLookup()
    {
        DummyViewLocatorHandler dummyHandler1 = new DummyViewLocatorHandler("ACTION1");
        DummyViewLocatorHandler dummyHandler2 = new DummyViewLocatorHandler("ACTION2");

        registry.registerHandler(dummyHandler1);
        registry.registerHandler(dummyHandler2);

        ViewLocator locator = new ViewLocator("ACTION=ACTION2");

        registry.handleLocator(locator);

        assertFalse(dummyHandler1.wasCalled);
        assertTrue(dummyHandler2.wasCalled);
    }
}
