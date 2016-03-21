/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/common/search/PermIdSearchCriteria",
		"as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/vocabulary/search/VocabularyTermSearchCriteria", "as/dto/vocabulary/search/VocabularyCodeSearchCriteria" ], 
		function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {
	var VocabularyTermSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(VocabularyTermSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.search.TagSearchCriteria';
		constructor.serialVersionUID = 1;
		
		prototype.withPermId = function() {
	        return this.with(new require("as/dto/common/search/PermIdSearchCriteria")());
	    };

		prototype.withCode = function() {
	        return this.with(new require("as/dto/vocabulary/search/VocabularyTermSearchCriteria")());
	    };

		prototype.withVocabularyCode = function() {
	        return this.with(new require("as/dto/vocabulary/search/VocabularyCodeSearchCriteria")());
	    };

		prototype.withOrOperator = function() {
	        return this.withOperator(new SearchOperator().OR);
	    };

		prototype.withAndOperator = function() {
	        return this.withOperator(new SearchOperator().AND);
	    };
	}, {
//		criteria : {
//			name : "Collection",
//			arguments : [ "ISearchCriteria" ]
//		}
	});
	return VocabularyTermSearchCriteria;
})