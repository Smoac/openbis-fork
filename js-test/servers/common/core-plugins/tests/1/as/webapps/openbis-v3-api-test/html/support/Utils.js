/**
 * Library
 */
define(["support/type_registry", "support/underscore-min"], function(reg, _) {
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
		return fromJsonLocal(jsonObject, {})
	}

	var objectPropertyFromJson = function (key, property, hashedObjects) {
		if (_.isNumber(property) && key.indexOf("Date") == -1 && key != "@id") {
					// I don't know what is the better way to distinguish between id numbers, and real numbers
				    // As here we only analyse the fields and not property values the check if a field is not a date
				    // should be enough
         	if (property in hashedObjects) {
				return hashedObjects[property]
			} else {
				throw "Expected that integer fields are id's of objects, and they should be present in cache"
			}
		} else if (property instanceof Object && property["@type"] !== undefined) {
			return fromJsonLocal(property, hashedObjects);
		} else if (property instanceof Array) {
			for (idx in property) {
				property[idx] = objectPropertyFromJson(key, property[idx], hashedObjects)
			}
			return property
		}
		else {
			return property
		}
	}
    
	var fromJsonLocal = function (jsonObject, hashedObjects) {
		var jsonType = jsonObject["@type"]
		var jsonId = jsonObject["@id"]
		
		var prototype = reg.get(jsonType);
		var object = new prototype;

        
		if (jsonId) {
			if (jsonId in hashedObjects) {
				throw "This object has already been cached!"
			}
			hashedObjects[jsonId] = object
		}

		for(var key in jsonObject) {
			object[key] = objectPropertyFromJson(key, jsonObject[key], hashedObjects)
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

	return stjsUtil;
});