/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.console.client.application;

import java.util.Date;

import com.google.gwt.i18n.client.Messages;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IMessageResources extends Messages
{
    /**
     * Returns a default invocation exception message (as it obviously does not contain any).
     * 
     * @gwt.key exception.invocation.message
     */
    public String getInvocationExceptionMessage();

    /**
     * Returns a default message for exception that does not contain any message.
     * <p>
     * The returned error message will contain the type of the exception thrown.
     * </p>
     * 
     * @gwt.key exception.without.message
     */
    public String getExceptionWithoutMessage(final String typeName);

    /**
     * Returns the footer text.
     * 
     * @gwt.key footer.text
     */
    public String getFooterText(String version);
    
    /**
     * Returns the header title.
     * 
     * @gwt.key header.title
     */
    public String getHeaderTitle();
    
    /**
     * Returns welcome text that appears on the first page (login page).
     * 
     * @gwt.key login.welcome.text
     */
    public String getLoginWelcomeText();
    
    /**
     * Returns the password label for the login form.
     * 
     * @gwt.key login.password.label
     */
    public String getLoginPasswordLabel();

    /**
     * Returns the email label for the login form.
     * 
     * @gwt.key login.user.label
     */
    public String getLoginUserLabel();

    /**
     * Returns the button label for the login form.
     * 
     * @gwt.key login.button.label
     */
    public String getLoginButtonLabel();

    /**
     * Returns the status line with the specified timestamp.
     * 
     * @gwt.key console.status.line
     */
    public String getConsoleStatusLine(Date timestamp);

}
