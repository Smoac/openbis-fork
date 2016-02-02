
function DeBockLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(DeBockLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		StandardProfile.prototype.init.call(this, serverFacade);
	
		
}
});
