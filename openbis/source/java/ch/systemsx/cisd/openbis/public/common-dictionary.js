// Common dictionary
var common = {

  applicationName: "openBIS",
  welcome: "Welcome to openBIS",
  footer: "openBIS (Version {0})",
  
  //
  // Common Labels
  // 
  
  attachment: "Attachment",
  code: "Code",
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
  show_details_link_column_name: "Show Details Link",
  show_details_link_text_value: "Permlink",
  table_operations: "Table:",
  entity_operations: "Entity:",
  url: "URL",
  reason: "Reason",
  delete_confirmation_message_with_reason: "You are deleting {0} {1}(s).<br><br>Please enter a reason or cancel deletion.",
  add_attachment: "Add attachment",
  
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
  button_upload_data_via_cifex: "Upload Data",
  
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
  // Search
  //
  
  search_button: "Search",
  global_search: "[{0}]: '{1}'",
  identifier: "Identifier",
  no_match: "No results found for '{0}'.",
  entity_type: "Entity Type",
  entity_kind: "Entity Kind",
  matching_text: "Matching Text",
  matching_field: "Matching Field",
  too_generic: "Query string '{0}' is too generic.",
  show_related_datasets: "Show Related Data Sets",
  show_related_datasets_message: "Select between showing Data Sets related to selected ({0}) entities or to all entities from the grid and click on OK button.",
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
  group: "Group",
  project: "Project",
  experiment: "Experiment",
  experiment_identifier: "Experiment Identifier",
  generated_samples: "Generated Samples",
  generated_from: "Parent {0}",
  part_of: "Container",
  invalidation: "Invalidation",
  invalidation_template: "Invalidated by: {0}<br>Invalidation Date: {1}<br>Invalidation Reason: {2}",
  
  //
  // Experiment Browser
  //
  
  experiment_type: "Experiment Type",
  
  //
  // DataSet Browser
  //
  
  children_datasets: "Children",
  
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
  
  listable: "Listable",
  is_listable: "Listable?",
  show_container: "Show Container",
  is_show_container: "Show Container?",
  generated_from_hierarchy_depth: "Derived Hierarchy Depth",
   
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
  vocabulary_terms: "Terms",
  vocabulary_terms_file_format: "<pre>code		label (optional)		description (optional)</pre>",
  vocabulary_terms_url_template: "URL Template",
  vocabulary_terms_empty: "Space or comma separated list of terms.",
  vocabulary_show_available_terms_in_choosers: "Show available terms in choosers",
  confirm_vocabulary_show_available_terms_in_chooosers_msg: "If this vocabulary has many terms (more than 100) application will slow down.<br><br>Are you sure?",
  vocabulary_show_available_terms_in_choosers: "Show available terms in choosers",
  missing_vocabulary_terms: "Missing vocabulary term.",
  default_value: "Initial Value",
  default_value_tooltip: "The value of the assigned property for all currently existing entities.",
  entity_type_assignments: "{0} Type Assignment{1}",
  
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
  ADMINISTRATION_MENU_MANAGE_GROUPS: "Groups",
  
  menu_authorization: "Authorization",
  AUTHORIZATION_MENU_USERS: "Users",
  AUTHORIZATION_MENU_AUTHORIZATION_GROUPS: "User Groups",
  AUTHORIZATION_MENU_ROLES: "Roles",

  menu_data_set: "Data Set",
  DATA_SET_MENU_SEARCH: "Search",
  DATA_SET_MENU_TYPES: "Types",
  DATA_SET_MENU_FILE_FORMATS: "File Types",
  DATA_SET_MENU_UPLOAD: "Upload",
  
  menu_experiment: "Experiment",
  EXPERIMENT_MENU_BROWSE: "Browse",
  EXPERIMENT_MENU_NEW: "New",
  EXPERIMENT_MENU_TYPES: "Types",
  
  menu_material: "Material",
  MATERIAL_MENU_BROWSE: "Browse",
  MATERIAL_MENU_IMPORT: "Import",
  MATERIAL_MENU_TYPES: "Types",
   
  menu_sample: "Sample",
  SAMPLE_MENU_SEARCH: "Search",
  SAMPLE_MENU_BROWSE: "Browse",
  SAMPLE_MENU_NEW: "New",
  SAMPLE_MENU_IMPORT: "Import",
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
  USER_MENU_CHANGE_HOME_GROUP: "Home Group",
  USER_MENU_LOGOUT: "Logout",
    
  menu_compute: "Perform",
  COMPUTE_MENU_QUERIES: "Query",
  COMPUTE_MENU_PROCESSING: "Processing",
    
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
  sample_registration: "Sample Registration",
  sample_broser: "Sample Browser",
  list_groups: "Groups Browser",
  confirm_title: "Confirmation",
  confirm_close_msg: "All unsaved changes will be lost. Are you sure?",

  change_user_home_group_dialog_title: "Change Home Group",  
  
  
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
 // Group Browser
 //
 group_browser: "Group Browser",
 leader: "Head",

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
 data_set_type: "Data Set Type",
 parent: "Parent",
 parent_code: "Parent Code",
 file_format_type: "File Type",
 production_date: "Production Date",
 data_producer_code: "Producer",
 data_store_code: "Data Store",
 button_upload_datasets: "Export Data",
 confirm_dataset_upload_title: "Uploading Confirmation and Authentication",
 confirm_dataset_upload_msg: "You are going to upload data set(s) to CIFEX ({0}) in a single ZIP file.<br/><br/>Please, enter additional information:", 
 confirm_dataset_upload_file_name_field: "File name",
 confirm_dataset_upload_comment_field: "Comment",
 confirm_dataset_upload_user_field: "CIFEX user",
 confirm_dataset_upload_password_field: "CIFEX password",
 
 
 
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
incorrect_experiment_syntax: "Incorrect experiment specification. Please provide the experiment group, project and code using the format '/group/project/code'.",
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

  GRID_COLUMN_CHOOSER_TITLE: "Configure grid columns",
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

 
all_radio: "all",
data_sets_radio_group_label: "Data Sets",
only_selected_radio: "selected ({0})",
experiments_radio_group_label: "Experiments",

//
// Filters 
//
 name: "Name",
 is_public: "Is Public?",
 expression: "Expression",
 columns: "Columns",
 grid_settings_title: "Table settings",
 custom_filters: "Custom filters",
 apply_filter: "Apply",
 add_new_filter: "Add a New Filter",
 how_to_address: "How To Address", 
 
 // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};
