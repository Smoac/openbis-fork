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
	
	this.repaint = function($container) {
		//
		// Form setup
		//
		var _this = this;
		$container.empty();
		
		var $form = $("<span>", { "class" : "row col-md-8"});
		
		var $formColumn = $("<form>", {
			"class" : "form-horizontal form-panel-one", 
			'role' : "form",
			'action' : 'javascript:void(0);',
			'onsubmit' : 'mainController.currentView.createUpdateCopySample();'
		});
		
		var $rightPanel = $("<span>", { "class" : "row col-md-4 form-panel-two", "style" : "margin-left:20px; margin-top:20px;"});
		
		//
		var windowScrollManager = function($form, $rightPanel) {
			return function() {
				if($(window).width() > 1024) { //Min Desktop resolution
					var scrollablePanelCss = {'min-height' : $(window).height() + 'px', 'max-height' : $(window).height() + 'px' };
					$("body").css("overflow", "hidden");
					$formColumn.css(scrollablePanelCss);
					$rightPanel.css(scrollablePanelCss);
				} else {
					var normalPanelCss = {'min-height' : 'initial', 'max-height' : 'initial' };
					$("body").css("overflow", "auto");
					$formColumn.css(normalPanelCss);
					$rightPanel.css(normalPanelCss);
				}
			}
		}
		windowScrollManager = windowScrollManager($form, $rightPanel);
		$(window).resize(windowScrollManager);
		this._sampleFormController._windowHandlers.push(windowScrollManager);
		$(window).resize();
		//
		
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
		
		var entityPath = $("<span>");
		var codeWithContainer = this._sampleFormModel.sample.code;
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			var containerIdentifierEnd = this._sampleFormModel.sample.identifier.lastIndexOf(":");
			if(containerIdentifierEnd !== -1) {
				var containerCodeStart = this._sampleFormModel.sample.identifier.lastIndexOf("/") + 1;
				var codeWithContainerParts = this._sampleFormModel.sample.identifier.substring(containerCodeStart).split(":");
				var containerCode = codeWithContainerParts[0];
				var containerCodeLink = $("<a>", { "class" : "browser-compatible-javascript-link" }).append(containerCode);
				containerCodeLink.click(function() {
					Util.blockUI();
					var containerIdentifier = _this._sampleFormModel.sample.identifier.substring(0, containerIdentifierEnd);
					mainController.serverFacade.searchWithIdentifiers([containerIdentifier], function(containerSample) {
						mainController.changeView("showViewSamplePageFromPermId", containerSample[0].permId);
					});
				});
				codeWithContainer = $("<span>").append(containerCodeLink).append(":").append(codeWithContainerParts[1]);
			}
		}
		
		if(this._sampleFormModel.sample.experimentIdentifierOrNull) {
			entityPath.append(this._sampleFormModel.sample.experimentIdentifierOrNull).append("/").append(codeWithContainer);
		} else {
			entityPath.append("/").append(this._sampleFormModel.sample.spaceCode).append("/").append(codeWithContainer);
		}
		
		var $formTitle = $("<div>");
		
		var nameLabel = this._sampleFormModel.sample.properties[profile.propertyReplacingCode];
		if(!nameLabel) {
			nameLabel = this._sampleFormModel.sample.code;
		}
		
		var title = null;
		
		switch(this._sampleFormModel.mode) {
	    	case FormMode.CREATE:
	    		title = "Create Sample " + this._sampleFormModel.sample.sampleTypeCode;
	    		break;
	    	case FormMode.EDIT:
	    		title = "Update Sample: " + nameLabel;
	    		break;
	    	case FormMode.VIEW:
	    		title = "Sample: " + nameLabel;
	    		break;
		}
		
		$formTitle
			.append($("<h2>").append(title))
			.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			
			//Edit
			if(this._sampleFormModel.mode === FormMode.VIEW) {
				var $editButton = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
					mainController.changeView('showEditSamplePageFromPermId', _this._sampleFormModel.sample.permId);
				});
				toolbarModel.push({ component : $editButton, tooltip: "Edit" });
			}
			
			//Copy
			var $copyButton = $("<a>", { 'class' : 'btn btn-default'} )
			.append($('<img>', { 'src' : './img/copy-icon.png', 'style' : 'width:16px; height:16px;' }));
			$copyButton.click(_this._getCopyButtonEvent());
			toolbarModel.push({ component : $copyButton, tooltip: "Copy" });
			
			//Delete
			var warningText = null;
			if(this._sampleFormModel.sample.children.length > 0 || this._sampleFormModel.datasets.length > 0) {
				warningText = ""
				if(this._sampleFormModel.sample.children.length > 0) {
					warningText += "The sample has " + this._sampleFormModel.sample.children.length + " children samples, these relationships will be broken but the children will remain:\n";
					var numChildrenToShow = this._sampleFormModel.sample.children.length;
					if(numChildrenToShow > 10) {
						numChildrenToShow = 10;
					}
					for(var cIdx = 0 ; cIdx < numChildrenToShow; cIdx++) {
						warningText += "\n\t" + this._sampleFormModel.sample.children[cIdx].code;
					}
					if(numChildrenToShow > 10) {
						warningText += "\n\t...";
					}
				}
				if(this._sampleFormModel.datasets.length > 0) {
					warningText += "\n\nThe sample has " + this._sampleFormModel.datasets.length + " datasets, these will be deleted with the sample:\n";
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
			toolbarModel.push({ component : $deleteButton, tooltip: "Delete" });
			
			//Print
			var $printButton = $("<a>", { 'class' : 'btn btn-default'} ).append($('<span>', { 'class' : 'glyphicon glyphicon-print' }));
			$printButton.click(function() {
				PrintUtil.printEntity(_this._sampleFormModel.sample);
			});
			toolbarModel.push({ component : $printButton, tooltip: "Print" });
			
			//Hierarchy Graph
			var $hierarchyGraph = FormUtil.getButtonWithImage("./img/hierarchy-icon.png", function () {
				mainController.changeView('showSampleHierarchyPage', _this._sampleFormModel.sample.permId);
			});
			toolbarModel.push({ component : $hierarchyGraph, tooltip: "Hierarchy Graph" });
			
			//Hierarchy Table
			var $hierarchyTable = FormUtil.getButtonWithIcon("glyphicon-list", function () {
				mainController.changeView('showSampleHierarchyTablePage', _this._sampleFormModel.sample.permId);
			});
			toolbarModel.push({ component : $hierarchyTable, tooltip: "Hierarchy Table" });
			
			//Create Dataset
			var $uploadBtn = FormUtil.getButtonWithIcon("glyphicon-upload", function () {
				mainController.changeView('showCreateDataSetPageFromPermId',_this._sampleFormModel.sample.permId);
			});
			toolbarModel.push({ component : $uploadBtn, tooltip: "Upload Dataset" });
			
			//Export
			var $export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
				Util.blockUI();
				var facade = mainController.serverFacade;
				facade.exportAll([{ type: "SAMPLE", permId : _this._sampleFormModel.sample.permId, expand : true }], facade.getUserId(), function(error, result) {
					if(error) {
						Util.showError(error);
					} else {
						Util.showSuccess("Export is being processed, you will receibe an email when is ready.", function() { Util.unblockUI(); });
					}
				});
			});
			toolbarModel.push({ component : $export, tooltip: "Export" });
		}
		
		$formColumn.append($formTitle);
		$formColumn.append(FormUtil.getToolbar(toolbarModel));
		$formColumn.append($("<br>"));
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
			
			if($(window).width() > 1024) { //Min Desktop resolution
				$rightPanel.append($previewImage);
			} else {
				$formColumn.append($previewImage);
			}
			
			
		}
		
		//
		// Identification Info on Create
		//
		if(this._sampleFormModel.mode === FormMode.CREATE) {
			this._paintIdentificationInfo($formColumn, sampleType);
		}
		
		//
		// Form Defined Properties from General Section
		//
		var isStorageAvailable = false;
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			if(propertyTypeGroup.name === "General") {
				isStorageAvailable = isStorageAvailable || this._paintPropertiesForSection($formColumn, propertyTypeGroup, i);
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
		$formColumn.append($sampleParentsWidget);
		var isDisabled = this._sampleFormModel.mode === FormMode.VIEW;
		
		var currentParentsLinks = (this._sampleFormModel.sample)?this._sampleFormModel.sample.parents:null;

		this._sampleFormModel.sampleLinksParents = new LinksController("Parents",
																		requiredParents,
																		isDisabled,
																		currentParentsLinks,
																		this._sampleFormModel.mode === FormMode.CREATE || this._sampleFormModel.mode === FormMode.EDIT);
		this._sampleFormModel.sampleLinksParents.init($sampleParentsWidget);
		//
		// LINKS TO CHILDREN
		//
		var requiredChildren = [];
		if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"]) {
			requiredChildren = sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"];
		}
		
		var sampleChildrenWidgetId = "sampleChildrenWidgetId";
		var $sampleChildrenWidget = $("<div>", { "id" : sampleChildrenWidgetId });
		$formColumn.append($sampleChildrenWidget);
		
		var currentChildrenLinks = (this._sampleFormModel.sample)?this._sampleFormModel.sample.children:null;
		this._sampleFormModel.sampleLinksChildren = new LinksController("Children",
														requiredChildren,
														isDisabled,
														currentChildrenLinks,
														this._sampleFormModel.mode === FormMode.CREATE);
		
		this._sampleFormModel.sampleLinksChildren.init($sampleChildrenWidget);
		//
		// GENERATE CHILDREN
		//
		if((this._sampleFormModel.mode !== FormMode.VIEW) && this._sampleFormModel.isELNSample) {
			var $generateChildrenBtn = $("<a>", { 'class' : 'btn btn-default', 'style' : 'margin-left:25px;', 'id' : 'generate_children'}).text("Generate Children");
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
		var isStorageAvailable = false;
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			if(propertyTypeGroup.name !== "General") {
				isStorageAvailable = isStorageAvailable || this._paintPropertiesForSection($formColumn, propertyTypeGroup, i);
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
		// Identification Info on View/Edit
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			this._paintIdentificationInfo($formColumn);
		}
		
		//
		// Storage
		//
		if(isStorageAvailable) {
			var storageListContainer = $("<div>", { 'id' : 'sample-form-storage-list' });
			$formColumn.append($("<legend>").append("Storage"));
			$formColumn.append(storageListContainer);
			var storageListController = new StorageListController(this._sampleFormModel.sample, this._sampleFormModel.mode === FormMode.VIEW);
			storageListController.init(storageListContainer);
		}
		
		//
		// Extra Content
		//
		$formColumn.append($("<div>", { 'id' : 'sample-form-content-extra' }));
		
		//
		// FORM SUBMIT
		//
		if(this._sampleFormModel.mode !== FormMode.VIEW) {
			var btnTitle = null;
			switch(this._sampleFormModel.mode) {
		    	case FormMode.CREATE:
		    		btnTitle = "Create Sample";
		    		break;
		    	case FormMode.EDIT:
		    		btnTitle = "Update Sample";
		    		break;
			}
			
			var $updateBtn = $("<input>", { "type": "submit", "class" : "btn btn-primary", 'value' : btnTitle });
			$formColumn.append($("<br>"));
			$formColumn.append($updateBtn);
		}
		
		//
		// DATASETS
		//
		
		var $dataSetViewerContainer = $("<div>", { 'id' : 'dataSetViewerContainer', 'style' : 'margin-top:10px;'});
		$rightPanel.append($dataSetViewerContainer);

		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			var $inlineDataSetForm = $("<div>");
			$rightPanel.append($inlineDataSetForm);
			var $dataSetFormController = new DataSetFormController(this, FormMode.CREATE, this._sampleFormModel.sample, null, true);
			$dataSetFormController.init($inlineDataSetForm);
		}
		
		//
		// INIT
		//
		$container.append($form).append($rightPanel);
		
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
	
	this._paintPropertiesForSection = function($formColumn, propertyTypeGroup, i) {
		var _this = this;
		var sampleTypeCode = this._sampleFormModel.sample.sampleTypeCode;
		var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
		var $fieldset = $('<div>');
		var $legend = $('<legend>'); 
		$fieldset.append($legend);
			
		if((propertyTypeGroup.name !== null) && (propertyTypeGroup.name !== "")) {
			$legend.text(propertyTypeGroup.name);
		} else if((i === 0) || ((i !== 0) && (sampleType.propertyTypeGroups[i-1].name !== null) && (sampleType.propertyTypeGroups[i-1].name !== ""))) {
			$legend.text("Metadata");
		} else {
			$legend.remove();
		}
			
		var storagePropertyGroup = profile.getPropertyGroupFromStorage(propertyTypeGroup.name);
		if(storagePropertyGroup) {
			$legend.remove();
			return true;
		}
			
		var propertyGroupPropertiesOnForm = 0;
		for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
			var propertyType = propertyTypeGroup.propertyTypes[j];
			FormUtil.fixStringPropertiesForForm(propertyType, this._sampleFormModel.sample);
			if(!propertyType.showInEditViews && this._sampleFormModel.mode === FormMode.EDIT) { //Skip
				continue;
			} else if(propertyType.dinamic && this._sampleFormModel.mode === FormMode.CREATE) { //Skip
				continue;
			}
			
			if(propertyType.code === "ANNOTATIONS_STATE" || propertyType.code === "FREEFORM_TABLE_STATE") {
				continue;
			} else if(propertyType.code === "XMLCOMMENTS") {
				var $commentsContainer = $("<div>");
				$fieldset.append($commentsContainer);
				var isAvailable = this._sampleFormController._addCommentsWidget($commentsContainer);
				if(!isAvailable) {
					continue;
				}
			} else {
				if(propertyType.code === "SHOW_IN_PROJECT_OVERVIEW") {
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
						if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
							value = FormUtil.getVocabularyLabelForTermCode(propertyType, value);
						}
						$controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, value, propertyType.code);
					} else {
						continue;
					}
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType);
					//Update values if is into edit mode
					if(this._sampleFormModel.mode === FormMode.EDIT) {
						if(propertyType.dataType === "BOOLEAN") {
							$($($component.children()[0]).children()[0]).prop('checked', value === "true");
						} else if(propertyType.dataType === "TIMESTAMP") {
							$($($component.children()[0]).children()[0]).val(value);
						} else {
							$component.val(value);
						}
					} else {
						$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
					}
						
					var changeEvent = function(propertyType) {
						return function() {
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
								_this._sampleFormModel.sample.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
							}
						}
					}
					
					//Avoid modifications in properties managed by scripts
					if(propertyType.managed || propertyType.dinamic) {
						$component.prop('disabled', true);
					}
					
					if(propertyType.dataType === "MULTILINE_VARCHAR") {
						$component = FormUtil.activateRichTextProperties($component, changeEvent(propertyType));
					} else if(propertyType.dataType === "TIMESTAMP") {
						$component.on("dp.change", changeEvent(propertyType));
					} else {
						$component.change(changeEvent(propertyType));
					}
					
					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}
				$fieldset.append($controlGroup);
			}
			
			if(propertyType.code !== "ANNOTATIONS_STATE") {
				propertyGroupPropertiesOnForm++;
			}	
		}
			
		if(propertyGroupPropertiesOnForm === 0) {
			$legend.remove();
		}
		
		$formColumn.append($fieldset);
			
		return false;
	}
	
	this._paintIdentificationInfo = function($formColumn, sampleType) {
		var _this = this;
		//
		// Identification Info
		//
		$formColumn.append($("<legend>").append("Identification Info"));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Type", this._sampleFormModel.sample.sampleTypeCode));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Experiment", this._sampleFormModel.sample.experimentIdentifierOrNull));
		
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
		
		$formColumn.append($codeField);
		
		//
		// Identification Info - Registration and modification times
		//
		if(this._sampleFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._sampleFormModel.sample.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$formColumn.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)))
			$formColumn.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$formColumn.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$formColumn.append($modificationDate);
		}
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
				var component = "<div class='form-horizontal'>"
					component += "<legend>Duplicate Entity</legend>";
					component += "<div class='form-inline'>";
					component += "<div class='form-group " + FormUtil.shortformColumClass + "'>";
					component += "<label class='control-label " + FormUtil.labelColumnClass + "'>Options </label>";
					component += "<div class='" + FormUtil.controlColumnClass + "'>";
					component += "<span class='checkbox'><label><input type='checkbox' id='linkParentsOnCopy'> Link Parents </label></span>";
					component += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
					component += "<span class='checkbox'><label><input type='checkbox' id='copyChildrenOnCopy'> Copy Children </label></span>";
					component += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
					component += "<span class='checkbox'><label><input type='checkbox' id='copyCommentsLogOnCopy'> Copy Comments Log </label></span>";
					component += "</div>";
					component += "</div>";
					component += "</div>";
					component += "<br /><br />";
					component += "<div class='form-group " + FormUtil.shortformColumClass + "'>";
					component += "<label class='control-label  " + FormUtil.labelColumnClass+ "'>Code&nbsp;(*):</label>";
					component += "<div class='" + FormUtil.shortControlColumnClass + "'>";
					component += "<input type='text' class='form-control' placeholder='Code' id='newSampleCodeForCopy' pattern='[a-zA-Z0-9_\\-\\.]+' required>";
					component += "</div>";
					component += "<div class='" + FormUtil.shortControlColumnClass + "'>";
					component += " (Allowed characters are: letters, numbers, '-', '_', '.')";
					component += "</div>";
					component += "</div>";
					
				var css = {
						'text-align' : 'left',
						'top' : '15%',
						'width' : '70%',
						'left' : '15%',
						'right' : '20%',
						'overflow' : 'auto'
				};
				
				Util.blockUI(component + "<br><br><br> <a class='btn btn-default' id='copyAccept'>Accept</a> <a class='btn btn-default' id='copyCancel'>Cancel</a>", css);
				
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
		var $childrenStorageDropdown = FormUtil.getDefaultBenchDropDown('childrenStorageSelector', true);
		if($childrenStorageDropdown && !$("#childrenStorageSelector").length) {
			var $childrenStorageDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenStorageDropdown, 'Storage');
			$("#newChildrenOnBenchDropDown").append($childrenStorageDropdownWithLabel);
		}
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
				Util.showError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
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
			
			var numberOfReplicas = parseInt($("#childrenReplicas").val());
			if(isNaN(numberOfReplicas) || numberOfReplicas < 0 || numberOfReplicas > 1000) {
				Util.showError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
				return;
			}
			var generatedChildrenCodes = getGeneratedChildrenCodes();
			var generatedChildrenType = $("#childrenTypeSelector").val();
			if(generatedChildrenType === "") {
				Util.showError("Please select the children type.", function() {}, true);
			} else {
				for(var i = 0; i < generatedChildrenCodes.length; i++) {
					var virtualSample = new Object();
					virtualSample.newSample = true;
					virtualSample.permId = Util.guid();
					virtualSample.code = generatedChildrenCodes[i];
					virtualSample.identifier = "/" + generatedChildrenSpace + "/" + virtualSample.code;
					virtualSample.sampleTypeCode = generatedChildrenType;
					_this._sampleFormModel.sampleLinksChildren.addSample(virtualSample);
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
		$childrenGenerator.append($("<form>", { "class" : "form-horizontal" , "style" : "margin-left:20px; margin-right:20px;"})
									.append($("<h1>").append("Children Generator"))
									.append($parentsComponent)
									.append($childrenComponent)
									.append($previewComponent)
									.append($("<br>")).append($generateButton)
								);
		
		// Show Widget
		Util.blockUI($childrenGenerator, {'text-align' : 'left', 'top' : '10%', 'width' : '80%', 'left' : '10%', 'right' : '10%', 'height' : '80%', 'overflow' : 'auto'});
	}
}