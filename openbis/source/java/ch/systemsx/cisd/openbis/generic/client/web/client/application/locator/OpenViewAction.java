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

import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Encapsulates code to run to open a view specified by a ViewLocator.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class OpenViewAction implements IDelegatedAction
{
    private final ViewLocatorHandlerRegistry registry;

    private final ViewLocator viewLocator;

    public OpenViewAction(ViewLocatorHandlerRegistry registry, ViewLocator viewLocator)
    {
        this.registry = registry;
        this.viewLocator = viewLocator;
    }

    public void execute()
    {
        openView();
    }

    /**
     * Opens the initial view and handles any user failure exceptions that may result in the
     * process.
     */
    protected void openView()
    {
        try
        {
            openViewUnderExceptionHandler();
        } catch (UserFailureException exception)
        {
            MessageBox.alert("Error", exception.getMessage(), null);
        }
    }

    /**
     * Opens an initial tab if a parameter is specified in URL.
     */
    private void openViewUnderExceptionHandler() throws UserFailureException
    {
        registry.handleLocator(viewLocator);
    }

    /**
     * A public version of openInitialTabUnderExceptionHandler() used by the test case.
     */
    public void openInitialTabUnderExceptionHandlerForTest() throws UserFailureException
    {
        openViewUnderExceptionHandler();
    }

}
