define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Material tests");

		QUnit.test("mapMaterials()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createMaterialPermId("H2O", "COMPOUND"), c.createMaterialFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapMaterials([ permId ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(materials) {
				c.assertObjectsCount(Object.keys(materials), 1);

				var material = materials["H2O (COMPOUND)"];
				c.assertEqual(material.getCode(), "H2O", "Code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

	}
});
