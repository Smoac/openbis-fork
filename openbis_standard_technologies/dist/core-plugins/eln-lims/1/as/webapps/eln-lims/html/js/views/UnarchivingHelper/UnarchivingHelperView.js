function UnarchivingHelperView(unarchivingHelperController, unarchivingHelperModel) {
	this._unarchivingHelperController = unarchivingHelperController;
	this._unarchivingHelperModel = unarchivingHelperModel;
	
	this.repaint = function(views) {
		var _this = this;
		var $header = views.header;
		$header.append($("<h1>").append("Unarchiving Helper"));
		var $btnUnarchive = $("<a>", { "class" : "btn btn-primary", "style" : "margin-top: 10px;"}).append("Unarchive");
		$header.append($btnUnarchive);
		
		var $container = views.content;
		$container.empty();
		$explanationBox = FormUtil.getInfoBox("Data sets are usually archived together in bundles. "
				+ "Unarchiving one means that all data sets of the bundle are unarchived, too. " 
				+ "Note, that all these data sets are unarchived on a scratch disk. " 
				+ "They can be deleted without further notice. "
				+ "If they are needed again they have to be unarchived again.", []);
		$container.append($explanationBox);
		$explanationBox.css("border", "none");
		$container.append($("<div>").text("Please, enter the names/codes of the archived datasets you want " 
				+ "to unarchive, or the names/codes of the experiments/objects which contain those datasets."));
		var $datasetsContainer = $("<div>", { style : "width: 100%;" });
		$container.append(FormUtil.getFieldForComponentWithLabel($datasetsContainer, "Datasets"));
		var datasetsSearchDropdown = new AdvancedEntitySearchDropdown2("Select as many datasets as you need");
		datasetsSearchDropdown.search = this._unarchivingHelperController.searchDataSets;
		datasetsSearchDropdown.renderResult = this.renderDataSets;
		datasetsSearchDropdown.isRequired = true;
		$container.append($("<span>", { style : "font-weight: bold;"}).text("Archiving information:"));
		var $infosContainer = $("<div>", { style : "width: 100%;" });
		$container.append($infosContainer);
		datasetsSearchDropdown.onChange(function(dataSets) {
			var ids = dataSets.map(d => d.id);
			_this._unarchivingHelperController.getInfo(ids, function(infos) {
				$infosContainer.empty();
				var $dataSetsContainer = $("<div>", { style : "width: 100%;" });
				$infosContainer.append($dataSetsContainer);
				_this._addDataSetsTable($dataSetsContainer, ids, infos);
				var $totalSizeContainer = $("<div>", { style : "width: 100%; font-weight: bold;" });
				$totalSizeContainer.append("Unarchiving all of them needs " 
						+ PrintUtil.renderNumberOfBytes(infos["total size"]) + " free scratch disk space.");
				$infosContainer.append($totalSizeContainer);
				$btnUnarchive.off("click");
				$btnUnarchive.click(function() {
					_this._unarchivingHelperController.unarchive(ids, function(success) {
						if (success) {
							Util.showSuccess("Unarchiving has been triggered.");
							datasetsSearchDropdown.clear();
							$infosContainer.empty();
						}
					});
				});
			});
		});
		datasetsSearchDropdown.init($datasetsContainer);

	}
	
	this._addDataSetsTable = function($container, ids, infos) {
		var _this = this;
		
		var renderRightAligned = function(info, property)
		{
			return $("<span>", { style : "float: right;"}).text(info[property]);
		}
		
		var columns = [ {
			label : 'Data Set Code',
			property : 'code',
			sortable : true
		} , {
			label : 'Data Set Size',
			property : 'dataSetSize',
			sortable : true,
			render : function(info, grid) {return renderRightAligned(info, "dataSetSize")}
		} , {
			label : 'Data Sets in Bundle',
			property : 'bundle',
			sortable : false,
			render : function(info, grid) {return renderRightAligned(info, "bundle")}
		} , {
			label : 'Bundle Size',
			property : 'bundleSize',
			sortable : false,
			render : function(info, grid) {return renderRightAligned(info, "bundleSize")}
		}];
		
		var getDataList = function(callback) {
			var data = []
			ids.forEach(function(id) {
				var info = infos[id];
				data.push({
					code : id,
					dataSetSize : PrintUtil.renderNumberOfBytes(info["size"]),
					bundle : info["container"].length,
					bundleSize : PrintUtil.renderNumberOfBytes(info["container size"])
				});
			});
			callback(data);
		}
		
		var dataGrid = new DataGridController(null, columns, [], null, getDataList, null, true, "UNARCHIVING_TABLE");
		dataGrid.init($container);
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