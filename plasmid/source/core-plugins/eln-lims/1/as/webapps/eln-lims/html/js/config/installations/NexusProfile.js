
function NexusProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(NexusProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.inventorySpaces = ["LIBRARIES"];
		
		this.storagesConfiguration = {
			"isEnabled" : false
		};
		
		this.sampleTypeDefinitionsExtension = {}
}
});
