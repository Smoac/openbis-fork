/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractEntitySearchCriteria", "dto/search/DataSetSearchRelation", "dto/search/ExperimentSearchCriteria", "dto/search/NoExperimentSearchCriteria",
		"dto/search/SampleSearchCriteria", "dto/search/NoSampleSearchCriteria", "dto/search/SearchOperator" ], function(stjs, AbstractEntitySearchCriteria, DataSetSearchRelation,
		ExperimentSearchCriteria, NoExperimentSearchCriteria, SampleSearchCriteria, NoSampleSearchCriteria, SearchOperator) {
	var DataSetSearchCriteria = function(relation) {
		AbstractEntitySearchCriteria.call(this);
		this.relation = relation ? relation : DataSetSearchRelation.DATASET;
	};
	stjs.extend(DataSetSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.relation = null;
		prototype.withParents = function() {
			return this.addCriteria(new DataSetParentsSearchCriteria());
		};
		prototype.withChildren = function() {
			return this.addCriteria(new DataSetChildrenSearchCriteria());
		};
		prototype.withContainer = function() {
			return this.addCriteria(new DataSetContainerSearchCriteria());
		};
		prototype.withExperiment = function() {
			return this.addCriteria(new ExperimentSearchCriteria());
		};
		prototype.withoutExperiment = function() {
			return this.addCriteria(new NoExperimentSearchCriteria());
		};
		prototype.withSample = function() {
			return this.addCriteria(new SampleSearchCriteria());
		};
		prototype.withoutSample = function() {
			return this.addCriteria(new NoSampleSearchCriteria());
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
			var builder = AbstractEntitySearchCriteria.prototype.createBuilder.call(this);
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
			arguments : [ "ISearchCriteria" ]
		}
	});

	var DataSetParentsSearchCriteria = function() {
		DataSetSearchCriteria.call(this, DataSetSearchRelation.PARENTS);
	};
	stjs.extend(DataSetParentsSearchCriteria, DataSetSearchCriteria, [ DataSetSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetParentsSearchCriteria';
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
			arguments : [ "ISearchCriteria" ]
		}
	});

	var DataSetChildrenSearchCriteria = function() {
		DataSetSearchCriteria.call(this, DataSetSearchRelation.CHILDREN);
	};
	stjs.extend(DataSetChildrenSearchCriteria, DataSetSearchCriteria, [ DataSetSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetChildrenSearchCriteria';
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
			arguments : [ "ISearchCriteria" ]
		}
	});

	var DataSetContainerSearchCriteria = function() {
		DataSetSearchCriteria.call(this, DataSetSearchRelation.CONTAINER);
	};
	stjs.extend(DataSetContainerSearchCriteria, DataSetSearchCriteria, [ DataSetSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetContainerSearchCriteria';
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
			arguments : [ "ISearchCriteria" ]
		}
	});

	return DataSetSearchCriteria;
})