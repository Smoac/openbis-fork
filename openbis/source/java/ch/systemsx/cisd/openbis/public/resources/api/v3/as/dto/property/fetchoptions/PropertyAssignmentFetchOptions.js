define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/vocabulary/fetchoptions/VocabularyFetchOptions", 
         "as/dto/property/fetchoptions/PropertyAssignmentSortOptions" ], function(stjs, FetchOptions) {
	var PropertyAssignmentFetchOptions = function() {
	};
	stjs.extend(PropertyAssignmentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyAssignmentFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.vocabulary = null;
		prototype.sort = null;
		prototype.withVocabulary = function() {
			if (this.vocabulary == null) {
				var VocabularyFetchOptions = require("as/dto/vocabulary/fetchoptions/VocabularyFetchOptions");
				this.vocabulary = new VocabularyFetchOptions();
			}
			return this.vocabulary;
		};
		prototype.withVocabularyUsing = function(fetchOptions) {
			return this.vocabulary = fetchOptions;
		};
		prototype.hasVocabulary = function() {
			return this.vocabulary != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var PropertyAssignmentSortOptions = require("as/dto/property/fetchoptions/PropertyAssignmentSortOptions");
				this.sort = new PropertyAssignmentSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "PropertyAssignmentSortOptions"
	});
	return PropertyAssignmentFetchOptions;
})