/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractEntitySearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/sample/search/SampleSearchRelation", "as/dto/space/search/SpaceSearchCriteria",
		"as/dto/experiment/search/ExperimentSearchCriteria", "as/dto/experiment/search/NoExperimentSearchCriteria",
		"as/dto/sample/search/NoSampleContainerSearchCriteria", "as/dto/sample/search/SampleTypeSearchCriteria" ], 
		function(require, stjs, AbstractEntitySearchCriteria, SearchOperator, SampleSearchRelation) {

	var SampleSearchCriteria = function(relation) {
		AbstractEntitySearchCriteria.call(this);
		this.relation = relation ? relation : SampleSearchRelation.SAMPLE;
	};
	stjs.extend(SampleSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SampleSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.relation = null;
		prototype.withSpace = function() {
			var SpaceSearchCriteria = require("as/dto/space/search/SpaceSearchCriteria");
			return this.addCriteria(new SpaceSearchCriteria());
		};
		prototype.withoutSpace = function() {
			var NoSpaceSearchCriteria = require("as/dto/space/search/NoSpaceSearchCriteria");
			return this.addCriteria(new NoSpaceSearchCriteria());
		};
		prototype.withExperiment = function() {
			var ExperimentSearchCriteria = require("as/dto/experiment/search/ExperimentSearchCriteria");
			return this.addCriteria(new ExperimentSearchCriteria());
		};
		prototype.withoutExperiment = function() {
			var NoExperimentSearchCriteria = require("as/dto/experiment/search/NoExperimentSearchCriteria");
			this.addCriteria(new NoExperimentSearchCriteria());
			return this;
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
			var NoSampleContainerSearchCriteria = require("as/dto/sample/search/NoSampleContainerSearchCriteria");
			this.addCriteria(new NoSampleContainerSearchCriteria());
			return this;
		};
		prototype.withType = function() {
			var SampleTypeSearchCriteria = require("as/dto/sample/search/SampleTypeSearchCriteria");
			return this.addCriteria(new SampleTypeSearchCriteria());
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
		prototype['@type'] = 'as.dto.sample.search.SampleParentsSearchCriteria';
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
		prototype['@type'] = 'as.dto.sample.search.SampleChildrenSearchCriteria';
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
		prototype['@type'] = 'as.dto.sample.search.SampleContainerSearchCriteria';
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