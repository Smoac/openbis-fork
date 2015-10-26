/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/search/AbstractObjectSearchCriteria", "dto/search/SearchOperator",
         "dto/search/CodeSearchCriteria", "dto/search/EntityTypeSearchCriteria", "dto/search/PermIdSearchCriteria",
         "dto/search/RegistrationDateSearchCriteria", "dto/search/ModificationDateSearchCriteria",
         "dto/search/NumberPropertySearchCriteria", "dto/search/TagSearchCriteria", "dto/search/StringPropertySearchCriteria",
         "dto/search/DatePropertySearchCriteria", "dto/search/AnyPropertySearchCriteria", "dto/search/AnyFieldSearchCriteria",
         "dto/search/AbstractCompositeSearchCriteria"],
		function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {
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
			var CodeSearchCriteria = require("dto/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withType = function() {
			var EntityTypeSearchCriteria = require("dto/search/EntityTypeSearchCriteria");
			return this.addCriteria(new EntityTypeSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("dto/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withRegistrationDate = function() {
			var RegistrationDateSearchCriteria = require("dto/search/RegistrationDateSearchCriteria");
			return this.addCriteria(new RegistrationDateSearchCriteria());
		};
		prototype.withModificationDate = function() {
			var ModificationDateSearchCriteria = require("dto/search/ModificationDateSearchCriteria");
			return this.addCriteria(new ModificationDateSearchCriteria());
		};
		prototype.withNumberProperty = function(propertyName) {
			var NumberPropertySearchCriteria = require("dto/search/NumberPropertySearchCriteria");
			return this.addCriteria(new NumberPropertySearchCriteria(propertyName));
		};
		prototype.withTag = function() {
			var TagSearchCriteria = require("dto/search/TagSearchCriteria");
			return this.addCriteria(new TagSearchCriteria());
		};
		prototype.withProperty = function(propertyName) {
			var StringPropertySearchCriteria = require("dto/search/StringPropertySearchCriteria");
			return this.addCriteria(new StringPropertySearchCriteria(propertyName));
		};
		prototype.withDateProperty = function(propertyName) {
			var DatePropertySearchCriteria = require("dto/search/DatePropertySearchCriteria");
			return this.addCriteria(new DatePropertySearchCriteria(propertyName));
		};
		prototype.withAnyProperty = function() {
			var AnyPropertySearchCriteria = require("dto/search/AnyPropertySearchCriteria");
			return this.addCriteria(new AnyPropertySearchCriteria());
		};
		prototype.withAnyField = function() {
			var AnyFieldSearchCriteria = require("dto/search/AnyFieldSearchCriteria");
			return this.addCriteria(new AnyFieldSearchCriteria());
		};
		prototype.createBuilder = function() {
			var AbstractCompositeSearchCriteria = require("dto/search/AbstractCompositeSearchCriteria");
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