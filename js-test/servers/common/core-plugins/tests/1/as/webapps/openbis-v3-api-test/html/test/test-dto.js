/**
 * Tests for loading, setting, and getting SearchCriterion beans.
 */
define([ 'test/common' ], function(common) {
	return function() {
		QUnit.module("DTO tests");

		var assertAttributes = function(assert, criterion, expectedName, expextedType, expectedValueType, expectedValue) {
			var c = new common(assert);
			c.assertEqual(criterion.getFieldName(), expectedName, "Field  name");
			c.assertEqual(criterion.getFieldType(), expextedType, "Field  type");
			c.assertEqual(criterion.getFieldValue()["@type"], "dto.search." + expectedValueType, "Field  value type");
			c.assertEqual(criterion.getFieldValue().getValue(), expectedValue, "Field  value");
		}

		var checkStringFieldSearchCriterion = function(assert, criterionClassName, name, type) {
			var done = assert.async();

			require([ 'dto/search/' + criterionClassName ], function(Criterion) {
				var criterion = new Criterion(name, type);
				criterion.thatEquals("ABC");
				assertAttributes(assert, criterion, name, type, "StringEqualToValue", "ABC");
				criterion.thatStartsWith("A");
				assertAttributes(assert, criterion, name, type, "StringStartsWithValue", "A");
				criterion.thatEndsWith("C");
				assertAttributes(assert, criterion, name, type, "StringEndsWithValue", "C");
				criterion.thatContains("B");
				assertAttributes(assert, criterion, name, type, "StringContainsValue", "B");
				done();
			});
		}

		QUnit.test("AnyFieldSearchCriterion", function(assert) {
			checkStringFieldSearchCriterion(assert, "AnyFieldSearchCriterion", "any", "ANY_FIELD");
		});

		QUnit.test("AnyPropertySearchCriterion", function(assert) {
			checkStringFieldSearchCriterion(assert, "AnyPropertySearchCriterion", "any", "ANY_PROPERTY");
		});

		QUnit.test("CodeSearchCriterion", function(assert) {
			checkStringFieldSearchCriterion(assert, "CodeSearchCriterion", "code", "ATTRIBUTE");
		});

		QUnit.test("PermIdSearchCriterion", function(assert) {
			checkStringFieldSearchCriterion(assert, "PermIdSearchCriterion", "perm id", "ATTRIBUTE");
		});

		QUnit.test("StringPropertySearchCriterion", function(assert) {
			checkStringFieldSearchCriterion(assert, "StringPropertySearchCriterion", "MY-PROPERTY", "PROPERTY");
		});

		QUnit.test("StringFieldSearchCriterion", function(assert) {
			checkStringFieldSearchCriterion(assert, "StringFieldSearchCriterion", "MY-FIELD", "MY-TYPE");
		});

		var checkNumberFieldSearchCriterion = function(assert, criterionClassName, name, type) {
			var done = assert.async();

			require([ 'dto/search/' + criterionClassName ], function(Criterion) {
				var criterion = new Criterion(name, type);
				criterion.equalTo(42);
				assertAttributes(assert, criterion, name, type, "NumberEqualToValue", 42);
				done();
			});
		}

		QUnit.test("NumberFieldSearchCriterion", function(assert) {
			checkNumberFieldSearchCriterion(assert, "NumberFieldSearchCriterion", "MY-FIELD", "MY-TYPE");
		});

		QUnit.test("NumberPropertySearchCriterion", function(assert) {
			checkNumberFieldSearchCriterion(assert, "NumberPropertySearchCriterion", "MY-FIELD", "PROPERTY");
		});

		var checkDateFieldSearchCriterionMethod = function(assert, criterion, methodName, basicClassName, methodDescription, name, type) {
			var c = new common(assert);
			criterion[methodName]("2105-02-25 21:23:24");
			assertAttributes(assert, criterion, name, type, "Date" + basicClassName, "2105-02-25 21:23:24");
			criterion[methodName]("2105-02-25");
			assertAttributes(assert, criterion, name, type, "Date" + basicClassName, "2105-02-25");
			var d = new Date("2105-02-25 17:18:19");
			criterion[methodName](d);
			assertAttributes(assert, criterion, name, type, "DateObject" + basicClassName, d);
			try {
				criterion[methodName]("abc");
				c.fail("ex");
			} catch (e) {
				c.assertEqual(e.message, "Date value: " + methodDescription + " to 'abc' does not match any of " + "the supported formats: YYYY-MM-DD,YYYY-MM-DD HH:mm,YYYY-MM-DD HH:mm:ss",
						"Error message");
			}
		}

		var checkDateFieldSearchCriterion = function(assert, criterionClassName, name, type) {
			var done = assert.async();

			require([ 'dto/search/' + criterionClassName ], function(Criterion) {
				var criterion = new Criterion(name, type);
				checkDateFieldSearchCriterionMethod(assert, criterion, "thatEquals", "EqualToValue", "equal", name, type);
				checkDateFieldSearchCriterionMethod(assert, criterion, "thatIsLaterThanOrEqualTo", "LaterThanOrEqualToValue", "later than or equal", name, type);
				checkDateFieldSearchCriterionMethod(assert, criterion, "thatIsEarlierThanOrEqualTo", "EarlierThanOrEqualToValue", "earlier than or equal", name, type);
				done();
			});
		}

		QUnit.test("DateFieldSearchCriterion", function(assert) {
			checkDateFieldSearchCriterion(assert, "DateFieldSearchCriterion", "MY_FIELD", "MY_TYPE");
		});

		QUnit.test("DatePropertySearchCriterion", function(assert) {
			checkDateFieldSearchCriterion(assert, "DatePropertySearchCriterion", "MY_PROPERTY", "PROPERTY");
		});

		QUnit.test("ModificationDateSearchCriterion", function(assert) {
			checkDateFieldSearchCriterion(assert, "ModificationDateSearchCriterion", "modification_date", "ATTRIBUTE");
		});

		QUnit.test("RegistrationDateSearchCriterion", function(assert) {
			checkDateFieldSearchCriterion(assert, "RegistrationDateSearchCriterion", "registration_date", "ATTRIBUTE");
		});
	}
});
