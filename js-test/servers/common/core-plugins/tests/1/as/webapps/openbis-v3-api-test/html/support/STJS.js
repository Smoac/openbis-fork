/**
 * Library
 */
var STJS = function() {};
STJS.prototype.extend = function(classToExtend, somethingNull, parentClasses, functionToReplay, metadata) {
	if(functionToReplay !== null) {
		functionToReplay(classToExtend.prototype, classToExtend.prototype);
	}
};

var stjs = new STJS();

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