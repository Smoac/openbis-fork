/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractEntitySearchCriterion", "dto/search/ProjectSearchCriterion", "dto/search/SearchOperator" ], function(stjs, AbstractEntitySearchCriterion,
		ProjectSearchCriterion, SearchOperator) {
	var ExperimentSearchCriterion = function() {
		AbstractEntitySearchCriterion.call(this);
	};
	stjs.extend(ExperimentSearchCriterion, AbstractEntitySearchCriterion, [ AbstractEntitySearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.ExperimentSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.withProject = function() {
			return this.addCriterion(new ProjectSearchCriterion());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
		prototype.createBuilder = function() {
			var builder = AbstractEntitySearchCriterion.prototype.createBuilder.call(this);
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
			arguments : [ "ISearchCriterion" ]
		}
	});
	return ExperimentSearchCriterion;
})