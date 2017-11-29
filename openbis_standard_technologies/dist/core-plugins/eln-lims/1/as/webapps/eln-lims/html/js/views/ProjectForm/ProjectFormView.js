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

function ProjectFormView(projectFormController, projectFormModel) {
	this._projectFormController = projectFormController;
	this._projectFormModel = projectFormModel;
	
	this.repaint = function(views) {
		var $container = views.content;
		var _this = this;
		
		var $form = $("<div>");
		
		var $formColumn = $("<form>", {
			'role' : "form",
			'action' : 'javascript:void(0);'
		});
		
		$form.append($formColumn);
		
		//
		// Title
		//
		var title = null;
		var isInventoryProject = this._projectFormModel.project && profile.isInventorySpace(this._projectFormModel.project.spaceCode);
		var typeTitle = "Project: ";
		
		var spaceCode = this._projectFormModel.project.spaceCode;
		var projectCode = (this._projectFormModel.mode !== FormMode.CREATE)?this._projectFormModel.project.code:null;
		var entityPath = FormUtil.getFormPath(spaceCode, projectCode);
		
		if(this._projectFormModel.mode === FormMode.CREATE) {
			title = "Create " + typeTitle;
		} else if (this._projectFormModel.mode === FormMode.EDIT) {
			title = "Update " + typeTitle + this._projectFormModel.project.code;
		} else {
			title = typeTitle + this._projectFormModel.project.code;
		}
		
		var $formTitle = $("<div>");
			$formTitle
				.append($("<h2>").append(title))
				.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		if(this._projectFormModel.mode === FormMode.VIEW) {
			var showSelectExperimentType = function() {
				var $dropdown = FormUtil.getExperimentTypeDropdown("experimentTypeDropdown", true);
				Util.blockUI("Select the type for the " + ELNDictionary.getExperimentKindName("/" + _this._projectFormModel.project.spaceCode) + ": <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='experimentTypeDropdownCancel'>Cancel</a>");
				
				$("#experimentTypeDropdown").on("change", function(event) {
					var experimentTypeCode = $("#experimentTypeDropdown")[0].value;
					_this._projectFormController.createNewExperiment(experimentTypeCode);
				});
				
				$("#experimentTypeDropdownCancel").on("click", function(event) { 
					Util.unblockUI();
				});
			}
			
			//Create Experiment
			var isDefaultExperimentPressent = mainController.profile.getExperimentTypeForExperimentTypeCode("DEFAULT_EXPERIMENT") != null;
			if(isDefaultExperimentPressent) {
				var $createExpBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
				if(profile.isInventorySpace(_this._projectFormModel.project.spaceCode)) {
					var experimentType = profile.getExperimentTypeForExperimentTypeCode(_this._projectFormModel.project.spaceCode);
					if(experimentType) {
						_this._projectFormController.createNewExperiment(_this._projectFormModel.project.spaceCode);
					} else {
						showSelectExperimentType();
					}
				} else {
					_this._projectFormController.createNewExperiment("DEFAULT_EXPERIMENT");
				}
				});
				
				toolbarModel.push({ component : $createExpBtn, tooltip: "Create " + ELNDictionary.getExperimentKindName("/" + _this._projectFormModel.project.spaceCode) });
			}
			
			//Edit
			var $editBtn = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
				_this._projectFormController.enableEditing();
			});
			toolbarModel.push({ component : $editBtn, tooltip: "Edit" });
			
			//Delete
			var $deleteBtn = FormUtil.getDeleteButton(function(reason) {
				_this._projectFormController.deleteProject(reason);
			}, true);
			toolbarModel.push({ component : $deleteBtn, tooltip: "Delete" });
			
			//Export
			var $export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
				Util.blockUI();
				var facade = mainController.serverFacade;
				facade.exportAll([{ type: "PROJECT", permId : _this._projectFormModel.project.permId, expand : true }], false, function(error, result) {
					if(error) {
						Util.showError(error);
					} else {
						Util.showSuccess("Export is being processed, you will receive an email when is ready, if you logout the process will stop.", function() { Util.unblockUI(); });
					}
				});
			});
			toolbarModel.push({ component : $export, tooltip: "Export" });
			
			//Jupyter Button
			if(profile.jupyterIntegrationServerEndpoint) {
				var $jupyterBtn = FormUtil.getButtonWithIcon("glyphicon-log-in", function () {
					var jupyterNotebook = new JupyterNotebookController(_this._projectFormModel.project);
					jupyterNotebook.init();
				});
				toolbarModel.push({ component : $jupyterBtn, tooltip: "Create Jupyter notebook" });
			}
			
			//Operations
			var $operationsMenu = FormUtil.getOperationsMenu([{ label: "Create " + ELNDictionary.getExperimentKindName("/" + _this._projectFormModel.project.spaceCode), event: function() {
				showSelectExperimentType();
			}}]);
			toolbarModel.push({ component : $operationsMenu, tooltip: "Extra operations" });
		} else {
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._projectFormController.updateProject();
			}, "Save");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn, tooltip: "Save" });
		}
		
		var $header = views.header;
		$header.append($formTitle);
		$header.append(FormUtil.getToolbar(toolbarModel));
		
		//
		// Metadata Fields
		//
		$formColumn.append($("<legend>").append("General"));
		
		var description = Util.getEmptyIfNull(this._projectFormModel.project.description);
		if(this._projectFormModel.mode !== FormMode.VIEW) {
			var $textBox = FormUtil._getTextBox(null, "Description", false);
			var textBoxEvent = function(jsEvent, newValue) {
				var valueToUse = null;
				if(newValue !== undefined && newValue !== null) {
					valueToUse = newValue;
				} else {
					valueToUse = $(this).val();
				}
				_this._projectFormModel.project.description = valueToUse;
				_this._projectFormModel.isFormDirty = true;
			};
			$textBox.val(description);
			$textBox = FormUtil.activateRichTextProperties($textBox, textBoxEvent, null);
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textBox, "Description"));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Description", description));
		}
		
		// Experiment And Samples Table
		if(this._projectFormModel.mode !== FormMode.CREATE && !isInventoryProject) {
			var $experimentsContainer = $("<div>");
			$formColumn.append($("<legend>").append(ELNDictionary.getExperimentKindName("/" + _this._projectFormModel.project.spaceCode, true)))
			$formColumn.append($experimentsContainer);
			
			var experimentTableController = new ExperimentTableController(this._projectFormController, null, jQuery.extend(true, {}, this._projectFormModel.project), true);
			experimentTableController.init($experimentsContainer);
			
			$formColumn.append($("<legend>").append("" + ELNDictionary.Samples + ""))
			var $samplesContainerHeader = $("<div>");
			$formColumn.append($samplesContainerHeader);
			var $samplesContainer = $("<div>");
			$formColumn.append($samplesContainer);
			
			var views = {
					header : $samplesContainerHeader,
					content : $samplesContainer
			}
			var sampleTableController = new SampleTableController(this._projectFormController, null, null, this._projectFormModel.project.permId, true);
			sampleTableController.init(views);
		}
		
		//
		// Identification info
		//
		$formColumn.append($("<legend>").append("Identification Info"));
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Space", this._projectFormModel.project.spaceCode));
		
		if(this._projectFormModel.mode === FormMode.CREATE) {
			var $textField = FormUtil._getInputField('text', null, "Project Code", null, true);
			$textField.keyup(function(event){
				var textField = $(this);
				var caretPosition = this.selectionStart;
				textField.val(textField.val().toUpperCase());
				this.selectionStart = caretPosition;
				this.selectionEnd = caretPosition;
				_this._projectFormModel.project.code = textField.val();
				_this._projectFormModel.isFormDirty = true;
			});
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._projectFormModel.project.code));
		}
		
		if(this._projectFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._projectFormModel.project.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$formColumn.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)));
			$formColumn.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$formColumn.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$formColumn.append($modificationDate);
		}
		
		$container.append($form);
	}
}