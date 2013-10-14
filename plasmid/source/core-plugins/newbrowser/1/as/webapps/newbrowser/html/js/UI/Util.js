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
 * Utility class Util, created as anonimous, it contains utility methods mainly to show messages.
 *
 * Contains methods used for common tasks.
 */
var Util = new function() {

	//
	// Methods to block user input
	//
	this.blockUINoMessage = function() {
		this.unblockUI();
		BlockScrollUtil.disable_scroll();
		$('#navbar').block({ message: '', css: { width: '0px' } });
		$.blockUI({ message: '', css: { width: '0px' } });
	}
	
	this.blockUI = function(message) {
		this.unblockUI();
		BlockScrollUtil.disable_scroll();
		
		var css = { 
					'border': 'none', 
					'padding': '10px',
					'-webkit-border-radius': '6px 6px 6px 6px', 
					'-moz-border-radius': '6px 6px 6px 6px', 
					'border-radius' : '6px 6px 6px 6px',
					'box-shadow' : '0 1px 10px rgba(0, 0, 0, 0.1)',
					'cursor' : 'default'
		};
		
		$('#navbar').block({ message: '', css: { width: '0px' } });
		if(message) {
			$.blockUI({ message: message, css: css});
		} else {
			$.blockUI({ message: '<h1><img src="./js/busy.gif" /> Just a moment...</h1>', css: css });
		}
		
	}
	
	//
	// Methods to allow user input
	//
	this.unblockUI = function(callback) {
		BlockScrollUtil.enable_scroll();
		$('#navbar').unblock();
		$.unblockUI({ 
		                onUnblock: callback 
					});
	}
	
	//
	// Methods to show messages as pop ups
	//
	this.showError = function(withHTML, andCallback) {
		var isiPad = navigator.userAgent.match(/iPad/i) != null;
		if(!isiPad) {
			withHTML = withHTML + "<br>" + "<a class='btn'>Accept</a>";
		}
		
		this.blockUINoMessage();
		var localReference = this;
		jError(
				withHTML,
				{
				  autoHide : isiPad,
				  clickOverlay : false,
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
				  onClosed : function(){ if(andCallback) { andCallback();} else { localReference.unblockUI();}},
				  onCompleted : function(){ }
		});
	}
	
	this.showSuccess = function(withHTML, andCallback) {
		var isiPad = navigator.userAgent.match(/iPad/i) != null;
		if(!isiPad) {
			withHTML = withHTML + "<br>" + "<a class='btn'>Accept</a>";
		}
		
		this.blockUINoMessage();
		var localReference = this;
		jSuccess(
				withHTML,
				{
				  autoHide : isiPad,
				  clickOverlay : false,
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
				  onClosed : function(){ if(andCallback) { andCallback();} else { localReference.unblockUI();}},
				  onCompleted : function(){ }
		});
	}
	
	this.showInfo = function(withHTML, andCallback) {
		var isiPad = navigator.userAgent.match(/iPad/i) != null;
		if(!isiPad) {
			withHTML = withHTML + "<br>" + "<a class='btn'>Accept</a>";
		}
		
		this.blockUINoMessage();
		var localReference = this;
		jNotify(
				withHTML,
				{
				  autoHide : isiPad,
				  clickOverlay : false,
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
				  onClosed : function(){ if(andCallback) { andCallback();} else { localReference.unblockUI();}},
				  onCompleted : function(){ }
		});
	}
	
	//
	// Other
	//
	this.getEmptyIfNull = function(toCheck) {
		if(	toCheck === undefined ||
			toCheck === null ||
			toCheck === "�(undefined)") {
			return "";
		} else {
			return toCheck;
		}
	}
} 