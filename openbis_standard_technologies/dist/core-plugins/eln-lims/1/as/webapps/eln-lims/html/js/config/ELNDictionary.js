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
		storages : {
			title : "Storages",
			info : "Create and browse storages.",
		},
		mainMenu : {
			title : "Main Menu",
			info : "These options give the opportunity to the administrator to show/hide different sections of the user interface from the main menu.",
		},
		forcedDisableRTF : {
			title : "Forced Disable RTF",
			info : "By default all MULTILINE_VARCHAR properties have RTF. Use this section to disable the RTF on specific properties.",
		},
		forceMonospaceFont : {
			title : "Forced Monospace Font",
			info : "Use this section to force the use of monospace font for selected MULTILINE_VARCHAR properties.",
		},
		inventorySpaces : {
			title : "Inventory Spaces",
			info : "By default all new spaces created in openBIS are shown under the Lab Notebook in the main menu. Spaces listed here are shown under the Inventory.",
		},
		dataSetTypeForFileName : {
			title : "Dataset types for filenames",
			info : "When listing a combination of file extension / " + ELNDictionary.Sample + " Type on this section the Dataset uploader will select a Dataset Type by default. This decision can be overridden by users afterwards but provides a nice default to avoid mistakes.",			
		},
		sampleTypeDefinitionsExtension : {
			title : ELNDictionary.Sample +" Type definitions Extension",
			info : "This section is used to extend the common openBIS definitions to give: 1. Intended parent/children hints. 2. Support annotations for these links using properties.",			
		},
	}
}
