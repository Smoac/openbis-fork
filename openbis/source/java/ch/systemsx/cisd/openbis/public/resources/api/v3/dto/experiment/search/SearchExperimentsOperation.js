/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var SearchForExperimentsOperation = function() {
	};
	stjs.extend(SearchForExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.search.SearchExperimentsOperation';
	}, {});
	return SearchForExperimentsOperation;
})