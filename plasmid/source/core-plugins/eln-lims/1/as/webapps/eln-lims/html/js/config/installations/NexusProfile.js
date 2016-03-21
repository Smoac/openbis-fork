
function NexusProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(NexusProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.mainMenu = {
				showLabNotebook : false,
				showInventory : false,
				showDrawingBoard : false,
				showSampleBrowser : true,
				showStorageManager : false,
				showAdvancedSearch : false,
				showTrashcan : true,
				showVocabularyViewer : false,
				showUserManager : false
		}
		
		this.propertyReplacingCode = "CELLARIO_BC";
		this.inventorySpaces = ["LIBRARIES"];
		this.searchSamplesUsingV3OnDropbox = true;
		this.searchSamplesUsingV3OnDropboxRunCustom = true;
		
		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
			if(sampleTypeCode === "PLATE" && sample.spaceCode === "LIBRARIES") {
				var clickFunc = function() {
					//Data
					var $plateCode = FormUtil.getFieldForLabelWithText("Plate Identifier", sample.identifier, "plate_identifier");
					var $dateField = FormUtil._getDatePickerField("expiry_date", "Expiry Date", true);
					
					var $expiryDate = FormUtil.getFieldForComponentWithLabel($dateField,"Expiry Date(*)");
					
					//Cancel/Ok buttons
					var $cancelButton = $("<a>", { "class" : "btn btn-default" }).append("<span class='glyphicon glyphicon-remove'></span>");
					$cancelButton.click(function(event) { 
						Util.unblockUI();
					});
					
					var retireAction = function() {
						var plate_identifier = sample.identifier;
						var expiry_date = $($($dateField.children()[0]).children()[0]).val();
						
						if(!expiry_date) {
							Util.showError("Expiry date missing.", function() {}, true);
							return;
						}
						
						Util.blockUI();
						
						require([	'openbis',
						         	'as/dto/service/id/CustomASServiceCode',
						         	'as/dto/service/CustomASServiceExecutionOptions'], function(openbis, CustomASServiceCode, CustomASServiceExecutionOptions) {
							
							var testProtocol = window.location.protocol;
							var testHost = window.location.hostname;
							var testPort = window.location.port;
							var testUrl = testProtocol + "//" + testHost + ":" + testPort;
							var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";
							
							var v3Api = new openbis(testApiUrl);
							v3Api._private.sessionToken = mainController.serverFacade.getSession();
							
							var serviceCode = new CustomASServiceCode("plate_version_service");
							var serviceOptions = new CustomASServiceExecutionOptions();
							serviceOptions.withParameter("plate_identifier", plate_identifier).withParameter("expiry_date", expiry_date);
							
							v3Api.executeCustomASService(serviceCode, serviceOptions)
							.done(function(result) {
								Util.showSuccess("Plate Retired");
								Util.unblockUI();
							})
							.fail(function(result) {
								Util.showError("Plate Not Retired");
								Util.unblockUI();
							});
						});
					};
					var $retireButton = FormUtil.getButtonWithText("Retire Plate!", retireAction, "btn-warning");
					
					// Mounting the widget with the components
					var $retirePlateWidget = $("<div>");
					$retirePlateWidget.append($("<div>", {"style" : "text-align:right;"}).append($cancelButton));
					$retirePlateWidget.append($("<form>", { "class" : "form-horizontal" , "style" : "margin-left:20px; margin-right:20px;"})
												.append($("<h1>").append("Retire Plate"))
												.append($plateCode)
												.append($expiryDate)
												.append($("<br>")).append($retireButton)
											);
					
					// Show Widget
					Util.blockUI($retirePlateWidget, {'text-align' : 'left', 'top' : '10%', 'width' : '80%', 'left' : '10%', 'right' : '10%', 'height' : '300px', 'overflow' : 'auto'});
					
				};
				$("#" + containerId)
						.append($("<br>"))
						.append(FormUtil.getButtonWithText("Retire Plate", clickFunc, "btn-warning"));
			}
		}
		
		this.storagesConfiguration = {
			"isEnabled" : true,
			/*
			* Should be the same across all storages, if not correct behaviour is not guaranteed.
			*/
			"STORAGE_PROPERTIES": [{
				"STORAGE_PROPERTY_GROUP" : "Physical Storage", //Where the storage will be painted.
				"STORAGE_GROUP_DISPLAY_NAME" : "Physical Storage", //Storage Group Name
				"NAME_PROPERTY" : 		"STORAGE_NAMES", //Should be a Vocabulary.
				"ROW_PROPERTY" : 		"STORAGE_ROW", //Should be an integer.
				"COLUMN_PROPERTY" : 	"STORAGE_COLUMN",  //Should be an integer.
				"BOX_PROPERTY" : 		"STORAGE_BOX_NAME", //Should be text.
				"BOX_SIZE_PROPERTY" : 	"STORAGE_BOX_SIZE", //Should be Vocabulary.
				"USER_PROPERTY" : 		"STORAGE_USER", //Should be text.
				"POSITION_PROPERTY" : 	"STORAGE_POSITION" //Should be text.
			}],
			/*
			* Storages map, can hold configurations for several storages.
			*/
			"STORAGE_CONFIGS": {
					"MY_FRIDGE-1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 4, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"MY_FRIDGE-2" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								}
				}
		};
		
		this.sampleTypeDefinitionsExtension = {}
}
});
