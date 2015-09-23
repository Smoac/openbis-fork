define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var AttachmentSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(AttachmentSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.attachment.AttachmentSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return AttachmentSortOptions;
})