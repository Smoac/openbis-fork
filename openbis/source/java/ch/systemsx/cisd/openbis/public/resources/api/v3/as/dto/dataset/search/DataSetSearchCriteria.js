/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractEntitySearchCriteria", "as/dto/common/search/SearchOperator", "as/dto/dataset/search/DataSetSearchRelation", ,
		"as/dto/experiment/search/ExperimentSearchCriteria", "as/dto/experiment/search/NoExperimentSearchCriteria", "as/dto/sample/search/SampleSearchCriteria", 
		"as/dto/sample/search/NoSampleSearchCriteria", "as/dto/dataset/search/DataSetTypeSearchCriteria" ],
		function(require, stjs, AbstractEntitySearchCriteria, SearchOperator, DataSetSearchRelation) {
			var DataSetSearchCriteria = function(relation) {
				AbstractEntitySearchCriteria.call(this);
				this.relation = relation ? relation : DataSetSearchRelation.DATASET;
			};
			stjs.extend(DataSetSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
				prototype['@type'] = 'as.dto.dataset.search.DataSetSearchCriteria';
				constructor.serialVersionUID = 1;
				prototype.relation = null;
				prototype.withType = function() {
					var DataSetTypeSearchCriteria = require("as/dto/dataset/search/DataSetTypeSearchCriteria");
					return this.addCriteria(new DataSetTypeSearchCriteria());
				};
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
					var ExperimentSearchCriteria = require("as/dto/experiment/search/ExperimentSearchCriteria");
					return this.addCriteria(new ExperimentSearchCriteria());
				};
				prototype.withoutExperiment = function() {
					var NoExperimentSearchCriteria = require("as/dto/experiment/search/NoExperimentSearchCriteria");
					this.addCriteria(new NoExperimentSearchCriteria());
					return this;
				};
				prototype.withSample = function() {
					var SampleSearchCriteria = require("as/dto/sample/search/SampleSearchCriteria");
					return this.addCriteria(new SampleSearchCriteria());
				};
				prototype.withoutSample = function() {
					var NoSampleSearchCriteria = require("as/dto/sample/search/NoSampleSearchCriteria");
					this.addCriteria(new NoSampleSearchCriteria());
					return this;
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
				prototype['@type'] = 'as.dto.dataset.search.DataSetParentsSearchCriteria';
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
				prototype['@type'] = 'as.dto.dataset.search.DataSetChildrenSearchCriteria';
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
				prototype['@type'] = 'as.dto.dataset.search.DataSetContainerSearchCriteria';
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