// Proteomics dictionary
var proteomics = {
  ALL_PROTEINS_OF_AN_EXPERIMENT_menu_item: "All Proteins of an Experiment",
  ALL_PROTEINS_OF_AN_EXPERIMENT_tab_label: "Proteins of an Experiment",
  selected_experiment_label: "Experiment",
  false_discovery_rate_filter_label: "False Discovery Rate",
  aggregate_function_label: "Aggregate Function",
  aggregate_on_treatment_type_label: "Aggregate on",
  aggregate_original_label: "Aggregate original", 
  
  // Experiment View
  data_set_processing_section_title: "Available Data Set Processing Tasks",
    
  // Protein grid
  accession_number: "Accession Number",
  protein_description: "Protein",
  coverage: "Protein Coverage (in %)",
  false_discovery_rate: "FDR",
  fdr_combo_info: "<html><div style='width:250px'>This False Discovery Rate determines which ProteinProphet probability " +
                    "cut-off is applied to the combined ProteinProphet output. Only proteins above this " +
                    "probability are displayed in the table.<br/><br/>" + 
                    "Note, that the spectral counts are not affected by this threshold and values for individual " +
                    "samples stay unchanged as long as the protein is displayed at all.</div></html>",
  
  // Protein in experiment viewer
  loading_protein_details_waiting_message: "Loading details for protein {0}",
  proteins_section: "Proteins",
  protein_in_experiment_tab_label: "{0} in {1}",
  experiment_label: "Experiment",
  sequence_short_name: "Sequence Reference",
  protein_browser: "Proteins",
  protein_summary: "Protein/Peptide Counts",
  database_name_and_version: "Database",
  primary_protein: "Primary Protein and Peptides",
  indistinguishable_proteins: "Indistinguishable Proteins",
  sequence: "Amino Acid Sequence",
  sequences: "Amino Acid Sequences",
  data_set_proteins: "Data Sets",
  data_set_perm_id: "Data Set",
  protein_count: "Proteins (no decoy)",
  peptide_count: "Peptides (no decoy)",
  decoy_protein_count: "Proteins (decoy)",
  decoy_peptide_count: "Peptides (decoy)",
  peptides: "Peptides ({0})",
  sequence_name: "Sequence Reference",
  false_discovery_rate_column: "FDR (Protein Prophet)",
  protein_probability: "Probability (Protein Prophet)",
  button_delete_protein: "Delete Protein",
  
  // ProteinRelatedSampleGrid
  openbis_protein_related_sample_grid_main_SAMPLE_IDENTIFIER: "Identifier",
  openbis_protein_related_sample_grid_main_SAMPLE_TYPE: "Sample Type",
  openbis_protein_related_sample_grid_main_ABUNDANCE: "Abundance",
  openbis_protein_related_sample_grid_main_MODIFIED_AMINO_ACID: "Modified Amino Acid",
  openbis_protein_related_sample_grid_main_MODIFICATION_POSITION: "Modification Position",
  openbis_protein_related_sample_grid_main_MODIFICATION_MASS: "Modification Mass",
  openbis_protein_related_sample_grid_main_MODIFICATION_FRACTION: "Modification Fraction",
  
  // MS_INJECTION annotation wizard
  
  MS_INJECTION_SAMPLE_CHOOSING_left_content: "MS data are added to openBIS in an automated process. "
        + "The corresponding data sets are associated with samples of type <tt>MS_INJECTION</tt>."
        + "<p>Before MS data of such samples can be processed in a protein identification pipeline "
        + "they have to be <b>annotated</b>. In the terminology of openBIS this means: "
        + "An <tt>MS_INJECTION</tt> sample is linked to a biological sample where "
        + "the biological sample is the parent and the <tt>MS_INJECTION</tt> sample is the child. "
        + "The biological sample has all annotations (called properties in openBIS). "
        + "They define the scientific context of proteins found and have to be created "
        + "by the user before the MS_INJECTION sample can be linked to it. "
        + "<p>This wizard helps you adding these important annotations to openBIS.",
  openbis_parentless_ms_injection_sample_main_IDENTIFIER: "MS_INJECTION Sample",
  openbis_parentless_ms_injection_sample_main_REGISTRATION_DATE: "Registration Date",
  CHOOSE_OR_CREATE_QUESTION_left_content: "Annotating the <tt>MS_INJECTION</tt> samples you have chosen "
        + "means to link them to a <b>biological sample</b>. "
        + "You can choose an existing biological sample or you can create a new one.",
  BIOLOGICAL_SAMPLE_CHOOSING_left_content: "Annotating <tt>MS_INJECTION</tt> sample by choosing one biological sample means " 
        + "that all properties of the biological sample is also for the <tt>MS_INJECTION</tt> to be annotated.",
  openbis_biological_sample_main_IDENTIFIER: "Biological Sample",
  openbis_biological_sample_main_REGISTRATION_DATE: "Registration Date",
  BIOLOGICAL_SAMPLE_CREATING_left_content: "Annotating <tt>MS_INJECTION</tt> sample by creating a new biological sample means " 
        + "mainly specifying properties of a freshly created biological sample in openBIS.",
  
  
  // RawDataSample Browser
  
  ANNOTATE_MS_INJECTION_SAMPLES_menu_item: "Annotate MS INJECTION Samples",
  ANNOTATE_MS_INJECTION_SAMPLES_tab_label: "MS INJECTION Samples Annotation Wizard",
  ALL_RAW_DATA_SAMPLES_menu_item: "All MS INJECTION Samples",  
  ALL_RAW_DATA_SAMPLES_tab_label: "MS INJECTION Samples and Related Biological Samples",  
  openbis_raw_data_sample_browser_CODE: "MS INJECTION Sample",
  openbis_raw_data_sample_browser_REGISTRATION_DATE: "Registration Date",
  openbis_raw_data_sample_browser_PARENT: "Biological Sample",
  openbis_raw_data_sample_browser_EXPERIMENT: "Biological Experiment",
  copy_data_sets_button_label: "Process Data Sets",
  copy_data_sets_title: "Process Data Sets",
  copy_data_sets_message: "Please, enter the data set type of the data sets of the samples {1} to be processed by '{0}'.", 
  copy_data_sets_data_set_type_field: "Data Set Type",
  
  // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};