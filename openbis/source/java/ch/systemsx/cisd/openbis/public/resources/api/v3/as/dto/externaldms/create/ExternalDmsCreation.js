/**
 * @author anttil
 */
define([ "stjs" ], function(stjs) {
	var ExternalDmsCreation = function() {
	};
	stjs.extend(ExternalDmsCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.create.ExternalDmsCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.label = null;
		prototype.urlTemplate = null;
		prototype.type = null;
		prototype.creationId = null;

		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setUrlTemplate = function(urlTemplate) {
			this.urlTemplate = urlTemplate;
		};
		prototype.getUrlTemplate = function() {
			return this.urlTemplate;
		};
		prototype.setType = function(type) {
			this.type = type;
		};
		prototype.getType = function() {
			return this.type;
		};
		prototype.getCreationId = function() {
			return this.creationId;
		};
		prototype.setCreationId = function(creationId) {
			this.creationId = creationId;
		};
	}, {
		creationId : "CreationId"
	});
	return ExternalDmsCreation;
})