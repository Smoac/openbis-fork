/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractObjectSearchCriterion", "dto/search/SearchOperator", "dto/search/CodeSearchCriterion", "dto/search/EntityTypeSearchCriterion",
		"dto/search/PermIdSearchCriterion", "dto/search/RegistrationDateSearchCriterion", "dto/search/ModificationDateSearchCriterion", "dto/search/TagSearchCriterion",
		"dto/search/StringPropertySearchCriterion", "dto/search/DatePropertySearchCriterion", "dto/search/AnyPropertySearchCriterion", "dto/search/AnyFieldSearchCriterion",
		"dto/search/AbstractCompositeSearchCriterion" ], function(stjs, AbstractObjectSearchCriterion, SearchOperator, CodeSearchCriterion, EntityTypeSearchCriterion, PermIdSearchCriterion,
		RegistrationDateSearchCriterion, ModificationDateSearchCriterion, TagSearchCriterion, StringPropertySearchCriterion, DatePropertySearchCriterion, AnyPropertySearchCriterion,
		AnyFieldSearchCriterion, AbstractCompositeSearchCriterion) {
	var AbstractEntitySearchCriterion = function() {
		AbstractObjectSearchCriterion.call(this);
	};
	stjs.extend(AbstractEntitySearchCriterion, AbstractObjectSearchCriterion, [ AbstractObjectSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractEntitySearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.operator = SearchOperator.AND;
		prototype.withOperator = function(anOperator) {
			this.operator = anOperator;
			return this;
		};
		prototype.getOperator = function() {
			return this.operator;
		};
		prototype.withCode = function() {
			return this.addCriterion(new CodeSearchCriterion());
		};
		prototype.withType = function() {
			return this.addCriterion(new EntityTypeSearchCriterion());
		};
		prototype.withPermId = function() {
			return this.addCriterion(new PermIdSearchCriterion());
		};
		prototype.withRegistrationDate = function() {
			return this.addCriterion(new RegistrationDateSearchCriterion());
		};
		prototype.withModificationDate = function() {
			return this.addCriterion(new ModificationDateSearchCriterion());
		};
		prototype.withTag = function() {
			return this.addCriterion(new TagSearchCriterion());
		};
		prototype.withProperty = function(propertyName) {
			return this.addCriterion(new StringPropertySearchCriterion(propertyName));
		};
		prototype.withDateProperty = function(propertyName) {
			return this.addCriterion(new DatePropertySearchCriterion(propertyName));
		};
		prototype.withAnyProperty = function() {
			return this.addCriterion(new AnyPropertySearchCriterion());
		};
		prototype.withAnyField = function() {
			return this.addCriterion(new AnyFieldSearchCriterion());
		};
		prototype.createBuilder = function() {
			var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
			builder.setOperator(this.operator);
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
	return AbstractEntitySearchCriterion;
})