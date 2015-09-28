/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractSearchCriteria", "dto/search/SearchOperator" ], function(stjs, AbstractSearchCriteria, SearchOperator) {
	var AbstractCompositeSearchCriteria = function() {
		AbstractSearchCriteria.call(this);
		this.criteria = [];
	};
	stjs.extend(AbstractCompositeSearchCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractCompositeSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.operator = SearchOperator.AND;
		prototype.getCriteria = function() {
			return this.criteria;
		};
		prototype.setCriteria = function(criteria) {
			this.criteria = criteria;
		};
		prototype.addCriteria = function(criteria) {
			this.criteria.push(criteria);
			return criteria;
		};
		prototype.withOrOperator = function() {
			this.operator = SearchOperator.OR;
		}
		prototype.withAndOperator = function() {
			this.operator = SearchOperator.AND;
		}
		prototype.toString = function() {
			return this.toString("");
		};
		prototype.toString = function(indentation) {
			return this.createBuilder().toString(indentation);
		};
		prototype.createBuilder = function() {
			var builder = new SearchCriteriaToStringBuilder();
			builder.setCriteria(this.criteria);
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			operator : "SearchOperator",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return AbstractCompositeSearchCriteria;
})