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
			info : "CHANGE ME",
		},
		forcedDisableRTF : {
			title : "Forced Disable RTF",
			info : "CHANGE ME",
		},
		forceMonospaceFont : {
			title : "Forced Monospace Font",
			info : "CHANGE ME",
		},
		inventorySpaces : {
			title : "Inventory Spaces",
			info : "CHANGE ME",
		},
		sampleTypeProtocols : {
			title : "Sample Type Protocols",
			info : "CHANGE ME",
		},
		dataSetTypeForFileName : {
			title : "Dataset types for filenames",
			info : "CHANGE ME",			
		},
		sampleTypeDefinitionsExtension : {
			title : "Sample type definitions",
			info : "CHANGE ME",			
		},
	}
}
