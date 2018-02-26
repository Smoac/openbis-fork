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
			var parameters = {};
				parameters[SortParameter.FULL_MATCH_CODE_BOOST] = 		"1000000";
				parameters[SortParameter.PARTIAL_MATCH_CODE_BOOST] = 	 	 "100000";
				parameters[SortParameter.FULL_MATCH_PROPERTY_BOOST] = 	  "10000";
				parameters[SortParameter.FULL_MATCH_TYPE_BOOST] = 		   "1000";
				parameters[SortParameter.PARTIAL_MATCH_PROPERTY_BOOST] =   	"100";
			
			return this.getOrCreateSortingWithParameters(fields.FETCHED_FIELDS_SCORE, parameters);
		};
		prototype.getFetchedFieldsScore = function() {
			return this.getSorting(fields.FETCHED_FIELDS_SCORE);
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