/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/update/ListUpdateValue" ], function(stjs, ListUpdateValue) {
	var IdListUpdateValue = function() {
		ListUpdateValue.call(this);
	};
	stjs.extend(IdListUpdateValue, ListUpdateValue, [ ListUpdateValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.update.IdListUpdateValue';
		constructor.serialVersionUID = 1;
	}, {
		actions : {
			name : "List",
			arguments : [ {
				name : "ListUpdateValue.ListUpdateAction",
				arguments : [ "ACTION" ]
			} ]
		}
	});
	return IdListUpdateValue;
})