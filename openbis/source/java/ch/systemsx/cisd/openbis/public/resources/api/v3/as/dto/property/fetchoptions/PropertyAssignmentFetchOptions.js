define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/property/fetchoptions/PropertyTypeFetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions",
		"as/dto/property/fetchoptions/PropertyAssignmentSortOptions" ], function(stjs, FetchOptions) {
	var PropertyAssignmentFetchOptions = function() {
	};
	stjs.extend(PropertyAssignmentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyAssignmentFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.propertyType = null;
		prototype.registrator = null;
		prototype.sort = null;
		prototype.withPropertyType = function() {
			if (this.propertyType == null) {
				var PropertyTypeFetchOptions = require("as/dto/property/fetchoptions/PropertyTypeFetchOptions");
				this.propertyType = new PropertyTypeFetchOptions();
			}
			return this.propertyType;
		};
		prototype.withPropertyTypeUsing = function(fetchOptions) {
			return this.propertyType = fetchOptions;
		};
		prototype.hasPropertyType = function() {
			return this.propertyType != null;
		};
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
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
		propertyType : "PropertyType",
		registrator : "Person",
		sort : "PropertyAssignmentSortOptions"
	});
	return PropertyAssignmentFetchOptions;
})