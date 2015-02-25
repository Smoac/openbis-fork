/**
 * Tests for loading, setting, and getting SearchCriterion beans.
 */
define(function() {
	return function() {
		QUnit.module("SearchCriterion tests");
		var assertAttributes = function(criterion, expectedName, expextedType, expectedValueType, expectedValue) {
			equal(criterion.getFieldName(), expectedName, "Field  name");
			equal(criterion.getFieldType(), expextedType, "Field  type");
			equal(criterion.getFieldValue()["@type"], "dto.search." + expectedValueType, "Field  value type");
			equal(criterion.getFieldValue().getValue(), expectedValue, "Field  value");
		}
		
		var checkStringFieldSearchCriterion = function(criterionClassName, name, type) {
			require(['dto/search/' + criterionClassName], function(Criterion) {
				var criterion = new Criterion(name, type);
				criterion.thatEquals("ABC");
				assertAttributes(criterion, name, type, "StringEqualToValue", "ABC");
				criterion.thatStartsWith("A");
				assertAttributes(criterion, name, type, "StringStartsWithValue", "A");
				criterion.thatEndsWith("C");
				assertAttributes(criterion, name, type, "StringEndsWithValue", "C");
				criterion.thatContains("B");
				assertAttributes(criterion, name, type, "StringContainsValue", "B");
				start();
			});
		}
		
		asyncTest("AnyFieldSearchCriterion", function() {
			checkStringFieldSearchCriterion("AnyFieldSearchCriterion", "any", "ANY_FIELD");
		});
		
		asyncTest("AnyPropertySearchCriterion", function() {
			checkStringFieldSearchCriterion("AnyPropertySearchCriterion", "any", "ANY_PROPERTY");
		});
		
		asyncTest("CodeSearchCriterion", function() {
			checkStringFieldSearchCriterion("CodeSearchCriterion", "code", "ATTRIBUTE");
		});
		
		asyncTest("PermIdSearchCriterion", function() {
			checkStringFieldSearchCriterion("PermIdSearchCriterion", "perm id", "ATTRIBUTE");
		});
		
		asyncTest("StringPropertySearchCriterion", function() {
			checkStringFieldSearchCriterion("StringPropertySearchCriterion", "MY-PROPERTY", "PROPERTY");
		});
		
		asyncTest("StringFieldSearchCriterion", function() {
			checkStringFieldSearchCriterion("StringFieldSearchCriterion", "MY-FIELD", "MY-TYPE");
		});
		
		var checkNumberFieldSearchCriterion = function(criterionClassName, name, type) {
			require(['dto/search/' + criterionClassName], function(Criterion) {
				var criterion = new Criterion(name, type);
				criterion.equalTo(42);
				assertAttributes(criterion, name, type, "NumberEqualToValue", 42);
				start();
			});
		}
		
		asyncTest("NumberFieldSearchCriterion", function() {
			checkNumberFieldSearchCriterion("NumberFieldSearchCriterion", "MY-FIELD", "MY-TYPE");
		});
		
		asyncTest("NumberPropertySearchCriterion", function() {
			checkNumberFieldSearchCriterion("NumberPropertySearchCriterion", "MY-FIELD", "PROPERTY");
		});
		
		var checkDateFieldSearchCriterion = function(criterionClassName, name, type) {
			require(['dto/search/' + criterionClassName], function(Criterion) {
				var criterion = new Criterion(name, type);
				criterion.thatEquals("2105-02-25 21:23:24");
				assertAttributes(criterion, name, type, "DateObjectEqualToValue", "ABC");
				start();
			});
		}
		
//		asyncTest("DateFieldSearchCriterion", function() {
//			checkDateFieldSearchCriterion("DateFieldSearchCriterion", "MY_FIELD", "MY_TYPE");
//		});
	}
});
