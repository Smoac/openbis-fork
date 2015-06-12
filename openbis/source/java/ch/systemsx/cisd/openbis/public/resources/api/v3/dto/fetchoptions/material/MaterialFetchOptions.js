/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/material/MaterialTypeFetchOptions", "dto/fetchoptions/history/HistoryEntryFetchOptions", "dto/fetchoptions/person/PersonFetchOptions",
		"dto/fetchoptions/property/PropertyFetchOptions", "dto/fetchoptions/tag/TagFetchOptions" ], function(require, stjs) {
	var MaterialFetchOptions = function() {
	};
	stjs.extend(MaterialFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.material.MaterialFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.type = null;
		prototype.history = null;
		prototype.registrator = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.tags = null;
		prototype.withType = function() {
			if (this.type == null) {
				var MaterialTypeFetchOptions = require("dto/fetchoptions/material/MaterialTypeFetchOptions");
				this.type = new MaterialTypeFetchOptions();
			}
			return this.type;
		};
		prototype.withTypeUsing = function(fetchOptions) {
			return this.type = fetchOptions;
		};
		prototype.hasType = function() {
			return this.type != null;
		};
		prototype.withHistory = function() {
			if (this.history == null) {
				var HistoryEntryFetchOptions = require("dto/fetchoptions/history/HistoryEntryFetchOptions");
				this.history = new HistoryEntryFetchOptions();
			}
			return this.history;
		};
		prototype.withHistoryUsing = function(fetchOptions) {
			return this.history = fetchOptions;
		};
		prototype.hasHistory = function() {
			return this.history != null;
		};
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("dto/fetchoptions/person/PersonFetchOptions");
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
		prototype.withProperties = function() {
			if (this.properties == null) {
				var PropertyFetchOptions = require("dto/fetchoptions/property/PropertyFetchOptions");
				this.properties = new PropertyFetchOptions();
			}
			return this.properties;
		};
		prototype.withPropertiesUsing = function(fetchOptions) {
			return this.properties = fetchOptions;
		};
		prototype.hasProperties = function() {
			return this.properties != null;
		};
		prototype.withMaterialProperties = function() {
			if (this.materialProperties == null) {
				this.materialProperties = new MaterialFetchOptions();
			}
			return this.materialProperties;
		};
		prototype.withMaterialPropertiesUsing = function(fetchOptions) {
			return this.materialProperties = fetchOptions;
		};
		prototype.hasMaterialProperties = function() {
			return this.materialProperties != null;
		};
		prototype.withTags = function() {
			if (this.tags == null) {
				var TagFetchOptions = require("dto/fetchoptions/tag/TagFetchOptions");
				this.tags = new TagFetchOptions();
			}
			return this.tags;
		};
		prototype.withTagsUsing = function(fetchOptions) {
			return this.tags = fetchOptions;
		};
		prototype.hasTags = function() {
			return this.tags != null;
		};
	}, {
		type : "MaterialTypeFetchOptions",
		history : "HistoryEntryFetchOptions",
		registrator : "PersonFetchOptions",
		properties : "PropertyFetchOptions",
		materialProperties : "MaterialFetchOptions",
		tags : "TagFetchOptions"
	});
	return MaterialFetchOptions;
})