define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria"], 
	function(require, stjs, AbstractObjectSearchCriteria) {
	var CustomASServiceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(CustomASServiceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.CustomASServiceSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.createBuilder = function() {
			var AbstractCompositeSearchCriteria = require("as/dto/common/search/AbstractCompositeSearchCriteria");
			var builder = AbstractCompositeSearchCriteria.prototype.createBuilder.call(this);
			builder.setName("CUSTOM_AS_SERVICE");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return CustomASServiceSearchCriteria;
})