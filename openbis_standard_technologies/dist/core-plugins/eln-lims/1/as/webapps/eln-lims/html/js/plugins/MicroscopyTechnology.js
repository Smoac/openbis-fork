function MicroscopyTechnology() {
	this.init();
}

$.extend(MicroscopyTechnology.prototype, ELNLIMSPlugin.prototype, {
	init: function() {
		
	},
	sampleTypeDefinitionsExtension : {
				"MICROSCOPY_SAMPLE_TYPE" : {
					"SHOW" : false,
					"SAMPLE_CHILDREN_DISABLED" : true,
					"SAMPLE_PARENTS_DISABLED" : true,
					"TOOLBAR" : { CREATE : false, EDIT : true, MOVE : false, COPY : false, DELETE : false, PRINT : true, HIERARCHY_GRAPH : true, HIERARCHY_TABLE : true, UPLOAD_DATASET : false, UPLOAD_DATASET_HELPER : false, EXPORT_ALL : true, EXPORT_METADATA : true }
				},
				"MICROSCOPY_EXPERIMENT" : {
					"SHOW" : false,
					"SAMPLE_CHILDREN_DISABLED" : false,
					"SAMPLE_PARENTS_DISABLED" : true,
					"TOOLBAR" : { CREATE : false, EDIT : true, MOVE : false, COPY : false, DELETE : false, PRINT : true, HIERARCHY_GRAPH : true, HIERARCHY_TABLE : true, UPLOAD_DATASET : false, UPLOAD_DATASET_HELPER : false, EXPORT_ALL : true, EXPORT_METADATA : true }
				}
	},
	dataSetTypeDefinitionsExtension : {
			"MICROSCOPY_ACCESSORY_FILE" : {
				"DATASET_PARENTS_DISABLED" : true,
				"TOOLBAR" : { EDIT : true, MOVE : false, ARCHIVE : true, DELETE : false, HIERARCHY_TABLE : true, EXPORT_ALL : true, EXPORT_METADATA : true }
			},
			"MICROSCOPY_IMG" : {
				"DATASET_PARENTS_DISABLED" : true,
				"TOOLBAR" : { EDIT : true, MOVE : false, ARCHIVE : true, DELETE : false, HIERARCHY_TABLE : true, EXPORT_ALL : true, EXPORT_METADATA : true }
			},
			"MICROSCOPY_IMG_OVERVIEW" : {
				"DATASET_PARENTS_DISABLED" : true,
				"TOOLBAR" : { EDIT : true, MOVE : false, ARCHIVE : true, DELETE : false, HIERARCHY_TABLE : true, EXPORT_ALL : true, EXPORT_METADATA : true }
			},
			"MICROSCOPY_IMG_THUMBNAIL" : {
				"DATASET_PARENTS_DISABLED" : true,
				"TOOLBAR" : { EDIT : true, MOVE : false, ARCHIVE : true, DELETE : false, HIERARCHY_TABLE : true, EXPORT_ALL : true, EXPORT_METADATA : true }
			},
			"MICROSCOPY_IMG_CONTAINER" : {
				"DATASET_PARENTS_DISABLED" : true,
				"TOOLBAR" : { EDIT : true, MOVE : false, ARCHIVE : true, DELETE : false, HIERARCHY_TABLE : true, EXPORT_ALL : true, EXPORT_METADATA : true }
			}
	},
	sampleFormTop : function($container, model) {
		if(model.datasets && model.datasets.length > 0) {
			var imageViewerDataSets = [];
			for(var dsIdx = 0; dsIdx < model.datasets.length; dsIdx++) {
				if(profile.isImageViewerDataSetCode(model.datasets[dsIdx].dataSetTypeCode)) {
					imageViewerDataSets.push(model.datasets[dsIdx].code);
				}
			}
			
			if(imageViewerDataSets.length > 0) {
					require(["openbis-screening", "components/imageviewer/ImageViewerWidget" ], function(openbis, ImageViewerWidget) {
					var screningFacade = new openbis(null);
					screningFacade._internal.sessionToken = mainController.openbisV1._internal.sessionToken;
					
					// Create the image viewer component for the specific data sets
					var widget = new ImageViewerWidget(screningFacade, imageViewerDataSets);
					
					// Render the component and add it to the page
					$container.append($('<legend>').text('Microscopy Viewer'));
					var $imageWidgetContainer = new $('<div>');
					$imageWidgetContainer.css("margin", "20px");
					$container.append($imageWidgetContainer);
					$imageWidgetContainer.append(widget.render());
		   		});
			}
		}
	},
	sampleFormBottom : function($container, model) {
	
	},
	dataSetFormTop : function($container, model) {
		if(model.dataSetV3 && profile.isImageViewerDataSetCode(model.dataSetV3.type.code)) {
    			require(["openbis-screening", "components/imageviewer/ImageViewerWidget" ], function(openbis, ImageViewerWidget) {
				var screningFacade = new openbis(null);
				screningFacade._internal.sessionToken = mainController.openbisV1._internal.sessionToken;
				
		        // Create the image viewer component for the specific data sets
		        var widget = new ImageViewerWidget(screningFacade, [model.dataSetV3.permId.permId]);
		
		        // Render the component and add it to the page
		        $container.append($('<legend>').text('Microscopy Viewer'));
		        var $imageWidgetContainer = new $('<div>');
		        	$imageWidgetContainer.css("margin", "20px");
		        $container.append($imageWidgetContainer);
		        $imageWidgetContainer.append(widget.render());
   		 	});
		}
	},
	dataSetFormBottom : function($container, model) {
		
	}
});