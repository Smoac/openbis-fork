define([ 'jquery', 'openbis', 'test/common', 'dto/entity/dataset/DataSetUpdate', 'dto/id/dataset/DataSetPermId' ], function($, openbis, common, DataSetUpdate, DataSetPermId) {
	return function() {
		QUnit.module("Dataset tests");

		QUnit.test("mapDataSets()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createDataSetPermId("20130415093804724-403"), c.createDataSetFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapDataSets([ permId ], fetchOptions).done(function() {
					facade.logout()
				})
			}).done(function(dataSets) {
				c.assertObjectsCount(Object.keys(dataSets), 1);
				var dataSet = dataSets["20130415093804724-403"];
				c.assertEqual(dataSet.getCode(), "20130415093804724-403", "Code");
				c.assertEqual(dataSet.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(dataSet.getExperiment().getCode(), "TEST-EXPERIMENT-2", "Experiment code");
				c.assertEqual(dataSet.getSample().getCode(), "TEST-SAMPLE-2", "Sample code");
				c.assertEqual(dataSet.getProperties()["DESCRIPTION"], "403 description", "Property DESCRIPTION");

				var externalData = dataSet.getExternalData();
				c.assertEqual(externalData.getShareId(), "1", "Share id");
				c.assertEqual(externalData.getLocation(), "1FD3FF61-1576-4908-AE3D-296E60B4CE06/06/e5/ad/20130415093804724-403", "Location");
				c.assertEqual(externalData.getStatus(), "AVAILABLE", "Status");
				c.assertEqual(externalData.getFileFormatType().getCode(), "PROPRIETARY", "File format type");
				c.assertEqual(externalData.getLocatorType().getCode(), "RELATIVE_LOCATION", "Locator type");

				c.assertObjectsWithValues(dataSet.getParents(), "code", [ "20130415100158230-407" ]);
				c.assertObjectsWithValues(dataSet.getChildren(), "code", [ "20130415100238098-408", "20130415100308111-409" ]);

				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("searchDataSets()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createDataSetSearchCriterion(), c.createDataSetFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("20130415093804724-403");

				return facade.searchDataSets(criterion, fetchOptions).done(function() {
					facade.logout()
				})
			}).done(function(dataSets) {
				c.assertObjectsCount(Object.keys(dataSets), 1);
				var dataSet = dataSets[0];
				c.assertEqual(dataSet.getCode(), "20130415093804724-403", "Code");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("updateDataSets()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			var update = new DataSetUpdate();
			update.setDataSetId(new DataSetPermId("20130415100308111-409"));
			update.setProperty("DESCRIPTION", "new 409 description");

			$.when(c.createFacadeAndLogin(), c.createDataSetFetchOptions()).then(function(facade, fetchOptions) {
				return facade.updateDataSets([ update ]).then(function() {
					return facade.mapDataSets([ update.getDataSetId() ], fetchOptions).done(function() {
						facade.logout()
					});
				})
			}).done(function(dataSets) {
				c.assertObjectsCount(Object.keys(dataSets), 1);
				var dataSet = dataSets[update.getDataSetId().getPermId()];
				c.assertEqual(dataSet.getCode(), update.getDataSetId().getPermId(), "Code");
				c.assertEqual(dataSet.getProperties()["DESCRIPTION"], update.getProperties()["DESCRIPTION"], "Property DESCRIPTION");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

	}
});
