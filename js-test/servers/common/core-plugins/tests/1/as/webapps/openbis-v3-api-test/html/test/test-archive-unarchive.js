define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Archive/Unarchive")

		var testAction = function(c, fAction) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				return fAction(facade).then(function(result) {
					c.ok("Archived/Unarchived");
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("archiveDataSets()", function(assert) {
			var c = new common(assert);

			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.archiveDataSets(ids, new c.DataSetArchiveOptions());
				});
			}

			testAction(c, fAction);
		});

		QUnit.test("unarchiveDataSets()", function(assert) {
			var c = new common(assert);

			var fAction = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					var ids = [ permId1, permId2 ];
					return facade.archiveDataSets(ids, new c.DataSetArchiveOptions()).then(function() {
						return facade.unarchiveDataSets(ids, new c.DataSetUnarchiveOptions());
					});
				});
			}

			testAction(c, fAction);
		});

	}
})