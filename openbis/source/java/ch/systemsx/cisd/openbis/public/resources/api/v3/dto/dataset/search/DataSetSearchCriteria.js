/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/common/search/AbstractEntitySearchCriteria", "dto/common/search/SearchOperator", "dto/dataset/search/DataSetSearchRelation", ,
		"dto/experiment/search/ExperimentSearchCriteria", "dto/experiment/search/NoExperimentSearchCriteria", "dto/sample/search/SampleSearchCriteria", "dto/sample/search/NoSampleSearchCriteria" ],
		function(require, stjs, AbstractEntitySearchCriteria, SearchOperator, DataSetSearchRelation) {
			var DataSetSearchCriteria = function(relation) {
				AbstractEntitySearchCriteria.call(this);
				this.relation = relation ? relation : DataSetSearchRelation.DATASET;
			};
			stjs.extend(DataSetSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
				prototype['@type'] = 'dto.dataset.search.DataSetSearchCriteria';
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
					var ExperimentSearchCriteria = require("dto/experiment/search/ExperimentSearchCriteria");
					return this.addCriteria(new ExperimentSearchCriteria());
				};
				prototype.withoutExperiment = function() {
					var NoExperimentSearchCriteria = require("dto/experiment/search/NoExperimentSearchCriteria");
					return this.addCriteria(new NoExperimentSearchCriteria());
				};
				prototype.withSample = function() {
					var SampleSearchCriteria = require("dto/sample/search/SampleSearchCriteria");
					return this.addCriteria(new SampleSearchCriteria());
				};
				prototype.withoutSample = function() {
					var NoSampleSearchCriteria = require("dto/sample/search/NoSampleSearchCriteria");
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
				prototype['@type'] = 'dto.dataset.search.DataSetParentsSearchCriteria';
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
				prototype['@type'] = 'dto.dataset.search.DataSetChildrenSearchCriteria';
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
				prototype['@type'] = 'dto.dataset.search.DataSetContainerSearchCriteria';
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