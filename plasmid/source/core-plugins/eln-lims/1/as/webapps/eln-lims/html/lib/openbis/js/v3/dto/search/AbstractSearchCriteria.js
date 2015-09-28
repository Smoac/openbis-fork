/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var AbstractSearchCriteria = function() {
	};
	stjs.extend(AbstractSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return AbstractSearchCriteria;
})