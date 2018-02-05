/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/common/search/CodeSearchCriteria" ], function(require, stjs, AbstractCompositeSearchCriteria) {
	var ExternalDmsSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(ExternalDmsSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.ExternalDmsSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return ExternalDmsSearchCriteria;
})