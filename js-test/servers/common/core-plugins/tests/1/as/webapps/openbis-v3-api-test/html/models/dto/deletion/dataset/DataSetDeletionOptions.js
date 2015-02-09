/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var DataSetDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(DataSetDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.deletion.dataset.DataSetDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetDeletionOptions;
})