/**
 *  Represents value together with {@code isModified} flag
 *  
 *  @author Jakub Straszewski
 */
define([], function () {
    var FieldUpdateValue = function() {};
    stjs.extend(FieldUpdateValue, null, [], function(constructor, prototype) {
        prototype['@type'] = 'FieldUpdateValue';
        constructor.serialVersionUID = 1;
        prototype.isModified = false;
        prototype.value = null;
        /**
         *  value for update
         */
        prototype.setValue = function(value) {
            this.value = value;
            this.isModified = true;
        };
        /**
         *  @return {@code true} if the value has been set for update.
         */
        prototype.isModified = function() {
            return this.isModified;
        };
        /**
         *  @return value for update
         */
        prototype.getValue = function() {
            return this.value;
        };
    }, {});
    return FieldUpdateValue;
})