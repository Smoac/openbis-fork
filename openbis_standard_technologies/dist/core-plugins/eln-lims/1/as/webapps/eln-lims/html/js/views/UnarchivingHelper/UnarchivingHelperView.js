function UnarchivingHelperView(unarchivingHelperController, unarchivingHelperModel) {
	this._unarchivingHelperController = unarchivingHelperController;
	this._unarchivingHelperModel = unarchivingHelperModel;
	
	this.repaint = function(views) {
		var _this = this;
		var $header = views.header;
		$header.append($("<h1>").append("Unarchiving Helper"));
		
		var $container = views.content;
		$container.empty();
		$container.append($("<span>").text("Please, enter the names/codes of the archived datasets you want " 
				+ "to unarchive, or the names/codes of the experiments/objects which contain those datasets."));
		var $datasetsContainer = $("<div>", { style : "width: 100%;" });
		$container.append(FormUtil.getFieldForComponentWithLabel($datasetsContainer, "Datasets"));
		var datasetsSearchDropdown = new AdvancedEntitySearchDropdown2("Select as many datasets as you need");
		datasetsSearchDropdown.search = this._unarchivingHelperController.searchDataSets;
		datasetsSearchDropdown.renderResult = this.renderDataSets;
		datasetsSearchDropdown.isRequired = true;
		$container.append($("<span>").text("Archiving information:"));
		var $infosContainer = $("<div>", { style : "width: 100%;" });
		$container.append($infosContainer);
		datasetsSearchDropdown.onChange(function(dataSets) {
			var ids = dataSets.map(d => d.id);
			_this._unarchivingHelperController.getInfo(ids, function(infos) {
				$infosContainer.empty();
				dataSets.forEach(function (dataSet) {
					var info = infos[dataSet.id];
					$infosContainer.append(renderInfo(dataSet.text, info));
				});
				var totalSize = infos["total size"];
				var $totalSizeContainer = $("<div>", { style : "width: 100%;" });
				$totalSizeContainer.append("Unarchiving all of them needs " + totalSize + " bytes free disk space.");
				$infosContainer.append($totalSizeContainer);
				var $btnUnarchive = $('<div>', { 'class' : 'btn btn-default', 'text' : 'Unarchive', 'id' : 'unarchive' });
				$btnUnarchive.click(function() {
					_this._unarchivingHelperController.unarchive(ids);
				});
				$infosContainer.append($btnUnarchive);
			});
		});
		datasetsSearchDropdown.init($datasetsContainer);

	}
	
	
	this.renderDataSets = function(dataSets) {
		var result = []
		for (var i = 0; i < dataSets.length; i++) {
			var dataSet = dataSets[i];
			var label = Util.getDisplayNameForEntity2(dataSet);
			result.push({id: dataSet.permId.permId,
				text: label,
				data: dataSet
				});
		}
		return result;
	}
	
	renderInfo = function(dataSetLabel, info)
	{
		var $infoContainer = $("<div>", { style : "width: 100%;" });
		$infoContainer.append(dataSetLabel);
		var $infoDetailsContainer = $("<div>", { style : "width: 100%;" });
		var dataSetSize = info["size"];
		var container = info["container"];
		var containerSize = info["container size"];
		$infoDetailsContainer.append("has " + dataSetSize + " bytes.");
		if (containerSize > dataSetSize) {
			$infoDetailsContainer.append(" It is part of a bundle of " + container.length 
					+ " datasets. Total size: " + containerSize + " bytes.");
		}
		$infoContainer.append($infoDetailsContainer);
		return $infoContainer;
	}
}