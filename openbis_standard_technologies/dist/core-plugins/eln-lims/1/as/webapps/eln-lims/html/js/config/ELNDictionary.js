var ELNDictionary = {
	Sample : "Object",
	Samples : "Objects",
	sample : "object",
	samples : "objects",
	ExperimentELN : "Experiment",
	ExperimentInventory : "Collection",
	ExperimentsELN : "Experiments",
	ExperimentsInventory : "Collections"
}

ELNDictionary.getExperimentDualName = function() {
	return ELNDictionary.ExperimentELN + "/" + ELNDictionary.ExperimentInventory;
}

ELNDictionary.getExperimentKindName = function(identifier, isPlural) {
	var space = identifier.split("/")[1];
	if(profile.isInventorySpace(space)) {
		return (isPlural)?ELNDictionary.ExperimentsInventory:ELNDictionary.ExperimentInventory;
	} else {
		return (isPlural)?ELNDictionary.ExperimentsELN:ELNDictionary.ExperimentELN;
	}
}

ELNDictionary.settingsView = {
	sections : {
		mainMenu : {
			title : "Main Menu",
			info : "This options gives the oportunity to the administrator to show/hide different sections of the user interface from the main menu.",
		},
		forcedDisableRTF : {
			title : "Forced Disable RTF",
			info : "When listing a MULTILINE_TEXT property type on this section the rich text editor will be disabled on the forms.",
		},
		forceMonospaceFont : {
			title : "Forced Monospace Font",
			info : "When listing a MULTILINE_TEXT property type on this section it will use a monospace font.",
		},
		inventorySpaces : {
			title : "Inventory Spaces",
			info : "When listing a SPACE code on this section, it will be treated by the user interface as an invetory space, being shown on the appropriate section.",
		},
		sampleTypeProtocols : {
			title : ELNDictionary.Sample + " Type Protocols",
			info : "When listing a " + ELNDictionary.Sample + " Type on this section, it will be treated as a protocol type. Protocols can be duplicated on the Parents/Children widget.",
		},
		dataSetTypeForFileName : {
			title : "Dataset types for filenames",
			info : "When listing a combination of file extension / " + ELNDictionary.Sample + " Type on this section the dataset uploader will select a dataset type by default. This decision can be overridden by users afterwards but provides a nice default to avoid mistakes.",			
		},
		sampleTypeDefinitionsExtension : {
			title : ELNDictionary.Sample +" Type definitions Extension",
			info : "This section is used to extend the common openBIS definitions to give: 1. Intended parent/children hints. 2. Support annotations for these links using properties.",			
		},
	}
}
