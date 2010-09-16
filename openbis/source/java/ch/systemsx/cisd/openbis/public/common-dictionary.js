// Common dictionary
var common = {

  applicationName: "openBIS",
  openbis_instance: "",
  footer: "openBIS (Version {0})",
  openbis_logo_title: "openBIS",
  
  //
  // Common Labels
  // 
  
  attachment: "Attachment",
  code: "Code",
  ordinal: "Ordinal",
  position_after: "Position After",
  section: "Section",
  file: "File",
  perm_id: "PermID",
  registrator: "Registrator",
  registration_date: "Registration Date",
  filter: "Filter",
  filters: "Filters",
  not_implemented: "Sorry, feature has not been implemented yet!",
  data_set: "Data Set",
  edit: "Edit",
  material: "Material",
  load_in_progress: "Loading...",
  details_title: "{0} {1}",
  edit_title: "Edit {0} {1}",
  permlink_column_name: "Permlink",
  show_details_link_column_name: "Show Details Link",
  show_details_link_text_value: "Permlink",
  table_operations: "Table:",
  entity_operations: "Entity:",
  url: "URL",
  reason: "Reason",
  delete_confirmation_message_with_reason: "You are deleting {0}(s).<br><br>Please enter a reason or cancel deletion.",
  add_attachment: "Add attachment...",
  data_view: "Data View",
  main_data_set_path: "Main Data Set Path",
  main_data_set_path_tooltip: "The path (relative to the top directory of a data set) that will be used as a starting point of 'main data set' lookup.\nExample: 'original/images/'",
  main_data_set_path_example: "for example: original/images/",
  main_data_set_pattern: "Main Data Set Pattern",
  main_data_set_pattern_tooltip: "If there is just one data set with path matching chosen 'main data set' pattern, it will be automatically displayed.\nA Regular Expression is expected. Example: '.*\.jpg'",
  main_data_set_pattern_example: "for example: .*\.jpg",
	auto_resolve_label: "Smart View",
	data_report_label: "Report:",
	explore_label: "Explore:",
	  
  //
  // Field
  //
  
  combobox_empty: "- No {0} found -",
  combobox_choose: "Choose {0}...",
  combo_box_expected_value_from_the_list: "Value from the list required",
  invalid_code_message: "Code contains invalid characters. Allowed characters are: {0}.",
  invalid_term_code_message: "{0} contains invalid characters. Allowed characters are: letters, numbers, hyphen (\"-\"), underscore (\"_\"), colon (\":\") and dot (\".\").",
  file_template_label: "Download file template",
 
  //
  // MessageBox
  //
  
  messagebox_error: "Error",
  messagebox_warning: "Warning",
  messagebox_info: "Info",
  
	//
  // Buttons
  //
  
  button_add: "Add {0}",
  button_save: "Save",
  button_choose: "Choose",
  button_cancel: "Cancel",
  button_reset: "Clear",
  button_submit: "Submit",
  button_refresh: "Refresh",
  button_show: "Show",
  button_exportTable: "Export",
  button_show_details: "Show Details",
  button_browse: "Browse",
  button_view: "View",
  button_delete: "Delete",
  button_configure: "Settings",  
  button_filters: "Filters",  
  button_upload_data_via_cifex: "Upload Data",
  button_process: "Process",
  button_top: "Files",
  
  tooltip_refresh_disabled: "To activate select appropriate criteria first.",
  tooltip_refresh_enabled: "Load or update the table.",
  tooltip_export_enabled: "Export the table visible on the screen to an Excel file.",
  tooltip_export_disabled: "Refresh the data before exporting them.",
  tooltip_config_enabled: "Choose the columns.",
  tooltip_config_disabled: "Load the data to activate this option.",
  tooltip_view_dataset: "View data set in Data Set Download Service.",
  
  tooltip_vocabulary_managed_internally: "This operation is not available for a vocabulary that is managed internally.",
  
  //
  // LoginWidget
  //
  
  login_invitation: "Please login to start your session:", 
  login_userLabel: "User",
  login_passwordLabel: "Password",
  login_buttonLabel: "Login",
  login_failed: "Sorry, you entered an invalid username or password. Please try again.",
  
  //
  // AbstractAsyncCallback
  //
  
  exception_invocationMessage: "Failed to contact the server. Please try again later or contact your administrator.", 
  exception_withoutMessage: "Unexpected error has occurred, please contact your administrator:<br>{0}",
  
  //
  // Header
  //
  
  header_userWithoutHomegroup: "{0}",
  header_userWithHomegroup: "{0} ({1})",
  
  //
  // Help Info
  //

  info_button_tooltip: "Displays short help information about the application.",
  info_box_title: "OpenBIS Help",
  info_box_msg: "There is an online help under construction for tabs and dialog windows opened in openBIS.<br/><br/>To get help information about specific:<li><b>tab</b> - right click on an opened tab's header and select <i>Help</i> from its context menu,<li><b>dialog window</b> - click on <i>?</i> icon visible in window's header in the right corner.",
  
  //
  // Search
  //
  
  search_button: "Search",
  global_search: "[{0}]: '{1}'",
  identifier: "Identifier",
  no_match: "No results found for '{0}'.<br><br><br>Note, that you are {1} using wildcard search mode. Try to turn it {2} in your user settings.",
  entity_type: "Entity Type",
  entity_kind: "Entity Kind",
  matching_text: "Matching Text",
  matching_field: "Matching Field",
  too_generic: "Query string '{0}' is too generic.",
  show_related_datasets: "Show Related Data Sets",
  show_related_datasets_message: "Select between showing Data Sets related to selected ({0}) entities or to all entities from the table and click on OK button.",
  show_related_datasets_radio_label: "Related entities",
  
  //
  // Sample Browser
  //
  
  subcode: "Subcode",
  sample: "Sample",
  sample_type: "Sample Type",
  database_instance: "Database Instance",
  sample_identifier: "Identifier",
  is_instance_sample: "Shared?",
  is_invalid: "Invalid?",
  group: "Space",
  groups: "Spaces",
  project: "Project",
  experiment: "Experiment",
  experiment_identifier: "Experiment Identifier",
  generated_from: "Parent {0}",
  part_of: "Container",
  invalidation: "Invalidation",
  invalidation_template: "Invalidated by: {0}<br>Invalidation Date: {1}<br>Invalidation Reason: {2}",
  
  //
  // Experiment Browser
  //
  
  experiments_grid_header: "Experiments",
  experiment_type: "Experiment Type",
  project_selector_title: "Project Selection",
  project_selector_tooltip: "Click on a row with project code to select it.",
  project_selector_description_not_available: "(not available)",
  project_selector_code_column: "Space / Project",
  project_selector_details_link_label: "(info)",
  project_selector_details_link_tooltip: "Click on the link to see project details.",
  
  
  //
  // DataSet Browser
  //
  
  children_datasets: "Children",
  no_datasets_selected: "No Data Sets were selected.",
  datasets_from_different_stores_selected: "Data Sets from different Data Stores were selected, so no operation can be performed on all of them.",
  perform_computation_on_all_datasets_msg_template: "{0} Select a data store service to perform {1} computation on all Data Sets from the table that have type and data store appropriate to the selected service and click on a Run button.",
  perform_computation_on_selected_or_all_datasets_msg_template: "Select between performing {0} computation only on selected Data Sets ({1}) or on all Data Sets of appropriate types from the table, then select a data store service and click on a Run button.",
  perform_archiving_on_all_datasets_msg_template: "{0} {1} will be performed on all Data Sets from the table that are '{2}' (see their status) after you click on a Run button.",
  perform_archiving_on_selected_or_all_datasets_msg_template: "Select between performing {0} only on selected Data Sets ({1}) or on all Data Sets from the table and click on a Run button. Note that only Data Sets that are '{2}' (see their status) will be processed.",
  
  //
  // Entity Type Browser
  //

  add_new_type_button: "Add",
  edit_type_button: "Edit",
  add_type_title_template: "Add a new {0} Type",
  edit_type_title_template: "Edit {0} Type {1}",
  delete_confirmation_title: "Confirm Deletion",
  delete_confirmation_message: "Are you sure you want to delete [{0}]?",
  
  //
  // Sample Type Browser
  //
  
  subcode_unique_label: "Unique Subcodes",
  auto_generate_codes_label: "Generate Codes Automatically",
  generated_code_prefix: "Generated Code Prefix",
  listable: "Listable",
  is_listable: "Listable?",
  show_container: "Show Container",
  show_parents: "Show Parents",
  is_show_container: "Show Container?",
  is_show_parents: "Show Parents?",
   
  //
  // Property Type Browser
  //
  
  label: "Label",
  data_type: "Data Type",
  data_type_code: "Data Type Code",
  description: "Description",
  sample_types: "Sample Types",
  material_types: "Material Types",
  data_set_types: "Data Set Types",
  file_format_types: "File Types",
  experiment_types: "Experiment Types",
  mandatory: "Mandatory",
  is_mandatory: "Mandatory?",
  property_type: "Property Type",
  property_type_code: "Property Type Code",
  assigned_to: "Entity Type",
  type_of: "Entity",
  vocabulary: "Vocabulary",
  button_edit_content: "Edit Content",
  edit_content_title: "Edit Terms in Vocabulary {0}",
  vocabulary_terms: "Terms",
  vocabulary_terms_file_format: "<pre>code    [label]    [description]</pre> ([...] means that column is optional)",
  vocabulary_terms_url_template: "URL Template",
  vocabulary_terms_empty: "Space or comma separated list of terms.",
  vocabulary_show_available_terms_in_choosers: "Show available terms in choosers",
  confirm_vocabulary_show_available_terms_in_chooosers_msg: "If this vocabulary has many terms (more than 100) application will slow down.<br><br>Are you sure?",
  vocabulary_show_available_terms_in_choosers: "Show available terms in choosers",
  missing_vocabulary_terms: "Missing vocabulary term.",
  section_tooltip: "The name of the section in which the assigned property should appear in entity registration and edition forms.",
  default_value: "Initial Value",
  default_value_tooltip: "The value of the assigned property for all currently existing entities.",
  entity_type_assignments: "{0} Type Assignment{1}",
  xml_schema: "XML Schema",
  xslt: "XSLT",
  
  
  //
  // Property Type Assignments Browser
  //
  
  unassign_button_label: "Release Assignment",
  unassignment_confirmation_dialog_title: "Unassignment Confirmation",
  unassignment_confirmation_template_without_properties: "Removing assignment between {0} type {1} and property type {2}. This can be safely done because no {0} has this property filled in.<br><br>Do you want to remove the assignment?",
  unassignment_confirmation_template_with_properties: "Removing assignment between {0} type {1} and property type {2}. There are {3} {0}(s) where value for this property has been filled in.<br><br>Do you want to delete these values and remove the assignment?",
  edit_property_type_assignment_title : "Edit assignment between {0} type {1} and property type {2}",
  default_update_value: "Update Value",
  default_update_value_tooltip: "The value of the assigned property for all currently existing entities that didn't have any value for this property.",
  
  //
  // Menu Titles
  //
  
  menu_administration: "Administration",
  ADMINISTRATION_MENU_MANAGE_GROUPS: "Spaces",
  
  menu_authorization: "Authorization",
  AUTHORIZATION_MENU_USERS: "Users",
  AUTHORIZATION_MENU_AUTHORIZATION_GROUPS: "User Groups",
  AUTHORIZATION_MENU_ROLES: "Roles",
  LOGGING_CONSOLE: "Logging Console",

  menu_data_set: "Data Set",
  DATA_SET_MENU_SEARCH: "Advanced Search",
  DATA_SET_MENU_TYPES: "Types",
  DATA_SET_MENU_FILE_FORMATS: "File Types",
  DATA_SET_MENU_UPLOAD: "Upload",
  DATA_SET_MENU_MASS_UPDATE: "Batch Update",
  data_set_batch_update: "Batch Update",
  
  menu_experiment: "Experiment",
  EXPERIMENT_MENU_BROWSE: "Browse",
  EXPERIMENT_MENU_NEW: "New",
  EXPERIMENT_MENU_TYPES: "Types",
  
  menu_material: "Material",
  MATERIAL_MENU_BROWSE: "Browse",
  MATERIAL_MENU_IMPORT: "Batch Import",
  MATERIAL_MENU_TYPES: "Types",
   
  menu_sample: "Sample",
  SAMPLE_MENU_SEARCH: "Advanced Search",
  SAMPLE_MENU_BROWSE: "Browse",
  SAMPLE_MENU_NEW: "New",
  SAMPLE_MENU_IMPORT: "Batch Import",
  SAMPLE_MENU_MASS_UPDATE: "Batch Update",
  SAMPLE_MENU_TYPES: "Types",
  
  menu_project: "Project",
  PROJECT_MENU_BROWSE: "Browse",
  PROJECT_MENU_NEW: "New",
  
  menu_property_types: "Property Type",
  PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES: "Browse Property Types",
  PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS: "Browse Assignments",
  PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES: "New Property Type",
  PROPERTY_TYPES_MENU_ASSIGN_TO_EXPERIMENT_TYPE: "Assign To Experiment Type",
  PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE: "Assign To Sample Type",
  PROPERTY_TYPES_MENU_ASSIGN_TO_MATERIAL_TYPE: "Assign To Material Type",
  PROPERTY_TYPES_MENU_ASSIGN_TO_DATA_SET_TYPE: "Assign To Data Set Type", 
  
  menu_vocabulary: "Vocabulary",
  VOCABULARY_MENU_BROWSE: "Browse",
  VOCABULARY_MENU_NEW: "New",
  
  // menu user
  USER_MENU_CHANGE_SETTINGS: "Settings",
  USER_MENU_LOGOUT: "Logout",
    
  menu_compute: "Perform",
  COMPUTE_MENU_QUERIES: "Query",
  COMPUTE_MENU_PROCESSING: "Processing",
  
  menu_archiving: "Archiving",
  ARCHIVING_MENU_ARCHIVE: "Archive",
  ARCHIVING_MENU_UNARCHIVE: "Unarchive",
  ARCHIVING_MENU_LOCK: "Disable",
  ARCHIVING_MENU_UNLOCK: "Enable",
    
  menu_modules: "Utilities",
    
  //
  // Tab Titles
  //

  data_set_upload: "Data Set Upload",
  assign_data_set_property_type: "Assign Data Set Property Type",
  assign_material_property_type: "Assign Material Property Type",  
  assign_experiment_property_type: "Assign Experiment Property Type",
  assign_sample_property_type: "Assign Sample Property Type",
  property_type_assignments: "Property Type Assignments",
  property_type_registration: "Property Type Registration",
  property_types: "Property Types",
  experiment_browser: "Experiment Browser",
  vocabulary_registration: "Vocabulary Registration",
  sample_batch_registration: "Sample Batch Registration",
  sample_batch_update: "Sample Batch Update",
  sample_registration: "Sample Registration",
  sample_broser: "Sample Browser",
  list_groups: "Spaces Browser",
  confirm_title: "Confirmation",
  confirm_close_msg: "All unsaved changes will be lost. Are you sure?",

  
  //
  // Help Page Titles
  //
  
  HELP__EXPERIMENT__BROWSE: "Experiment Browser",
  HELP__EXPERIMENT__VIEW: "Experiment Viewer",
  HELP__EXPERIMENT__EDIT: "Experiment Editor",
  HELP__EXPERIMENT__REGISTER: "Experiment Registration",
  HELP__EXPERIMENT__EXPERIMENT_TYPE__BROWSE: "Experiment Type Browser",
  HELP__SAMPLE__BROWSE: "Sample Browser",
  HELP__SAMPLE__VIEW: "Sample Viewer",
  HELP__SAMPLE__EDIT: "Sample Editor",
  HELP__SAMPLE__SEARCH: "Sample Advanced Search",
  HELP__SAMPLE__REGISTER: "Sample Registration",
  HELP__SAMPLE__IMPORT: "Sample Import",
  HELP__SAMPLE__BATCH_UPDATE: "Sample Batch Update",
  HELP__SAMPLE__SAMPLE_TYPE__BROWSE: "Sample Type Browser",
  HELP__SAMPLE__SAMPLE_TYPE__REGISTER: "Add or Edit Sample Type",
  HELP__SAMPLE__SAMPLE_TYPE__EDIT: "Add or Edit Sample Type",
  HELP__MATERIAL__BROWSE: "Material Browser",
  HELP__MATERIAL__VIEW: "Material Viewer",
  HELP__MATERIAL__EDIT: "Material Editor",
  HELP__MATERIAL__IMPORT: "Sample Import",
  HELP__MATERIAL__MATERIAL_TYPE__BROWSE: "Material Type Browser",
  HELP__DATA_SET__VIEW: "Data Set Viewer",
  HELP__DATA_SET__EDIT: "Data Set Editor",
  HELP__DATA_SET__SEARCH: "Data Set Advanced Search",
  HELP__DATA_SET__REGISTER: "Data Set Upload",
  HELP__DATA_SET__REPORT: "Data Set Report",
  HELP__DATA_SET__DATA_SET_TYPE__BROWSE: "Data Set Type Browser",
  HELP__DATA_SET__DATA_SET_TYPE__REGISTER: "Add or Edit Data Set Type",
  HELP__DATA_SET__DATA_SET_TYPE__EDIT: "Add or Edit Data Set Type",
  HELP__RELATED_DATA_SETS__BROWSE: "Related Data Sets Browser",
  HELP__ADMINISTRATION__FILE_TYPE__BROWSE: "File Type Browser",
  HELP__ADMINISTRATION__GROUP__BROWSE: "Space Browser",
  HELP__ADMINISTRATION__VOCABULARY__BROWSE: "Vocabulary Browser",
  HELP__ADMINISTRATION__VOCABULARY__REGISTER: "Vocabulary Registration",
  HELP__ADMINISTRATION__VOCABULARY__TERM__BROWSE: "Vocabulary Terms Browser",
  HELP__ADMINISTRATION__VOCABULARY__TERM__REGISTER: "Adding Vocabulary Terms",
  HELP__ADMINISTRATION__VOCABULARY__TERM__DELETE: "Deletion of Vocabulary Terms with Replacements",
  HELP__ADMINISTRATION__VOCABULARY__TERM__BATCH_UPDATE: "Vocabulary Terms Batch Update",
  HELP__ADMINISTRATION__PROJECT__VIEW: "Project Viewer",
  HELP__ADMINISTRATION__PROJECT__EDIT: "Project Editor",
  HELP__ADMINISTRATION__PROJECT__BROWSE: "Project Browser",
  HELP__ADMINISTRATION__PROJECT__REGISTER: "Project Registration",
  HELP__ADMINISTRATION__PROPERTY_TYPE__BROWSE: "Property Type Browser",
  HELP__ADMINISTRATION__PROPERTY_TYPE__REGISTER: "Property Type Registration",
  HELP__ADMINISTRATION__PROPERTY_TYPE__ASSIGNMENT__BROWSE: "Property Type Assignment Browser",
  HELP__ADMINISTRATION__PROPERTY_TYPE__ASSIGNMENT__REGISTER: "Property Type Assignment",
  HELP__ADMINISTRATION__AUTHORIZATION__AUTHORIZATION_GROUPS__BROWSE: "Authorization Group Browser",
  HELP__ADMINISTRATION__AUTHORIZATION__AUTHORIZATION_GROUPS__VIEW: "Users From Authorization Group",
  HELP__ADMINISTRATION__AUTHORIZATION__ROLES__BROWSE: "Role Assignment Browser",
  HELP__ADMINISTRATION__AUTHORIZATION__ROLES__REGISTER: "Role Assignment",
  HELP__ADMINISTRATION__AUTHORIZATION__USERS__BROWSE: "Person Browser", 
  HELP__ADMINISTRATION__AUTHORIZATION__USERS__REGISTER: "Person Registration", 
  HELP__SEARCH__ACTION: "Global Search",
  HELP__ATTACHMENTS__VIEW: "Attachment Versions",
  HELP__CHANGE_USER_SETTINGS__ACTION: "Change User Settings",
  HELP__TABLE_SETTINGS__ACTION: "Table Settings",
  HELP__TABLE_SETTINGS__CUSTOM_COLUMN__REGISTER: "Add or Edit Custom Column",
  HELP__TABLE_SETTINGS__CUSTOM_COLUMN__EDIT: "Add or Edit Custom Column",
  HELP__TABLE_SETTINGS__CUSTOM_FILTER__REGISTER: "Add or Edit Custom Filter",
  HELP__TABLE_SETTINGS__CUSTOM_FILTER__EDIT: "Add or Edit Custom Filter",
  HELP__EXPORT_DATA__ACTION: "Data Exporting",
  HELP__PERFORM_COMPUTATION__ACTION: "Performing Computations on Data Sets",
    
  //
  // User Settings Dialog
  //
  
  change_user_settings_dialog_title: "Change User Settings",  
  home_group_label: "Home Space",
  use_wildcard_search_mode_label: "Search with Wildcards",
  use_wildcard_search_mode_tooltip: "Check to treat '*' and '?' as wildcards in searched text. Note, that only whole words will match if you don't add '*' at the beginning and at the end of the text.",
  reset_user_settings_button: "Restore Default",
  reset_user_settings_confirmation_msg: "Are you sure that you want to restore default settings?<br><br><b>Note that:</b><li>Every setting you changed in this application apart from home space, e.g. hiding/showing certain columns in a browser, will be lost.<li>All currently opened tabs will be closed.",
  real_number_formating_fields: "Custom Real Number Formating in Tables",
  scientific_formating: "Scientific",
  real_number_formating_precision: "Precision",
  error_message_formating: "Debugging Error Messages",
    
  //
  // Role View
  //
  role: "Role",
  confirm_role_removal_msg: "Do you want to remove selected role?",
  confirm_role_removal_title: "Role removal confirmation",
  authorization_group: "User Group",
  
  //
  // Experiment Registration
  //
  experiment_registration: "Experiment Registration",
  samples: "Samples",
  samples_list: "List of samples (codes or identifiers) separated by commas (\",\") or one sample per line.",
  
  //
  // Data Set Edition
  //
  parents: "Parents",
  parents_empty: "List of parent data set codes separated by commas (\",\") or one code per line.",
  
 //
 // Vocabulary Browser
 //
 vocabulary_browser: "Vocabulary Browser",
 is_managed_internally: "Managed Internally?",
 url_template: "URL Template",
 terms: "Terms",
 VOCABULARY_TERMS_BROWSER: "Vocabulary Terms of {0}",
 TERM_FOR_SAMPLES_USAGE: "Usages for Samples",
 TERM_FOR_DATA_SET_USAGE: "Usages for Data Sets",
 TERM_FOR_EXPERIMENTS_USAGE: "Usages for Experiments",
 TERM_FOR_MATERIALS_USAGE: "Usages for Materials",
 TERM_TOTAL_USAGE: "Total Usages Number",
 add_vocabulary_terms_button: "Add Terms",
 add_vocabulary_terms_title: "Add Terms",
 add_vocabulary_terms_ok_button: "OK",
 update_vocabulary_terms_button: "Update Terms",
 update_vocabulary_terms_title: "Update Vocabulary Terms of {0}",
 update_vocabulary_terms_message: "<b>Upload a TSV file</b> with all current terms of the vocabulary listed in columns:<br/><br/>{0}<br/><br/>With this file you can:<li>update label and description of current terms,<li>add new terms,<li>reorder terms (new order is taken from the order that terms are listed).<br/><br/>{1}",
 update_vocabulary_terms_message_2: "<b>To get a file for upload</b> with all current terms listed:<li>show only <i>Code</i>, <i>Label</i> and <i>Description</i> columns using table <i>Settings</i></li><li>use table <i>Export</i> functionality</li>",
 vocabulary_terms_validation_message: "Term '{0}' already exists.", 
 delete_vocabulary_terms_button: "Delete/Replace Terms",
 delete_vocabulary_terms_invalid_title: "Invalid Deletion",
 delete_vocabulary_terms_invalid_message: "Can not delete all terms. A vocabulary should have at least one term.",
 delete_vocabulary_terms_confirmation_title: "Deletion of Vocabulary Terms",
 delete_vocabulary_terms_confirmation_message_no_replacements_singular: "Do you want to delete the selected term?",
 delete_vocabulary_terms_confirmation_message_no_replacements: "Do you want to delete the {0} selected terms?",
 delete_vocabulary_terms_confirmation_message_for_replacements: "{0} terms will be deleted.\n\nThe terms below are used. They have to be replaced by one of the remaining terms.",
 edit_vocabulary_term_button: "Edit Term",
 
 //
 // Person Browser
 //
 person_browser: "Person Browser",
 user_id: "User ID",
 first_name: "First Name",
 last_name: "Last Name",
 email: "Email",
 
 //
 // Role Browser
 //
 role_assignment_browser: "Role Assignment Browser",
 person: "Person",
 button_assign_role: "Assign Role",
 button_release_role_assignment: "Release Assignment",
 
 //
 // Space Browser
 //
 group_browser: "Space Browser",
 leader: "Head",
 add_group_title: "Add a new space",

 //
 // Project Browser
 //
 project_browser: "Project Browser",

 //
 // Project Registration
 //
 project_registration: "Project Registration",
  
 //
 // Detailed Search
 //
 data_set_search: "Data Set Search",
 sample_search: "Sample Search",
 match_all: "Match all criteria",
 match_any: "Match any criteria",
 button_change_query : "Change Search Criteria",
  
 //
 // Data Set Browser
 //
 location: "Location",
 external_data_sample_identifier: "Sample Identifier",
 external_data_experiment_identifier: "Experiment Identifier",
 source_type: "Source Type",
 is_complete: "Complete?",
 complete: "Complete",
 archiving_status: "Archiving Status",
 data_set_type: "Data Set Type",
 parent: "Parent",
 parent_code: "Parent Code",
 file_format_type: "File Type",
 production_date: "Production Date",
 data_producer_code: "Producer",
 data_store_code: "Data Store",
 button_upload_datasets: "Export Data",
 confirm_dataset_upload_title: "Uploading Confirmation and Authentication",
 confirm_dataset_upload_msg: "This operation combines the chosen data set(s) into a single zip file, which is uploaded to CIFEX ({0}). Once in CIFEX, you may download the data or send it to colleagues.<br/><br/>Please, enter the following information:", 
 confirm_dataset_upload_file_name_field: "File name",
 confirm_dataset_upload_comment_field: "Comment",
 confirm_dataset_upload_user_field: "CIFEX user",
 confirm_dataset_upload_password_field: "CIFEX password",
 
 //
 // Data Set Viewer
 //
 
 processing_info_title: "Processing",
 processing_info_msg: "'{0}' has been scheduled successfully. Email will be send to you when it is finished.",
 dataset_not_available_msg: "Data Set {0} is {1}. You can not perform any operation using its data.",
 
 //
 // Material Browser
 //
 material_type: "Material Type",
 material_browser: "Material Browser", 
 infibitor_of: "Inhibitor of",
 allow_any_type: "(Allow Any Type)",
 
 //
 // Import Materials
 //
 material_import: "Import Materials",
 
 
 // 
 // Material Chooser
 //

 title_choose_material: "Choose a Material",
 choose_any_material: "Choose Any Material...",
 incorrect_material_syntax: "Incorrect material specification. Please provide the material code followed by the type in brackets: 'code (type)'.",
 TITLE_CHOOSE_EXPERIMENT: "Choose an Experiment",
incorrect_experiment_syntax: "Incorrect experiment specification. Please provide the experiment space, project and code using the format '/space/project/code'.",
 title_choose_sample: "Choose a Sample",

 //
 // Attachments
 //
 
 attachments: "Attachments",
 no_attachments_found: "There are no attachments in this {0}",
 file_name: "File Name",
 title: "Title",
 version_file_name: "File Version",
 version: "Version",
 show_all_versions: "show all versions",
 button_show_all_versions: "Show All Versions",
 button_download: "Download",
 
//
// Grid Column Chooser
//

  GRID_COLUMN_CHOOSER_TITLE: "Configure table columns",
  GRID_COLUMN_NAME_HEADER: "Column",
  GRID_IS_COLUMN_VISIBLE_HEADER: "Visible?",
  GRID_COLUMN_HAS_FILTER_HEADER: "Has Filter?",
    

  message_no_external_upload_service: "External upload service (CIFEX) has not been configured. Ask the administrator for more details.",
 
 //
 // Authorization Browser
 //
 authorization_group_browser: "User Groups Browser",
 edit_persons: "Edit Users",
 add_person_to_authorization_group_title: "Add person(s) to the user group '{0}'", 
 persons_ids_label: "Users IDs",
 person_ids_list: "List of users (IDs) separated by commas (\",\") or one user per line.",
 authorization_group_users: "Persons from User Group '{0}'",
 button_show_users: "Show Users",
 radio_one_user: "One User",
 radio_many_users: "Many Users",
 remove_persons_from_authorization_group_confirmation_title: "Users Removal Confirmation",
 remove_persons_from_authorization_group_confirmation_message: "Do you really want to remove {0} users ({1}) from the user group '{2}'?",

 
all_radio: "all ({0})",
only_selected_radio: "selected ({0})",
data_sets_radio_group_label: "Data Sets",
experiments_radio_group_label: "Experiments",
samples_radio_group_label: "Samples",
materials_radio_group_label: "Materials",

//
// Import samples
//
default_group: "Default Space",

//
// Filters 
//
 name: "Name",
 is_public: "Is Public?",
 expression: "Expression",
 column: "Column",
 columns: "Columns",
 grid_settings_title: "Table Settings",
 grid_custom_filters: "Custom Filters",
 grid_custom_columns: "Custom Columns",
 apply_filter: "Apply",
 reset_filter: "Reset",
 add_new_filter: "Add a New Filter",
 add_new_column: "Add a New Column",
 how_to_address: "How To Address", 
 insert_columns: "Insert Columns", 
 
 // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};
