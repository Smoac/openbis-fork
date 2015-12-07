/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/AbstractCompositeSearchCriteria", "dto/common/search/IdSearchCriteria" ], function(stjs, AbstractCompositeSearchCriteria, IdSearchCriteria) {
	var AbstractObjectSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(AbstractObjectSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.AbstractObjectSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withId = function() {
			return this.addCriteria(new IdSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return AbstractObjectSearchCriteria;
})