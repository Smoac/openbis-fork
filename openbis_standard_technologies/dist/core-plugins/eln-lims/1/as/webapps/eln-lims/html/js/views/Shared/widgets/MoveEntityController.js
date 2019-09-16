function MoveEntityController(entityType, entityPermId) {
	var moveEntityModel = new MoveEntityModel();
	var moveEntityView = new MoveEntityView(this, moveEntityModel);
	
	var searchAndCallback = function(callback) {
		var criteria = { entityKind : entityType, logicalOperator : "AND", rules : { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : entityPermId } } };
		
		switch(entityType) {
			case "EXPERIMENT":
				mainController.serverFacade.searchForExperimentsAdvanced(criteria, null, callback);
				break;
			case "SAMPLE":
				mainController.serverFacade.searchForSamplesAdvanced(criteria, null, callback);
				break;
			case "DATASET":
				mainController.serverFacade.searchForDataSetsAdvanced(criteria, null, callback);
				break;
			case "PROJECT":
				mainController.serverFacade.searchForProjectsAdvanced(criteria, null, callback);
				break;
		}
	};
	
	this.init = function() {
		searchAndCallback(function(result) {
			moveEntityModel.entity = result.objects[0];
			moveEntityView.repaint();
		});
	};
	
	var waitForIndexUpdate = function() {
		searchAndCallback(function(result) {
			var entity = result.objects[0];
			var found = false;
			switch(entityType) {
				case "EXPERIMENT":
					found = entity.getProject().getIdentifier().identifier === moveEntityModel.selected.getIdentifier().identifier;
					break;
				case "SAMPLE":
					found = entity.getExperiment().getIdentifier().identifier === moveEntityModel.selected.getIdentifier().identifier;
					break;
				case "DATASET":
					found = (entity.getSample() && entity.getSample().getIdentifier().identifier === moveEntityModel.selected.getIdentifier().identifier)
							||
							(entity.getExperiment() && entity.getExperiment().getIdentifier().identifier === moveEntityModel.selected.getIdentifier().identifier);
					break;
				case "PROJECT":
					found = entity.getSpace().getPermId().identifier === moveEntityModel.selected.getPermId().identifier;
					break;
			}
			
			if(!found) {
				setTimeout(function(){ waitForIndexUpdate(); }, 300);
			} else {
				Util.showSuccess("Move successfull", function() { 
					Util.unblockUI();
					
					mainController.sideMenu.refreshNodeParent(entity.getPermId().permId); // Refresh old node parent
					mainController.sideMenu.refreshNode(moveEntityModel.selected.getPermId().permId); // New node parent
					
					switch(entityType) {
						case "EXPERIMENT":
							mainController.changeView("showExperimentPageFromIdentifier", entity.getIdentifier().identifier);
							break;
						case "SAMPLE":
							mainController.changeView("showViewSamplePageFromPermId", entity.getPermId().permId);
							break;
						case "DATASET":
							mainController.changeView("showViewDataSetPageFromPermId", entity.getPermId().permId);
							break;
						case "PROJECT":
							mainController.changeView("showProjectPageFromIdentifier", entity.getPermId().permId);
							break;
					}
				});
			}
		});
	}
	
	this.move = function() {
		Util.blockUI();
		
		var done = function() {
			waitForIndexUpdate();
		};
		var fail = function(error) {
			Util.showError("Move failed: " + JSON.stringify(error));
		};
		
		switch(entityType) {
			case "EXPERIMENT":
				require([ "as/dto/experiment/update/ExperimentUpdate"], 
			        function(ExperimentUpdate) {
			            var experimentUpdate = new ExperimentUpdate();
			            experimentUpdate.setExperimentId(moveEntityModel.entity.getIdentifier());
			 			experimentUpdate.setProjectId(moveEntityModel.selected.getIdentifier());
			            mainController.openbisV3.updateExperiments([ experimentUpdate ]).done(done).fail(fail);
        			});
				break;
			case "SAMPLE":
				require([ "as/dto/sample/update/SampleUpdate"], 
			        function(SampleUpdate) {
			            var sampleUpdate = new SampleUpdate();
			            sampleUpdate.setSampleId(moveEntityModel.entity.getIdentifier());
			 			sampleUpdate.setExperimentId(moveEntityModel.selected.getIdentifier());
			            mainController.openbisV3.updateSamples([ sampleUpdate ]).done(done).fail(fail);
        			});
				break;
			case "DATASET":
				require([ "as/dto/dataset/update/DataSetUpdate"], 
			        function(DataSetUpdate) {
			            var datasetUpdate = new DataSetUpdate();
			            datasetUpdate.setDataSetId(moveEntityModel.entity.getPermId());
			            
			            switch(moveEntityModel.selected["@type"]) {
							case "as.dto.experiment.Experiment":
								datasetUpdate.setExperimentId(moveEntityModel.selected.getIdentifier());
							break;
							case "as.dto.sample.Sample":
								datasetUpdate.setSampleId(moveEntityModel.selected.getIdentifier());
							break;
						}
						
			            mainController.openbisV3.updateDataSets([ datasetUpdate ]).done(done).fail(fail);
        			});
				break;
			case "PROJECT":
				require(["as/dto/project/update/ProjectUpdate"], function (ProjectUpdate) {
					var projectUpdate = new ProjectUpdate();
					projectUpdate.setProjectId(moveEntityModel.entity.getIdentifier());
					projectUpdate.setSpaceId(moveEntityModel.selected.getPermId());
					mainController.openbisV3.updateProjects([projectUpdate]).done(done).fail(fail);
				});
				break;
		}
		
	}
}