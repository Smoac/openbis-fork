
function SanchezLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(SanchezLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
	
		
}
});
