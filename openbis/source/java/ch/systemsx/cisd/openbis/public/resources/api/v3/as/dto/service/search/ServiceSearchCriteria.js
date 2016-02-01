define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria"], 
	function(require, stjs, AbstractObjectSearchCriteria) {
	var ServiceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(ServiceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.ServiceSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.createBuilder = function() {
			var AbstractCompositeSearchCriteria = require("as/dto/common/search/AbstractCompositeSearchCriteria");
			var builder = AbstractCompositeSearchCriteria.prototype.createBuilder.call(this);
			builder.setName("SERVICE");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return ServiceSearchCriteria;
})