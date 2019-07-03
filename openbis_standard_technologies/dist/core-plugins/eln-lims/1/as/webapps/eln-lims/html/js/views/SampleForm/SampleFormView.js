/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function SampleFormView(sampleFormController, sampleFormModel) {
	this._sampleFormController = sampleFormController;
	this._sampleFormModel = sampleFormModel;
	
	this.repaint = function(views, loadFromTemplate) {
		var $container = views.content;
		//
		// Form setup
		//
		var _this = this;

		var $form = $("<span>");
		
		var $formColumn = $("<form>", {
			'role' : "form",
			'action' : 'javascript:void(0);'
		});
		
		var $rightPanel = null;
		if(this._sampleFormModel.mode === FormMode.VIEW) {
			$rightPanel = views.auxContent;
		}
		
		$form.append($formColumn);
		
		//
		// Extra Metadata
		//
		var sampleTypeCode = this._sampleFormModel.sample.sampleTypeCode;
		var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		var sampleTypeDefinitionsExtension = profile.sampleTypeDefinitionsExtension[this._sampleFormModel.sample.sampleTypeCode];
		
		//
		// TITLE
		//
		var spaceCode = this._sampleFormModel.sample.spaceCode;
		var projectCode;
		var experimentCode;
		if(this._sampleFormModel.sample.experimentIdentifierOrNull) {	
			var experimentIdentifier = this._sampleFormModel.sample.experimentIdentifierOrNull;
			projectCode = IdentifierUtil.getProjectCodeFromExperimentIdentifier(experimentIdentifier);
			experimentCode = IdentifierUtil.getCodeFromIdentifier(experimentIdentifier);
		}
		var containerSampleIdentifier;
		var containerSampleCode;
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			containerSampleIdentifier = IdentifierUtil.getContainerSampleIdentifierFromContainedSampleIdentifier(this._sampleFormModel.sample.identifier);
			if(containerSampleIdentifier) {
				containerSampleCode = IdentifierUtil.getCodeFromIdentifier(containerSampleIdentifier);
			}
		}
		var sampleCode = this._sampleFormModel.sample.code;
		var samplePermId = this._sampleFormModel.sample.permId;
		var entityPath = FormUtil.getFormPath(spaceCode, projectCode, experimentCode, containerSampleCode, containerSampleIdentifier, sampleCode, samplePermId);
		
		var $formTitle = $("<div>");
		var nameLabel = this._sampleFormModel.sample.properties[profile.propertyReplacingCode];
		if(nameLabel) {
			nameLabel = html.sanitize(nameLabel);
		} else if(this._sampleFormModel.sample.sampleTypeCode === "STORAGE_POSITION") {
			var properties = this._sampleFormModel.sample.properties;
			var storagePropertyGroup = profile.getStoragePropertyGroup();
			var boxProperty = properties[storagePropertyGroup.boxProperty];
			if(!boxProperty) {
				boxProperty = "NoBox";
			}
			var positionProperty = properties[storagePropertyGroup.positionProperty];
			if(!positionProperty) {
				positionProperty = "NoPos";
			}
			nameLabel = boxProperty + " - " + positionProperty;
		} else {
			nameLabel = this._sampleFormModel.sample.code;
		}
		
		var title = null;
		
		switch(this._sampleFormModel.mode) {
	    	case FormMode.CREATE:
	    		title = "Create " + ELNDictionary.Sample + " " + Util.getDisplayNameFromCode(this._sampleFormModel.sample.sampleTypeCode);
	    		break;
	    	case FormMode.EDIT:
	    		title = "Update " + ELNDictionary.Sample + ": " + nameLabel;
	    		break;
	    	case FormMode.VIEW:
	    		title = "" + ELNDictionary.Sample + ": " + nameLabel;
	    		break;
		}
		
		$formTitle
			.append($("<h2>").append(title))
			.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var toolbarConfig = profile.getSampleTypeToolbarConfiguration(_this._sampleFormModel.sample.sampleTypeCode);
		
		if(this._sampleFormModel.mode === FormMode.VIEW) {
			//Create Experiment Step
			if(_this._sampleFormModel.sample.sampleTypeCode === "EXPERIMENTAL_STEP" && _this._allowedToCreateChild()) {
				var $createBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
					var argsMap = {
							"sampleTypeCode" : "EXPERIMENTAL_STEP",
							"experimentIdentifier" : _this._sampleFormModel.sample.experimentIdentifierOrNull
					}
					var argsMapStr = JSON.stringify(argsMap);
					
					mainController.changeView("showCreateSubExperimentPage", argsMapStr);
					
					var setParent = function() {
						mainController.currentView._sampleFormModel.sampleLinksParents.addSample(_this._sampleFormModel.sample);
						Util.unblockUI();
					}
					
					var repeatUntilSet = function() {
						if(mainController.currentView.isLoaded()) {
							setParent();
						} else {
							setTimeout(repeatUntilSet, 100);
						}
					}
					
					repeatUntilSet();
				});
				if(toolbarConfig.CREATE) {
					toolbarModel.push({ component : $createBtn, tooltip: "Create Experimental Step" });
				}
			}
			
			if (_this._allowedToEdit()) {
				//Edit
				if(this._sampleFormModel.mode === FormMode.VIEW) {
					var $editButton = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
						var args = {
								permIdOrIdentifier : _this._sampleFormModel.sample.permId,
								paginationInfo : _this._sampleFormModel.paginationInfo
						}
						
						mainController.changeView('showEditSamplePageFromPermId', args);
					});
					if(toolbarConfig.EDIT) {
						toolbarModel.push({ component : $editButton, tooltip: "Edit" });
					}
				}
			}
			if (_this._allowedToMove()) {
				//Move
				var $moveBtn = FormUtil.getButtonWithIcon("glyphicon-move", function () {
					var moveEntityController = new MoveEntityController("SAMPLE", _this._sampleFormModel.sample.permId);
					moveEntityController.init();
				});
				if(toolbarConfig.MOVE) {
					toolbarModel.push({ component : $moveBtn, tooltip: "Move" });
				}
			}
			if (_this._allowedToDelete()) {
				//Delete
				var warningText = null;
				if(this._sampleFormModel.sample.children.length > 0 || this._sampleFormModel.datasets.length > 0) {
					warningText = ""
						var childrenThatAreNotPositions = 0;
					for(var idx = 0; idx < this._sampleFormModel.sample.children.length; idx++) {
						var child = this._sampleFormModel.sample.children[idx];
						if(child.sampleTypeCode !== "STORAGE_POSITION") {
							childrenThatAreNotPositions++;
						}
					}
					
					if(this._sampleFormModel.sample.children.length > 0) {
						warningText += "The sample has " + childrenThatAreNotPositions + " children samples, these relationships will be broken but the children will remain:\n";
						var numChildrenToShow = childrenThatAreNotPositions;
						if(numChildrenToShow > 10) {
							numChildrenToShow = 10;
						}
						for(var cIdx = 0 ; cIdx < numChildrenToShow; cIdx++) {
							var child = this._sampleFormModel.sample.children[cIdx];
							if(child.sampleTypeCode !== "STORAGE_POSITION") {
								warningText += "\n\t" + child.code;
							}
						}
						if(numChildrenToShow > 10) {
							warningText += "\n\t...";
						}
					}
					if(this._sampleFormModel.datasets.length > 0) {
						warningText += "\n\nThe " + ELNDictionary.sample + " has " + this._sampleFormModel.datasets.length + " datasets, these will be deleted with the " + ELNDictionary.sample + ":\n";
						var numDatasetsToShow = this._sampleFormModel.datasets.length;
						if(numDatasetsToShow > 10) {
							numDatasetsToShow = 10;
						}
						for(var cIdx = 0 ; cIdx < numDatasetsToShow; cIdx++) {
							warningText += "\n\t" + this._sampleFormModel.datasets[cIdx].code;
						}
						if(numDatasetsToShow > 10) {
							warningText += "\n\t...";
						}
					}
				}
				
				var $deleteButton = FormUtil.getDeleteButton(function(reason) {
					_this._sampleFormController.deleteSample(reason);
				}, true, warningText);
				
				if(toolbarConfig.DELETE) {
					toolbarModel.push({ component : $deleteButton, tooltip: "Delete" });
				}
				
			}
			if (_this._allowedToCopy()) {
				//Copy
				var $copyButton = $("<a>", { 'class' : 'btn btn-default'} )
				.append($('<img>', { 'src' : './img/copy-icon.png', 'style' : 'width:16px; height:16px;' }));
				$copyButton.click(_this._getCopyButtonEvent());
				if(toolbarConfig.COPY) {
					toolbarModel.push({ component : $copyButton, tooltip: "Copy" });
				}
			}
				
				
			
			//Print
			var $printButton = $("<a>", { 'class' : 'btn btn-default'} ).append($('<span>', { 'class' : 'glyphicon glyphicon-print' }));
			$printButton.click(function() {
				PrintUtil.printEntity(_this._sampleFormModel.sample);
			});
			if(toolbarConfig.PRINT) {
				toolbarModel.push({ component : $printButton, tooltip: "Print" });
			}
			
			//Hierarchy Graph
			var $hierarchyGraph = FormUtil.getButtonWithImage("./img/hierarchy-icon.png", function () {
				mainController.changeView('showSampleHierarchyPage', _this._sampleFormModel.sample.permId);
			});
			if(toolbarConfig.HIERARCHY_GRAPH) {
				toolbarModel.push({ component : $hierarchyGraph, tooltip: "Hierarchy Graph" });
			}
			
			//Hierarchy Table
			var $hierarchyTable = FormUtil.getButtonWithIcon("glyphicon-list", function () {
				mainController.changeView('showSampleHierarchyTablePage', _this._sampleFormModel.sample.permId);
			});
			if(toolbarConfig.HIERARCHY_TABLE) {
				toolbarModel.push({ component : $hierarchyTable, tooltip: "Hierarchy Table" });
			}
			
			if(_this._allowedToRegisterDataSet()) {
				//Create Dataset
				var $uploadBtn = FormUtil.getButtonWithIcon("glyphicon-upload", function () {
					mainController.changeView('showCreateDataSetPageFromPermId',_this._sampleFormModel.sample.permId);
				});
				if(toolbarConfig.UPLOAD_DATASET) {
					toolbarModel.push({ component : $uploadBtn, tooltip: "Upload Dataset" });
				}
			
				//Get dropbox folder name
				var $uploadBtn = FormUtil.getButtonWithIcon("glyphicon-circle-arrow-up", (function () {
					var nameElements = [
						"O",
						_this._sampleFormModel.sample.spaceCode,
						IdentifierUtil.getProjectCodeFromSampleIdentifier(_this._sampleFormModel.sample.identifier),
						_this._sampleFormModel.sample.code,
					];
					FormUtil.showDropboxFolderNameDialog(nameElements);
				}).bind(this));
				if(toolbarConfig.UPLOAD_DATASET_HELPER) {
					toolbarModel.push({ component : $uploadBtn, tooltip: "Helper tool for Dataset upload using eln-lims dropbox" });
				}
			}
			
			//Export
			var $exportAll = FormUtil.getExportButton([{ type: "SAMPLE", permId : _this._sampleFormModel.sample.permId, expand : true }], false);
			if(toolbarConfig.EXPORT_ALL) {
				toolbarModel.push({ component : $exportAll, tooltip: "Export Metadata & Data" });
			}
			
			var $exportOnlyMetadata = FormUtil.getExportButton([{ type: "SAMPLE", permId : _this._sampleFormModel.sample.permId, expand : true }], true);
			if(toolbarConfig.EXPORT_METADATA) {
				toolbarModel.push({ component : $exportOnlyMetadata, tooltip: "Export Metadata only" });
			}
			
			//Jupyter Button
			if(profile.jupyterIntegrationServerEndpoint) {
				var $jupyterBtn = FormUtil.getButtonWithImage("./img/jupyter-icon.png", function () {
					var jupyterNotebook = new JupyterNotebookController(_this._sampleFormModel.sample);
					jupyterNotebook.init();
				});
				toolbarModel.push({ component : $jupyterBtn, tooltip: "Create Jupyter notebook" });
			}

            //Freeze
            if(_this._sampleFormModel.v3_sample && _this._sampleFormModel.v3_sample.frozen !== undefined) { //Freezing available on the API
                var isEntityFrozen = _this._sampleFormModel.v3_sample.frozen;
                var isEntityFrozenTooltip = (isEntityFrozen)?"Entity Frozen":"Freeze Entity (Disable further modifications)";
            	var $freezeButton = FormUtil.getFreezeButton("SAMPLE", this._sampleFormModel.v3_sample.permId.permId, isEntityFrozen);
                if(toolbarConfig.FREEZE) {
                    toolbarModel.push({ component : $freezeButton, tooltip: isEntityFrozenTooltip });
                }
            }
		} else { //Create and Edit
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._sampleFormController.createUpdateCopySample();
			}, "Save");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn, tooltip: "Save" });
		}
		
		if(this._sampleFormModel.mode !== FormMode.CREATE && this._sampleFormModel.paginationInfo) {
			var moveToIndex = function(index) {
				var pagOptionsToSend = $.extend(true, {}, _this._sampleFormModel.paginationInfo.pagOptions);
				pagOptionsToSend.pageIndex = index;
				pagOptionsToSend.pageSize = 1;
				_this._sampleFormModel.paginationInfo.pagFunction(function(result) {
					if(result && result.objects && result.objects[0] && result.objects[0].permId) {
						_this._sampleFormModel.paginationInfo.currentIndex = index;
						var arg = {
								permIdOrIdentifier : result.objects[0].permId,
								paginationInfo : _this._sampleFormModel.paginationInfo
						}
						mainController.changeView('showViewSamplePageFromPermId', arg);
					} else {
						window.alert("The item to go to is no longer available.");
					}
				}, pagOptionsToSend);
			}
			
			if(this._sampleFormModel.paginationInfo.currentIndex > 0) {
				var $backBtn = FormUtil.getButtonWithIcon("glyphicon-arrow-left", function () {
					moveToIndex(_this._sampleFormModel.paginationInfo.currentIndex-1);
				});
				toolbarModel.push({ component : $backBtn, tooltip: "Go to previous Object from list" });
			}
			
			if(this._sampleFormModel.paginationInfo.currentIndex+1 < this._sampleFormModel.paginationInfo.totalCount) {
				var $nextBtn = FormUtil.getButtonWithIcon("glyphicon-arrow-right", function () {
					moveToIndex(_this._sampleFormModel.paginationInfo.currentIndex+1);
				});
				toolbarModel.push({ component : $nextBtn, tooltip: "Go to next Object from list" });
			}
		}
		
		var $header = views.header;
		
		$header.append($formTitle);
		$header.append(FormUtil.getToolbar(toolbarModel));
		
		//
		// Identification Info on Create
		//
		if(this._sampleFormModel.mode === FormMode.CREATE) {
			this._paintIdentificationInfo($formColumn, sampleType);
		}
		
		// Plugin Hook
		var $sampleFormTop = new $('<div>');
		$formColumn.append($sampleFormTop);
		profile.sampleFormTop($sampleFormTop, _this._sampleFormModel);
		
		//
		// Form Defined Properties from General Section
		//
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			if(propertyTypeGroup.name === "General" || propertyTypeGroup.name === "General info") {
				this._paintPropertiesForSection($formColumn, propertyTypeGroup, i, loadFromTemplate);
			}
		}
		
		//
		// LINKS TO PARENTS
		//
		var requiredParents = [];
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_PARENTS_HINT"]) {
			requiredParents = sampleTypeDefinitionsExtension["SAMPLE_PARENTS_HINT"];
		}
		
		var sampleParentsWidgetId = "sampleParentsWidgetId";
		var $sampleParentsWidget = $("<div>", { "id" : sampleParentsWidgetId });
		
		if(this._sampleFormModel.mode !== FormMode.VIEW || (this._sampleFormModel.mode === FormMode.VIEW && this._sampleFormModel.sample.parents.length > 0)) {
			$formColumn.append($sampleParentsWidget);
		}
		
		
		var isDisabled = this._sampleFormModel.mode === FormMode.VIEW;
		
		var currentParentsLinks = (this._sampleFormModel.sample)?this._sampleFormModel.sample.parents:null;
		var parentsTitle = "Parents";
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_PARENTS_TITLE"]) {
			parentsTitle = sampleTypeDefinitionsExtension["SAMPLE_PARENTS_TITLE"];
		}
		var parentsAnyTypeDisabled = sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_PARENTS_ANY_TYPE_DISABLED"];
		this._sampleFormModel.sampleLinksParents = new LinksController(parentsTitle,
																			requiredParents,
																			isDisabled,
																			currentParentsLinks,
																			this._sampleFormModel.mode === FormMode.CREATE || this._sampleFormModel.mode === FormMode.EDIT,
																			parentsAnyTypeDisabled,
																			sampleTypeCode);
		if(!sampleTypeDefinitionsExtension || !sampleTypeDefinitionsExtension["SAMPLE_PARENTS_DISABLED"]) {
			this._sampleFormModel.sampleLinksParents.init($sampleParentsWidget);
		}
		
		//
		// LINKS TO CHILDREN
		//
		var requiredChildren = [];
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"]) {
			requiredChildren = sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"];
		}
			
		var sampleChildrenWidgetId = "sampleChildrenWidgetId";
		var $sampleChildrenWidget = $("<div>", { "id" : sampleChildrenWidgetId });
		
		if(this._sampleFormModel.mode !== FormMode.VIEW || (this._sampleFormModel.mode === FormMode.VIEW && this._sampleFormModel.sample.children.length > 0)) {
			$formColumn.append($sampleChildrenWidget);
		}
			
		var currentChildrenLinks = (this._sampleFormModel.sample)?this._sampleFormModel.sample.children:null;
		
		var childrenTitle = "Children";
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_TITLE"]) {
			childrenTitle = sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_TITLE"];
		}
		
		var currentChildrenLinksNoStorage = [];
		if(currentChildrenLinks != null) {
			for(var cIdx = 0; cIdx < currentChildrenLinks.length; cIdx++) {
				if(currentChildrenLinks[cIdx].sampleTypeCode !== "STORAGE_POSITION") {
					currentChildrenLinksNoStorage.push(currentChildrenLinks[cIdx]);
				}
			}
		}
		
		var childrenAnyTypeDisabled = sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_ANY_TYPE_DISABLED"];
		this._sampleFormModel.sampleLinksChildren = new LinksController(childrenTitle,
															requiredChildren,
															isDisabled,
															currentChildrenLinksNoStorage,
															this._sampleFormModel.mode === FormMode.CREATE,
															childrenAnyTypeDisabled,
															sampleTypeCode);
		if(!sampleTypeDefinitionsExtension || !sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_DISABLED"]) {
			this._sampleFormModel.sampleLinksChildren.init($sampleChildrenWidget);
		}
		
		//
		// GENERATE CHILDREN
		//
		var childrenDisabled = sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_DISABLED"];
		
		if((this._sampleFormModel.mode !== FormMode.VIEW) && this._sampleFormModel.isELNSample && !childrenDisabled) {
			var $generateChildrenBtn = $("<a>", { 'class' : 'btn btn-default', 'style' : 'margin-top:15px;', 'id' : 'generate_children'}).text("Generate Children");
			$generateChildrenBtn.click(function(event) {
				_this._generateChildren();
			});
			
			var $generateChildrenBox = $("<div>")
											.append($("<div>", { 'class' : 'form-group' }).append($generateChildrenBtn))
											.append($("<div>", { 'id' : 'newChildrenOnBenchDropDown' }))
			$formColumn.append($generateChildrenBox);
		}
		
		//
		// Form Defined Properties from non General Section
		//
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			if(propertyTypeGroup.name !== "General" && propertyTypeGroup.name !== "General info") {
				this._paintPropertiesForSection($formColumn, propertyTypeGroup, i, loadFromTemplate);
			}
		}
		
		//
		// Plate View
		//
		if(this._sampleFormModel.sample.sampleTypeCode === "PLATE" && this._sampleFormModel.mode !== FormMode.CREATE) {
			var plateContainer = $("<div>", { 'id' : 'sample-form-plate-view' });
			$formColumn.append($("<legend>").append("Plate"));
			var plateController = new PlateController(this._sampleFormModel.sample, this._sampleFormModel.mode !== FormMode.EDIT);
			plateController.init(plateContainer);
			$formColumn.append(plateContainer);
			this._sampleFormController._plateController = plateController;
		}
		
		//
		// Storage
		//
		var isStorageAvailable = profile.isSampleTypeWithStorage(this._sampleFormModel.sample.sampleTypeCode);
		if(isStorageAvailable && profile.storagesConfiguration["isEnabled"]) {
			var $fieldsetOwner = $("<div>");
			var $legend = $("<legend>").append("Storage");
			var storageListContainer = $("<div>", { 'id' : 'sample-form-storage-list' });
			$fieldsetOwner.append($legend);
			$fieldsetOwner.append(storageListContainer);
			$formColumn.append($fieldsetOwner);
			
			$legend.prepend(FormUtil.getShowHideButton(storageListContainer, "SAMPLE-" + this._sampleFormModel.sample.sampleTypeCode + "-storage"));
			
			var storageListController = new StorageListController(this._sampleFormModel.sample, this._sampleFormModel.mode === FormMode.VIEW);	
			storageListController.init(storageListContainer);
		}
		
		//
		// Extra Content
		//
		$formColumn.append($("<div>", { 'id' : 'sample-form-content-extra' }));
		
		// Plugin Hook
		var $sampleFormBottom = new $('<div>');
		$formColumn.append($sampleFormBottom);
		profile.sampleFormBottom($sampleFormBottom, _this._sampleFormModel);
		
		//
		// Identification Info on View/Edit
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			this._paintIdentificationInfo($formColumn);
		}
		
		//
		// PREVIEW IMAGE
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			var $previewImage = $("<img>", { 'data-preview-loaded' : 'false',
											 'class' : 'zoomableImage',
											 'id' : 'preview-image',
											 'src' : './img/image_loading.gif',
											 'style' : 'max-width:100%; display:none;'
											});
			$previewImage.click(function() {
				Util.showImage($("#preview-image").attr("src"));
			});
			
			if($rightPanel !== null) { //Min Desktop resolution
				$rightPanel.append($previewImage);
			} else {
				$formColumn.append($previewImage);
			}
		}
		
		//
		// DATASETS
		//
		var $dataSetViewerContainer = $("<div>", { 'id' : 'dataSetViewerContainer', 'style' : 'margin-top:10px;'});
		if($rightPanel) {
			$rightPanel.append($dataSetViewerContainer);
		} else {
			$formColumn.append($dataSetViewerContainer);
		}
		
		if(this._sampleFormModel.mode === FormMode.VIEW && _this._allowedToRegisterDataSet()) {
			var $inlineDataSetForm = $("<div>");
			if($rightPanel) {
				$rightPanel.append($inlineDataSetForm);
			} else {
				$formColumn.append($inlineDataSetForm);
			}
			var $dataSetFormController = new DataSetFormController(this, FormMode.CREATE, this._sampleFormModel.sample, null, true);
			var viewsForDS = {
					content : $inlineDataSetForm
			}
			$dataSetFormController.init(viewsForDS);
		}
		
		//
		// INIT
		//
		$container.append($form);
		
		//
		// Extra content
		//
		//Extra components
		try {
			profile.sampleFormContentExtra(this._sampleFormModel.sample.sampleTypeCode, this._sampleFormModel.sample, "sample-form-content-extra");
		} catch(err) {
			Util.manageError(err);
		}
		
		//
		// TO-DO: Legacy code to be refactored
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			//Preview image
			this._reloadPreviewImage();
			
			// Dataset Viewer
			this._sampleFormModel.dataSetViewer = new DataSetViewerController("dataSetViewerContainer", profile, this._sampleFormModel.sample, mainController.serverFacade, profile.getDefaultDataStoreURL(), this._sampleFormModel.datasets, false, true);
			this._sampleFormModel.dataSetViewer.init();
		}
		
		this._sampleFormModel.isFormLoaded = true;
	}
	
	this._paintPropertiesForSection = function($formColumn, propertyTypeGroup, i, loadFromTemplate) {
		var _this = this;
		var sampleTypeCode = this._sampleFormModel.sample.sampleTypeCode;
		var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
		var $fieldsetOwner = $('<div>');
		var $fieldset = $('<div>');
		var $legend = $('<legend>');
		
		$fieldsetOwner.append($legend).append($fieldset);
		
		if((propertyTypeGroup.name !== null) && (propertyTypeGroup.name !== "")) {
			$legend.text(propertyTypeGroup.name);
		} else if((i === 0) || ((i !== 0) && (sampleType.propertyTypeGroups[i-1].name !== null) && (sampleType.propertyTypeGroups[i-1].name !== ""))) {
			$legend.text("Metadata");
		} else {
			$legend.remove();
		}
		
		var propertyGroupPropertiesOnForm = 0;
		for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
			var propertyType = propertyTypeGroup.propertyTypes[j];
			FormUtil.fixStringPropertiesForForm(propertyType, this._sampleFormModel.sample);
			if(!propertyType.showInEditViews && this._sampleFormModel.mode === FormMode.EDIT && propertyType.code !== "$XMLCOMMENTS") { //Skip
				continue;
			} else if(propertyType.dinamic && this._sampleFormModel.mode === FormMode.CREATE) { //Skip
				continue;
			}
			
			if(propertyType.code === "$ANNOTATIONS_STATE" || propertyType.code === "$FREEFORM_TABLE_STATE" || propertyType.code === "$ORDER.ORDER_STATE" ) {
				continue;
			} else if(propertyType.code === "$XMLCOMMENTS") {
				var $commentsContainer = $("<div>");
				$fieldset.append($commentsContainer);
				var isAvailable = this._sampleFormController._addCommentsWidget($commentsContainer);
				if(!isAvailable) {
					continue;
				}
			} else {
				if(propertyType.code === "$SHOW_IN_PROJECT_OVERVIEW") {
					if(!(profile.inventorySpaces.length > 0 && $.inArray(this._sampleFormModel.sample.spaceCode, profile.inventorySpaces) === -1)) {
						continue;
					}
				}
				var $controlGroup =  null;
				var value = this._sampleFormModel.sample.properties[propertyType.code];
				if(!value && propertyType.code.charAt(0) === '$') {
					value = this._sampleFormModel.sample.properties[propertyType.code.substr(1)];
					this._sampleFormModel.sample.properties[propertyType.code] = value;
					delete this._sampleFormModel.sample.properties[propertyType.code.substr(1)];
				}
				
				if(this._sampleFormModel.mode === FormMode.VIEW) { //Show values without input boxes if the form is in view mode
					if(Util.getEmptyIfNull(value) !== "") { //Don't show empty fields, whole empty sections will show the title
						$controlGroup = FormUtil.createPropertyField(propertyType, value);
					} else {
						continue;
					}
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType, value);
					
					//Update values if is into edit mode
					if(this._sampleFormModel.mode === FormMode.EDIT || loadFromTemplate) {
						if(propertyType.dataType === "BOOLEAN") {
							$($($component.children()[0]).children()[0]).prop('checked', value === "true");
						} else if(propertyType.dataType === "TIMESTAMP") {
						} else {
							$component.val(value);
						}
					} else {
						$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
					}
						
					var changeEvent = function(propertyType) {
						return function(jsEvent, newValue) {
							var propertyTypeCode = null;
							propertyTypeCode = propertyType.code;
							_this._sampleFormModel.isFormDirty = true;
							var field = $(this);
							if(propertyType.dataType === "BOOLEAN") {
								_this._sampleFormModel.sample.properties[propertyTypeCode] = $(field.children()[0]).children()[0].checked;
							} else if (propertyType.dataType === "TIMESTAMP") {
								var timeValue = $($(field.children()[0]).children()[0]).val();
								_this._sampleFormModel.sample.properties[propertyTypeCode] = timeValue;
							} else {
								if(newValue !== undefined && newValue !== null) {
									_this._sampleFormModel.sample.properties[propertyTypeCode] = Util.getEmptyIfNull(newValue);
								} else {
									_this._sampleFormModel.sample.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
								}
							}
						}
					}
					
					//Avoid modifications in properties managed by scripts
					if(propertyType.managed || propertyType.dinamic) {
						$component.prop('disabled', true);
					}
					
					if(propertyType.dataType === "MULTILINE_VARCHAR") {
						$component = FormUtil.activateRichTextProperties($component, changeEvent(propertyType), propertyType);
					} else if(propertyType.dataType === "TIMESTAMP") {
						$component.on("dp.change", changeEvent(propertyType));
					} else {
						$component.change(changeEvent(propertyType));
					}
					
					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}
				$fieldset.append($controlGroup);
			}
			
			if(propertyType.code !== "$ANNOTATIONS_STATE") {
				propertyGroupPropertiesOnForm++;
			}	
		}
			
		if(propertyGroupPropertiesOnForm === 0) {
			$legend.remove();
		}
		
		$legend.prepend(FormUtil.getShowHideButton($fieldset, "SAMPLE-" + sampleTypeCode + "-" + propertyTypeGroup.name));
		$formColumn.append($fieldsetOwner);
			
		return false;
	}
	
	this._paintIdentificationInfo = function($formColumn, sampleType) {
		var _this = this;
		//
		// Identification Info
		//
		var $fieldsetOwner = $("<div>");
		var $legend = $("<legend>").append("Identification Info");
		var $fieldset = $("<div>");
		
		$fieldsetOwner.append($legend);
		$fieldsetOwner.append($fieldset);
		
		$fieldset.append(FormUtil.getFieldForLabelWithText("Type", this._sampleFormModel.sample.sampleTypeCode));
		if(this._sampleFormModel.sample.experimentIdentifierOrNull) {
			$fieldset.append(FormUtil.getFieldForLabelWithText(ELNDictionary.getExperimentKindName(this._sampleFormModel.sample.experimentIdentifierOrNull), this._sampleFormModel.sample.experimentIdentifierOrNull));
		}
		
		//
		// Identification Info - Code
		//
		var $codeField = null;
		if(this._sampleFormModel.mode === FormMode.CREATE) {
			var $textField = FormUtil._getInputField('text', null, "Code", null, true);
			$textField.keyup(function(event){
				var textField = $(this);
				var caretPosition = this.selectionStart;
				textField.val(textField.val().toUpperCase());
				this.selectionStart = caretPosition;
				this.selectionEnd = caretPosition;
				_this._sampleFormModel.sample.code = textField.val();
				_this._sampleFormModel.isFormDirty = true;
			});
			$codeField = FormUtil.getFieldForComponentWithLabel($textField, "Code");
			
			mainController.serverFacade.generateCode(sampleType, function(autoGeneratedCode) {
				$textField.val(autoGeneratedCode);
				_this._sampleFormModel.sample.code = autoGeneratedCode;
				_this._sampleFormModel.isFormDirty = true;
			});
			
		} else {
			$codeField = FormUtil.getFieldForLabelWithText("Code", this._sampleFormModel.sample.code);
		}
		
		$fieldset.append($codeField);
		
		//
		// Identification Info - Registration and modification times
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._sampleFormModel.sample.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$fieldset.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)))
			$fieldset.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$fieldset.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$fieldset.append($modificationDate);
		}
		
		$legend.prepend(FormUtil.getShowHideButton($fieldset, "SAMPLE-" + this._sampleFormModel.sample.sampleTypeCode + "-identificationInfo"));
		$formColumn.append($fieldsetOwner);
	}
	
	//
	// TO-DO: Legacy code to be refactored
	//
	
	//
	// Copy Sample pop up
	//
	this._getCopyButtonEvent = function() {
		var _this = this;
		return function() {
			Util.blockUINoMessage();
			
			var copyFunction = function(defaultCode) {
				var component = "<div>"
					component += "<legend>Duplicate Entity</legend>";
					component += "<div>";
					component += "<div class='form-group'>";
					component += "<label class='control-label'>Options </label>";
					component += "<div class='controls'>";
					component += "<span class='checkbox'><label><input type='checkbox' id='linkParentsOnCopy'> Link Parents </label></span>";
					component += "<span class='checkbox'><label><input type='checkbox' id='copyChildrenOnCopy'> Copy Children </label></span>";
					component += "<span class='checkbox'><label><input type='checkbox' id='copyCommentsLogOnCopy'> Copy Comments Log </label></span>";
					component += "</div>";
					component += "</div>";
					component += "</div>";
					component += "<div class='form-group'>";
					component += "<label class='control-label'>Code&nbsp;(*):</label>";
					component += "<div>";
					component += "<input type='text' class='form-control' placeholder='Code' id='newSampleCodeForCopy' pattern='[a-zA-Z0-9_\\-\\.]+' required>";
					component += "</div>";
					component += "<div>";
					component += " (Allowed characters are: letters, numbers, '-', '_', '.')";
					component += "</div>";
					component += "</div>";
				
				Util.blockUI(component + "<a class='btn btn-default' id='copyAccept'>Accept</a> <a class='btn btn-default' id='copyCancel'>Cancel</a>", FormUtil.getDialogCss());
				
				$("#newSampleCodeForCopy").on("keyup", function(event) {
					$(this).val($(this).val().toUpperCase());
				});
				
				$("#newSampleCodeForCopy").val(defaultCode);
				
				$("#copyAccept").on("click", function(event) {
					var newSampleCodeForCopy = $("#newSampleCodeForCopy");
					var linkParentsOnCopy = $("#linkParentsOnCopy")[0].checked;
					var copyChildrenOnCopy = $("#copyChildrenOnCopy")[0].checked;
					var copyCommentsLogOnCopy = $("#copyCommentsLogOnCopy")[0].checked;
					var isValid = newSampleCodeForCopy[0].checkValidity();
					if(isValid) {
						var newSampleCodeForCopyValue = newSampleCodeForCopy.val();
						_this._sampleFormController.createUpdateCopySample(newSampleCodeForCopyValue, linkParentsOnCopy, copyChildrenOnCopy, copyCommentsLogOnCopy);
					} else {
						Util.showError("Invalid code.", function() {}, true);
					}
				});
				
				$("#copyCancel").on("click", function(event) { 
					Util.unblockUI();
				});
			};
			
			var spaceCode = _this._sampleFormModel.sample.spaceCode;
			if(profile.isInventorySpace(spaceCode)) {
				var sampleType = profile.getSampleTypeForSampleTypeCode(_this._sampleFormModel.sample.sampleTypeCode);
				mainController.serverFacade.generateCode(sampleType, copyFunction);
			} else {
				_this._sampleFormController.getNextCopyCode(copyFunction);
			}
			
		}
	}
	
	//
	// Preview Image
	//
	this._reloadPreviewImage = function() {
		var _this = this;
		var previewCallback = 
			function(data) {
				if (data.result.length == 0) {
					_this._updateLoadingToNotAvailableImage();
				} else {
					var listFilesForDataSetCallback = 
						function(dataFiles) {
							var found = false;
							if(!dataFiles.result) {
								//DSS Is not running probably
							} else {
								for(var pathIdx = 0; pathIdx < dataFiles.result.length; pathIdx++) {
									if(!dataFiles.result[pathIdx].isDirectory) {
										var elementId = 'preview-image';
										var downloadUrl = profile.getDefaultDataStoreURL() + '/' + data.result[0].code + "/" + dataFiles.result[pathIdx].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
										
										var img = $("#" + elementId);
										img.attr('src', downloadUrl);
										img.attr('data-preview-loaded', 'true');
										img.show();
										break;
									}
								}
							}
						};
					mainController.serverFacade.listFilesForDataSet(data.result[0].code, "/", true, listFilesForDataSetCallback);
				}
			};
		
		mainController.serverFacade.searchDataSetsWithTypeForSamples("ELN_PREVIEW", [this._sampleFormModel.sample.permId], previewCallback);
	}
	
	this._updateLoadingToNotAvailableImage = function() {
		var notLoadedImages = $("[data-preview-loaded='false']");
		notLoadedImages.attr('src', "./img/image_unavailable.png");
	}
	
	//
	// Children Generator
	//
	this._childrenAdded = function() {
		FormUtil.getDefaultBenchDropDown('childrenStorageSelector', true, function($childrenStorageDropdown) {
			if($childrenStorageDropdown && !$("#childrenStorageSelector").length) {
				var $childrenStorageDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenStorageDropdown, 'Storage');
				$("#newChildrenOnBenchDropDown").append($childrenStorageDropdownWithLabel);
			}
		});
	}
	
	this._generateChildren = function() {
		var _this = this;
		// Utility self contained methods
		var getGeneratedChildrenCodes = function() {
			//Get selected parents
			var $parentsFields = $("#parentsToGenerateChildren").find("input:checked");
			//Group parents by type - this structure helps the create children algorithm
			var selectedParentsByType = {};
			for(var i = 0; i < $parentsFields.length; i++) {
				var parentIdentifier = $parentsFields[i].id;
				var parent = parentsByIdentifier[parentIdentifier];
				var typeList = selectedParentsByType[parent.sampleTypeCode];
				if(!typeList) {
					typeList = [];
					selectedParentsByType[parent.sampleTypeCode] = typeList;
				}
				typeList.push(parent);
			}
			//Generate Children from parents
			var generatedChildren = [];
			var parentSampleCode = _this._sampleFormModel.sample.code;
			for(var sampleTypeCode in selectedParentsByType) {
				var parentsOfType = selectedParentsByType[sampleTypeCode];
				
				var newGeneratedChildren = [];
				
				for(var i = 0; i < parentsOfType.length; i++) {
					var parentOfType = parentsOfType[i];
					if(generatedChildren.length === 0) {
						newGeneratedChildren.push(parentSampleCode + "_" + parentOfType.code);
					} else {
						for(var k = 0; k < generatedChildren.length; k++) {
							newGeneratedChildren.push(generatedChildren[k] + "_" + parentOfType.code);
						}
					}
				}
				
				generatedChildren = newGeneratedChildren;
			}
			
			//Number of Replicas
			var numberOfReplicas = parseInt($("#childrenReplicas").val());
			if(isNaN(numberOfReplicas) || numberOfReplicas < 0 || numberOfReplicas > 1000) {
				Util.showUserError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
				return;
			}
			
			var generatedChildrenWithReplicas = [];
			for(var i = 0; i < generatedChildren.length; i++) {
				for(var j = 0; j < numberOfReplicas; j++) {
					generatedChildrenWithReplicas.push(generatedChildren[i] + "_" + (j + 1));
				}
			}
			
			return generatedChildrenWithReplicas;
		}
		
		var showPreview = function() {
			$("#previewChildrenGenerator").empty();
			
			var generatedChildren = getGeneratedChildrenCodes();
			//Show generated children
			if(generatedChildren) {
				for(var i = 0; i < generatedChildren.length; i++) {
					$("#previewChildrenGenerator").append(generatedChildren[i] + "<br />");
				}
			}
		}
		
		var _this = this;
		// Buttons
		var $generateButton = $("<a>", { "class" : "btn btn-default" }).append("Generate!");
		$generateButton.click(function(event) { 
			var generatedChildrenSpace = _this._sampleFormModel.sample.spaceCode;
			var generatedChildrenProject = IdentifierUtil.getProjectCodeFromSampleIdentifier(_this._sampleFormModel.sample.identifier);
			var numberOfReplicas = parseInt($("#childrenReplicas").val());
			if(isNaN(numberOfReplicas) || numberOfReplicas < 0 || numberOfReplicas > 1000) {
				Util.showUserError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
				return;
			}
			var generatedChildrenCodes = getGeneratedChildrenCodes();
			var generatedChildrenType = $("#childrenTypeSelector").val();
			if(generatedChildrenType === "") {
				Util.showUserError("Please select the children type.", function() {}, true);
			} else {
				for(var i = 0; i < generatedChildrenCodes.length; i++) {
					var virtualSample = new Object();
					virtualSample.newSample = true;
					virtualSample.permId = Util.guid();
					virtualSample.code = generatedChildrenCodes[i];
					virtualSample.identifier = IdentifierUtil.getSampleIdentifier(generatedChildrenSpace, generatedChildrenProject, virtualSample.code);
					virtualSample.sampleTypeCode = generatedChildrenType;
					_this._sampleFormModel.sampleLinksChildren.addVirtualSample(virtualSample);
				}
				
				_this._childrenAdded();
				Util.unblockUI();
			}
			
			
		});
		
		var $cancelButton = $("<a>", { "class" : "btn btn-default" }).append("<span class='glyphicon glyphicon-remove'></span>");
		$cancelButton.click(function(event) { 
			Util.unblockUI();
		});
		
		var $selectAllButton = $("<a>", { "class" : "btn btn-default" }).append("Enable/Disable All");
		$selectAllButton.attr("ison", "false");
		
		$selectAllButton.click(function(event) {
			var $button = $(this);
			var isOn = !($button.attr("ison") === "true");
			$button.attr("ison", isOn);
			
			var $parentsFields = $("#parentsToGenerateChildren").find("input");
			for(var i = 0; i < $parentsFields.length; i++) {
				var $parentField = $parentsFields[i];
				$parentField.checked = isOn;
			}
			
			showPreview();
		});
		
		// Parents
		var $parents = $("<div>");
		var parentsIdentifiers = _this._sampleFormModel.sampleLinksParents.getSamplesIdentifiers();
		var parentsByType = {}; //This is the main model
		var parentsByIdentifier = {}; // Used by the getGeneratedChildrenCodes function
		for(var i = 0; i < parentsIdentifiers.length; i++) {
			var parent = _this._sampleFormModel.sampleLinksParents.getSampleByIdentifier(parentsIdentifiers[i]);
			var typeList = parentsByType[parent.sampleTypeCode];
			if(!typeList) {
				typeList = [];
				parentsByType[parent.sampleTypeCode] = typeList;
			}
			typeList.push(parent);
			parentsByIdentifier[parent.identifier] = parent;
		}
		
		var $parentsTable = $("<table>", { "class" : "table table-bordered table-compact" });
		var $headerRow = $("<tr>");
		$parentsTable.append($headerRow);
		var maxDepth = 0;
		for (var key in parentsByType) {
			$headerRow.append($("<th>", {"class" : "text-center-important"}).text(key));
			maxDepth = Math.max(maxDepth, parentsByType[key].length);
		}

		for (var idx = 0; idx < maxDepth; idx++) {
			var $tableRow = $("<tr>");
			for (key in parentsByType) {
				if (idx < parentsByType[key].length) {
					var parent = parentsByType[key][idx];
					var parentProperty = {
							code : parent.identifier,
							description : parent.identifier,
							label : parent.code,
							dataType : "BOOLEAN"
					};
					
					var $checkBox = $('<input>', {'style' : 'margin-bottom:7px;', 'type' : 'checkbox', 'id' : parent.identifier, 'alt' : parent.identifier, 'placeholder' : parent.identifier });
					$checkBox.change(function() { 
						showPreview();
					});
					
					var $field = $('<div>');
					$field.append($checkBox);
					$field.append(" " + parent.code);
					
					$tableRow.append($("<td>").append($field));
 				} else {
 					$tableRow.append($("<td>").html("&nbsp;"));
 				}
			}
			$parentsTable.append($tableRow);
		}
		
		$parents.append($parentsTable);
		
		var $parentsComponent = $("<div>", { "id" : 'parentsToGenerateChildren'} );
		$parentsComponent.append($("<legend>").append("Parents ").append($selectAllButton))
		$parentsComponent.append($parents);
		
		// Children
		var $childrenComponent = $("<div>");
		$childrenComponent.append($("<legend>").text("Children"))
		
		var $childrenTypeDropdown = FormUtil.getSampleTypeDropdown('childrenTypeSelector', true);
		var $childrenTypeDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenTypeDropdown, 'Type');
		$childrenComponent.append($childrenTypeDropdownWithLabel);
		
		var $childrenReplicas = FormUtil._getInputField('number', 'childrenReplicas', 'Children Replicas', '1', true);
		$childrenReplicas.val("1");
		$childrenReplicas.keyup(function() { 
			showPreview();
		});
		
		var $childrenReplicasWithLabel = FormUtil.getFieldForComponentWithLabel($childrenReplicas, 'Children Replicas');
		$childrenComponent.append($childrenReplicasWithLabel);
		
		// Preview
		var $previewComponent = $("<div>");
		$previewComponent.append($("<legend>").append("Preview"));
		$previewComponent.append($("<div>", {"id" : "previewChildrenGenerator"}));
		
		// Mounting the widget with the components
		var $childrenGenerator = $("<div>");
		$childrenGenerator.append($("<div>", {"style" : "text-align:right;"}).append($cancelButton));
		$childrenGenerator.append($("<form>")
									.append($("<h1>").append("Children Generator"))
									.append($parentsComponent)
									.append($childrenComponent)
									.append($previewComponent)
									.append($("<br>")).append($generateButton)
								);
		
		// Show Widget
		Util.blockUI($childrenGenerator, {'text-align' : 'left', 'top' : '10%', 'width' : '80%', 'left' : '10%', 'right' : '10%', 'height' : '80%', 'overflow' : 'auto'});
	}
	
	this._allowedToCreateChild = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.frozenForChildren == false && sample.experiment.frozenForSamples == false;
	}
	
	this._allowedToEdit = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.frozen == false;
	}
	
	this._allowedToMove = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.experiment.frozenForSamples == false;
	}
	
	this._allowedToDelete = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.frozen == false && sample.experiment.frozenForSamples == false;
	}
	
	this._allowedToCopy = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.experiment.frozenForSamples == false;
	}
	
	this._allowedToRegisterDataSet = function() {
		var sample = this._sampleFormModel.v3_sample;
		return sample.frozenForDataSets == false && sample.experiment.frozenForDataSets == false;
	}
}