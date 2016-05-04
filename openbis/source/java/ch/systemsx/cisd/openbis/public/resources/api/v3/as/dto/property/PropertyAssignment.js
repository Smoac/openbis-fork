define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PropertyAssignment = function() {
	};
	stjs.extend(PropertyAssignment, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.PropertyAssignment';
		constructor.serialVersionUID = 1;
		prototype.mandatory = null;
		prototype.propertyType = null;
		prototype.isMandatory = function() {
			return this.mandatory;
		};
		prototype.setMandatory = function(mandatory) {
			this.mandatory = mandatory;
		};
		prototype.getPropertyType = function() {
			return this.propertyType;
		};
		prototype.setPropertyType = function(propertyType) {
			this.propertyType = propertyType;
		};
		prototype.getVocabularyFetchOptions = function() {
			if (this.propertyType == null) {
				return null;
			}
			return this.propertyType.getVocabularyFetchOptions();
		}
		prototype.getVocabulary = function() {
			if (this.getVocabularyFetchOptions()) {
				return this.propertyType.getVocabulary();
			} else {
				throw new exceptions.NotFetchedException("Vocabulary has not been fetched.");
			}		}
	}, {
		propertyType : "PropertyType"
	});
	return PropertyAssignment;
})