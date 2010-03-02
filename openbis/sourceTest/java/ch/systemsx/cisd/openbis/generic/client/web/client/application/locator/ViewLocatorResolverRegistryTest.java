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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ViewLocatorResolverRegistryTest extends AbstractGWTTestCase
{
    ViewLocatorResolverRegistry registry;

    @Override
    protected void setUpTest() throws Exception
    {
        client.onModuleLoad();
        IViewContext<ICommonClientServiceAsync> viewContext = client.tryToGetViewContext();
        registry = viewContext.getLocatorResolverRegistry();
    }

    public void testResolvePermlinkLocator()
    {
        ViewLocator locator = new ViewLocator("entity=SAMPLE&permId=20100104150239401-871");
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();
    }
    
    public void testResolveSearchLocator()
    {
        ViewLocator locator = new ViewLocator("searchEntity=SAMPLE&code=CL1");
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();
    }
}
