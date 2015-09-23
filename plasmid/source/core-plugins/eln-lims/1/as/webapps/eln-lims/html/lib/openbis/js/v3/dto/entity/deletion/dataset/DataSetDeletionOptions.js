/**
 * @author pkupczyk
 */
define([ "stjs", "dto/entity/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var DataSetDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(DataSetDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.deletion.dataset.DataSetDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetDeletionOptions;
})