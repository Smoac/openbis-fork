/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractEntitySearchCriteria", "dto/search/SearchOperator" ], function(stjs, AbstractEntitySearchCriteria, SearchOperator) {
	var MaterialSearchCriteria = function() {
		AbstractEntitySearchCriteria.call(this);
	};
	stjs.extend(MaterialSearchCriteria, AbstractEntitySearchCriteria, [ AbstractEntitySearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.MaterialSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
		prototype.createBuilder = function() {
			var builder = AbstractEntitySearchCriteria.prototype.createBuilder.call(this);
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
			arguments : [ "ISearchCriteria" ]
		}
	});
	return MaterialSearchCriteria;
})