/**
 * @author Franz-Josef Elmer
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var DataTypeCode = function() {
		Enum.call(this, [ "INTEGER", "VARCHAR", "MULTILINE_VARCHAR", "REAL", "TIMESTAMP", "BOOLEAN", 
		                  "CONTROLLEDVOCABULARY", "MATERIAL", "HYPERLINK", "XML" ]);
	};
	stjs.extend(DataTypeCode, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new DataTypeCode();
})
