/**
 * @author pkupczyk
 */
define([ "stjs", "dto/entity/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var SpaceDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(SpaceDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.deletion.space.SpaceDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SpaceDeletionOptions;
})