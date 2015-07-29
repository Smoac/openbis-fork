define([ 'jquery', 'openbis', 'test/common' ], function($, openbis, common) {
	return function() {
		QUnit.module("Dataset tests");

		QUnit.test("mapDataSets()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.mapDataSets([ new c.DataSetPermId("20130415093804724-403") ], c.createDataSetFetchOptions()).done(function() {
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

				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("searchDataSets()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				var criterion = new c.DataSetSearchCriterion();
				criterion.withCode().thatEquals("20130415093804724-403");

				return facade.searchDataSets(criterion, c.createDataSetFetchOptions()).done(function() {
					facade.logout()
				})
			}).done(function(dataSets) {
				c.assertObjectsCount(Object.keys(dataSets), 1);
				var dataSet = dataSets[0];
				c.assertEqual(dataSet.getCode(), "20130415093804724-403", "Code");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("updateDataSets()", function(assert) {
			var c = new common(assert);
			c.start();

			var externalUpdate = new c.ExternalDataUpdate();
			externalUpdate.setFileFormatTypeId(new c.FileFormatTypePermId("TIFF"));

			var update = new c.DataSetUpdate();
			update.setDataSetId(new c.DataSetPermId("20130415100308111-409"));
			update.setProperty("DESCRIPTION", "new 409 description");
			update.setExternalData(externalUpdate);

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.updateDataSets([ update ]).then(function() {
					return facade.mapDataSets([ update.getDataSetId() ], c.createDataSetFetchOptions()).done(function() {
						facade.logout()
					});
				})
			}).done(function(dataSets) {
				c.assertObjectsCount(Object.keys(dataSets), 1);
				var dataSet = dataSets[update.getDataSetId().getPermId()];
				c.assertEqual(dataSet.getCode(), "20130415100308111-409", "Code");
				c.assertEqual(dataSet.getProperties()["DESCRIPTION"], "new 409 description", "Property DESCRIPTION");
				c.assertEqual(dataSet.getExternalData().getFileFormatType().getCode(), "TIFF", "File format type");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

	}
});
