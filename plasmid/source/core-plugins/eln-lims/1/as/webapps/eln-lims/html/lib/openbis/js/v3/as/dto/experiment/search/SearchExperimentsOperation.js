/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperation" ], function(stjs, IOperation) {
	var SearchForExperimentsOperation = function() {
	};
	stjs.extend(SearchForExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.search.SearchExperimentsOperation';
	}, {});
	return SearchForExperimentsOperation;
})