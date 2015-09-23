/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractSearchCriterion", "dto/search/SearchOperator" ], function(stjs, AbstractSearchCriterion, SearchOperator) {
	var AbstractCompositeSearchCriterion = function() {
		AbstractSearchCriterion.call(this);
		this.criteria = [];
	};
	stjs.extend(AbstractCompositeSearchCriterion, AbstractSearchCriterion, [ AbstractSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractCompositeSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.operator = SearchOperator.AND;
		prototype.getCriteria = function() {
			return this.criteria;
		};
		prototype.setCriteria = function(criteria) {
			this.criteria = criteria;
		};
		prototype.addCriterion = function(criterion) {
			this.criteria.push(criterion);
			return criterion;
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
			var builder = new SearchCriterionToStringBuilder();
			builder.setCriteria(this.criteria);
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			operator : "SearchOperator",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return AbstractCompositeSearchCriterion;
})