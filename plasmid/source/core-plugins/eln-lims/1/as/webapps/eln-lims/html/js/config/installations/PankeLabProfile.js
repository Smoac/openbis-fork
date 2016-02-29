
function PankeLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(PankeLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		StandardProfile.prototype.init.call(this, serverFacade);
	
		
}
});
