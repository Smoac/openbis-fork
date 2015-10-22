/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractObjectSearchCriteria", "dto/search/SearchOperator", "dto/search/CodeSearchCriteria", "dto/search/EntityTypeSearchCriteria",
		"dto/search/PermIdSearchCriteria", "dto/search/RegistrationDateSearchCriteria", "dto/search/ModificationDateSearchCriteria", "dto/search/TagSearchCriteria",
		"dto/search/StringPropertySearchCriteria", "dto/search/DatePropertySearchCriteria", "dto/search/AnyPropertySearchCriteria", "dto/search/AnyFieldSearchCriteria",
		"dto/search/AbstractCompositeSearchCriteria", "dto/search/NumberPropertySearchCriteria" ], function(stjs, AbstractObjectSearchCriteria, SearchOperator, CodeSearchCriteria, EntityTypeSearchCriteria, PermIdSearchCriteria,
		RegistrationDateSearchCriteria, ModificationDateSearchCriteria, TagSearchCriteria, StringPropertySearchCriteria, DatePropertySearchCriteria, AnyPropertySearchCriteria,
		AnyFieldSearchCriteria, AbstractCompositeSearchCriteria, NumberPropertySearchCriteria) {
	var AbstractEntitySearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(AbstractEntitySearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractEntitySearchCriteria';
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
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withType = function() {
			return this.addCriteria(new EntityTypeSearchCriteria());
		};
		prototype.withPermId = function() {
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withRegistrationDate = function() {
			return this.addCriteria(new RegistrationDateSearchCriteria());
		};
		prototype.withModificationDate = function() {
			return this.addCriteria(new ModificationDateSearchCriteria());
		};
		prototype.withNumberProperty = function(propertyName) {
			return this.addCriteria(new NumberPropertySearchCriteria(propertyName));
		};
		prototype.withTag = function() {
			return this.addCriteria(new TagSearchCriteria());
		};
		prototype.withProperty = function(propertyName) {
			return this.addCriteria(new StringPropertySearchCriteria(propertyName));
		};
		prototype.withDateProperty = function(propertyName) {
			return this.addCriteria(new DatePropertySearchCriteria(propertyName));
		};
		prototype.withAnyProperty = function() {
			return this.addCriteria(new AnyPropertySearchCriteria());
		};
		prototype.withAnyField = function() {
			return this.addCriteria(new AnyFieldSearchCriteria());
		};
		prototype.createBuilder = function() {
			var builder = AbstractCompositeSearchCriteria.prototype.createBuilder.call(this);
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
			arguments : [ "ISearchCriteria" ]
		}
	});
	return AbstractEntitySearchCriteria;
})