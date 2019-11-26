function UnarchivingHelperController(mainController) {
	this._mainController = mainController;
	this._unarchivingHelperModel = new UnarchivingHelperModel();
	this._unarchivingHelperView = new UnarchivingHelperView(this, this._unarchivingHelperModel);
	
	this.init = function(views) {
		var _this = this;
		_this._unarchivingHelperView.repaint(views);
	}

	this.searchDataSets = function(query, callback) {
		var _this = this;
		require([ "as/dto/dataset/search/DataSetSearchCriteria", "as/dto/dataset/fetchoptions/DataSetFetchOptions" ],
			function(DataSetSearchCriteria, DataSetFetchOptions) {
				var searchCriteria = new DataSetSearchCriteria();
				_populateSearchCriteria(searchCriteria, query);
				var fetchOptions = new DataSetFetchOptions();
				fetchOptions.withPhysicalData();
				fetchOptions.withExperiment();
				fetchOptions.withSample();
				mainController.openbisV3.searchDataSets(searchCriteria, fetchOptions).done(function(results) {
					var archivedDataSets = results.getObjects().filter(function (dataSet) {
						return dataSet.getPhysicalData().getStatus() === "ARCHIVED";
					});
					callback(archivedDataSets);
				});
			});
	}
	
	_populateSearchCriteria = function(searchCriteria, query) {
		searchCriteria.withOrOperator();
		searchCriteria.withProperty("$NAME").thatContains(query);
		searchCriteria.withCode().thatContains(query);
		searchCriteria.withSample().withProperty("$NAME").thatContains(query);
		searchCriteria.withSample().withCode().thatContains(query);
		searchCriteria.withExperiment().withProperty("$NAME").thatContains(query);
		searchCriteria.withExperiment().withCode().thatContains(query);
	}

	this.getInfo = function(ids, callback) {
		mainController.serverFacade.getArchivingInfo(ids, function(info) {
			callback(info);
		});
	}
	
	this.unarchive = function(ids) {
		alert(ids);
	}
}