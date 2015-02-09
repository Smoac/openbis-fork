/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractEntitySearchCriterion", "dto/search/DataSetSearchRelation", "dto/search/DataSetParentsSearchCriterion", "dto/search/DataSetChildrenSearchCriterion",
		"dto/search/DataSetContainerSearchCriterion", "dto/search/ExperimentSearchCriterion", "dto/search/SampleSearchCriterion", "dto/search/SearchOperator" ], function(stjs,
		AbstractEntitySearchCriterion, DataSetSearchRelation, DataSetParentsSearchCriterion, DataSetChildrenSearchCriterion, DataSetContainerSearchCriterion, ExperimentSearchCriterion,
		SampleSearchCriterion, SearchOperator) {
	var DataSetSearchCriterion = function() {
		this.relation = DataSetSearchRelation.DATASET;
	};
	stjs.extend(DataSetSearchCriterion, AbstractEntitySearchCriterion, [ AbstractEntitySearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.relation = null;
		prototype.withParents = function() {
			return this.addCriterion(new DataSetParentsSearchCriterion());
		};
		prototype.withChildren = function() {
			return this.addCriterion(new DataSetChildrenSearchCriterion());
		};
		prototype.withContainer = function() {
			return this.addCriterion(new DataSetContainerSearchCriterion());
		};
		prototype.withExperiment = function() {
			return this.addCriterion(new ExperimentSearchCriterion());
		};
		prototype.withSample = function() {
			return this.addCriterion(new SampleSearchCriterion());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
		prototype.getRelation = function() {
			return this.relation;
		};
		prototype.createBuilder = function() {
			var builder = AbstractEntitySearchCriterion.prototype.createBuilder.call(this);
			builder.setName(this.relation.name());
			return builder;
		};
	}, {
		relation : {
			name : "Enum",
			arguments : [ "DataSetSearchRelation" ]
		},
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return DataSetSearchCriterion;
})