/**
 * Represents value together with {@code isModified} flag
 * 
 * @author Jakub Straszewski
 */
define([ "support/stjs" ], function(stjs) {
	var FieldUpdateValue = function() {
	};
	stjs.extend(FieldUpdateValue, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.FieldUpdateValue';
		constructor.serialVersionUID = 1;
		prototype.isModified = false;
		prototype.value = null;
		/**
		 * value for update
		 */
		prototype.setValue = function(value) {
			this.value = value;
			this.isModified = true;
		};
		/**
		 * @return {@code true} if the value has been set for update.
		 */
		prototype.isModified = function() {
			return this.isModified;
		};
		/**
		 * @return value for update
		 */
		prototype.getValue = function() {
			return this.value;
		};
	}, {});
	return FieldUpdateValue;
})