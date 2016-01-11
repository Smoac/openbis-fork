
function SanchezLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(SanchezLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		StandardProfile.prototype.init.call(this, serverFacade);
	
		
}
});
