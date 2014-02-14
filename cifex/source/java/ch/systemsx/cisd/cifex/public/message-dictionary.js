// All message keys should be in upper case.
var message_dict = {
  //
  // General
  //
  
  UNKNOWN_LABEL: 'unknown',
  
  //
  // Grid
  //
  
  GRID_FILTERS_LABEL: 'Filters',
  
  GRID_COLUMNS_LABEL: 'Columns',
  
  //
  // Help
  //
  
  HELP_CONTACT_SUPPORT_LABEL: 'Contact Support',
  
  HELP_DISCLAIMER_LABEL: 'Disclaimer',
  
  HELP_DISCLAIMER_TITLE: 'Disclaimer',
  
  HELP_FAQ_LABEL: 'FAQ',
  
  HELP_FAQ_TITLE: 'Frequently Asked Questions',
  
  HELP_MANUAL_LABEL: 'User Manual',
  
  HELP_TOOLS_LABEL: 'Tools',
  
  HELP_LINK_LABEL: 'Help',
  
  HELP_LINK_TOOLTIP: 'Information on how to use CIFEX',
  
  //
  // Exception
  //
  
  EXCEPTION_INVOCATION_MSG: 'Failed to contact service.',
  
  UNKNOWN_FAILURE_MSG: 'Unknown failure has occurred (ask support):<br>{0}',
  
  EXCEPTION_STATUS_CODE0: 'StatusCodeException with status code 0 &mdash; Is browser in offline mode? If so, turn off "Work Offline" mode.',
  
  UNKNOWN_USER_MSG: 'User [{0}] is not known to the system.',
  
  //
  // Message Box
  //
  
  MESSAGE_BOX_ERROR_TITLE: 'Error',
  
  MESSAGE_BOX_WARNING_TITLE: 'Warning',
  
  MESSAGE_BOX_INFO_TITLE: 'Information',
  
  //
  // Login
  //
  
  LOGIN_BUTTON_LABEL: 'Login',
  
  LOGIN_USER_LABEL: 'User',
  
  LOGIN_PASSWORD_LABEL: 'Password',
  
  LOGIN_FAILED_MSG: 'Sorry, you entered an invalid<br/>user or password. Please try again.',
  
  //
  // Logout
  //
  
  LOGOUT_LINK_LABEL: 'Logout',
  
  LOGOUT_LINK_TOOLTIP: 'Click here to end your session',
  
  //
  // Inbox View
  //

  INBOX_VIEW_LABEL: 'Inbox',

  //
  // Share View
  //

  SHARE_VIEW_LABEL: 'Share',
  
  //
  // Invite View
  //
  
  INVITE_VIEW_LABEL: 'Invite',
  
  //
  // Admin View
  //
  
  ADMIN_VIEW_LABEL: 'Admin',
  
  //
  // Profile
  //
  
  PROFILE_LINK_LABEL: 'Profile',
  
  PROFILE_LINK_TOOPTIP: 'Edit my profile',
  
  //
  // User
  //
  
  USER_ID_LABEL: 'User Name',
  
  USER_ACTIVE_LABEL: 'Active',
  
  //
  // Edit User
  //
  
  EDIT_USER_LABEL: 'Edit User',
  
  EDIT_USER_UPDATE_BUTTON_LABEL: 'Update',
  
  EDIT_USER_NOTIFY_LABEL: 'Notify User',
  
  EDIT_USER_EXTERNAL_AUTHENTICATION_LABEL: 'External Authentication',
  
  EDIT_USER_DIALOG_TITLE: 'Edit User > {0}',
  
  //
  // Create / Edit User
  //
  
  CREATE_USER_LABEL: 'Create Account',
  
  CREATE_TEMP_USER_LABEL: 'Create Temporary Account',
  
  CREATE_USER_PASSWD_MISMATCH_MSG: 'The two passwords do not match!',
  
  CREATE_USER_INVALID_DATA_MSG: 'Form contains invalid data.',
  
  CREATE_USER_COMMENT_LABEL: 'Comment',
  
  CREATE_USER_PASSWD_LABEL: 'Password',
  
  CREATE_USER_REPEAT_PASSWD_LABEL: 'Repeat Password',
  
  CREATE_USER_STATUS_LABEL: 'Status',
  
  CREATE_USER_ROLE_ADMIN_TAG: 'Admin',
  
  CREATE_USER_ROLE_REGULAR_TAG: 'Regular',
  
  CREATE_USER_ROLE_TEMP_TAG: 'Temp.',
  
  CREATE_USER_ROLE_ADMIN_LABEL: 'Admin',
  
  CREATE_USER_ROLE_REGULAR_LABEL: 'Regular User',
  
  CREATE_USER_ROLE_TEMP_LABEL: 'Temp. User until {0}',
  
  CREATE_USER_EXPIRATION_DATE_LABEL: 'Expiration Date',
  
  CREATE_USER_EXPIRATION_DATE_TOOLTIP: 'For temporary accounts, choose a date in the allowed range.',
  
  CREATE_USER_MAX_FILESIZE_LABEL: 'Max. size (all files) in MB',
  
  CREATE_USER_MAX_FILECOUNT_LABEL: 'Max. number of all files',
  
  CREATE_USER_FILE_RETENTION_LABEL: 'File Retention Time (in days)',
  
  CREATE_USER_USER_RETENTION_LABEL: 'User Retention Time (in days)',
  
  //
  // Delete User
  //
  
  DELETE_USER_MSGBOX_TITLE: 'Delete User',
  
  DELETE_USER_CONFIRM_LABEL: 'Are you sure you want<br/>to delete user [{0}]?',
  
  //
  // Rename User
  //
  
  RENAME_USER_MSGBOX_TITLE: 'Rename User',
  
  RENAME_USER_CONFIRM_MSGBOX_TITLE: 'Confirm Renaming',
  
  RENAME_USER_CONFIRM_LABEL: 'Are you sure you want to change<br/>\
    the user code of user [{0}] to [{1}]?',
  
  //
  // User List
  //
  
  LIST_USERS_GRID_TITLE: 'Users',
  
  LIST_OWNUSERS_GRID_TITLE: 'Users I have created',
  
  LIST_USERS_EMAIL_COLUMN_HEADER: 'Email',
  
  LIST_USERS_FULLNAME_COLUMN_HEADER: 'Full Name',
  
  LIST_USERS_STATUS_COLUMN_HEADER: 'Status',
  
  LIST_USERS_CREATOR_COLUMN_HEADER: 'Created by',
  
  LIST_USERS_FILESIZE_COLUMN_HEADER: 'File Size',
  
  LIST_USERS_FILECOUNT_COLUMN_HEADER: '# Files',
  
  LIST_USERS_QUOTASIZE_COLUMN_HEADER: 'Quota',
  
  LIST_USERS_QUOTACOUNT_COLUMN_HEADER: 'Quota #',
  
  LIST_USERS_LOADING_MSG: 'Loading Users...',
  
  LIST_USERS_EMPTY_MSG: 'No Users.',
  
  LIST_USERS_FILESHARING_EXISTING_GRID_TITLE: 'Existing Users',
  
  LIST_USERS_FILESHARING_NEW_GRID_TITLE: 'New Users',
  
  LIST_USERS_FILESHARING_SHAREFLAG_COLUMN_HEADER: 'Share',
  
  //
  // Edit File
  //
  
  EDIT_FILE_DIALOG_TITLE: 'Edit File > {0}',
  
  EDIT_FILE_EXPIRATION_DATE_LABEL: 'Expiration Date',
  
  //
  // Delete File
  //
  
  DELETE_FILE_MSGBOX_TITLE: 'Delete File',
  
  DELETE_FILE_CONFIRM_LABEL: 'Are you sure you want<br/>to delete file [{0}]?',
  
  //
  // Upload File 
  //
  
  UPLOAD_FILES_PANEL_TITLE: '<b>Upload Files</b>',
  
  UPLOAD_FILES_BROWSER_PANEL_TITLE: 'Upload Files via Browser (for files < 2GB &mdash; for larger files use the CIFEX Uploader)',
  
  UPLOAD_FILES_PERMANENT_USER_INFO: '<b>Notes:</b><br/><i>Limits:</i> You are using {1} of your <b>{0}</b>; \
    you are sharing {3} file(s) and may share up to <b>{2}</b> files.<br/>\
    <i>Recipients</i>: Temporary accounts will automatically be created.<br/>\
    <i>Security</i>: If you share confidential files, please have a look at the FAQ.<br/>',
  
  UPLOAD_FILES_TEMPORARY_USER_INFO: '<b>Notes:</b><br/><i>Limits:</i> You are using {1} of your <b>{0}</b>; \
    you are sharing {3} file(s) and may share up to <b>{2}</b> files.<br/>\
    <i>Recipients</i>: Only email addresses of existing accounts may be specified.<br/>\
    <i>Security</i>: If you share confidential files, please have a look at the FAQ.<br/>',
    
  UPLOAD_FILES_FILE_FIELD_LABEL: 'File {0}',
  
  UPLOAD_FILES_SUBMIT_BUTTON_LABEL: 'Upload',
  
  UPLOAD_FILES_RESET_BUTTON_LABEL: 'Reset',
  
  UPLOAD_FILES_VALIDATE_USERS_BUTTON_LABEL: 'Validate Users',
  
  UPLOAD_FILES_RECIPIENT_FIELD_LABEL: 'Recipients',
  
  UPLOAD_FILES_RECIPIENT_FIELD_TOOLTIP: 'Comma separated list of email addresses<br/><i>Example: john@smith.com, emma@smith.com</i>',
  
  UPLOAD_FILES_RECIPIENT_FIELD_INVALID_MSG: 'Only valid email addresses or user names with the prefix "id:" separated by commas and blanks are allowed here.',
  
  UPLOAD_FILES_DUPLICATES_MSGBOX_TITLE: 'Duplicated Filenames',
  
  UPLOAD_FILES_DUPLICATES_MSG: 'Some of the files requested selected for upload have the same names. Are you sure you want to upload all of them?',
  
  UPLOAD_FILE_FEEDBACK_MSGBOX_TITLE: 'File Upload',
  
  UPLOAD_FILE_FEEDBACK_MSG: 'Please wait...',
  
  UPLOAD_FILE_FEEDBACK_FILE_LABEL: 'File being read: <i>{0}</i>',
  
  UPLOAD_FILE_FEEDBACK_PROGRESS_LABEL: 'Bytes read: <i>{0} of {1}</i>',
  
  UPLOAD_FILE_FEEDBACK_TIME_REMAINING_LABEL: 'Time remaining: <i>{0}</i>',
  
  //
  // Download File
  //
  
  DOWNLOAD_FILES_PANEL_TITLE: 'Available Files',
  
  DOWNLOAD_FILES_LOADING_MSG: 'Loading Files Available for Download...',
  
  DOWNLOAD_FILES_EMPTY_MSG: 'No Files to Download.',
  
  //
  // Share File
  //
  
  SHARE_FILE_DIALOG_TITLE: 'File Sharing > {0}',
  
  SHARE_FILES_PANEL_TITLE: 'Share Files',
  
  SHARE_FILE_ADDUSER_PANEL_TITEL: 'Add User',
  
  SHARE_FILE_ADDUSER_BUTTON_LABEL: 'Add',
  
  SHARE_FILE_ADDUSER_EMAIL_LABEL: 'Email or Id',
  
  SHARE_FILE_SUBMIT_BUTTON_LABEL: 'Update File Share',
  
  //
  // File List
  //
  
  LIST_FILES_SHARED_TITLE: 'Shared Files',
  
  LIST_FILES_SHARED_LOADING_MSG: 'Loading Shared Files...',
  
  LIST_FILES_SHARED_EMPTY_MSG: 'No Shared Files.',
  
  LIST_FILES_TITLE: 'Files',
  
  LIST_FILES_LOADING_MSG: 'Loading Files...',
  
  LIST_FILES_EMPTY_MSG: 'No Files.',
  
  LIST_FILES_NAME_COLUMN_HEADER: 'File Name',
  
  LIST_FILES_COMMENT_COLUMN_HEADER: 'Comment',
  
  LIST_FILES_CONTENTTYPE_COLUMN_HEADER: 'Content Type',
  
  LIST_FILES_SIZE_COLUMN_HEADER: 'Size',
  
  LIST_FILES_COMPLETESIZE_COLUMN_HEADER: 'Complete Size',
  
  LIST_FILES_ISCOMPLETE_COLUMN_HEADER: 'Complete?',
  
  LIST_FILES_CRC32CHECKSUM_COLUMN_HEADER: 'CRC32',
  
  LIST_FILES_EXPIRATIONDATE_COLUMN_HEADER: 'Will be deleted at',
  
  LIST_FILES_REGISTRATIONDATE_COLUMN_HEADER: 'Uploaded at',
  
  LIST_FILES_OWNER_COLUMN_HEADER: 'Owned by',
  
  LIST_FILES_SHAREDWITH_COLUMN_HEADER: 'Shared with',
  
  LIST_FILES_UPLOADER_COLUMN_HEADER: 'Uploaded by',
  
  LIST_FILES_COMMENT_MSGBOX_TITLE: 'Comment Provided on File Upload',
  
  //
  // Action
  //
  
  LIST_USERSFILES_ACTIONS_COLUMN_HEADER: 'Actions',
  
  ACTION_DELETE_LABEL: 'Delete',
  
  ACTION_EDIT_LABEL: 'Edit',
  
  ACTION_EDITSHARING_LABEL: 'Edit sharing',
  
  ACTION_RENAME_LABEL: 'Rename',
  
  //
  // Dialog
  //
  
  DIALOG_CLOSE_BUTTON_LABEL: 'Close',
  
  //
  // Validation
  //
  
  VALIDATION_REQUIRED_BLANK_MSG: 'Required field cannot be left blank',
  
  VALIDATION_EMAIL_MSG: 'This field should be an email address in the format "user@domain.com" ',
  
  VALIDATION_WRONG_USERCODE_MSG: 'Wrong user code',
  
  VALIDATION_INVALID_USER_CODE_TITLE: 'Invalid user code',
  
  VALIDATION_INVALID_USER_CODE_MSG: 'User code must not be empty and must contain only allowed characters: letters, digits, \'_\', \'.\', \'-\', \'@\'. Note that whitespaces are not allowed.',
  
  // LAST LINE: KEEP IT AT THE END
  lastline: '' // we need a line without a comma
};
