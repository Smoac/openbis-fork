/**
 * @author Franz-Josef Elmer
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
    var PluginType = function() {
        Enum.call(this, [ "JYTHON", "PREDEPLOYED" ]);
    };
    stjs.extend(PluginType, Enum, [ Enum ], function(constructor, prototype) {
    }, {});
    return new PluginType();
})
