define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var EntitySortOptions = function() {
		SortOptions.call(this);
	};

	var fields = {
		CODE : "CODE",
		REGISTRATION_DATE : "REGISTRATION_DATE",
		MODIFICATION_DATE : "MODIFICATION_DATE"
	};

	stjs.extend(EntitySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.fetchoptions.EntitySortOptions';
		constructor.serialVersionUID = 1;
		prototype.code = function() {
			return this.getOrCreateSorting(fields.CODE);
		};
		prototype.getCode = function() {
			return this.getSorting(fields.CODE);
		};
		prototype.registrationDate = function() {
			return this.getOrCreateSorting(fields.REGISTRATION_DATE);
		};
		prototype.getRegistrationDate = function() {
			return this.getSorting(fields.REGISTRATION_DATE);
		};
		prototype.modificationDate = function() {
			return this.getOrCreateSorting(fields.MODIFICATION_DATE);
		};
		prototype.getModificationDate = function() {
			return this.getSorting(fields.MODIFICATION_DATE);
		};
	}, {});
	return EntitySortOptions;
})