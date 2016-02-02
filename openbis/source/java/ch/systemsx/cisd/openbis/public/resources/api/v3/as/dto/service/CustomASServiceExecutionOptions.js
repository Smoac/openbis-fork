define([ "stjs"], function(stjs) {
	var CustomASServiceExecutionOptions = function() {
		this.parameters = {};
	};
	stjs.extend(ExecutionOptions, null, [ ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.CustomASServiceExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.parameters = null;
		prototype.withParameter = function(parameterName, value) {
			this.parameters[parameterName] = value;
			return this;
		}
		prototype.getParameters = function() {
			return this.parameters;
		}
	}, {
		parameters : {
			name : "Map",
			arguments : [ null, null ]
		}
	});
	return CustomASServiceExecutionOptions;
})