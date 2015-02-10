/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractEntitySearchCriterion", "dto/search/DataSetSearchRelation", "dto/search/ExperimentSearchCriterion", "dto/search/SampleSearchCriterion",
		"dto/search/SearchOperator" ], function(stjs, AbstractEntitySearchCriterion, DataSetSearchRelation, ExperimentSearchCriterion, SampleSearchCriterion, SearchOperator) {
	var DataSetSearchCriterion = function(relation) {
		AbstractEntitySearchCriterion.call(this);
		this.relation = relation ? relation : DataSetSearchRelation.DATASET;
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

	var DataSetParentsSearchCriterion = function() {
		DataSetSearchCriterion.call(this, DataSetSearchRelation.PARENTS);
	};
	stjs.extend(DataSetParentsSearchCriterion, DataSetSearchCriterion, [ DataSetSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetParentsSearchCriterion';
		constructor.serialVersionUID = 1;
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

	var DataSetChildrenSearchCriterion = function() {
		DataSetSearchCriterion.call(this, DataSetSearchRelation.CHILDREN);
	};
	stjs.extend(DataSetChildrenSearchCriterion, DataSetSearchCriterion, [ DataSetSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetChildrenSearchCriterion';
		constructor.serialVersionUID = 1;
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

	var DataSetContainerSearchCriterion = function() {
		DataSetSearchCriterion.call(this, DataSetSearchRelation.CONTAINER);
	};
	stjs.extend(DataSetContainerSearchCriterion, DataSetSearchCriterion, [ DataSetSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetContainerSearchCriterion';
		constructor.serialVersionUID = 1;
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