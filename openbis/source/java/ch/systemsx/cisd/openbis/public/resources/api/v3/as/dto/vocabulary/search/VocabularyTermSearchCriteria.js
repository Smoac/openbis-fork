/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria"], 
		function(require, stjs, AbstractObjectSearchCriteria) {
	var VocabularyTermSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(VocabularyTermSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.VocabularyTermSearchCriteria';
		constructor.serialVersionUID = 1;
		
		prototype.withPermId = function() {
			return this.with(new require("as/dto/common/search/PermIdSearchCriteria")());
		};
		prototype.withCode = function() {
			return this.addCriteria(new require("as/dto/common/search/CodeSearchCriteria")());
		};
		prototype.withVocabulary = function() {
			return this.with(new require("as/dto/vocabulary/search/VocabularySearchCriteria")());
		};
	}, {
//		criteria : {
//			name : "Collection",
//			arguments : [ "ISearchCriteria" ]
//		}
	});
	return VocabularyTermSearchCriteria;
})