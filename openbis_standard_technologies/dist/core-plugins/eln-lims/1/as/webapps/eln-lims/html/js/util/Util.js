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
		
		//Enable/Disable scroll when the mouse goes in/out
		$('.blockUI.blockMsg.blockPage').hover(function() {
				if(this.scrollHeight > this.clientHeight) { //Inside, if has scroll, enable when hovering in
					BlockScrollUtil.enable_scroll();
				}
			}, function() {
				BlockScrollUtil.disable_scroll(); //Always disable when hovering out
			}
		);
		
		//Enable/Disable scroll when components change
		$('.blockUI.blockMsg.blockPage').bind("DOMSubtreeModified",function() {
			if(this.scrollHeight > this.clientHeight) { //Inside, if has scroll, enable
				BlockScrollUtil.enable_scroll();
			} else { //If doesn't disable
				BlockScrollUtil.disable_scroll(); 
			}
		});
	}
	
	//
	// Methods to allow user input
	//
	this.unblockUI = function(callback) {
		$('#navbar').unblock();
		$.unblockUI({ 
			onUnblock: function() {
				window.setTimeout(function() { //Enable after all possible enable/disable events happen
					BlockScrollUtil.enable_scroll();
					if(callback) {
						callback();
					}
				}, 150);
			}
		});
	}
	
	//
	// Methods to show messages as pop ups
	//
	this.showStacktraceAsError = function(stacktrace) {
		var isUserFailureException = 	stacktrace.indexOf("ch.systemsx.cisd.common.exceptions.UserFailureException") === 0;
		var isAuthorizationException = 	stacktrace.indexOf("ch.systemsx.cisd.common.exceptions.AuthorizationFailureException") === 0;
		var startIndex = null;
		var endIndex = null;
		if(isUserFailureException) {
			startIndex = "ch.systemsx.cisd.common.exceptions.UserFailureException".length + 2;
			endIndex = stacktrace.indexOf("at ch.systemsx");
		} else if(isAuthorizationException) {
			startIndex = "ch.systemsx.cisd.common.exceptions.AuthorizationFailureException".length + 2;
			endIndex = stacktrace.indexOf("at ch.systemsx");
		} else {
			startIndex = 0;
			endIndex = stacktrace.length;
		}
		var errorMessage = stacktrace.substring(startIndex, endIndex).trim();
		Util.showError(errorMessage, function() {Util.unblockUI();});
	}
	
	this.showWarning = function(text, okCallback) {
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '20%',
				'background-color' : '#fcf8e3',
				'border-color' : '#faebcc',
				'color' : '#8a6d3b',
				'overflow' : 'auto'
		};
		
		var bootstrapWarning = "<strong>Warning!</strong></br></br>" + text;
		Util.blockUI(bootstrapWarning + "<br><br><br> <a class='btn btn-primary' id='warningAccept'>Accept</a> <a class='btn btn-default' id='warningCancel'>Cancel</a>", css);
		
		$("#warningAccept").on("click", function(event) {
			okCallback();
			Util.unblockUI();
		});
		
		$("#warningCancel").on("click", function(event) { 
			Util.unblockUI();
		});
	}
	
	this.showError = function(withHTML, andCallback, noBlock) {
		var isiPad = navigator.userAgent.match(/iPad/i) != null;
		if(!isiPad) {
			withHTML = withHTML + "<br>" + "<a class='btn btn-default'>OK</a>";
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
	
	this.showSuccess = function(withHTML, andCallback, forceAutoHide) {
		var localReference = this;
		jSuccess(
				withHTML,
				{
				  autoHide : true,
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
				  onClosed : function(){ if(andCallback) { andCallback();} else { }},
				  onCompleted : function(){ }
		});
	}
	
	this.showInfo = function(withHTML, andCallback, noBlock) {
		var isiPad = navigator.userAgent.match(/iPad/i) != null;
		if(!isiPad) {
			withHTML = withHTML + "<br>" + "<a class='btn btn-default'>OK</a>";
		}
		
		if(!noBlock) {
			this.blockUINoMessage();
		}
		
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

	this.showDirectLink = function(path) {
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '80%',
				'left' : '10%',
				'right' : '10%',
				'overflow' : 'hidden'
		};
		
		var hostName = window.location.hostname;
		var protocol = profile.directFileServer.protocol;
		var suffix = profile.directFileServer.UNCsuffix;
		if(!suffix) {
			suffix = ""; 
		}
		var port = profile.directFileServer.port;
		
		directLinkUnix = protocol + "://" + hostName + ":" + port + "/" + suffix + path;
		directLinkWin = "\\\\" + hostName + "\\" + (new String(suffix + path).replace(new RegExp("/", 'g'),"\\"));
		var $close = $("<div>", { style : "float:right;" })
							.append($("<a>", { class : 'btn btn-default' }).append($("<span>", { class : 'glyphicon glyphicon-remove' }))).click(function() {
								Util.unblockUI();
							});
		
		var isWindows = window.navigator.userAgent.toLowerCase().indexOf("windows") > -1;
		var $window = null;
		if(isWindows) {
			var $readOnlyInput = $("<input>", { class : "form-control", id : "windowsLink", readonly : "readonly", type : "text", value :  directLinkWin, onClick : "this.setSelectionRange(0, this.value.length)" });
			
			$window = $("<div>").append($close).append($("<h1>").append("Direct Link"))
					.append("Please use this link to access the folder though the network, copy it on the explorer address bar: ")
					.append($("<br>"))
					.append($readOnlyInput);
		} else {
			$window = $("<div>").append($close).append($("<h1>").append("Direct Link"))
					.append("Please use this link to access the folder though the network, directly clicking on it will open it with the default application: ")
					.append($("<br>"))
					.append($("<b>Link: </b>"))
					.append($("<a>", { "href" : directLinkUnix, "target" : "_blank"}).append(directLinkUnix));
		}
		
		$window.css("margin-bottom", "10px");
		$window.css("margin-left", "10px");
		$window.css("margin-right", "10px");
		
		Util.blockUI($window, css);
	}
	
	//HACK: This method is intended to be used by naughty SVG images that don't provide a correct with/height and don't resize correctly
	this.loadSVGImage = function(imageURL, containerWidth, containerHeight, callback, isSEQSVG) {
		d3.xml(imageURL, "image/svg+xml", 
				function(xml) {
					var importedNode = document.importNode(xml.documentElement, true);
					var d3Node = d3.select(importedNode);
					
					var imageWidth = d3Node.style("width");
					var imageHeight = d3Node.style("height");
					if((imageWidth === "auto" || imageHeight === "auto") || //Firefox some times
						(imageWidth === "" || imageHeight === "")) { //Safari and Chrome under any case
						imageWidth = containerWidth;
						imageHeight = containerHeight;
					} else if(imageWidth.indexOf("px") !== -1 || imageHeight.indexOf("px") !== -1) { //Firefox some times
						imageWidth = parseFloat(imageWidth.substring(0,imageWidth.length - 2));
						imageHeight = parseFloat(imageHeight.substring(0,imageHeight.length - 2));
					}
					
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
					
					if(isSEQSVG) {
						var size = (containerWidth > containerHeight)?containerHeight:containerWidth;
						d3Node.attr("width", size)
							.attr("height", size)
							.attr("viewBox", 0 + " " + 0 + " " + (size * 1.1) + " " + (size * 1.1));
					} else {
						d3Node.attr("width", imageWidth)
							.attr("height", imageHeight)
							.attr("viewBox", "0 0 " + imageWidth + " " + imageHeight);
					}
					
					
					callback($(importedNode));
		});
	}
	
	this.showImage = function(imageURL, isSEQSVG) {
		
		var showImageB = function($image) {
			var $imageWrapper = $("<div>", {"style" : "margin:10px"});
			$imageWrapper.append($image);
			
			var imageHTML = $imageWrapper[0].outerHTML;
			var isiPad = navigator.userAgent.match(/iPad/i) != null;
			if(!isiPad) {
				imageHTML = "<div style='text-align:right;'><a class='btn btn-default'><span class='glyphicon glyphicon-remove'></span></a></div>" + imageHTML;
			}
			
			Util.blockUINoMessage();
			jNotifyImage(imageHTML,
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
					  onClosed : function(){ Util.unblockUI(); },
					  onCompleted : function(){ }
					});
		};
		
		var containerWidth = $(window).width()*0.85;
		var containerHeight = $(window).height()*0.85;
		
		if(imageURL.toLowerCase().indexOf(".svg?sessionid") !== -1) {
			this.loadSVGImage(imageURL, containerWidth, containerHeight, showImageB, isSEQSVG);
			return;
		}
		
		var $image = $("<img>", {"src" : imageURL});
		$image.load(function() {
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
			showImageB($image);
		});
	}
	
	//
	// Date Formating
	//
	this.getFormatedDate = function(date) {
		var day = date.getDate();
		if(day < 10) {
			day = "0" + day;
		}
		var month = date.getMonth()+1;
		if(month < 10) {
			month = "0" + month;
		}
		var year = date.getFullYear();
		var hour = date.getHours();
		if(hour < 10) {
			hour = "0" + hour;
		}
		var minute = date.getMinutes();
		if(minute < 10) {
			minute = "0" + minute;
		}
		var second = date.getSeconds();
		if(second < 10) {
			second = "0" + second;
		}
		return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
	}
	
	//
	// Other
	//
	this.getMapAsString = function(map, length) {
		var mapAsString = "";
		for(key in map) {
			if(mapAsString.length > 0) {
				mapAsString += " , ";
			}
			mapAsString += "<b>" + key + "</b> : " + map[key];
		}
		
		if(length && mapAsString.length > length) {
			mapAsString = mapAsString.substring(0, length) + " ...";
		}
		return mapAsString;
	}
	
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
	
	this.queryString = function() {
		return function () {
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
	
	this.guid = function() {
		  var s4 = function() {
		    return Math.floor((1 + Math.random()) * 0x10000)
		               .toString(16)
		               .substring(1);
		  }
		  
		  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
		           s4() + '-' + s4() + s4() + s4();
	};
	
	this.getDisplayNameFromCode = function(openBISCode) {
		var normalizedCodeParts = openBISCode.toLowerCase().split('_');
		var displayName = "";
		for(var i = 0; i < normalizedCodeParts.length; i++) {
			if(i > 0) {
				displayName += " ";
			}
			displayName += normalizedCodeParts[i].capitalizeFirstLetter();
		}
		return displayName;
	}
	
	//
	// Grid related function
	//
	var alphabet = [null,'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
	this.getLetterForNumber = function(number) { //TODO Generate big numbers
		return alphabet[number];
	}
	
	this.getNumberFromLetter = function(letter) { //TODO Generate big numbers
		for(var i = 0; i < alphabet.length; i++) {
			if(letter === alphabet[i]) {
				return i;
			}
		}
		return null;
	}
	
	this.getXYfromLetterNumberCombination = function(label) {
		var parts = label.match(/[a-zA-Z]+|[0-9]+/g);
		var row = this.getNumberFromLetter(parts[0]);
		var column = parseInt(parts[1]);
		return [row, column];
	}
	
	this.manageError = function(err) {
		Util.showError(	"Please take a screenshot of the browser with the error report, add a description of the steps that lead to it and send it to your admin. <br/><br/>" +
						"<div style='color:black; background-color:lightgrey; padding:10px;'>" + 
						"User Agent: " + navigator.userAgent + "<br/>" +
						"URL: " + window.location.href + "<br/>" +
						"Session: " + mainController.serverFacade.openbisServer.getSession() + "<br/>" +
						"Time: " + new Date() + "<br/>" +
						"---------------------------------------------------------------------- <br/>" +
						"Error Name: " + err.name + "<br/>" +
						"Error Message: " + err.message + "<br/>" +
						"Error Stack: " + err.stack.replace(new RegExp('\n', 'g'), '<br/>') +
						"</div>");
	}
	
	//
	// URL Utils
	//
	this.getURLFor = function(menuId, view, argsForView) {
		return window.location.href.split("?")[0] + "?menuUniqueId=" +  menuId+ "&viewName=" + view + "&viewData=" + argsForView;
	}
	
	//
	// TSV Export
	//
	this.downloadTSV = function(arrayOfRowArrays, fileName) {
		for(var rIdx = 0; rIdx < arrayOfRowArrays.length; rIdx++) {
			for(var cIdx = 0; cIdx < arrayOfRowArrays[rIdx].length; cIdx++) {
				var value = arrayOfRowArrays[rIdx][cIdx];
				if(!value) {
					value = "";
				}
				value = String(value).replace(/\r?\n|\r|\t/g, " "); //Remove carriage returns and tabs
				arrayOfRowArrays[rIdx][cIdx] = value;
			}
		}
		
		var tsv = $.tsv.formatRows(arrayOfRowArrays);
		var indexOfFirstLine = tsv.indexOf('\n');
		var tsvWithoutNumbers = tsv.substring(indexOfFirstLine + 1);
		var blob = new Blob([tsvWithoutNumbers], {type: 'text'});
		saveAs(blob, fileName);
	}
	
	//
	// Components Resize events
	//
	this.dragContainerFunc = function(e) {
	    var menu = $('#sideMenu');
	    var drag = $('#dragContainer');
	    var main = $('#mainContainer');
	    var mouseX = e.pageX;
	    var windowWidth = window.outerWidth;
	    var mainWidth = windowWidth - mouseX;
	    menu.css("width", mouseX + "px");
	    main.css("width", mainWidth + "px");
	    main.css("left", mouseX);
	    drag.css("left", mouseX);
    };
}


String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function (prefix) {
    return this.slice(0, prefix.length) == prefix;
};

String.prototype.capitalizeFirstLetter = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
};

Array.prototype.uniqueOBISEntity = function() {
    var a = this.concat();
    for(var i=0; i<a.length; ++i) {
        for(var j=i+1; j<a.length; ++j) {
            if(a[i].identifier === a[j].identifier)
                a.splice(j--, 1);
        }
    }

    return a;
};
