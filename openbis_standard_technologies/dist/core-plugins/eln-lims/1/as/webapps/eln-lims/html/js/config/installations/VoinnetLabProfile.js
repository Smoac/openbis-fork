
function VoinnetLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(VoinnetLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		StandardProfile.prototype.init.call(this, serverFacade);
	
		
}
});
