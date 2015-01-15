/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs) {
    var SearchCriterionToStringBuilder = function() {};
    stjs.extend(SearchCriterionToStringBuilder, null, [], function(constructor, prototype) {
        prototype['@type'] = 'SearchCriterionToStringBuilder';
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
            for (var criterion in this.criteria) {
                if (stjs.isInstanceOf(criterion.constructor, AbstractCompositeSearchCriterion)) {
                    var compositeCriterion = criterion;
                    sb.append(compositeCriterion.toString(indentation));
                } else {
                    sb.append(indentation + criterion.toString() + "\n");
                }
            }
            return sb.toString();
        };
    }, {operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return SearchCriterionToStringBuilder;
})