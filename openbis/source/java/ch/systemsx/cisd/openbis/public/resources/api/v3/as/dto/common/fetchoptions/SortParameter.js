/**
 * @author juanf
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var SortParameter = function() {
		Enum.call(this, [ "FULL_MATCH_CODE_BOOST", "PARTIAL_MATCH_CODE_BOOST", "FULL_MATCH_TYPE_BOOST", "FULL_MATCH_PROPERTY_BOOST", "PARTIAL_MATCH_PROPERTY_BOOST" ]);
	};
	stjs.extend(SortParameter, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new SortParameter();
})
