function BioATProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(BioATProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		//Put on this list all experiment types, ELN experiments need to have both an experiment type and a sample type with the same CODE.
		this.ELNExperiments = ["SYSTEM_EXPERIMENT"];
		
		//Black list, put on this list all types that you don�t want to appear on the menu and the ELN experiments.
		this.notShowTypes = ["SYSTEM_EXPERIMENT", "COMPOUND", "CONTROL_WELL", "LIBRARY", "ORF", "ORF_WELL", "SHRNA", "SHRNA_WELL", "SIRNA", "SIRNA_WELL", "UNKNOWN"];
		
		//The properties you want to appear on the tables, if you don�t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {};
		
		//The configuration for the visual storages.
		this.storagesConfiguration = {
			"isEnabled" : false
		};
}
});