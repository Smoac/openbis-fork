/**
 * @author pkupczyk
 */
define([ "stjs", "dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var SampleDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(SampleDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.deletion.sample.SampleDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleDeletionOptions;
})