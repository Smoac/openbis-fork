/**
 * Library
 */
var reg = require("support/type_registry");
var STJSUtil = function() {}

STJSUtil.prototype.executeFunctionByName = function(functionName, context /*, args */) {
	  var args = [].slice.call(arguments).splice(2);
	  var namespaces = functionName.split(".");
	  var func = namespaces.pop();
	  for(var i = 0; i < namespaces.length; i++) {
	    context = context[namespaces[i]];
	  }
	  return context[func].apply(this, args);
}

STJSUtil.prototype.fromJson = function(jsonObject) {
	//console.log(jsonObject["@type"]);
	
	console.log("Creating " + jsonObject["@type"]);
	var prototype = reg.get(jsonObject["@type"]);
	var object = new prototype;
	
	for(var key in jsonObject) {
		//if (!prototype.hasOwnProperty(key))
		{
			var property = jsonObject[key];
			if(property instanceof Object && property["@type"] !== undefined) {
				property = this.fromJson(property);
			}
			object[key] = property;
		}
	}
	return object;
};

//var stjs = new STJS();
var stjsUtil = new STJSUtil();

var Serializable = function() {};

function RuntimeException(message) {
    this.name = "RuntimeException";
    this.message = (message || "");
}
RuntimeException.prototype = Error.prototype;

function IllegalArgumentException(message) {
    this.name = "IllegalArgumentException";
    this.message = (message || "");
}
IllegalArgumentException.prototype = Error.prototype;

function NotFetchedException(message) {
    this.name = "NotFetchedException";
    this.message = (message || "");
}
NotFetchedException.prototype = Error.prototype;



var HashMap = function() {}
HashMap.prototype.put = function(key, val) {
	this[key] = val;
};