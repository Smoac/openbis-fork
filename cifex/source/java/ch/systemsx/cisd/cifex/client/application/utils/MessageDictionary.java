/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.utils;

import com.google.gwt.i18n.client.Dictionary;

/**
 * Allows to access messages defined in <code>message-dictionary.js</code>. Entries in this
 * dictionary can be changed at runtime.
 * 
 * @author Tomasz Pylak
 */
public enum MessageDictionary
{
    UNKNOWN_LABEL,

    GRID_FILTERS_LABEL,

    GRID_COLUMNS_LABEL,

    HELP_CONTACT_SUPPORT_LABEL,

    HELP_DISCLAIMER_LABEL,

    HELP_DISCLAIMER_TITLE,

    HELP_FAQ_LABEL,
    
    HELP_FAQ_TITLE,
    
    HELP_MANUAL_LABEL,
    
    HELP_TOOLS_LABEL,
    
    HELP_LINK_LABEL,
    
    HELP_LINK_TOOLTIP,
    
    EXCEPTION_INVOCATION_MSG,
    
    UNKNOWN_FAILURE_MSG,
    
    EXCEPTION_STATUS_CODE0,
    
    UNKNOWN_USER_MSG,
    
    MESSAGE_BOX_ERROR_TITLE,
    
    MESSAGE_BOX_WARNING_TITLE,
    
    MESSAGE_BOX_INFO_TITLE,
    
    LOGIN_BUTTON_LABEL,
    
    LOGIN_USER_LABEL,
    
    LOGIN_PASSWORD_LABEL,
    
    LOGIN_FAILED_MSG,
    
    LOGOUT_LINK_LABEL,
    
    LOGOUT_LINK_TOOLTIP,
    
    INBOX_VIEW_LABEL,
    
    SHARE_VIEW_LABEL,
    
    INVITE_VIEW_LABEL,
    
    ADMIN_VIEW_LABEL,
    
    PROFILE_LINK_LABEL,
    
    PROFILE_LINK_TOOPTIP,
    
    USER_ID_LABEL,
    
    USER_ACTIVE_LABEL,
    
    EDIT_USER_LABEL,
    
    EDIT_USER_UPDATE_BUTTON_LABEL,
    
    EDIT_USER_NOTIFY_LABEL,
    
    EDIT_USER_EXTERNAL_AUTHENTICATION_LABEL,
    
    EDIT_USER_DIALOG_TITLE,
    
    CREATE_USER_LABEL,
    
    CREATE_TEMP_USER_LABEL,
    
    CREATE_USER_PASSWD_MISMATCH_MSG,
    
    CREATE_USER_INVALID_DATA_MSG,
    
    CREATE_USER_COMMENT_LABEL,
    
    CREATE_USER_PASSWD_LABEL,
    
    CREATE_USER_REPEAT_PASSWD_LABEL,
    
    CREATE_USER_STATUS_LABEL,
    
    CREATE_USER_ROLE_ADMIN_TAG,
    
    CREATE_USER_ROLE_REGULAR_TAG,
    
    CREATE_USER_ROLE_TEMP_TAG,
    
    CREATE_USER_ROLE_ADMIN_LABEL,
    
    CREATE_USER_ROLE_REGULAR_LABEL,
    
    CREATE_USER_ROLE_TEMP_LABEL,
    
    CREATE_USER_EXPIRATION_DATE_LABEL,
    
    CREATE_USER_EXPIRATION_DATE_TOOLTIP,

    CREATE_USER_MAX_FILESIZE_LABEL,
    
    CREATE_USER_MAX_FILECOUNT_LABEL,
    
    CREATE_USER_FILE_RETENTION_LABEL,
    
    CREATE_USER_USER_RETENTION_LABEL,
    
    DELETE_USER_MSGBOX_TITLE,
    
    DELETE_USER_CONFIRM_LABEL,
    
    RENAME_USER_MSGBOX_TITLE,
    
    RENAME_USER_CONFIRM_MSGBOX_TITLE,
    
    RENAME_USER_CONFIRM_LABEL,
    
    LIST_USERS_GRID_TITLE,
    
    LIST_OWNUSERS_GRID_TITLE,
    
    LIST_USERS_EMAIL_COLUMN_HEADER,
    
    LIST_USERS_FULLNAME_COLUMN_HEADER,
    
    LIST_USERS_STATUS_COLUMN_HEADER,
    
    LIST_USERS_CREATOR_COLUMN_HEADER,
    
    LIST_USERS_FILESIZE_COLUMN_HEADER,
    
    LIST_USERS_FILECOUNT_COLUMN_HEADER,
    
    LIST_USERS_QUOTASIZE_COLUMN_HEADER,
    
    LIST_USERS_QUOTACOUNT_COLUMN_HEADER,
    
    LIST_USERS_LOADING_MSG,
    
    LIST_USERS_EMPTY_MSG,
    
    LIST_USERS_FILESHARING_EXISTING_GRID_TITLE,
   
    LIST_USERS_FILESHARING_NEW_GRID_TITLE,
    
    LIST_USERS_FILESHARING_SHAREFLAG_COLUMN_HEADER,
    
    EDIT_FILE_DIALOG_TITLE,
    
    EDIT_FILE_EXPIRATION_DATE_LABEL,
    
    DELETE_FILE_MSGBOX_TITLE,
    
    DELETE_FILE_CONFIRM_LABEL,
    
    UPLOAD_FILES_PANEL_TITLE,
    
    UPLOAD_FILES_BROWSER_PANEL_TITLE,
    
    UPLOAD_FILES_PERMANENT_USER_INFO,

    UPLOAD_FILES_TEMPORARY_USER_INFO,
    
    UPLOAD_FILES_FILE_FIELD_LABEL,
    
    UPLOAD_FILES_SUBMIT_BUTTON_LABEL,
    
    UPLOAD_FILES_RESET_BUTTON_LABEL,
    
    UPLOAD_FILES_VALIDATE_USERS_BUTTON_LABEL,
    
    UPLOAD_FILES_RECIPIENT_FIELD_LABEL,
    
    UPLOAD_FILES_RECIPIENT_FIELD_TOOLTIP,
    
    UPLOAD_FILES_RECIPIENT_FIELD_INVALID_MSG,
    
    UPLOAD_FILES_DUPLICATES_MSGBOX_TITLE,
    
    UPLOAD_FILES_DUPLICATES_MSG,
    
    UPLOAD_FILE_FEEDBACK_MSGBOX_TITLE,
    
    UPLOAD_FILE_FEEDBACK_MSG,
    
    UPLOAD_FILE_FEEDBACK_FILE_LABEL,
    
    UPLOAD_FILE_FEEDBACK_PROGRESS_LABEL,
    
    UPLOAD_FILE_FEEDBACK_TIME_REMAINING_LABEL,
    
    DOWNLOAD_FILES_PANEL_TITLE,
    
    DOWNLOAD_FILES_LOADING_MSG,
    
    DOWNLOAD_FILES_EMPTY_MSG,
    
    SHARE_FILE_DIALOG_TITLE,
    
    SHARE_FILES_PANEL_TITLE,
    
    SHARE_FILE_ADDUSER_PANEL_TITEL,
    
    SHARE_FILE_ADDUSER_BUTTON_LABEL,
    
    SHARE_FILE_ADDUSER_EMAIL_LABEL,
    
    SHARE_FILE_SUBMIT_BUTTON_LABEL,
    
    LIST_FILES_SHARED_TITLE,
    
    LIST_FILES_SHARED_LOADING_MSG,
    
    LIST_FILES_SHARED_EMPTY_MSG,
    
    LIST_FILES_TITLE,
    
    LIST_FILES_LOADING_MSG,
    
    LIST_FILES_EMPTY_MSG,
    
    LIST_FILES_NAME_COLUMN_HEADER,
    
    LIST_FILES_COMMENT_COLUMN_HEADER,
    
    LIST_FILES_CONTENTTYPE_COLUMN_HEADER,
    
    LIST_FILES_SIZE_COLUMN_HEADER,
    
    LIST_FILES_COMPLETESIZE_COLUMN_HEADER,
    
    LIST_FILES_ISCOMPLETE_COLUMN_HEADER,
    
    LIST_FILES_CRC32CHECKSUM_COLUMN_HEADER,
    
    LIST_FILES_EXPIRATIONDATE_COLUMN_HEADER,
    
    LIST_FILES_REGISTRATIONDATE_COLUMN_HEADER,
    
    LIST_FILES_OWNER_COLUMN_HEADER,
    
    LIST_FILES_SHAREDWITH_COLUMN_HEADER,
    
    LIST_FILES_UPLOADER_COLUMN_HEADER,
    
    LIST_FILES_COMMENT_MSGBOX_TITLE,
    
    LIST_USERSFILES_ACTIONS_COLUMN_HEADER,
    
    ACTION_DELETE_LABEL,
    
    ACTION_EDIT_LABEL,
    
    ACTION_EDITSHARING_LABEL,
    
    ACTION_RENAME_LABEL,
    
    DIALOG_CLOSE_BUTTON_LABEL,
    
    VALIDATION_REQUIRED_BLANK_MSG,
    
    VALIDATION_EMAIL_MSG,
    
    VALIDATION_WRONG_USERCODE_MSG,
    
    VALIDATION_INVALID_USER_CODE_TITLE,
    
    VALIDATION_INVALID_USER_CODE_MSG,
    
    ;
    
    private static final Dictionary MSG_DICT = Dictionary.getDictionary("message_dict");

    private static final String[] PLACEHOLDERS =
        { "{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}" };

    /**
     * Returns a localized message string from <code>message-dictionary.js</code>.
     */
    public static final String msg(MessageDictionary key)
    {
        return MSG_DICT.get(key.name());
    }

    /**
     * Returns a localized message string from <code>message-dictionary.js</code>.
     */
    public static final String msg(MessageDictionary key, Object... args)
    {
        assert args.length <= PLACEHOLDERS.length;

        String value = MSG_DICT.get(key.name());
        for (int i = 0; i < args.length; ++i)
        {
            value = value.replace(PLACEHOLDERS[i], args[i].toString());
        }
        return value;
    }

}
