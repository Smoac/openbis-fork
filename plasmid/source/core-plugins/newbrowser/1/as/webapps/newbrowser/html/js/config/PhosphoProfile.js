function PhosphoProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(PhosphoProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		//Put on this list all experiment types, ELN experiments need to have both an experiment type and a sample type with the same CODE.
		this.ELNExperiments = ["SYSTEM_EXPERIMENT"];

		//Black list, put on this list all types that you don«t want to appear on the menu and the ELN experiments.
		this.notShowTypes = ["SYSTEM_EXPERIMENT", "FJELMER_TEST", "SEARCH"];
	
		//Use this with all known types to create groups, if a type is not specified by default will be added to the OTHERS group.
		this.typeGroups = {
			"BIOLOGICAL_SAMPLES_GROUP" : {
				"TYPE" : "BIOLOGICAL_SAMPLES_GROUP",
				"DISPLAY_NAME" : "Biological Samples",
				"LIST" : ["BIOLOGICAL_SAMPLE", "BIOL_APMS", "BIOL_BASIC", "BIOL_CLINICAL", "BIOL_DDB", "BIOL_DDB_PATIENT", "BIOL_IRRELEVANT", "BIOL_MICROORGANISMS", "BIOL_PHOSPHO", "BIOL_SYNTHETIC", "BIOL_XL"] 
			},
			"MS_INJECTION_GROUP" : {
				"TYPE" : "MS_INJECTION_GROUP",
				"DISPLAY_NAME" : "MS Injection",
				"LIST" : ["MS_INJECTION"] 
			},
			"ROSETTA_GROUP" : {
				"TYPE" : "ROSETTA_GROUP",
				"DISPLAY_NAME" : "Rosetta",
				"LIST" : ["ROSETTA_DENOVO"] 
			},
			"WORKFLOW_GROUP" : {
				"TYPE" : "WORKFLOW_GROUP",
				"DISPLAY_NAME" : "Work Flow",
				"LIST" : ["WORKFLOW"] 
			},
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [] 
			}
		};
		
		//The properties you want to appear on the tables, if you don«t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {
				"BIOLOGICAL_SAMPLE" : ["NAME", "COMMENT", "TREATMENT_TYPE1", "TREATMENT_VALUE1", "TREATMENT_TYPE2", "TREATMENT_VALUE2", "TREATMENT_TYPE3", "TREATMENT_VALUE3"],
				"BIOL_APMS" : ["NAME", "COMMENT", "BAIT", "DIGESTION"],
				"BIOL_BASIC" : ["NAME", "COMMENT", "BIOLOGICAL_SAMPLE_TYPE", "TAX_ID", "SAMPLE_PREPARATION", "FRACTIONATION", "DIGESTION", "LABELING"],
				"BIOL_CLINICAL" : ["NAME", "COMMENT", "DIGESTION", "LABELING"],
				"BIOL_DDB" : ["NAME", "EM_PATIENTS", "CK_PATIENTS", "GENOME", "STRAIN_NAME", "PRIMARY_CELL_TYPE"],
				"BIOL_MICROORGANISMS" : ["NAME", "BIOLOGICAL_SAMPLE_TYPE", "TAX_ID", "STRAIN", "SAMPLE_PREPARATION"],
				"BIOL_PHOSPHO" : ["NAME", "SAMPLE_PREPARATION", "TREATMENT_PH_1", "TREATMENT_MO_1_VALUE", "TREATMENT_MO_1_TIME"],
				"BIOL_SYNTHETIC" : ["NAME", "TYPE_SYNTHETIC", "SYNTHETIC_PEPTIDE"],
				"BIOL_XL" : ["NAME", "COMMENT","CROSS_LINKER"],
				"MS_INJECTION" : ["INSTRUMENT_TYPE"]
		};
		
		//The colors for the notes, if you don«t specify the color, light yellow will be used by default.
		this.colorForInspectors = {};
		
		//The configuration for the visual storages.
		this.storagesConfiguration = {
			"isEnabled" : false
		};
}
});