/**
 * @author Franz-Josef Elmer
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
    var ScriptType = function() {
        Enum.call(this, [ "DYNAMIC_PROPERTY", "MANAGED_PROPERTY", "ENTITY_VALIDATION" ]);
    };
    stjs.extend(ScriptType, Enum, [ Enum ], function(constructor, prototype) {
    }, {});
    return new ScriptType();
})
