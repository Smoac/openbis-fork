// Generic dictionary
var generic = {
  welcome: "Welcome to " + common["applicationName"],
  footer: common["applicationName"] + " (Version {0})",
  
  //
  // LoginWidget
  //
  
  login_invitation: "Please login to start your session:", 
  login_userLabel: "User",
  login_passwordLabel: "Password",
  login_buttonLabel: "login",
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
  header_logoutButtonLabel: "logout",
  
  personsView_heading: "Persons",
  groupsView_heading: "Groups",
  rolesView_heading: "Roles",

  sample_properties_heading: "Sample Properties",
  part_of_heading: "Components",
  external_data_heading: "External Data",
  
  //
  // Sample
  //
  
  sample: "Sample",
  sample_type: "Sample Type",
  instance: "Instance",
  sample_identifier: "Identifier",
  is_instance_sample: "Shared?",
  is_invalid: "Invalid?",
  group: "Group",
  project: "Project",
  experiment: "Experiment",
  experiment_identifier: "Experiment Identifier",
  generated_samples: "Generated Samples",
  generated_from: "Parent (gener.) {0}",
  part_of: "Parent (cont.) {0}",
    
  //
  // Invalidation
  //
  
  invalidation: "Invalidation",
  invalidation_template: "Invalidated by: {0}<br>Invalidation Date: {1}<br>Invalidation Reason: {2}",
  
  //
  // External data
  //
  
  location: "Location",
  file_format_type: "File Format",
  
  //
  // Search
  //
  
  search_button: "Search",
  global_search: "Search: '{0}'",
  identifier: "Identifier",
  no_match: "No results found for '{0}'",
  entity_type: "Entity Type",
  entity_kind: "Entity Kind",
  
  // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};