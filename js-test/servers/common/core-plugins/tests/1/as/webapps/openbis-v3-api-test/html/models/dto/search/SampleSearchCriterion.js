/**
 * @author pkupczyk
 */
define([ "require", "support/stjs", "dto/search/AbstractEntitySearchCriterion", "dto/search/SampleSearchRelation", "dto/search/SpaceSearchCriterion", "dto/search/ExperimentSearchCriterion",
		"dto/search/SearchOperator" ], function(require, stjs, AbstractEntitySearchCriterion, SampleSearchRelation, SpaceSearchCriterion, ExperimentSearchCriterion, SearchOperator) {

	var SampleSearchCriterion = function() {
		AbstractEntitySearchCriterion.call(this);
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
			return this.addCriterion(new SampleParentsSearchCriterion());
		};
		prototype.withChildren = function() {
			return this.addCriterion(new SampleChildrenSearchCriterion());
		};
		prototype.withContainer = function() {
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

	var SampleParentsSearchCriterion = function() {
		SampleSearchCriterion.call(this, SampleSearchRelation.PARENTS);
	};
	stjs.extend(SampleParentsSearchCriterion, SampleSearchCriterion, [ SampleSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleParentsSearchCriterion';
		constructor.serialVersionUID = 1;
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

	var SampleChildrenSearchCriterion = function() {
		SampleSearchCriterion.call(this, SampleSearchRelation.CHILDREN);
	};
	stjs.extend(SampleChildrenSearchCriterion, SampleSearchCriterion, [ SampleSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleChildrenSearchCriterion';
		constructor.serialVersionUID = 1;
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

	var SampleContainerSearchCriterion = function() {
		SampleSearchCriterion.call(this, SampleSearchRelation.CONTAINER);
	};
	stjs.extend(SampleContainerSearchCriterion, SampleSearchCriterion, [ SampleSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleContainerSearchCriterion';
		constructor.serialVersionUID = 1;
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