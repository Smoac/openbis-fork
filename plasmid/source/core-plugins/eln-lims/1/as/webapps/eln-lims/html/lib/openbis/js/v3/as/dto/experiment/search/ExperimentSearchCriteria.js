/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractEntitySearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/project/search/ProjectSearchCriteria" ], function(require, stjs,
		AbstractEntitySearchCriteria, SearchOperator) {
	var ExperimentSearchCriteria = function() {
		AbstractEntitySearchCriteria.call(this);
	};
	stjs.extend(ExperimentSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.search.ExperimentSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withProject = function() {
			var ProjectSearchCriteria = require("as/dto/project/search/ProjectSearchCriteria");
			return this.addCriteria(new ProjectSearchCriteria());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
	}, {
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return ExperimentSearchCriteria;
})