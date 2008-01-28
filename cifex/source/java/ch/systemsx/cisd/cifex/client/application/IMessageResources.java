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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.i18n.client.Messages;

/**
 * Contains all the application labels.
 * 
 * @author Christian Ribeaud
 */
public interface IMessageResources extends Messages
{
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
     * Returns a default invocation exception message (as it obviously does not contain any).
     * 
     * @gwt.key exception.invocation.message
     */
    public String getInvocationExceptionMessage();

    /**
     * Returns the button label for the login form.
     * 
     * @gwt.key login.button.label
     */
    public String getLoginButtonLabel();

    /**
     * Returns message that you get when the login failed.
     * 
     * @gwt.key login.failed.message
     */
    public String getLoginFailedMessage();

    /**
     * Returns the login field set legend.
     * 
     * @gwt.key login.legend
     */
    public String getLoginLegend();

    /**
     * Returns the password label for the login form.
     * 
     * @gwt.key login.password.label
     */
    public String getLoginPasswordLabel();

    /**
     * Returns the username label for the login form.
     * 
     * @gwt.key login.username.label
     */
    public String getLoginUsernameLabel();

    /**
     * Returns the logout label for the logout link.
     * 
     * @gwt.key logout.link.label
     */
    public String getLogoutLinkLabel();

    /**
     * Returns the logout tooltip for the logout link.
     * 
     * @gwt.key logout.link.tooltip
     */
    public String getLogoutLinkTooltip();

    /**
     * Returns the title for error message box.
     * 
     * @gwt.key messagebox.error.title
     */
    public String getMessageBoxErrorTitle();

    /**
     * Returns the title for warning message box.
     * 
     * @gwt.key messagebox.warning.title
     */
    public String getMessageBoxWarningTitle();

    /**
     * Returns the title of the file upload field set.
     * 
     * @gwt.key fileupload.legend
     */
    public String getFileUploadLegend();

    /**
     * Returns the file upload field label.
     * 
     * @gwt.key fileupload.field.label
     */
    public String getFileUploadFieldLabel(final int index);

    /**
     * Returns the file upload submit button label.
     * 
     * @gwt.key fileupload.button.label
     */
    public String getFileUploadButtonLabel();

    /**
     * Returns the title of the recipient field set.
     * 
     * @gwt.key recipient.legend
     */
    public String getRecipientLegend();

    /**
     * Returns the recipient text area field label.
     * 
     * @gwt.key recipient.field.label
     */
    public String getRecipientFieldLabel();

    /**
     * Returns the upload file part title.
     * 
     * @gwt.key uploadfiles.part.title
     */
    public String getUploadFilesPartTitle();

    /**
     * Returns the help text for uploading files.
     * 
     * @gwt.key uploadfiles.help
     */
    public String getUploadFilesHelp();

    /**
     * Returns the download file part title.
     * 
     * @gwt.key downloadfiles.part.title
     */
    public String getDownloadFilesPartTitle();

    /**
     * Returns the message text for no file to download.
     * 
     * @gwt.key downloadfiles.empty
     */
    public String getDownloadFilesEmpty();

    /**
     * Returns the label of the file name column.
     * 
     * @gwt.key file.name.label
     */
    public String getFileNameLabel();

    /**
     * Returns the label of the file content type column.
     * 
     * @gwt.key file.contenttype.label
     */
    public String getFileContentTypeLabel();

    /**
     * Returns the label of the file size column.
     * 
     * @gwt.key file.size.label
     */
    public String getFileSizeLabel();

    /**
     * Returns the label of the file expiration date column.
     * 
     * @gwt.key file.expirationdate.label
     */
    public String getFileExpirationDateLabel();
}