/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractEntitySearchCriterion", "dto/search/SearchOperator" ], function(stjs, AbstractEntitySearchCriterion, SearchOperator) {
	var MaterialSearchCriterion = function() {
		AbstractEntitySearchCriterion.call(this);
	};
	stjs.extend(MaterialSearchCriterion, AbstractEntitySearchCriterion, [ AbstractEntitySearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.MaterialSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
		prototype.createBuilder = function() {
			var builder = AbstractEntitySearchCriterion.prototype.createBuilder.call(this);
			builder.setName("MATERIAL");
			return builder;
		};
	}, {
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return MaterialSearchCriterion;
})