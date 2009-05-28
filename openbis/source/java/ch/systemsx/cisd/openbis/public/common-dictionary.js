// Common dictionary
var common = {

  applicationName: "openBIS",
  welcome: "Welcome to openBIS",
  footer: "openBIS (Version {0})",
  
  //
  // Common Labels
  // 
  
  code: "Code",
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
  
  //
  // Field
  //
  
  combobox_empty: "- No {0} found -",
  combobox_choose: "Choose {0}...",
  combo_box_expected_value_from_the_list: "Value from the list required",
  invalid_code_message: "{0} contains invalid characters. Allowed characters are: letters, numbers, hyphen (\"-\") and underscore (\"_\").",
  invalid_term_code_message: "{0} contains invalid characters. Allowed characters are: letters, numbers, hyphen (\"-\"), underscore (\"_\"), colon (\":\") and dot (\".\").",
  
  //
  // MessageBox
  //
  
  messagebox_error: "Error",
  messagebox_warning: "Warning",
  messagebox_info: "Info",
  
  //
  // Buttons
  //
  
  button_save: "Save",
  button_choose: "Choose",
  button_cancel: "Cancel",
  button_reset: "Reset",
  button_submit: "Submit",
  button_refresh: "Refresh",
  button_show: "Show",
  button_exportTable: "Export",
  button_add_group: "Add Group",
  button_show_details: "Show Details",
  button_browse: "Browse",
  button_delete: "Delete",
  button_configure: "Columns",  
  
  tooltip_refresh_disabled: "To activate select appropriate criteria first.",
  tooltip_refresh_enabled: "Load or update the table.",
  tooltip_export_enabled: "Export the table visible on the screen to an Excel file",
  tooltip_export_disabled: "Refresh the data before exporting them.",
  tooltip_config_enabled: "Choose the columns.",
  tooltip_config_disabled: "Load the data to activate this option.",
  
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
  // Authorization Management Console
  // 
  
  personsView_heading: "Persons",
  groupsView_heading: "Groups",
  rolesView_heading: "Roles",

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

  //
  // Sample Browser
  //
  
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
  part_of: "Container {0}",
  invalidation: "Invalidation",
  invalidation_template: "Invalidated by: {0}<br>Invalidation Date: {1}<br>Invalidation Reason: {2}",
  
  //
  // Experiment Browser
  //
  
  experiment_type: "Experiment Type",
  
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
  is_mandatory: "Mandatory?",
  property_type: "Property Type",
  property_type_code: "Property Type Code",
  assigned_to: "Entity Type",
  type_of: "Entity",
  vocabulary: "Vocabulary",
  vocabulary_terms: "Terms",
  vocabulary_terms_source_uri: "Source URI",
  vocabulary_terms_empty: "Space or comma separated list of terms.",
  vocabulary_show_available_terms_in_choosers: "Show available terms in choosers",
  missing_vocabulary_terms: "Missing vocabulary term.",
  mandatory: "Mandatory",
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
 
  //
  // Menu Titles
  //
  
  menu_administration: "Administration",
  ADMINISTRATION_MENU_MANAGE_GROUPS: "Groups",
  
  menu_authorization: "Authorization",
  AUTHORIZATION_MENU_USERS: "Users",
  AUTHORIZATION_MENU_ROLES: "Roles",

  menu_data_set: "Data Set",
  DATA_SET_MENU_SEARCH: "Search",
  DATA_SET_MENU_TYPES: "Types",
  DATA_SET_MENU_FILE_FORMATS: "File Types",
  
  menu_experiment: "Experiment",
  EXPERIMENT_MENU_BROWSE: "Browse",
  EXPERIMENT_MENU_NEW: "New",
  EXPERIMENT_MENU_TYPES: "Types",
  
  menu_material: "Material",
  MATERIAL_MENU_BROWSE: "Browse",
  MATERIAL_MENU_IMPORT: "Import",
  MATERIAL_MENU_TYPES: "Types",
   
  menu_sample: "Sample",
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
    
  //
  // Tab Titles
  //

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
  // Group View
  //
  leader: "Head",
  
  //
  // Role View
  //
  role: "Role",
  confirm_role_removal_msg: "Do you want to remove selected role?",
  confirm_role_removal_title: "Role removal confirmation",
  
  //
  // Experiment Registration
  //
  experiment_registration: "Experiment Registration",
  samples: "Samples",
  samples_list: "List of samples (codes or identifiers) separated by commas (\",\") or one sample per line.",

 //
 // Vocabulary Browser
 //
 vocabulary_browser: "Vocabulary Browser",
 is_managed_internally: "Managed Internally?",
 source_uri: "Source URI",
 terms: "Terms",
 VOCABULARY_TERMS_BROWSER: "Vocabulary Terms of {0}",
 TERM_FOR_SAMPLES_USAGE: "Usages for Samples",
 TERM_FOR_EXPERIMENTS_USAGE: "Usages for Experiments",
 TERM_FOR_MATERIALS_USAGE: "Usages for Materials",
 TERM_TOTAL_USAGE: "Total Usages Number",
 add_vocabulary_terms_button: "Add New Terms",
 add_vocabulary_terms_title: "Add New Terms",
 add_vocabulary_terms_ok_button: "OK",
 vocabulary_terms_validation_message: "Term '{0}' already exists.", 
 delete_vocabulary_terms_button: "Delete/Replace Terms",
 delete_vocabulary_terms_invalid_title: "Invalid Deletion",
 delete_vocabulary_terms_invalid_message: "Can not delete all terms. A vocabulary should have at least one term.",
 delete_vocabulary_terms_confirmation_title: "Deletion of Vocabulary Terms",
 delete_vocabulary_terms_confirmation_message_no_replacements_singular: "Do you want to delete the selected term?",
 delete_vocabulary_terms_confirmation_message_no_replacements: "Do you want to delete the {0} selected terms?",
 delete_vocabulary_terms_confirmation_message_for_replacements: "{0} terms will be deleted.\n\nThe terms below are used. They have to be replaced by one of the remaining terms.",
 
 //
 // Project Browser
 //
 project_browser: "Project Browser",

 //
 // Project Registration
 //
 project_registration: "Project Registration",
  
 //
 // Data Set Search
 //
 data_set_search: "Data Set Search",
 match_all: "Match all criteria",
 match_any: "Match any criteria",
 button_change_query : "Change Search Criteria",
  
 //
 // Data Set Browser
 //
 location: "Location",
 external_data_sample: "Sample",
 source_type: "Source Type",
 is_complete: "Complete?",
 data_set_type: "Data Set Type",
 parent_code: "Parent Code",
 file_format_type: "File Type",
 production_date: "Production Date",
 data_producer_code: "Producer",
 button_delete_datasets: "Delete",
 confirm_dataset_deletion_title: "Data Sets Deletion Confirmation",
 confirm_dataset_deletion_msg: "You are deleting {0} data set(s). Please enter a reason:",
 button_upload_datasets: "Export Data",
 confirm_dataset_upload_title: "Uploading Confirmation and Authentication",
 confirm_dataset_upload_msg: "You are uploading {0} data set(s) to CIFEX ({1}) in a single ZIP file.<br/><br/>Please, enter additional information:", 
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

 //
 // Attachments
 //
 no_attachments_found: "There are no attachments in this {0}",
 file_name: "File Name",
 version_file_name: "File Version",
 version: "Version",
 versions: "Versions",
 versions_template: "Show all versions ({0})",
 
//
// Grid Column Chooser
//

GRID_COLUMN_CHOOSER_TITLE: "Configure grid columns",
GRID_COLUMN_NAME_HEADER: "Column",
GRID_IS_COLUMN_VISIBLE_HEADER: "Visible?",
GRID_COLUMN_HAS_FILTER_HEADER: "Has Filter?",
    
 
 // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};
