/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/common/search/AbstractEntitySearchCriteria", "dto/common/search/SearchOperator", "dto/project/search/ProjectSearchCriteria" ], function(require, stjs,
		AbstractEntitySearchCriteria, SearchOperator) {
	var ExperimentSearchCriteria = function() {
		AbstractEntitySearchCriteria.call(this);
	};
	stjs.extend(ExperimentSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.search.ExperimentSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withProject = function() {
			var ProjectSearchCriteria = require("dto/project/search/ProjectSearchCriteria");
			return this.addCriteria(new ProjectSearchCriteria());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
		prototype.createBuilder = function() {
			var builder = AbstractEntitySearchCriteria.prototype.createBuilder.call(this);
			builder.setName("EXPERIMENT");
			return builder;
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