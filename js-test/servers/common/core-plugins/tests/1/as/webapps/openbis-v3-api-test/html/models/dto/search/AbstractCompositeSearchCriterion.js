/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractSearchCriterion" ], function(stjs, AbstractSearchCriterion) {
	var AbstractCompositeSearchCriterion = function() {
		AbstractSearchCriterion.call(this);
	};
	stjs.extend(AbstractCompositeSearchCriterion, AbstractSearchCriterion, [ AbstractSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractCompositeSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.criteria = [];
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
			arguments : [ "ISearchCriterion" ]
		}
	});
	return AbstractCompositeSearchCriterion;
})