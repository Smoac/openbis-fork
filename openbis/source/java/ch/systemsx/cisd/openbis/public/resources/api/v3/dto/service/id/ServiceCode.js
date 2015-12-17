/**
 * Generic service code. This is the name of an AS core plugin of type 'services'.
 *
 * @author Franz-Josef Elmer
 */
define([ "stjs", "dto/common/id/ObjectPermId", "dto/service/id/IServiceId" ], function(stjs, ObjectPermId, IServiceId) {
	var ServiceCode = function(code) {
		ObjectPermId.call(this, code);
	};
	stjs.extend(ServiceCode, ObjectPermId, [ ObjectPermId, IServiceId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.service.id.ServiceCode';
		constructor.serialVersionUID = 1;
	}, {});
	return ServiceCode;
})