define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PropertyType = function() {
	};
	stjs.extend(PropertyType, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.PropertyType';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;
		prototype.dataTypeCode = null;
		prototype.internalNameSpace = null;
		prototype.vocabularyFetchOptions = null;
		prototype.vocabulary = null;
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getDataTypeCode = function() {
			return this.dataTypeCode;
		};
		prototype.setDataTypeCode = function(dataTypeCode) {
			this.dataTypeCode = dataTypeCode;
		};
		prototype.isInternalNameSpace = function() {
			return this.internalNameSpace;
		};
		prototype.setInternalNameSpace = function(internalNameSpace) {
			this.internalNameSpace = internalNameSpace;
		};
		prototype.getVocabularyFetchOptions = function() {
			return this.vocabularyFetchOptions;
		};
		prototype.setVocabularyFetchOptions = function(vocabularyFetchOptions) {
			this.vocabularyFetchOptions = vocabularyFetchOptions;
		};
		prototype.getVocabulary = function() {
			if (this.getVocabularyFetchOptions()) {
				return this.vocabulary;
			} else {
				throw new exceptions.NotFetchedException("Vocabulary has not been fetched.");
			}
		};
		prototype.setVocabulary = function(vocabulary) {
			this.vocabulary = vocabulary;
		};
	}, {
		vocabularyFetchOptions : "VocabularyFetchOptions",
		vocabulary : "Vocabulary",
		dataTypeCode : "DataTypeCode"
	});
	return PropertyType;
})