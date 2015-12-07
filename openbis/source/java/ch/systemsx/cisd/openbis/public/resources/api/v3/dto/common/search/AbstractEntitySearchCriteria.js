/**
 * @author pkupczyk
 */
define(
		[ "require", "stjs", "dto/common/search/AbstractObjectSearchCriteria", "dto/common/search/SearchOperator", "dto/common/search/CodeSearchCriteria",
				"dto/entitytype/search/EntityTypeSearchCriteria", "dto/common/search/PermIdSearchCriteria", "dto/common/search/RegistrationDateSearchCriteria",
				"dto/common/search/ModificationDateSearchCriteria", "dto/common/search/NumberPropertySearchCriteria", "dto/tag/search/TagSearchCriteria",
				"dto/common/search/StringPropertySearchCriteria", "dto/common/search/DatePropertySearchCriteria", "dto/common/search/AnyPropertySearchCriteria",
				"dto/common/search/AnyFieldSearchCriteria", "dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {
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
					var CodeSearchCriteria = require("dto/common/search/CodeSearchCriteria");
					return this.addCriteria(new CodeSearchCriteria());
				};
				prototype.withType = function() {
					var EntityTypeSearchCriteria = require("dto/entitytype/search/EntityTypeSearchCriteria");
					return this.addCriteria(new EntityTypeSearchCriteria());
				};
				prototype.withPermId = function() {
					var PermIdSearchCriteria = require("dto/common/search/PermIdSearchCriteria");
					return this.addCriteria(new PermIdSearchCriteria());
				};
				prototype.withRegistrationDate = function() {
					var RegistrationDateSearchCriteria = require("dto/common/search/RegistrationDateSearchCriteria");
					return this.addCriteria(new RegistrationDateSearchCriteria());
				};
				prototype.withModificationDate = function() {
					var ModificationDateSearchCriteria = require("dto/common/search/ModificationDateSearchCriteria");
					return this.addCriteria(new ModificationDateSearchCriteria());
				};
				prototype.withNumberProperty = function(propertyName) {
					var NumberPropertySearchCriteria = require("dto/common/search/NumberPropertySearchCriteria");
					return this.addCriteria(new NumberPropertySearchCriteria(propertyName));
				};
				prototype.withTag = function() {
					var TagSearchCriteria = require("dto/tag/search/TagSearchCriteria");
					return this.addCriteria(new TagSearchCriteria());
				};
				prototype.withProperty = function(propertyName) {
					var StringPropertySearchCriteria = require("dto/common/search/StringPropertySearchCriteria");
					return this.addCriteria(new StringPropertySearchCriteria(propertyName));
				};
				prototype.withDateProperty = function(propertyName) {
					var DatePropertySearchCriteria = require("dto/common/search/DatePropertySearchCriteria");
					return this.addCriteria(new DatePropertySearchCriteria(propertyName));
				};
				prototype.withAnyProperty = function() {
					var AnyPropertySearchCriteria = require("dto/common/search/AnyPropertySearchCriteria");
					return this.addCriteria(new AnyPropertySearchCriteria());
				};
				prototype.withAnyField = function() {
					var AnyFieldSearchCriteria = require("dto/common/search/AnyFieldSearchCriteria");
					return this.addCriteria(new AnyFieldSearchCriteria());
				};
				prototype.createBuilder = function() {
					var AbstractCompositeSearchCriteria = require("dto/common/search/AbstractCompositeSearchCriteria");
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