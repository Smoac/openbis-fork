define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var VocabularyUpdate = function() {
		this.description = new FieldUpdateValue();
		this.managedInternally = new FieldUpdateValue();
		this.internalNameSpace = new FieldUpdateValue();
		this.chosenFromList = new FieldUpdateValue();
		this.urlTemplate = new FieldUpdateValue();
	};
	stjs.extend(VocabularyUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.update.VocabularyUpdate';
		constructor.serialVersionUID = 1;
		prototype.vocabularyId = null;
		prototype.description = null;
		prototype.managedInternally = null;
		prototype.internalNameSpace = null;
		prototype.chosenFromList = null;
		prototype.urlTemplate = null;

		prototype.getObjectId = function() {
			return this.getVocabularyId();
		};
		prototype.getVocabularyId = function() {
			return this.vocabularyId;
		};
		prototype.setVocabularyId = function(vocabularyId) {
			this.vocabularyId = vocabularyId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getManagedInternally = function() {
			return this.managedInternally;
		};
		prototype.setManagedInternally = function(managedInternally) {
			this.managedInternally.setValue(managedInternally);
		};
		prototype.getInternalNameSpace = function() {
			return this.internalNameSpace;
		};
		prototype.setInternalNameSpace = function(internalNameSpace) {
			this.internalNameSpace.setValue(internalNameSpace);
		};
		prototype.getChosenFromList = function() {
			return this.chosenFromList;
		};
		prototype.setChosenFromList = function(chosenFromList) {
			this.chosenFromList.setValue(chosenFromList);
		};
		prototype.getUrlTemplate = function() {
			return this.urlTemplate;
		};
		prototype.setUrlTemplate = function(urlTemplate) {
			this.urlTemplate.setValue(urlTemplate);
		};
	}, {
		vocabularyId : "IVocabularyId"
	});
	return VocabularyUpdate;
})
