/**
 * @author Franz-Josef Elmer
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var DataType = function() {
		Enum.call(this, [ "INTEGER", "VARCHAR", "MULTILINE_VARCHAR", "REAL", "TIMESTAMP", "BOOLEAN",
		 "CONTROLLEDVOCABULARY", "MATERIAL", "HYPERLINK", "XML", "SAMPLE", "DATE",
		  "JSON", "ARRAY_INTEGER", "ARRAY_REAL", "ARRAY_STRING", "ARRAY_TIMESTAMP"]);
	};
	stjs.extend(DataType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new DataType();
})
