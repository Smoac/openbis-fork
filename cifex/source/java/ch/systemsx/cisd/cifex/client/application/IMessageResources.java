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
    /** Returns the label for the delete action. */
    @Key(value = "action.delete.label")
    public String getActionDeleteLabel();

    /** Returns the label for the edit action. */
    @Key(value = "action.edit.label")
    public String getActionEditLabel();

    /** Returns the action label. */
    @Key(value = "action.label")
    public String getActionLabel();

    /** Returns the label for OK action. */
    @Key(value = "action.ok.label")
    public String getActionOKLabel();

    /** Returns label for the 'rename' field. */
    @Key(value = "action.rename.label")
    public String getActionRenameLabel();

    /** Returns the label for the renew action. */
    @Key(value = "action.renew.label")
    public String getActionRenewLabel();

    /** Returns the label for the share action. */
    @Key(value = "action.shared.label")
    public String getActionSharedLabel();

    /** Returns label for the 'stop sharing' field. */
    @Key(value = "action.stop_sharing.label")
    public String getActionStopSharingLabel();

    /** Returns the label for the Add User button. */
    @Key(value = "share.user.add.button.label")
    public String getAddUserButtonLabel();

    /** Returns the title for the Form to add user. */
    @Key(value = "share.user.add.title")
    public String getAddUserFormTitle();

    /** Returns the label for creating new User */
    @Key(value = "usercreation.label.admin")
    public String getAdminCreateUserLabel();

    /** Returns the role name for an administrator. */
    @Key(value = "usercreation.role.admin")
    public String getAdminRoleName();

    /** Label for the button for the admin view. */
    @Key(value = "adminview.link.label")
    public String getAdminViewLinkLabel();

    /** Label for the tooltip of button, for the admin view. */
    @Key(value = "adminview.link.tooltip")
    public String getAdminViewTooltipLabel();

    /** Returns <i>CISD</i> logo title. */
    @Key(value = "cisd.logo.title")
    public String getCISDLogoTitle();

    /** Returns label for the Comment field. */
    @Key(value = "usercreation.comment.label")
    public String getCommentLabel();

    /** Returns the label for creating new User */
    @Key(value = "usercreation.label")
    public String getCreateUserLabel();

    /**
     * Returns a default invocation exception message (as it obviously does not contain any).
     */
    @Key(value = "dialog.closebutton.label")
    public String getDialogCloseButtonLabel();

    /** Returns the message text for downloadable files being loaded. */
    @Key(value = "downloadfiles.loading")
    public String getDownloadFilesLoading();

    /** Returns the message text for no file to download. */
    @Key(value = "downloadfiles.empty")
    public String getDownloadFilesEmpty();

    /** Returns the download file part title. */
    @Key(value = "downloadfiles.part.title")
    public String getDownloadFilesPartTitle();

    /** Returns the WebStart download client part title. */
    @Key(value = "downloadfiles.part.title.greater2GB")
    public String getDownloadFilesPartTitleGreater2GB();

    /** Returns the WebStart download client help text title. */
    @Key(value = "downloadfiles.help.javaDownloaderTitle")
    public String getDownloadFilesHelpJavaDownloaderTitle();

    /** Returns the WebStart download client help text link. */
    @Key(value = "downloadfiles.help.javaDownloaderLink")
    public String getDownloadFilesHelpJavaDownloaderLink();

    /** Returns the help text for downlading files section Java download. */
    @Key(value = "downloadfiles.help.javaDownload")
    public String getDownloadFilesHelpJavaDownload(Object linkWebstart);

    /** Returns the label of Java dowloader button title. */
    @Key(value = "downloadfiles.launchJavaDownloaderButtonTitle")
    public String getLaunchJavaDownloaderButtonTitle();

    /** Returns the text with pros of WebStart download client. */
    @Key(value = "downloadfiles.javaDownloaderPros")
    public String getJavaDownloaderPros();

    /** Returns label for the button of editing a user. */
    @Key(value = "edituser.button.label")
    public String getEditUserButtonLabel();

    /** Returns dialog window title for editing an user. */
    @Key(value = "edituser.dialog.title")
    public String getEditUserDialogTitle(final String userCode);

    /** Returns label for editing a user. */
    @Key(value = "edituser.label")
    public String getEditUserLabel();

    /** Label for the button, to edit the user */
    @Key(value = "edituser.link.label")
    public String getEditUserLinkLabel();

    /** Label for the tooltip of button, to edit the user */
    @Key(value = "edituser.link.tooltip")
    public String getEditUserTooltipLabel();

    /** Returns the label of the field, to add new Email Adresses. */
    @Key(value = "share.email.field.label")
    public String getEmailFielLabel();

    /**
     * Returns a default message for exception that does not contain any message. *
     * <p>
     * The returned error message will contain the type of the exception thrown. *
     * </p>
     */
    @Key(value = "exception.without.message")
    public String getExceptionWithoutMessage(final String typeName);

    /** Returns the title for the existing user table */
    @Key(value = "share.user.existing.title")
    public String getExistingUserTableTitle();

    /** Returns the label for external authentication button. */
    @Key(value = "external.authentication.button.tooltip")
    public String getExternalAuthenticationButtonTooltip();

    /** Returns the explanation for external authentication. */
    @Key(value = "external.authentication.explanation")
    public String getExternalAuthenticationExplanation();

    /** Returns the message for external authentication failure. */
    @Key(value = "external.authentication.fail")
    public String getExternalAuthenticationFail(final String errorMsg);

    /** Returns external authentication label. */
    @Key(value = "external.authentication.label")
    public String getExternalAuthenticationLabel();

    /** Returns external authentication password button label. */
    @Key(value = "external.authentication.password.label")
    public String getExternalAuthenticationPasswordLabel();

    /** Returns the message for successful external authentication. */
    @Key(value = "external.authentication.successful")
    public String getExternalAuthenticationSuccessful();

    /** Returns dialog window title for editing a file. */
    @Key(value = "editfile.dialog.title")
    public String getEditFileDialogTitle(final String fileName);

    /** Returns the label of the comment name column. */
    @Key(value = "file.comment.label")
    public String getFileCommentLabel();

    /** Returns the title for showing the comment. */
    @Key(value = "file.comment.title")
    public String getFileCommentTitle();

    /** Returns the label of the file content type column. */
    @Key(value = "file.contenttype.label")
    public String getFileContentTypeLabel();

    /** Returns the content the confirm MessageBox, to delete a file. */
    @Key(value = "file.delete.confirm.text")
    public String getFileDeleteConfirmText(final String name);

    /** Returns the title for file deletion. */
    @Key(value = "file.delete.title")
    public String getFileDeleteTitle();

    /** Returns the label of the file expiration date column. */
    @Key(value = "file.expirationdate.label")
    public String getFileExpirationDateLabel();

    /** Returns the label of the file name column. */
    @Key(value = "file.name.label")
    public String getFileNameLabel();

    /** Returns the label of the file registration date column. */
    @Key(value = "file.registrationdate.label")
    public String getFileRegistrationDateLabel();

    /** Returns the label of the file registrator column. */
    @Key(value = "file.registrator.label")
    public String getFileRegistratorLabel();

    /** Returns the label of the file owner column. */
    @Key(value = "file.owner.label")
    public String getFileOwnerLabel();

    /** Returns the label of the file "shared with" column. */
    @Key(value = "file.shared_with.label")
    public String getFileSharedWithLabel();

    /** Returns the label of the button for adding a new user for file sharing.. */
    @Key(value = "filesharing.addButton.label")
    public String getFileSharingAddButtonLabel();

    /** Returns the title of the add user dialog for file sharing. */
    @Key(value = "filesharing.adduser.title")
    public String getFileSharingAddUserTitle();

    /** Returns the title of the file sharing user dialog. */
    @Key(value = "filesharing.title")
    public String getFileSharingTitle(String fileName);

    /** Returns the label of the file size column. */
    @Key(value = "file.size.label")
    public String getFileSizeLabel();

    /** Returns the label of the complete file size column. */
    @Key(value = "file.complete_size.label")
    public String getCompleteFileSizeLabel();

    /** Returns the label of the is_complete column. */
    @Key(value = "file.is_complete.label")
    public String getIsCompleteLabel();

    /** Returns the label of the CRC32 checksum column. */
    @Key(value = "file.crc32_checksum.label")
    public String getCRC32ChecksumLabel();

    /** Returns the label of the files part of the admin page. */
    @Key(value = "files.part.title")
    public String getFilesPartTitle();

    /** Returns the message text for files being loaded. */
    @Key(value = "files.loading")
    public String getFilesLoading();

    /** Returns the message text for no file uploaded. */
    @Key(value = "files.empty")
    public String getFilesEmpty();

    /** Returns the file upload submit button label. */
    @Key(value = "fileupload.button.label")
    public String getFileUploadButtonLabel();

    /** Returns the file upload feedback 'byte read till now' label. */
    @Key(value = "fileupload.feedback.bytes.label")
    public String getFileUploadFeedbackBytesLabel(final String bytesRead, final String contentLength);

    /** Returns the file upload feedback 'file being read' label. */
    @Key(value = "fileupload.feedback.file.label")
    public String getFileUploadFeedbackFileLabel(final String fileName);

    /** Returns the file upload feedback window initializing message. */
    @Key(value = "fileupload.feedback.message")
    public String getFileUploadFeedbackMessage();

    /** Returns the file upload feedback 'time remaining' label. */
    @Key(value = "fileupload.feedback.time.label")
    public String getFileUploadFeedbackTimeLabel(final String remainingTime);

    /** Returns the file upload feedback window title. */
    @Key(value = "fileupload.feedback.title")
    public String getFileUploadFeedbackTitle();

    /** Returns the file upload field label. */
    @Key(value = "fileupload.field.label")
    public String getFileUploadFieldLabel(final int index);

    /** Returns the title of the file upload field set. */
    @Key(value = "fileupload.legend")
    public String getFileUploadLegend();

    /** Returns title for confirmation window when uploading files with duplicated names. */
    @Key(value = "fileupload.duplicates.title")
    public String getFileUploadDuplicatesTitle();

    /** Returns message for confirmation window when uploading files with duplicated names. */
    @Key(value = "fileupload.duplicates.msg")
    public String getFileUploadDuplicatesMsg();

    @Key(value = "header.webpage.link")
    public String getWebpageLink();

    /** Returns application description. */
    @Key(value = "footer.application.description")
    public String getFooterApplicationDescription();

    /** Returns footer contact administrator label. */
    @Key(value = "footer.contact.administrator.text")
    public String getFooterContactAdministrator();

    /** Returns title of the disclaimer window dialog. */
    @Key(value = "footer.disclaimer.dialog.title")
    public String getFooterDisclaimerDialogTitle();

    /** Returns disclaimer link label. */
    @Key(value = "footer.disclaimer.link.label")
    public String getFooterDisclaimerLinkLabel();

    /** Returns title of the disclaimer window dialog. */
    @Key(value = "footer.documentation.dialog.title")
    public String getFooterDocumentationDialogTitle();

    /** Returns documentation link label. */
    @Key(value = "footer.documentation.link.label")
    public String getFooterDocumentationLinkLabel();

    /** Returns the 'powered by' label. */
    @Key(value = "footer.powered.by.text")
    public String getFooterPoweredBy();

    /** Returns tools link label. */
    @Key(value = "help.tools.link.label")
    public String getHelpToolsLinkLabel();

    /** Returns documentation link label. */
    @Key(value = "help.documentation.link.label")
    public String getHelpDocumentationLinkLabel();

    /** Returns FAQ link label. */
    @Key(value = "help.faq.link.label")
    public String getHelpFAQLinkLabel();

    /**
     * Returns a default invocation exception message (as it obviously does not contain any).
     */
    @Key(value = "exception.invocation.message")
    public String getInvocationExceptionMessage();

    /**
     * Returns the label for the login form that decides whether it is an admin login.
     */
    @Key(value = "login.admin.label")
    public String getLoginAdminLabel();

    /** Returns the button label for the login form. */
    @Key(value = "login.button.label")
    public String getLoginButtonLabel();

    /** Returns message that you get when the login failed. */
    @Key(value = "login.failed.message")
    public String getLoginFailedMessage();

    /** Returns the login field set legend. */
    @Key(value = "login.legend")
    public String getLoginLegend();

    /** Returns the password label for the login form. */
    @Key(value = "login.password.label")
    public String getLoginPasswordLabel();

    /** Returns the email label for the login form. */
    @Key(value = "login.user.label")
    public String getLoginUserLabel();

    /** Returns welcome text that appears on the first page (login page). */
    @Key(value = "login.welcome.text")
    public String getLoginWelcomeText();

    /** Returns the logout label for the logout link. */
    @Key(value = "logout.link.label")
    public String getLogoutLinkLabel();

    /** Returns the logout tooltip for the logout link. */
    @Key(value = "logout.link.tooltip")
    public String getLogoutLinkTooltip();

    /** Label for the button for the inbox view. */
    @Key(value = "inboxview.link.label")
    public String getInboxViewLinkLabel();

    /** Label for the tooltip of button, for the inbox view. */
    @Key(value = "inboxview.link.tooltip")
    public String getInboxViewTooltipLabel();

    /** Label for the button for the share view. */
    @Key(value = "shareview.link.label")
    public String getShareViewLinkLabel();

    /** Label for the tooltip of button, for the share view. */
    @Key(value = "shareview.link.tooltip")
    public String getShareViewTooltipLabel();

    /** Label for the button for the invite view. */
    @Key(value = "inviteview.link.label")
    public String getInviteViewLinkLabel();

    /** Label for the tooltip of button, for the invite view. */
    @Key(value = "inviteview.link.tooltip")
    public String getInviteViewTooltipLabel();

    /** Label for the button for the invite view. */
    @Key(value = "help.link.label")
    public String getHelpPageLinkLabel();

    /** Label for the tooltip of button, for the invite view. */
    @Key(value = "help.link.tooltip")
    public String getHelpPageTooltipLabel();

    /** Label for the button for the Main view. */
    @Key(value = "mainview.link.label")
    public String getMainViewLinkLabel();

    /** Label for the tooltip of button, for the main view. */
    @Key(value = "mainview.link.tooltip")
    public String getMainViewTooltipLabel();

    /** Returns the title for error message box. */
    @Key(value = "messagebox.error.title")
    public String getMessageBoxErrorTitle();

    /** Returns the title for info message box. */
    @Key(value = "messagebox.info.title")
    public String getMessageBoxInfoTitle();

    /** Returns the title for warning message box. */
    @Key(value = "messagebox.warning.title")
    public String getMessageBoxWarningTitle();

    /** Returns the title for the new user table */
    @Key(value = "share.user.new.label")
    public String getNewUserTableTitle();

    /** Returns the title of Own User Table. */
    @Key(value = "userlist.ownusers.part.title")
    public String getOwnUserTitle();

    /** Returns label for the Password field. */
    @Key(value = "usercreation.password.label")
    public String getPasswordLabel();

    /** Returns the Message for thhe alert Box, if the 2 Password did not match. */
    @Key(value = "usercreation.password.missmatch.message")
    public String getPasswordMissmatchMessage();

    /** Returns the role name for an permanent user. */
    @Key(value = "usercreation.role.permanent")
    public String getPermanentRoleName();

    /** Returns the recipient text area field label. */
    @Key(value = "recipient.field.invalid.text")
    public String getRecipientFieldInvalidText();

    /** Returns the recipient text area field label. */
    @Key(value = "value.required.text")
    public String getValueRequiredText();

    /** Returns the recipient text area field label. */
    @Key(value = "recipient.field.label")
    public String getRecipientFieldLabel();

    /** Returns the title of the recipient field set. */
    @Key(value = "recipient.legend")
    public String getRecipientLegend();

    /** Returns the tooltip of the recipient field set. */
    @Key(value = "recipient.tooltip")
    public String getRecipientFieldToolTip();

    /** Returns the label of the user Registrator column. */
    @Key(value = "userlist.registrator.label")
    public String getRegistratorLabel();

    /** Returns the label of the user "Total File Size" column. */
    @Key(value = "userlist.total-filesize.label")
    public String getTotalFileSizeLabel();

    /** Returns the label of the user "Total File Count" column. */
    @Key(value = "userlist.total-filecount.label")
    public String getTotalFileCountLabel();

    /** Returns the label of the user "Custom Quota Size" column. */
    @Key(value = "userlist.custom-quota-size.label")
    public String getQuotaSizeLabel();

    /** Returns the label of the user "Total File Count" column. */
    @Key(value = "userlist.custom-quota-count.label")
    public String getQuotaCountLabel();

    /** Returns text for the 'rename' confirm box. */
    @Key(value = "rename.confirm.text")
    public String getRenameConfirmText(String before, String after);

    /** Returns title for the 'rename' confirm box. */
    @Key(value = "rename.confirm.title")
    public String getRenameConfirmTitle();

    /** Returns title for the 'rename' prompt box. */
    @Key(value = "rename.prompt.title")
    public String getRenamePromptTitle();

    /** Returns label for the Comment field. */
    @Key(value = "edituser.sendUpdateInformation.label")
    public String getSendUserUpdateInformationLabel();

    /** Returns the share label. */
    @Key(value = "file.share.label")
    public String getShareLabel();

    /**
     * Returns the error message when a user with given <var>userCode</var> is not found.
     */
    @Key(value = "user.not.found")
    public String getUserNotFound(String userCode);

    /**
     * Returns the error message when a file with given <var>fileName</var> is not found.
     */
    @Key(value = "file.not.found")
    public String getFileNotFound(String fileName);

    /** Returns the label of the submit button of the Dialog */
    @Key(value = "share.submit.button.label")
    public String getShareSubmitDialogButtonLabel();

    /** Returns the label of the user Status column. */
    @Key(value = "userlist.status.label")
    public String getStatusLabel();

    /** Returns the role name for an permanent user. */
    @Key(value = "usercreation.role.temporary")
    public String getTemporaryRoleName();

    /** Returns unknown label. */
    @Key(value = "unknown.label")
    public String getUnknownLabel();

    /** Returns the message text for shared files being loaded. */
    @Key(value = "uploadedfiles.loading")
    public String getSharedFilesLoading();

    /** Returns the message text for no file uploaded. */
    @Key(value = "uploadedfiles.empty")
    public String getSharedFilesEmpty();

    /** Returns the uploaded files part title. */
    @Key(value = "uploadedfiles.part.title")
    public String getSharedFilesPartTitle();

    /** Returns the help text for uploading files section upload. */
    @Key(value = "uploadfiles.help.upload")
    public String getSharedFilesHelpUpload(String maxFileSizePerQuotaGroup, String currentFileSize,
            String maxFileCountPerQuotaGroup, int currentFileCount);

    /** Returns the help text for uploading files section Java upload. */
    @Key(value = "uploadfiles.help.javaUpload")
    public String getUploadFilesHelpJavaUpload(Object linkWebstart, Object linkCliDist);

    /** Returns the name of Java uploader link. */
    @Key(value = "uploadfiles.help.javaUploaderLink")
    public String getUploadFilesHelpJavaUploaderLink();

    /** Returns the title of Java uploader link. */
    @Key(value = "uploadfiles.help.javaUploaderTitle")
    public String getUploadFilesHelpJavaUploaderTitle();

    /** Returns the label of Java uploader button title. */
    @Key(value = "uploadfiles.launchJavaUploaderButtonTitle")
    public String getLaunchJavaUploaderButtonTitle();

    /** Returns the text with pros of WebStart upload client. */
    @Key(value = "uploadfiles.javaUploaderPros")
    public String getJavaUploaderPros();

    /** Returns the name of the Command Line Client ZIP file. */
    @Key(value = "uploadfiles.help.cliLink")
    public String getUploadFilesHelpCLILink();

    /** Returns the title of Command Line Client link. */
    @Key(value = "uploadfiles.help.cliTitle")
    public String getUploadFilesHelpCLITitle();

    /** Returns the help text for uploading files section security. */
    @Key(value = "uploadfiles.help.security")
    public String getUploadFilesHelpSecurity();

    /**
     * Returns the help text for uploading files section recipients (case permanent user).
     */
    @Key(value = "uploadfiles.help.permanentUser")
    public String getUploadFilesHelpPermanentUser();

    /**
     * Returns the help text for uploading files section recipients (case temporary user).
     */
    @Key(value = "uploadfiles.help.temporaryUser")
    public String getUploadFilesHelpTemporaryUser();

    /** Returns the upload file part title. */
    @Key(value = "uploadfiles.part.title")
    public String getUploadFilesPartTitle();

    /** Returns the upload files part title section &lt; 2GB. */
    @Key(value = "uploadfiles.part.title.less2GB")
    public String getUploadFilesPartTitleLess2GB();

    /** Returns the upload files part title section &gt; 2GB. */
    @Key(value = "uploadfiles.part.title.greater2GB")
    public String getUploadFilesPartTitleGreater2GB();

    /** Returns label for the User Code field. */
    @Key(value = "usercreation.usercode.label")
    public String getUserCodeLabel();

    /** Returns the label of the field for the maximum size of all files in MB */
    @Key(value = "usercreation.max.file.size.label")
    public String getMaxFileSizeLabel();

    /** Returns the label of the field for the maximum number of all files in MB */
    @Key(value = "usercreation.max.file.count.label")
    public String getMaxFileCountLabel();

    /** Returns the label of the field for file retention time */
    @Key(value = "usercreation.file.retention")
    public String getFileRetention();

    /** Returns the label of the field for user retention time */
    @Key(value = "usercreation.user.retention")
    public String getUserRetention();

    /** Returns the label the User Create Button */
    @Key(value = "usercreation.button.label")
    public String getUserCreateButton();

    /** Returns the content the confirm MessageBox, to delete an user. */
    @Key(value = "user.delete.confirm.text")
    public String getUserDeleteConfirmText(final String name);

    /** Returns the title for user deletion. */
    @Key(value = "user.delete.title")
    public String getUserDeleteTitle();

    /** Returns the label of the user email column. */
    @Key(value = "userlist.email.label")
    public String getUserEmailLabel();

    /** Returns the label of the user Full Name column. */
    @Key(value = "userlist.fullname.label")
    public String getUserFullNameLabel();

    /** Returns the User List part title. */
    @Key(value = "userlist.part.title")
    public String getUsersPartTitle();

    /** Returns the message that users are being loaded. */
    @Key(value = "userlist.loading")
    public String getUsersLoading();

    /** Returns the message that the users list is empty. */
    @Key(value = "userlist.empty")
    public String getUsersEmpty();

    /** Returns user status label. */
    @Key(value = "user.status.label")
    public String getUserStatusLabel();

    /** Returns user "Is Active" label. */
    @Key(value = "usercreation.role.active")
    public String getUserActiveLabel();

    /** Returns label for the validate Password field. */
    @Key(value = "usercreation.password.validate.label")
    public String getValidatePasswordLabel();

    /** Returns the label of the button to validate the users */
    @Key(value = "fileupload.validate.button.label")
    public String getValidateUsersButtonLabel();

    /** Returns the email validation text */
    @Key(value = "value.email.text")
    public String getValueEmailText();

    /** Returns the user code validation text */
    @Key(value = "value.user.code.text")
    public String getValueUserCode();

    /** Returns the grid filters label */
    @Key(value = "grid.filters")
    public String getGridFiltersLabel();

    /** Returns the expiration date for user / file creation editing */
    @Key(value = "userfile.expiration.date")
    public String getExpirationDateLabel();

    /** Returns the title of Java Web Start Applications button. */
    @Key(value = "launchJWSApplicationTitle")
    public String getLaunchJWSApplicationTitle();

    /** Returns the title of the reset button */
    @Key(value = "reset.button.title")
    public String getResetButtonTitle();
}