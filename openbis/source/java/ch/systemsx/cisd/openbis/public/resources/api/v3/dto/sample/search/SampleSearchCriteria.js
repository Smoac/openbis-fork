/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/common/search/AbstractEntitySearchCriteria", "dto/common/search/SearchOperator", "dto/sample/search/SampleSearchRelation", "dto/space/search/SpaceSearchCriteria",
		"dto/project/search/ProjectSearchCriteria", "dto/project/search/NoProjectSearchCriteria", "dto/experiment/search/ExperimentSearchCriteria", "dto/experiment/search/NoExperimentSearchCriteria",
		"dto/sample/search/NoSampleContainerSearchCriteria" ], function(require, stjs, AbstractEntitySearchCriteria, SearchOperator, SampleSearchRelation) {

	var SampleSearchCriteria = function(relation) {
		AbstractEntitySearchCriteria.call(this);
		this.relation = relation ? relation : SampleSearchRelation.SAMPLE;
	};
	stjs.extend(SampleSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.search.SampleSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.relation = null;
		prototype.withSpace = function() {
			var SpaceSearchCriteria = require("dto/space/search/SpaceSearchCriteria");
			return this.addCriteria(new SpaceSearchCriteria());
		};
		prototype.withProject = function() {
			var ProjectSearchCriteria = require("dto/project/search/ProjectSearchCriteria");
			return this.addCriteria(new ProjectSearchCriteria());
		};
		prototype.withoutProject = function() {
			var NoProjectSearchCriteria = require("dto/project/search/NoProjectSearchCriteria");
			return this.addCriteria(new NoProjectSearchCriteria());
		};
		prototype.withExperiment = function() {
			var ExperimentSearchCriteria = require("dto/experiment/search/ExperimentSearchCriteria");
			return this.addCriteria(new ExperimentSearchCriteria());
		};
		prototype.withoutExperiment = function() {
			var NoExperimentSearchCriteria = require("dto/experiment/search/NoExperimentSearchCriteria");
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
			var NoSampleContainerSearchCriteria = require("dto/sample/search/NoSampleContainerSearchCriteria");
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
		prototype['@type'] = 'dto.sample.search.SampleParentsSearchCriteria';
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
		prototype['@type'] = 'dto.sample.search.SampleChildrenSearchCriteria';
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
		prototype['@type'] = 'dto.sample.search.SampleContainerSearchCriteria';
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