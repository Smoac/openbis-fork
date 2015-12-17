/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var AbstractSearchCriteria = function() {
	};
	stjs.extend(AbstractSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.AbstractSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.hashCode = function() {
		};
	}, {});
	return AbstractSearchCriteria;
})