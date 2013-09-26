/*
 * Copyright 2013 ETH Zuerich, CISD
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

/**
 * Utility class Util, created as anonimous.
 *
 * Contains methods used for common tasks.
 */
var Util = new function() {
	this.getEmptyIfNull = function(toCheck) {
		if(	toCheck === undefined ||
			toCheck === null ||
			toCheck === "�(undefined)") {
			return "";
		} else {
			return toCheck;
		}
	}
	
	this.blockUINoMessage = function() {
		BlockScrollUtil.disable_scroll();
		$('#navbar').block({ message: '', css: { width: '0px' } });
		$.blockUI({ message: '', css: { width: '0px' } });
	}
	
	this.blockUI = function(message) {
		BlockScrollUtil.disable_scroll();
		
		var css = { 
					'border': 'none', 
					'padding': '10px',
					'-webkit-border-radius': '6px 6px 6px 6px', 
					'-moz-border-radius': '6px 6px 6px 6px', 
					'border-radius' : '6px 6px 6px 6px',
					'box-shadow' : '0 1px 10px rgba(0, 0, 0, 0.1)'
		};
		
		$('#navbar').block({ message: '', css: { width: '0px' } });
		if(message) {
			$.blockUI({ message: message, css: css});
		} else {
			$.blockUI({ message: '<h1><img src="./js/busy.gif" /> Just a moment...</h1>', css: css });
		}
		
	}
	
	this.unblockUI = function(callback) {
		BlockScrollUtil.enable_scroll();
		$('#navbar').unblock();
		$.unblockUI({ 
		                onUnblock: callback 
					});
	}
	
	this.showError = function(withHTML, andCallback) {
		this.blockUINoMessage();
		jError(
				withHTML,
				{
				  autoHide : false,
				  clickOverlay : true,
				  MinWidth : 250,
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : false,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ if(andCallback) { andCallback();}},
				  onCompleted : function(){ }
		});
	}
	
	this.showSuccess = function(withHTML, andCallback) {
		this.blockUINoMessage();
		jSuccess(
				withHTML,
				{
				  autoHide : false,
				  clickOverlay : true,
				  MinWidth : 250,
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : false,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ if(andCallback) { andCallback();}},
				  onCompleted : function(){ }
		});
	}
	
	this.showInfo = function(withHTML, andCallback) {
		this.blockUINoMessage();
		jNotify(
				withHTML,
				{
				  autoHide : false,
				  clickOverlay : true,
				  MinWidth : 250,
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'top',
				  ShowOverlay : false,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ if(andCallback) { andCallback();}},
				  onCompleted : function(){ }
		});
	}
	
	this.fileUpload = function(fileId, callbackHandler) {
		//File
		var file = document.getElementById(fileId).files[0];
		
		//Building Form Data Object for Multipart File Upload
		var formData = new FormData();
		formData.append("sessionKeysNumber", 1);
		formData.append("sessionKey_0", "sample-file-upload");
		formData.append("sample-file-upload", file);
		formData.append("sessionID", mainController.openbisServer.getSession());
		
		$.ajax({
			type: "POST",
			url: "/openbis/openbis/upload",
			contentType: false,
			processData: false,
			data: formData,
			success: function(result) {
				callbackHandler(result);
			}
		});
	}
} 