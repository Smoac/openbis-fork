// Common dictionary
var common = {

  applicationName: "openBIS",
  welcome: "Welcome to openBIS",
  footer: "openBIS (Version {0})",
  
  //
  // Common Labels
  // 
  
  code: "Code",
  registrator: "Registrator",
  registration_date: "Registration Date",
  filter: "Filter",
  not_implemented: "Sorry, feature has not been implemented yet!",
  edit: "Edit",
  material: "Material",
  
  //
  // Field
  //
  
  combobox_empty: "- No {0} found -",
  combobox_choose: "Choose {0}...",
  combo_box_expected_value_from_the_list: "Value from the list required",
  invalid_code_message: "{0} contains invalid characters. Allowed characters are: letters, numbers, hyphen (\"-\") and underscore (\"_\").",
  
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
  button_exportData: "Export Table",
  button_add_group: "Add Group",
  button_show_details: "Show Details",
  button_browse: "Browse",
  
  tooltip_refresh_disabled: "To activate select appropriate criteria first.",
  tooltip_refresh_enabled: "Load or update the table.",
  tooltip_export_enabled: "Export the table visible on the screen to an Excel file",
  tooltip_export_disabled: "Refresh the data before exporting them.",

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
  
  exception_invocationMessage: "Failed to contact service.", 
  exception_withoutMessage: "Unknown failure has occurred (ask administrator):<br>{0}",
  
  //
  // Header
  //
  
  header_userWithoutHomegroup: "{0}",
  header_userWithHomegroup: "{0} ({1})",
  header_logoutButtonLabel: "Logout",
  
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
  // Property Type Browser
  //
  
  label: "Label",
  data_type: "Data Type",
  data_type_code: "Data Type Code",
  description: "Description",
  sample_types: "Sample Types",
  material_types: "Material Types",
  data_set_types: "Data Set Types",
  experiment_types: "Experiment Types",
  is_mandatory: "Mandatory?",
  property_type: "Property Type",
  property_type_code: "Property Type Code",
  assigned_to: "Entity Type",
  type_of: "Entity",
  vocabulary: "Vocabulary",
  vocabulary_terms: "Terms",
  vocabulary_terms_empty: "Space or comma separated list of terms.",
  missing_vocabulary_terms: "Missing vocabulary term.",
  mandatory: "Mandatory",
  default_value: "Initial Value",
  default_value_tooltip: "The value of the assigned property for all currently existing entities.",
  entity_type_assignments: "{0} Type Assignment{1}",
  
  //
  // Menu Titles
  //
  
  menu_administration: "Administration",
  ADMINISTRATION_MENU_MANAGE_GROUPS: "Manage Groups",
  
  menu_authorization: "Authorization",
  AUTHORIZATION_MENU_USERS: "Users",
  AUTHORIZATION_MENU_ROLES: "Roles",

  menu_data_set: "Data Set",
  DATA_SET_MENU_SEARCH: "Search",
  
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
  
  //
  // Group View
  //
  leader: "Leader",
  
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
 procedure_type: "Procedure Type",
 external_data_sample: "Sample",
 is_derived: "Derived?",
 is_complete: "Complete?",
 data_set_type: "Data Set Type",
 parent_code: "Parent Code",
 file_format_type: "File Type",
 production_date: "Production Date",
 data_producer_code: "Producer",
 button_delete_datasets: "Delete",
 confirm_dataset_deletion_title: "Data Sets Deletion Confirmation",
 confirm_dataset_deletion_msg: "You are deleting {0} data set(s). Please enter a reason:",
 button_upload_datasets: "Upload to CIFEX",
 confirm_dataset_upload_title: "Uploading Confirmation and Authentication",
 confirm_dataset_upload_msg: "You are uploading {0} data set(s) to CIFEX ({1}). Please, enter your CIFEX password:", 
 
 
 
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
 
 // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};
