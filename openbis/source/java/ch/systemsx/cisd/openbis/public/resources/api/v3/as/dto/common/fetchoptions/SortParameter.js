/**
 * @author juanf
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var SortParameter = function() {
		Enum.call(this, [ "FULL_CODE_BOOST", "PARTIAL_CODE_BOOST", "FULL_PROPERTY_BOOST", "FULL_TYPE_BOOST", "PARTIAL_PROPERTY_BOOST" ]);
	};
	stjs.extend(SortParameter, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new SortParameter();
})
