/**
 * Library
 */
//var STJS = function() {};
//STJS.prototype.extend = function(classToExtend, parentClassToInherit, interfaces, functionToReplay, metadata) {
//	if(functionToReplay !== null) {
//		functionToReplay(classToExtend.prototype, classToExtend.prototype);
//	}
//};

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
	var object = new window[jsonObject["@type"]]();
	var prototype = object.__proto__ || object.constructor.prototype;
	
	for(var key in jsonObject) {
		if (prototype[key] === null) {
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