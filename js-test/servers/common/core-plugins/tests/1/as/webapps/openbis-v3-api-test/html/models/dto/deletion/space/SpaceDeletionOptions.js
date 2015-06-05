/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var SpaceDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(SpaceDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.deletion.space.SpaceDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SpaceDeletionOptions;
})