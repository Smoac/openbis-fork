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
		
		var checkDateFieldSearchCriterionMethod = function(criterion, methodName, basicClassName, 
				methodDescription, name, type) {
			criterion[methodName]("2105-02-25 21:23:24");
			assertAttributes(criterion, name, type, "Date" + basicClassName, "2105-02-25 21:23:24");
			criterion[methodName]("2105-02-25");
			assertAttributes(criterion, name, type, "Date" + basicClassName, "2105-02-25");
			var d = new Date("2105-02-25 17:18:19");
			criterion[methodName](d);
			assertAttributes(criterion, name, type, "DateObject" + basicClassName, d);
			try {
				criterion[methodName]("abc");
				fail("ex");
			} catch (e) {
				equal(e.message, "Date value: " + methodDescription + " to 'abc' does not match any of "
						+ "the supported formats: YYYY-MM-DD,YYYY-MM-DD HH:mm,YYYY-MM-DD HH:mm:ss", "Error message");
			}
		}
		
		var checkDateFieldSearchCriterion = function(criterionClassName, name, type) {
			require(['dto/search/' + criterionClassName], function(Criterion) {
				var criterion = new Criterion(name, type);
				checkDateFieldSearchCriterionMethod(criterion, "thatEquals", "EqualToValue", "equal", name, type);
				checkDateFieldSearchCriterionMethod(criterion, "thatIsLaterThanOrEqualTo", "LaterThanOrEqualToValue", 
						"later than or equal", name, type);
				checkDateFieldSearchCriterionMethod(criterion, "thatIsEarlierThanOrEqualTo", "EarlierThanOrEqualToValue", 
						"earlier than or equal", name, type);
				start();
			});
		}
		
		asyncTest("DateFieldSearchCriterion", function() {
			checkDateFieldSearchCriterion("DateFieldSearchCriterion", "MY_FIELD", "MY_TYPE");
		});
		
		asyncTest("DatePropertySearchCriterion", function() {
			checkDateFieldSearchCriterion("DatePropertySearchCriterion", "MY_PROPERTY", "PROPERTY");
		});
		
		asyncTest("ModificationDateSearchCriterion", function() {
			checkDateFieldSearchCriterion("ModificationDateSearchCriterion", "modification_date", "ATTRIBUTE");
		});
		
		asyncTest("RegistrationDateSearchCriterion", function() {
			checkDateFieldSearchCriterion("RegistrationDateSearchCriterion", "registration_date", "ATTRIBUTE");
		});
	}
});
