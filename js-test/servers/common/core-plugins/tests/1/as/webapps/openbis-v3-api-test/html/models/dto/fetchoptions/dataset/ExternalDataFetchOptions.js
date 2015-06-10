/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "support/stjs", "dto/fetchoptions/vocabulary/VocabularyTermFetchOptions", "dto/fetchoptions/dataset/FileFormatTypeFetchOptions", "dto/fetchoptions/dataset/LocatorTypeFetchOptions" ],
		function(stjs, VocabularyTermFetchOptions, FileFormatTypeFetchOptions, LocatorTypeFetchOptions) {
			var ExternalDataFetchOptions = function() {
			};
			stjs.extend(ExternalDataFetchOptions, null, [], function(constructor, prototype) {
				prototype['@type'] = 'dto.fetchoptions.dataset.ExternalDataFetchOptions';
				constructor.serialVersionUID = 1;
				prototype.storageFormat = null;
				prototype.fileFormatType = null;
				prototype.locatorType = null;
				prototype.withStorageFormat = function() {
					if (this.storageFormat == null) {
						this.storageFormat = new VocabularyTermFetchOptions();
					}
					return this.storageFormat;
				};
				prototype.withStorageFormatUsing = function(fetchOptions) {
					return this.storageFormat = fetchOptions;
				};
				prototype.hasStorageFormat = function() {
					return this.storageFormat != null;
				};
				prototype.withFileFormatType = function() {
					if (this.fileFormatType == null) {
						this.fileFormatType = new FileFormatTypeFetchOptions();
					}
					return this.fileFormatType;
				};
				prototype.withFileFormatTypeUsing = function(fetchOptions) {
					return this.fileFormatType = fetchOptions;
				};
				prototype.hasFileFormatType = function() {
					return this.fileFormatType != null;
				};
				prototype.withLocatorType = function() {
					if (this.locatorType == null) {
						this.locatorType = new LocatorTypeFetchOptions();
					}
					return this.locatorType;
				};
				prototype.withLocatorTypeUsing = function(fetchOptions) {
					return this.locatorType = fetchOptions;
				};
				prototype.hasLocatorType = function() {
					return this.locatorType != null;
				};
			}, {
				storageFormat : "VocabularyTermFetchOptions",
				fileFormatType : "FileFormatTypeFetchOptions",
				locatorType : "LocatorTypeFetchOptions"
			});
			return ExternalDataFetchOptions;
		})