/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/Enum" ], function(stjs, Enum) {
	var DataSetSearchRelation = function() {
		Enum.call(this, [ "DATASET", "PARENTS", "CHILDREN", "CONTAINER" ]);
	};
	stjs.extend(DataSetSearchRelation, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new DataSetSearchRelation();
})
