/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/search/AbstractEntitySearchCriteria", "dto/search/SampleSearchRelation", "dto/search/SpaceSearchCriteria", 
		"dto/search/ProjectSearchCriteria", "dto/search/NoProjectSearchCriteria", "dto/search/ExperimentSearchCriteria",
		"dto/search/NoExperimentSearchCriteria", "dto/search/NoSampleContainerSearchCriteria", "dto/search/SearchOperator" ], function(require, stjs, AbstractEntitySearchCriteria,
		SampleSearchRelation, SpaceSearchCriteria, ExperimentSearchCriteria, NoExperimentSearchCriteria, NoSampleContainerSearchCriteria, SearchOperator) {

	var SampleSearchCriteria = function(relation) {
		AbstractEntitySearchCriteria.call(this);
		this.relation = relation ? relation : SampleSearchRelation.SAMPLE;
	};
	stjs.extend(SampleSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.relation = null;
		prototype.withSpace = function() {
			return this.addCriteria(new SpaceSearchCriteria());
		};
		prototype.withProject = function() {
			return this.addCriteria(new ProjectSearchCriteria());
		};
		prototype.withoutProject = function() {
			return this.addCriteria(new NoProjectSearchCriteria());
		};
		prototype.withExperiment = function() {
			return this.addCriteria(new ExperimentSearchCriteria());
		};
		prototype.withoutExperiment = function() {
			return this.addCriteria(new NoExperimentSearchCriteria());
		};
		prototype.withParents = function() {
			return this.addCriteria(new SampleParentsSearchCriteria());
		};
		prototype.withChildren = function() {
			return this.addCriteria(new SampleChildrenSearchCriteria());
		};
		prototype.withContainer = function() {
			return this.addCriteria(new SampleContainerSearchCriteria());
		};
		prototype.withoutContainer = function() {
			return this.addCriteria(new NoSampleContainerSearchCriteria());
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
			arguments : [ "SampleSearchRelation" ]
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

	var SampleParentsSearchCriteria = function() {
		SampleSearchCriteria.call(this, SampleSearchRelation.PARENTS);
	};
	stjs.extend(SampleParentsSearchCriteria, SampleSearchCriteria, [ SampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleParentsSearchCriteria';
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
			arguments : [ "ISearchCriteria" ]
		}
	});

	var SampleChildrenSearchCriteria = function() {
		SampleSearchCriteria.call(this, SampleSearchRelation.CHILDREN);
	};
	stjs.extend(SampleChildrenSearchCriteria, SampleSearchCriteria, [ SampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleChildrenSearchCriteria';
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
			arguments : [ "ISearchCriteria" ]
		}
	});

	var SampleContainerSearchCriteria = function() {
		SampleSearchCriteria.call(this, SampleSearchRelation.CONTAINER);
	};
	stjs.extend(SampleContainerSearchCriteria, SampleSearchCriteria, [ SampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleContainerSearchCriteria';
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
			arguments : [ "ISearchCriteria" ]
		}
	});

	return SampleSearchCriteria;
})