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
public class ViewLocatorHandlerRegistry
{

    public static interface IViewLocatorHandler
    {
        /**
         * Test if this handler can handle the action and parameters specified in the locator.
         */
        public boolean canHandleLocator(ViewLocator locator);

        /**
         * Invoke the code to open the view specified by the locator. Implementations should try to
         * interpret the parameters to the best of their abilities and not fail unless absolutely
         * necessary (e.g., unnecessary parameters should be ignored).
         * 
         * @exception UserFailureException May be thrown if it is determined that some mandatory
         *                information is not specified.
         */
        public void invoke(ViewLocator locator) throws UserFailureException;
    }

    /**
     * Default implementation of the IViewLocatorHandler interface. Designed to be subclassed.
     * <p>
     * The default implementation is bound to one particular action. The method
     * {@link #canHandleLocator(ViewLocator)} returns true if the locator's action matches my
     * handledAction.
     * 
     * @author Chandrasekhar Ramakrishnan
     * @author Piotr Buczek
     */
    public static abstract class AbstractViewLocatorHandler implements IViewLocatorHandler
    {
        private final String handledAction;

        public AbstractViewLocatorHandler(String handledAction)
        {
            assert handledAction != null;
            this.handledAction = handledAction;
        }

        public boolean canHandleLocator(ViewLocator locator)
        {
            return handledAction.equals(locator.tryGetAction());
        }

        /**
         * Utility method that throws an exception with a standard error message if the required
         * paramter is not specified
         */
        protected void checkRequiredParameter(String valueOrNull, String parameter)
                throws UserFailureException
        {
            if (valueOrNull == null)
            {
                throw new UserFailureException("Missing URL parameter: " + parameter);
            }
        }
    }

    // All the handler I know about
    ArrayList<IViewLocatorHandler> handlers = new ArrayList<IViewLocatorHandler>();

    /**
     * Add a handler to the list of handler I use.
     */
    public void registerHandler(IViewLocatorHandler handler)
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
        for (IViewLocatorHandler handler : handlers)
        {
            if (handler.canHandleLocator(locator))
            {
                handler.invoke(locator);
                break;
            }
        }
    }

}
