/**
 * @author pkupczyk
 */
define([ "require", "support/stjs", "dto/search/AbstractEntitySearchCriterion", "dto/search/SampleSearchRelation", "dto/search/SpaceSearchCriterion", "dto/search/ExperimentSearchCriterion",
		"dto/search/SampleParentsSearchCriterion", "dto/search/SampleChildrenSearchCriterion", "dto/search/SampleContainerSearchCriterion", "dto/search/SearchOperator" ], function(require, stjs,
		AbstractEntitySearchCriterion, SampleSearchRelation, SpaceSearchCriterion, ExperimentSearchCriterion, SampleParentsSearchCriterion, SampleChildrenSearchCriterion,
		SampleContainerSearchCriterion, SearchOperator) {
	var SampleSearchCriterion = function() {
		this.relation = SampleSearchRelation.SAMPLE;
	};
	stjs.extend(SampleSearchCriterion, AbstractEntitySearchCriterion, [ AbstractEntitySearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.relation = null;
		prototype.withSpace = function() {
			return this.addCriterion(new SpaceSearchCriterion());
		};
		prototype.withExperiment = function() {
			return this.addCriterion(new ExperimentSearchCriterion());
		};
		prototype.withParents = function() {
			// require again because of circular dependency
			// SampleSearchCriterion<->SampleParentsSearchCriterion
			var SampleParentsSearchCriterion = require("dto/search/SampleParentsSearchCriterion");
			return this.addCriterion(new SampleParentsSearchCriterion());
		};
		prototype.withChildren = function() {
			// require again because of circular dependency
			// SampleSearchCriterion<->SampleChildrenSearchCriterion
			var SampleChildrenSearchCriterion = require("dto/search/SampleChildrenSearchCriterion");
			return this.addCriterion(new SampleChildrenSearchCriterion());
		};
		prototype.withContainer = function() {
			// require again because of circular dependency
			// SampleSearchCriterion<->SampleContainerSearchCriterion
			var SampleContainerSearchCriterion = require("dto/search/SampleContainerSearchCriterion");
			return this.addCriterion(new SampleContainerSearchCriterion());
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
			arguments : [ "SampleSearchRelation" ]
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
	return SampleSearchCriterion;
})