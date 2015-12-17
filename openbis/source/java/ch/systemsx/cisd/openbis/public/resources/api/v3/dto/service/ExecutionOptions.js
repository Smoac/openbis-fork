define([ "stjs"], function(stjs) {
	var ExecutionOptions = function() {
		this.parameters = {};
	};
	stjs.extend(ExecutionOptions, null, [ ], function(constructor, prototype) {
		prototype['@type'] = 'dto.service.ExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.parameters = null;
		prototype.withParameter = function(parameterName, value) {
			this.parameters[parameterName] = value;
		}
		proptotype.getParameters = function() {
			return this.parameters;
		}
	}, {
		parameters : {
			name : "Map",
			arguments : [ null, null ]
		}
	});
	return ExecutionOptions;
})