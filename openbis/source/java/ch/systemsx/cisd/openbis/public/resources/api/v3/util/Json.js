/**
 * Library
 */
define([ 'jquery', 'underscore' ], function(jquery, _) {
	var Json = function() {
	}

	Json.prototype.fromJson = function(jsonType, jsonObject) {
		var dfd = jquery.Deferred();

		var types = {}
		collectTypes(jsonObject, types);

		var moduleNames = Object.keys(types).map(function(type) {
			return typeToModuleName(type);
		});
		require(moduleNames, function() {
			var moduleMap = {};

			for (var i = 0; i < arguments.length; i++) {
				var moduleName = moduleNames[i];
				var module = arguments[i];
				moduleMap[moduleName] = module;
			}

			var dto = fromJsonObjectWithTypeOrArrayOrMap(jsonType, jsonObject, {}, moduleMap);
			dfd.resolve(dto);
		});

		return dfd.promise();
	}

	Json.prototype.decycle = function(object) {
		return decycleLocal(object, []);
	}

	var collectTypes = function(jsonObject, types) {
		if (jsonObject instanceof Array) {
			jsonObject.forEach(function(item) {
				collectTypes(item, types);
			});
		} else if (jsonObject instanceof Object) {
			Object.keys(jsonObject).forEach(function(key) {
				var value = jsonObject[key];
				if (key == "@type") {
					types[value] = value;
				} else {
					collectTypes(value, types);
				}
			});
		}
	}

	var typeToModuleName = function(type) {
		return type.replace(/\./g, '/');
	}

	var fromJsonObjectWithTypeOrArrayOrMap = function(jsonType, jsonObject, hashedObjects, modulesMap) {
		if (jsonObject instanceof Array) {
			if (jsonType && _.isString(jsonType) && jsonObject.length == 2) {
				return jsonObject[1];
			} else {
				var array = [];
				var jsonType = jsonType ? jsonType["arguments"][0] : null;

				jsonObject.forEach(function(item, index) {
					var dto = fromJsonObjectWithTypeOrArrayOrMap(jsonType, item, hashedObjects, modulesMap);
					array.push(dto);
				});
				return array;
			}
		} else if (jsonObject instanceof Object) {
			if (jsonObject["@type"]) {
				return fromJsonObjectWithType(jsonObject, hashedObjects, modulesMap)
			} else {
				var map = {};
				var jsonType = jsonType ? jsonType["arguments"][1] : null;

				Object.keys(jsonObject).forEach(function(key) {
					var dto = fromJsonObjectWithTypeOrArrayOrMap(jsonType, jsonObject[key], hashedObjects, modulesMap);
					map[key] = dto;
				});
				return map;
			}
		} else {
			if (_.isNumber(jsonObject) && jsonType && jsonType != "Date") {
				if (jsonObject in hashedObjects) {
					return hashedObjects[jsonObject];
				} else {
					throw "Object with id: " + JSON.stringify(jsonObject) + " and type: " + jsonType + " haven't been found in cache";
				}
			} else {
				return jsonObject;
			}
		}
	}

	var fromJsonObjectWithType = function(jsonObject, hashedObjects, modulesMap) {
		var jsonId = jsonObject["@id"]
		var jsonType = jsonObject["@type"]

		var moduleName = typeToModuleName(jsonType);
		var module = modulesMap[moduleName];
		var moduleFieldTypeMap = module.$typeDescription || {};
		try {
			var object = new module(""); // some entities have a mandatory non-null constructor parameters
		} catch(e) {
			throw "Failed deserializing object " + JSON.stringify(jsonObject) + " into type " + jsonType + " with error " + e.message;
		}

		if (jsonId) {
			if (jsonId in hashedObjects) {
				throw "This object has already been cached!"
			}
			hashedObjects[jsonId] = object
		}

		for ( var key in jsonObject) {
			var fieldType = moduleFieldTypeMap[key];
			var fieldValue = jsonObject[key];

			if (object["_" + key] !== undefined) {
				key = "_" + key;
			}

			object[key] = fromJsonObjectWithTypeOrArrayOrMap(fieldType, fieldValue, hashedObjects, modulesMap)
		}
		return object;
	};

	var decycleLocal = function(object, references) {
		if (object === null) {
			return object;
		}
		index = _.indexOf(references, object);
		if (index >= 0) {
			return index;
		}
		if (_.isArray(object)) {
			return _.map(object, function(el, key) {
				return decycleLocal(el, references)
			});
		} else if (_.isObject(object)) {
			var result = {};
			if (object["@type"] != null) {
				id = references.length;
				result["@id"] = id;
				references.push(object);
			}
			for ( var i in object) {
				if (_.isFunction(object[i]) === false && i !== "@id") {
					var value = decycleLocal(object[i], references);
					if (i.indexOf("_") == 0) {
						i = i.substring(1);
					}
					result[i] = value;

				}
			}
			return result;
		}
		return object;
	};

	return new Json();
});