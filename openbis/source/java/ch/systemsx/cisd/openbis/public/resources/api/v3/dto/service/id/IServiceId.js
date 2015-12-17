/**
 * Holds information that uniquely identifies a generic service in openBIS.
 *
 * @author Franz-Josef Elmer
 */
define([ "stjs", "dto/common/id/IObjectId" ], function(stjs, IObjectId) {
  var IServiceId = function() {
  };
  stjs.extend(IServiceId, null, [ IObjectId ], null, {});
  return IServiceId;
})