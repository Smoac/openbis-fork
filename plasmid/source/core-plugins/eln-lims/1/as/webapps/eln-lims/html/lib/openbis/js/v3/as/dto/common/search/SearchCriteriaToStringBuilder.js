/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria" ], function(stjs, AbstractCompositeSearchCriteria) {
	var SearchCriteriaToStringBuilder = function() {
	};
	stjs.extend(SearchCriteriaToStringBuilder, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.SearchCriteriaToStringBuilder';
		prototype.name = null;
		prototype.operator = null;
		prototype.criteria = null;
		prototype.setName = function(name) {
			this.name = name;
			return this;
		};
		prototype.setOperator = function(operator) {
			this.operator = operator;
			return this;
		};
		prototype.setCriteria = function(criteria) {
			this.criteria = criteria;
			return this;
		};
		prototype.toString = function(anIndentation) {
			// TODO rewrite to JS version
			var sb = new StringBuilder();
			var indentation = anIndentation;
			if (indentation.isEmpty()) {
				sb.append(this.name.toUpperCase() + "\n");
			} else {
				sb.append(indentation + "with " + this.name.toLowerCase() + ":\n");
			}
			indentation += "    ";
			if (this.operator != null) {
				sb.append(indentation + "with operator '" + this.operator + "'\n");
			}
			for ( var criterion in this.criteria) {
				if (stjs.isInstanceOf(criterion.constructor, AbstractCompositeSearchCriteria)) {
					var compositeCriteria = criterion;
					sb.append(compositeCriteria.toString(indentation));
				} else {
					sb.append(indentation + criterion.toString() + "\n");
				}
			}
			return sb.toString();
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
	return SearchCriteriaToStringBuilder;
})