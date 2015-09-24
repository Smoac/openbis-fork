/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractEntitySearchCriteria", "dto/search/ProjectSearchCriteria", "dto/search/SearchOperator" ], function(stjs, AbstractEntitySearchCriteria,
		ProjectSearchCriteria, SearchOperator) {
	var ExperimentSearchCriteria = function() {
		AbstractEntitySearchCriteria.call(this);
	};
	stjs.extend(ExperimentSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.ExperimentSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withProject = function() {
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