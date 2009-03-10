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
  button_cancel: "Cancel",
  button_reset: "Reset",
  button_submit: "Submit",
  button_refresh: "Refresh",
  button_show: "Show",
  button_exportData: "Export Data",
  button_add_group: "Add Group",
  button_show_details: "Show Details",
  
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
  
  header_userWithoutHomegroup: "{0} (no home group)",
  header_userWithHomegroup: "{0} (home group: {1})",
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
  button_invalidate: "Invalidate",
  
  //
  // Experiment Browser
  //
  
  experiment_type: "Experiment Type",
  
  //
  // Property Type Browser
  //
  
  label: "Label",
  data_type: "Data Type",
  description: "Description",
  sample_types: "Sample Types",
  material_types: "Material Types",
  experiment_types: "Experiment Types",
  is_mandatory: "Mandatory?",
  property_type: "Property Type",
  property_type_code: "Property Type Code",
  assigned_to: "Entity Type",
  type_of: "Entity",
  vocabulary: "Vocabulary",
  vocabulary_terms: "Terms",
  vocabulary_terms_empty: "White space or comma separated list",
  mandatory: "Mandatory",
  default_value: "Initial Value",
  default_value_tooltip: "The value of the assigned property for all currently existing entities.",
  entity_type_assignments: "{0} Type Assignment{1}",
  
  //
  // Tab Titles
  //
  
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
 
 //
 // Material Browser
 //
 material_type: "Material Type",
 material_browser: "Material Browser", 
 infibitor_of: "Inhibitor of",
 
 //
 // Import Materials
 //
 material_import: "Import Materials",
  // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};
