function ELNLIMSPlugin() {
	this.init();
}

$.extend(ELNLIMSPlugin.prototype, {
	init: function() {
	
	},
	forcedDisableRTF : ["$NAME"],
	forceMonospaceFont : [],
	sampleTypeDefinitionsExtension : {
	
	},
	dataSetTypeDefinitionsExtension : {
	
	},
	sampleFormTop : function($container, model) {
	
	},
	sampleFormBottom : function($container, model) {
	
	},
	dataSetFormTop : function($container, model) {
	
	},
	onSampleSave : function(sample, changesToDo, success, failed) {
        success();
	},
	dataSetFormBottom : function($container, model) {
	
	}
});