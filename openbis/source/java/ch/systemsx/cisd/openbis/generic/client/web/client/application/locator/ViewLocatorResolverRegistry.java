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

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * A class that dispatches view locators to handlers for processing (i.e., opening a view with the
 * parameters specified in the locator).
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public class ViewLocatorResolverRegistry
{

    // The handlers I know about
    private final ArrayList<IViewLocatorResolver> handlers = new ArrayList<IViewLocatorResolver>();

    /**
     * Add a handler to the list of handler I use.
     */
    public void registerHandler(IViewLocatorResolver handler)
    {
        handlers.add(handler);
    }

    /**
     * Try to find a handler for the locator and invoke it.
     * <p>
     * If no handler is found, no exception is thrown and no action takes place.
     * 
     * @exception UserFailureException Might be thrown by the handler.
     */
    public void handleLocator(ViewLocator locator) throws UserFailureException
    {
        for (IViewLocatorResolver handler : handlers)
        {
            if (handler.canHandleLocator(locator))
            {
                handler.resolve(locator);
                break;
            }
        }
    }

}
