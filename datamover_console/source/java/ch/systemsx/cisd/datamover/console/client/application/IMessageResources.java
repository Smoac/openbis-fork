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
    /** Returns a default invocation exception message (as it obviously does not contain any). */
    @Key("exception.invocation.message")
    public String getInvocationExceptionMessage();

    /**
     * Returns a default message for exception that does not contain any message.
     * <p>
     * The returned error message will contain the type of the exception thrown.
     * </p>
     */
    @Key("exception.without.message")
    public String getExceptionWithoutMessage(final String typeName);

    /** Returns the footer text. */
    @Key("footer.text")
    public String getFooterText(String version);
    
    /** Returns the header title. */
    @Key("header.title")
    public String getHeaderTitle();
    
    /** Returns welcome text that appears on the first page (login page). */
    @Key("login.welcome.text")
    public String getLoginWelcomeText();
    
    /** Returns legend of login field set. */
    @Key("login.legend")
    public String getLoginLegend();
    
    /** Returns the password label for the login form. */
    @Key("login.password.label")
    public String getLoginPasswordLabel();

    /** Returns the email label for the login form. */
    @Key("login.user.label")
    public String getLoginUserLabel();

    /** Returns the button label for the login form. */
    @Key("login.button.label")
    public String getLoginButtonLabel();

    /** Returns the text shown when login failed. */
    @Key("login.failed.message")
    public String getLoginFailedMessage();
    
    /** Returns the status line with the specified timestamp. */
    @Key("console.status.line")
    public String getConsoleStatusLine(Date timestamp);

    /** Returns the label of the logout button. */
    @Key("console.logout.button.label")
    public String getLogoutButtonLabel();
    
    /** Returns the wait message used when wait for server response. */
    @Key("console.wait.message")
    public String getConsoleWaitMessage();
    
    /** Returns the column header of datamover. */
    @Key("console.column.datamover")
    public String getDatamoverColumnHeader();
    
    /** Returns the column header of target location. */
    @Key("console.column.target.location")
    public String getTargetLocationColumnHeader();
    
    /** Returns the column header of status. */
    @Key("console.column.status")
    public String getStatusColumnHeader();
    
    /** Returns the column header of command. */
    @Key("console.column.command")
    public String getCommandColumnHeader();
    
    /** Returns the label of the start button. */
    @Key("console.start.button.label")
    public String getStartButtonLabel();
    
    /** Returns the label of the stop button. */
    @Key("console.stop.button.label")
    public String getStopButtonLabel();
    
    /** Returns the label of the refresh button. */
    @Key("console.refresh.button.label")
    public String getRefreshButtonLabel();
    
}
