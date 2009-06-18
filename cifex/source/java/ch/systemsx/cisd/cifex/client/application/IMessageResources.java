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
     * Returns the label for cancelling action.
     * 
     * @gwt.key action.cancel.label
     */
    public String getActionCancelLabel();

    /**
     * Returns the label for the delete action.
     * 
     * @gwt.key action.delete.label
     */
    public String getActionDeleteLabel();

    /**
     * Returns the label for the edit action.
     * 
     * @gwt.key action.edit.label
     */
    public String getActionEditLabel();

    /**
     * Returns the action label.
     * 
     * @gwt.key action.label
     */
    public String getActionLabel();

    /**
     * Returns the label for OK action.
     * 
     * @gwt.key action.ok.label
     */
    public String getActionOKLabel();

    /**
     * Returns label for the 'rename' field.
     * 
     * @gwt.key action.rename.label
     */
    public String getActionRenameLabel();

    /**
     * Returns the label for the renew action.
     * 
     * @gwt.key action.renew.label
     */
    public String getActionRenewLabel();

    /**
     * Returns the label for the share action.
     * 
     * @gwt.key action.shared.label
     */
    public String getActionSharedLabel();

    /**
     * Returns label for the 'stop sharing' field.
     * 
     * @gwt.key action.stop_sharing.label
     */
    public String getActionStopSharingLabel();

    /**
     * Returns the label for the Add User button.
     * 
     * @gwt.key share.user.add.button.label
     */
    public String getAddUserButtonLabel();

    /**
     * Returns the title for the Form to add user.
     * 
     * @gwt.key share.user.add.title
     */
    public String getAddUserFormTitle();

    /**
     * Returns the label for creating new User
     * 
     * @gwt.key usercreation.label.admin
     */
    public String getAdminCreateUserLabel();

    /**
     * Returns the role name for an administrator.
     * 
     * @gwt.key usercreation.role.admin
     */
    public String getAdminRoleName();

    /**
     * Label for the button for the admin view.
     * 
     * @gwt.key adminview.link.label
     */
    public String getAdminViewLinkLabel();

    /**
     * Label for the tooltip of button, for the admin view.
     * 
     * @gwt.key adminview.link.tooltip
     */
    public String getAdminViewTooltipLabel();

    /**
     * Returns <i>CISD</i> logo title.
     * 
     * @gwt.key cisd.logo.title
     */
    public String getCISDLogoTitle();

    /**
     * Returns label for the Comment field.
     * 
     * @gwt.key usercreation.comment.label
     */
    public String getCommentLabel();

    /**
     * Returns the label for creating new User
     * 
     * @gwt.key usercreation.label
     */
    public String getCreateUserLabel();

    /**
     * Returns a default invocation exception message (as it obviously does not contain any).
     * 
     * @gwt.key dialog.closebutton.label
     */
    public String getDialogCloseButtonLabel();

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
     * Returns label for the button of editing a user.
     * 
     * @gwt.key edituser.button.label
     */
    public String getEditUserButtonLabel();

    /**
     * Returns dialog window title for editing an user.
     * 
     * @gwt.key edituser.dialog.title
     */
    public String getEditUserDialogTitle(final String userCode);

    /**
     * Returns label for editing a user.
     * 
     * @gwt.key edituser.label
     */
    public String getEditUserLabel();

    /**
     * Label for the button, to edit the user
     * 
     * @gwt.key edituser.link.label
     */
    public String getEditUserLinkLabel();

    /**
     * Label for the tooltip of button, to edit the user
     * 
     * @gwt.key edituser.link.tooltip
     */
    public String getEditUserTooltipLabel();

    /**
     * Returns the label of the field, to add new Email Adresses.
     * 
     * @gwt.key share.email.field.label
     */
    public String getEmailFielLabel();

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
     * Returns the title for the existing user table
     * 
     * @gwt.key share.user.existing.title
     */
    public String getExistingUserTableTitle();

    /**
     * Returns the label for external authentication button.
     * 
     * @gwt.key external.authentication.button.tooltip
     */
    public String getExternalAuthenticationButtonTooltip();

    /**
     * Returns the explanation for external authentication.
     * 
     * @gwt.key external.authentication.explanation
     */
    public String getExternalAuthenticationExplanation();

    /**
     * Returns the message for external authentication failure.
     * 
     * @gwt.key external.authentication.fail
     */
    public String getExternalAuthenticationFail(final String errorMsg);

    /**
     * Returns external authentication label.
     * 
     * @gwt.key external.authentication.label
     */
    public String getExternalAuthenticationLabel();

    /**
     * Returns external authentication password button label.
     * 
     * @gwt.key external.authentication.password.label
     */
    public String getExternalAuthenticationPasswordLabel();

    /**
     * Returns the message for successful external authentication.
     * 
     * @gwt.key external.authentication.successful
     */
    public String getExternalAuthenticationSuccessful();

    /**
     * Returns the label of the comment name column.
     * 
     * @gwt.key file.comment.label
     */
    public String getFileCommentLabel();

    /**
     * Returns the title for showing the comment.
     * 
     * @gwt.key file.comment.title
     */
    public String getFileCommentTitle();

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
     * Returns the label of the file registration date column.
     * 
     * @gwt.key file.registrationdate.label
     */
    public String getFileRegistrationDateLabel();

    /**
     * Returns the label of the file registerer column.
     * 
     * @gwt.key file.registrator.label
     */
    public String getFileRegistratorLabel();

    /**
     * Returns the label of the file "shared with" column.
     * 
     * @gwt.key file.shared_with.label
     */
    public String getFileSharedWithLabel();

    /**
     * Returns the label of the button for adding a new user for file sharing..
     * 
     * @gwt.key filesharing.addButton.label
     */
    public String getFileSharingAddButtonLabel();

    /**
     * Returns the title of the add user dialog for file sharing.
     * 
     * @gwt.key filesharing.adduser.title
     */
    public String getFileSharingAddUserTitle();

    /**
     * Returns the title of the file sharing user dialog.
     * 
     * @gwt.key filesharing.title
     */
    public String getFileSharingTitle(String fileName);

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
     * Returns the file upload feedback 'byte read till now' label.
     * 
     * @gwt.key fileupload.feedback.bytes.label
     */
    public String getFileUploadFeedbackBytesLabel(final String bytesRead, final String contentLength);

    /**
     * Returns the file upload feedback 'file being read' label.
     * 
     * @gwt.key fileupload.feedback.file.label
     */
    public String getFileUploadFeedbackFileLabel(final String fileName);

    /**
     * Returns the file upload feedback window initializing message.
     * 
     * @gwt.key fileupload.feedback.message
     */
    public String getFileUploadFeedbackMessage();

    /**
     * Returns the file upload feedback 'time remaining' label.
     * 
     * @gwt.key fileupload.feedback.time.label
     */
    public String getFileUploadFeedbackTimeLabel(final String remainingTime);

    /**
     * Returns the file upload feedback window title.
     * 
     * @gwt.key fileupload.feedback.title
     */
    public String getFileUploadFeedbackTitle();

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
     * Returns application description.
     * 
     * @gwt.key footer.application.description
     */
    public String getFooterApplicationDescription();

    /**
     * Returns footer contact administrator label.
     * 
     * @gwt.key footer.contact.administrator.text
     */
    public String getFooterContactAdministrator();

    /**
     * Returns title of the disclaimer window dialog.
     * 
     * @gwt.key footer.disclaimer.dialog.title
     */
    public String getFooterDisclaimerDialogTitle();

    /**
     * Returns disclaimer link label.
     * 
     * @gwt.key footer.disclaimer.link.label
     */
    public String getFooterDisclaimerLinkLabel();

    /**
     * Returns title of the disclaimer window dialog.
     * 
     * @gwt.key footer.documentation.dialog.title
     */
    public String getFooterDocumentationDialogTitle();

    /**
     * Returns documentation link label.
     * 
     * @gwt.key footer.documentation.link.label
     */
    public String getFooterDocumentationLinkLabel();

    /**
     * Returns the 'powered by' label.
     * 
     * @gwt.key footer.powered.by.text
     */
    public String getFooterPoweredBy();

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
     * Returns the email label for the login form.
     * 
     * @gwt.key login.user.label
     */
    public String getLoginUserLabel();

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
     * Label for the button for the Main view.
     * 
     * @gwt.key mainview.link.label
     */
    public String getMainViewLinkLabel();

    /**
     * Label for the tooltip of button, for the main view.
     * 
     * @gwt.key mainview.link.tooltip
     */
    public String getMainViewTooltipLabel();

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
     * Returns the title for the new user table
     * 
     * @gwt.key share.user.new.label
     */
    public String getNewUserTableTitle();

    /**
     * Returns the title of Own User Table.
     * 
     * @gwt.key userlist.ownusers.part.title
     */
    public String getOwnUserTitle();

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
     * Returns text for the 'rename' confirm box.
     * 
     * @gwt.key rename.confirm.text
     */
    public String getRenameConfirmText(String before, String after);

    /**
     * Returns title for the 'rename' confirm box.
     * 
     * @gwt.key rename.confirm.title
     */
    public String getRenameConfirmTitle();

    /**
     * Returns title for the 'rename' prompt box.
     * 
     * @gwt.key rename.prompt.title
     */
    public String getRenamePromptTitle();

    /**
     * Returns label for the Comment field.
     * 
     * @gwt.key edituser.sendUpdateInformation.label
     */
    public String getSendUserUpdateInformationLabel();

    /**
     * Returns the share label.
     * 
     * @gwt.key file.share.label
     */

    public String getShareLabel();

    /**
     * Returns the error message when a user who should be added to the sharing users is not found.
     * 
     * @gwt.key share.submit.user.not.found
     */
    public String getShareSubmitUserNotFound(String userCode);
    
    /**
     * Returns the label of the submit button of the Dialog
     * 
     * @gwt.key share.submit.button.label
     */
    public String getShareSubmitDialogButtonLabel();

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
     * Returns unknown label.
     * 
     * @gwt.key unknown.label
     */
    public String getUnknownLabel();

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
     * Returns the help text for uploading files section upload.
     * 
     * @gwt.key uploadfiles.help.upload
     */
    public String getUploadFilesHelpUpload(String maxRequestUploadSize);

    /**
     * Returns the help text for uploading files section Java upload.
     * 
     * @gwt.key uploadfiles.help.javaUpload
     */
    public String getUploadFilesHelpJavaUpload(Object linkWebstart, Object linkCliDist);
    
    /**
     * Returns the name of Java uploader link.
     * 
     * @gwt.key uploadfiles.help.javaUploaderLink
     */
    public String getUploadFilesHelpJavaUploaderLink();
    
    /**
     * Returns the title of Java uploader link.
     * 
     * @gwt.key uploadfiles.help.javaUploaderTitle
     */
    public String getUploadFilesHelpJavaUploaderTitle();
    
    /**
     * Returns the name of the Command Line Client ZIP file.
     * 
     * @gwt.key uploadfiles.help.cliLink
     */
    public String getUploadFilesHelpCLILink();
    
    /**
     * Returns the title of Command Line Client link.
     * 
     * @gwt.key uploadfiles.help.cliTitle
     */
    public String getUploadFilesHelpCLITitle();
    
    /**
     * Returns the help text for uploading files section security.
     * 
     * @gwt.key uploadfiles.help.security
     */
    public String getUploadFilesHelpSecurity();
    
    /**
     * Returns the help text for uploading files section recipients (case permanent user).
     * 
     * @gwt.key uploadfiles.help.permanentUser
     */
    public String getUploadFilesHelpPermanentUser();
    
    /**
     * Returns the help text for uploading files section recipients (case temporary user).
     * 
     * @gwt.key uploadfiles.help.temporaryUser
     */
    public String getUploadFilesHelpTemporaryUser();

    /**
     * Returns the upload file part title.
     * 
     * @gwt.key uploadfiles.part.title
     */
    public String getUploadFilesPartTitle();

    /**
     * Returns the upload files part title section &lt; 2GB.
     * 
     * @gwt.key uploadfiles.part.title.less2GB
     */
    public String getUploadFilesPartTitleLess2GB();

    /**
     * Returns the upload files part title section &gt; 2GB.
     * 
     * @gwt.key uploadfiles.part.title.greater2GB
     */
    public String getUploadFilesPartTitleGreater2GB();
    
    /**
     * Returns label for the User Code field.
     * 
     * @gwt.key usercreation.usercode.label
     */
    public String getUserCodeLabel();

    /**
     * Returns the label of the field for the maximum upload file size in MB
     * 
     * @gwt.key usercreation.max.upload.size.label
     */
    public String getMaxUploadSizeLabel();
    
    /**
     * Returns the label of the field for file retention time
     * 
     * @gwt.key usercreation.file.retention
     */
    public String getFileRetention();
    
    /**
     * Returns the label the User Create Button
     * 
     * @gwt.key usercreation.button.label
     */
    public String getUserCreateButton();

    /**
     * Returns the content the confirm MessageBox, to delete an user.
     * 
     * @gwt.key user.delete.confirm.text
     */
    public String getUserDeleteConfirmText(final String name);

    /**
     * Returns the title for user deletion.
     * 
     * @gwt.key user.delete.title
     */
    public String getUserDeleteTitle();

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
     * Returns user status label.
     * 
     * @gwt.key user.status.label
     */
    public String getUserStatusLabel();
    
    /**
     * Returns user "Is Active" label.
     * 
     * @gwt.key usercreation.role.active
     */
    public String getUserActiveLabel();

    /**
     * Returns label for the validate Password field.
     * 
     * @gwt.key usercreation.password.validate.label
     */
    public String getValidatePasswordLabel();

    /**
     * Returns the label of the button to validate the users
     * 
     * @gwt.key fileupload.validate.button.label
     */
    public String getValidateUsersButtonLabel();

}