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

    /** Returns label for the 'rename' field. */
    @Key(value = "action.rename.label")
    public String getActionRenameLabel();

    /** Returns the label for the share action. */
    @Key(value = "action.shared.label")
    public String getActionSharedLabel();

    /** Returns the label for the Add User button. */
    @Key(value = "share.user.add.button.label")
    public String getAddUserButtonLabel();

    /** Returns the title for the Form to add user. */
    @Key(value = "share.user.add.title")
    public String getAddUserFormTitle();

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

    /** Returns the label of Java dowloader button title. */
    @Key(value = "downloadfiles.launchJavaDownloaderButtonTitle")
    public String getLaunchJavaDownloaderButtonTitle();

    /** Returns the text with pros of WebStart download client. */
    @Key(value = "downloadfiles.javaDownloaderPros")
    public String getJavaDownloaderPros();

    /** Returns the label of the field, to add new Email Adresses. */
    @Key(value = "share.email.field.label")
    public String getEmailFielLabel();

    /** Returns the title for the existing user table */
    @Key(value = "share.user.existing.title")
    public String getExistingUserTableTitle();

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

    /** Returns the label of the file owner column. */
    @Key(value = "file.owner.label")
    public String getFileOwnerLabel();

    /** Returns the label of the file "shared with" column. */
    @Key(value = "file.shared_with.label")
    public String getFileSharedWithLabel();

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

    /** Returns the title for the new user table */
    @Key(value = "share.user.new.label")
    public String getNewUserTableTitle();

    /** Returns the recipient text area field label. */
    @Key(value = "value.required.text")
    public String getValueRequiredText();

    /** Returns text for the 'rename' confirm box. */
    @Key(value = "rename.confirm.text")
    public String getRenameConfirmText(String before, String after);

    /** Returns title for the 'rename' confirm box. */
    @Key(value = "rename.confirm.title")
    public String getRenameConfirmTitle();

    /** Returns title for the 'rename' prompt box. */
    @Key(value = "rename.prompt.title")
    public String getRenamePromptTitle();

    /** Returns the share label. */
    @Key(value = "file.share.label")
    public String getShareLabel();

    /** Returns the label of the submit button of the Dialog */
    @Key(value = "share.submit.button.label")
    public String getShareSubmitDialogButtonLabel();

    /** Returns the content the confirm MessageBox, to delete an user. */
    @Key(value = "user.delete.confirm.text")
    public String getUserDeleteConfirmText(final String name);

    /** Returns the title for user deletion. */
    @Key(value = "user.delete.title")
    public String getUserDeleteTitle();

    /** Returns the email validation text */
    @Key(value = "value.email.text")
    public String getValueEmailText();

    /** Returns the user code validation text */
    @Key(value = "value.user.code.text")
    public String getValueUserCode();

}