/**
 * 
 *
 * @author Franz-Josef Elmer
 */
define([ "stjs", "as/dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var ObjectKindModificationSearchCriteria = function() {
		this.objectKinds = [];
		this.operationKinds = [];
	};
	stjs.extend(ObjectKindModificationSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withObjectKinds = function(objectKinds) {
			this.objectKinds = objectKinds;
		}
		prototype.withOperationKinds = function(operationKinds) {
			this.operationKinds = operationKinds;
		}
		prototype.getObjectKinds = function() {
			return this.objectKinds;
		}
		prototype.getOperationKinds = function() {
			return this.operationKinds;
		}
	}, {});
	return ObjectKindModificationSearchCriteria;
})
