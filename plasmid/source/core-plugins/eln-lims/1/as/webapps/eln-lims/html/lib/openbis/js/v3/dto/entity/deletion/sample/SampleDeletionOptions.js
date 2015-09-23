/**
 * @author pkupczyk
 */
define([ "stjs", "dto/entity/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var SampleDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(SampleDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.deletion.sample.SampleDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleDeletionOptions;
})