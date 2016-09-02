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