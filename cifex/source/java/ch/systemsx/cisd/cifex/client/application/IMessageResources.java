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
     * Returns the label for the delete action.
     * 
     * @gwt.key action.delete.label
     */
    public String getActionDeleteLabel();

    /**
     * Returns the action label.
     * 
     * @gwt.key action.label
     */
    public String getActionLabel();

    /**
     * Returns the role name for an administrator.
     * 
     * @gwt.key usercreation.role.admin
     */
    public String getAdminRoleName();

    /**
     * Returns <i>CISD</i> logo title.
     * 
     * @gwt.key cisd.logo.title
     */
    public String getCISDLogoTitle();

    /**
     * Returns the label for creating new User
     * 
     * @gwt.key usercreation.label.admin
     */
    public String getAdminCreateUserLabel();

    /**
     * Returns the label for creating new User
     * 
     * @gwt.key usercreation.label
     */
    public String getCreateUserLabel();

    /**
     * Returns the message text for no file to download.
     * 
     * @gwt.key downloadfiles.empty
     */
    public String getDownloadFilesEmpty();

    /**
     * Returns the download file part title.
     * 
     * @gwt.key downloadfiles.part.title
     */
    public String getDownloadFilesPartTitle();

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
     * Returns the label of the file content type column.
     * 
     * @gwt.key file.contenttype.label
     */
    public String getFileContentTypeLabel();

    /**
     * Returns the content the confirm MessageBox, to delete a file.
     * 
     * @gwt.key file.delete.confirm.text
     */
    public String getFileDeleteConfirmText(final String name);

    /**
     * Returns the title for file deletion.
     * 
     * @gwt.key file.delete.title
     */
    public String getFileDeleteTitle();

    /**
     * Returns the label of the file expiration date column.
     * 
     * @gwt.key file.expirationdate.label
     */
    public String getFileExpirationDateLabel();

    /**
     * Returns the label of the file name column.
     * 
     * @gwt.key file.name.label
     */
    public String getFileNameLabel();

    /**
     * Returns the label of the file registerer column.
     * 
     * @gwt.key file.registrator.label
     */
    public String getFileRegistratorLabel();

    /**
     * Returns the label of the file registration date column.
     * 
     * @gwt.key file.registrationdate.label
     */
    public String getFileRegistrationDateLabel();

    /**
     * Returns the label of the file size column.
     * 
     * @gwt.key file.size.label
     */
    public String getFileSizeLabel();

    /**
     * Returns the label of the file registerer column.
     * 
     * @gwt.key files.part.title
     */
    public String getFilesPartTitle();

    /**
     * Returns the file upload submit button label.
     * 
     * @gwt.key fileupload.button.label
     */
    public String getFileUploadButtonLabel();

    /**
     * Returns the file upload field label.
     * 
     * @gwt.key fileupload.field.label
     */
    public String getFileUploadFieldLabel(final int index);

    /**
     * Returns the title of the file upload field set.
     * 
     * @gwt.key fileupload.legend
     */
    public String getFileUploadLegend();

    /**
     * Returns a default invocation exception message (as it obviously does not contain any).
     * 
     * @gwt.key exception.invocation.message
     */
    public String getInvocationExceptionMessage();

    /**
     * Returns the label for the login form that decides whether it is an admin login.
     * 
     * @gwt.key login.admin.label
     */
    public String getLoginAdminLabel();

    /**
     * Returns the button label for the login form.
     * 
     * @gwt.key login.button.label
     */
    public String getLoginButtonLabel();

    /**
     * Returns the message when obtaining the program configuration failed.
     * 
     * @gwt.key login.getconfig.failed.message
     */
    public String getLoginConfigFailedMessage();

    /**
     * Returns the email label for the login form.
     * 
     * @gwt.key login.email.label
     */
    public String getLoginEmailLabel();

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
     * Returns welcome text that appears on the first page (login page).
     * 
     * @gwt.key login.welcome.text
     */
    public String getLoginWelcomeText();

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
     * Returns the title for info message box.
     * 
     * @gwt.key messagebox.info.title
     */
    public String getMessageBoxInfoTitle();

    /**
     * Returns the title for warning message box.
     * 
     * @gwt.key messagebox.warning.title
     */
    public String getMessageBoxWarningTitle();

    /**
     * Returns label for the Password field.
     * 
     * @gwt.key usercreation.password.label
     */
    public String getPasswordLabel();

    /**
     * Returns the Message for thhe alert Box, if the 2 Password did not match.
     * 
     * @gwt.key usercreation.password.missmatch.message
     */
    public String getPasswordMissmatchMessage();

    /**
     * Returns the role name for an permanent user.
     * 
     * @gwt.key usercreation.role.permanent
     */
    public String getPermanentRoleName();

    /**
     * Returns the recipient text area field label.
     * 
     * @gwt.key recipient.field.invalid.text
     */
    public String getRecipientFieldInvalidText();

    /**
     * Returns the recipient text area field label.
     * 
     * @gwt.key recipient.field.label
     */
    public String getRecipientFieldLabel();

    /**
     * Returns the title of the recipient field set.
     * 
     * @gwt.key recipient.legend
     */
    public String getRecipientLegend();

    /**
     * Returns the label of the user Registrator column.
     * 
     * @gwt.key userlist.registrator.label
     */
    public String getRegistratorLabel();

    /**
     * Returns the label of the user Status column.
     * 
     * @gwt.key userlist.status.label
     */
    public String getStatusLabel();

    /**
     * Returns the role name for an permanent user.
     * 
     * @gwt.key usercreation.role.temporary
     */
    public String getTemporaryRoleName();

    /**
     * Returns the message text for no file uploaded.
     * 
     * @gwt.key uploadedfiles.empty
     */
    public String getUploadedFilesEmpty();

    /**
     * Returns the uploaded files part title.
     * 
     * @gwt.key uploadedfiles.part.title
     */
    public String getUploadedFilesPartTitle();

    /**
     * Returns the help text for uploading files.
     * 
     * @gwt.key uploadfiles.help.permanent
     */
    public String getUploadFilesHelpPermanentUser(String maxRequestUploadSize);

    /**
     * Returns the help text for uploading files.
     * 
     * @gwt.key uploadfiles.help.temporary
     */
    public String getUploadFilesHelpTemporaryUser(String maxRequestUploadSize);

    /**
     * Returns the upload file part title.
     * 
     * @gwt.key uploadfiles.part.title
     */
    public String getUploadFilesPartTitle();

    /**
     * Returns the label the User Create Button
     * 
     * @gwt.key usercreation.button.label
     */
    public String getUserCreateButton();

    /**
     * Returns message that creating a new user was successfull.
     * 
     * @gwt.key usercreation.success.message
     */
    public String getUserCreationSuccessMessage(String user);

    /**
     * Returns the content the confirm MessageBox, to delete an user.
     * 
     * @gwt.key user.delete.confirm.text
     */
    public String getUserDeleteConfirmText(final String name);

    /**
     * Returns the error message the user gets when it tries to delete himself.
     * 
     * @gwt.key user.delete.himself
     */
    public String getUserDeleteHimself();

    /**
     * Returns the title for user deletion.
     * 
     * @gwt.key user.delete.title
     */
    public String getUserDeleteTitle();

    /**
     * Returns the label of the user code column.
     * 
     * @gwt.key userlist.code.label
     */
    public String getUserCodeLabel();

    /**
     * Returns the label of the user email column.
     * 
     * @gwt.key userlist.email.label
     */
    public String getUserEmailLabel();

    /**
     * Returns the label of the user Full Name column.
     * 
     * @gwt.key userlist.fullname.label
     */
    public String getUserFullNameLabel();

    /**
     * Returns the User List part title.
     * 
     * @gwt.key userlist.part.title
     */
    public String getUsersPartTitle();

    /**
     * Returns label for the validate Password field.
     * 
     * @gwt.key usercreation.password.validate.label
     */
    public String getValidatePasswordLabel();

    /**
     * Returns application name.
     * 
     * @gwt.key footer.powered.by.text
     */
    public String getFooterPoweredBy();

    /**
     * Returns application name.
     * 
     * @gwt.key footer.contact.administrator.text
     */
    public String getFooterContactAdministrator(final String email);

}