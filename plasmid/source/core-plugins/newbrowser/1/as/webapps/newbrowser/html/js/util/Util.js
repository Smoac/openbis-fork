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
	
	this.blockUI = function(message, extraCSS) {
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
		
		if(extraCSS) {
			for(extraCSSProperty in extraCSS) {
				var extraCSSValue = extraCSS[extraCSSProperty];
				css[extraCSSProperty] = extraCSSValue;
			}
		}
		
		$('#navbar').block({ message: '', css: { width: '0px' } });
		if(message) {
			$.blockUI({ message: message, css: css});
		} else {
			$.blockUI({ message: '<h1><img src="./img/busy.gif" /> Just a moment...</h1>', css: css });
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
	
	this.showError = function(withHTML, andCallback, noBlock) {
		var isiPad = navigator.userAgent.match(/iPad/i) != null;
		if(!isiPad) {
			withHTML = withHTML + "<br>" + "<a class='btn btn-default'>Accept</a>";
		}
		
		if(!noBlock) {
			this.blockUINoMessage();
		}
		
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
			withHTML = withHTML + "<br>" + "<a class='btn btn-default'>Accept</a>";
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
			withHTML = withHTML + "<br>" + "<a class='btn btn-default'>Accept</a>";
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
	
	this.showImage = function(imageURL) {
		var $image = $("<img>", {"src" : imageURL});
		
		var containerWidth = $(window).width()*0.85;
		var containerHeight = $(window).height()*0.85;
		
		var imageWidth = $image[0].width;
		var imageHeight = $image[0].height;
		
		if(containerWidth < imageWidth) {
			var newImageWidth = containerWidth;
			var newImageHeight = imageHeight * newImageWidth / imageWidth;
			
			imageWidth = newImageWidth;
			imageHeight = newImageHeight;
		}
		
		if(containerHeight < imageHeight) {
			var newImageHeight = containerHeight;
			var newImageWidth = imageWidth * newImageHeight / imageHeight;
			
			imageWidth = newImageWidth;
			imageHeight = newImageHeight;
		}
		
		$image.attr("width", imageWidth);
		$image.attr("height", imageHeight);
		
		
		var $imageWrapper = $("<div>", {"style" : "margin:10px"});
		$imageWrapper.append($image);
		
		var imageHTML = $imageWrapper[0].outerHTML;
		var isiPad = navigator.userAgent.match(/iPad/i) != null;
		if(!isiPad) {
			imageHTML = "<div style='text-align:right;'><a class='btn btn-default'><span class='glyphicon glyphicon-remove'></span></a></div>" + imageHTML;
		}
		
		this.blockUINoMessage();
		var localReference = this;
		jNotifyImage(
				imageHTML,
				{
				  autoHide : isiPad,
				  clickOverlay : false,
				  MinWidth : 250,
				  TimeShown : 2000,
				  ShowTimeEffect : 200,
				  HideTimeEffect : 200,
				  LongTrip :20,
				  HorizontalPosition : 'center',
				  VerticalPosition : 'center',
				  ShowOverlay : false,
		   		  ColorOverlay : '#000',
				  OpacityOverlay : 0.3,
				  onClosed : function(){ localReference.unblockUI(); },
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
	
	this.replaceURLWithHTMLLinks = function(text) {
	    var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
	    return text.replace(exp,"<a href='$1' target='_blank'>$1</a>"); 
	}
	
	this.queryString = function () {
		  // This function is anonymous, is executed immediately and 
		  // the return value is assigned to QueryString!
		  var query_string = {};
		  var query = window.location.search.substring(1);
		  var vars = query.split("&");
		  for (var i=0;i<vars.length;i++) {
		    var pair = vars[i].split("=");
		    	// If first entry with this name
		    if (typeof query_string[pair[0]] === "undefined") {
		      query_string[pair[0]] = pair[1];
		    	// If second entry with this name
		    } else if (typeof query_string[pair[0]] === "string") {
		      var arr = [ query_string[pair[0]], pair[1] ];
		      query_string[pair[0]] = arr;
		    	// If third or later entry with this name
		    } else {
		      query_string[pair[0]].push(pair[1]);
		    }
		  } 
		    return query_string;
	} ();
}


String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};