define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, c) {
	return function() {
		QUnit.module("Material tests");

		asyncTest("mapMaterials()", function() {
			$.when(c.createFacadeAndLogin(), c.createMaterialPermId("H2O", "COMPOUND"), c.createMaterialFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapMaterials([ permId ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(materials) {
				assertObjectsCount(Object.keys(materials), 1);

				var material = materials["H2O (COMPOUND)"];
				equal(material.getCode(), "H2O", "Code");
				equal(material.getType().getCode(), "COMPOUND", "Type code");
				start();
			}).fail(function(error) {
				ok(false, error.message);
				start();
			});
		});

	}
});
