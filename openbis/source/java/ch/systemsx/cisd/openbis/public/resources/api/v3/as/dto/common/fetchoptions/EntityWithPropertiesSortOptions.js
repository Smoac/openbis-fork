define([ "require", "stjs", "as/dto/common/fetchoptions/EntitySortOptions", "as/dto/common/fetchoptions/SortParameter" ], function(require, stjs, EntitySortOptions, SortParameter) {
	var EntityWithPropertiesSortOptions = function() {
		EntitySortOptions.call(this);
	};

	var fields = {
		FETCHED_FIELDS_SCORE : "FETCHED_FIELDS_SCORE",
		TYPE : "TYPE",
		PROPERTY : "PROPERTY"
	};
    
	stjs.extend(EntityWithPropertiesSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.fetchoptions.EntityWithPropertiesSortOptions';
		constructor.serialVersionUID = 1;

		prototype.fetchedFieldsScore = function() {
			var parameters = {
				SortParameter.FULL_CODE_BOOST : 		"1000000",
				SortParameter.PARTIAL_CODE_BOOST : 	 "100000",
				SortParameter.FULL_PROPERTY_BOOST :    "10000",
				SortParameter.FULL_TYPE_BOOST : 		   "1000",
				SortParameter.PARTIAL_PROPERTY_BOOST :   "100"
			};
			return this.getOrCreateSortingWithParameters(fields.FETCHED_FIELDS_SCORE, parameters);
		};
		
		prototype.type = function() {
			return this.getOrCreateSorting(fields.TYPE);
		};
		prototype.getType = function() {
			return this.getSorting(fields.TYPE);
		};
		prototype.property = function(propertyName) {
			return this.getOrCreateSorting(fields.PROPERTY + propertyName);
		};
		prototype.getProperty = function(propertyName) {
			return this.getSorting(fields.PROPERTY + propertyName);
		};

	}, {});
	return EntityWithPropertiesSortOptions;
})