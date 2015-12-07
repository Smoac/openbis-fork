/**
 * Sample identifier.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectIdentifier", "dto/sample/id/ISampleId" ], function(stjs, ObjectIdentifier, ISampleId) {
	/**
	 * @param identifier
	 *            Sample identifier, e.g. "/MY_SPACE/MY_SAMPLE" (space sample)
	 *            or "/MY_SAMPLE" (shared sample)
	 */
	var SampleIdentifier = function(identifier) {
		ObjectIdentifier.call(this, identifier);
	};
	stjs.extend(SampleIdentifier, ObjectIdentifier, [ ObjectIdentifier, ISampleId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.id.SampleIdentifier';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleIdentifier;
})