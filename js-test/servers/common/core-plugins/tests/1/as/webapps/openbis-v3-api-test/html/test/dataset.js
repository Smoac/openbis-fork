define([ 'jquery', 'openbis', 'test/common' ], function($, openbis, common) {
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

	}
});
