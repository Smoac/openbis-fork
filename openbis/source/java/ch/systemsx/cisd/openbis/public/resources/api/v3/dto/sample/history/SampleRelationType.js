/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/Enum" ], function(stjs, Enum) {
	var SampleRelationType = function() {
		Enum.call(this, [ "SPACE", "EXPERIMENT", "PARENT", "CHILD", "CONTAINER", "COMPONENT", "DATA_SET" ]);
	};
	stjs.extend(SampleRelationType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new SampleRelationType();
})
