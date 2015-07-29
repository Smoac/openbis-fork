define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Material tests");

		QUnit.test("mapMaterials()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.mapMaterials([ new c.MaterialPermId("H2O", "COMPOUND") ], c.createMaterialFetchOptions()).done(function() {
					facade.logout()
				});
			}).done(function(materials) {
				c.assertObjectsCount(Object.keys(materials), 1);

				var material = materials["H2O (COMPOUND)"];
				c.assertEqual(material.getCode(), "H2O", "Code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("searchMaterials()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				var criterion = new c.MaterialSearchCriterion();
				criterion.withCode().thatEquals("H2O");

				return facade.searchMaterials(criterion, c.createMaterialFetchOptions()).done(function() {
					facade.logout();
				})
			}).done(function(materials) {
				c.assertObjectsCount(materials, 1);

				var material = materials[0];
				c.assertEqual(material.getCode(), "H2O", "Material code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(Object.keys(properties), "DESCRIPTION", "Water");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("createAndDeleteAMaterial()", function(assert) {
			var c = new common(assert);
			c.start();

			var code = "CREATE_JSON_MATERIAL_" + (new Date().getTime());
			var materialCreation = new c.MaterialCreation();
			materialCreation.setTypeId(new c.EntityTypePermId("COMPOUND"));
			materialCreation.setCode(code);
			materialCreation.setProperty("DESCRIPTION", "Metal");

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.createMaterials([ materialCreation ]).then(function(permIds) {
					return facade.mapMaterials(permIds, c.createMaterialFetchOptions()).done(function() {
						var options = new c.MaterialDeletionOptions();
						options.setReason("test");
						facade.deleteMaterials([ new c.MaterialPermId(code, "COMPOUND") ], options).then(function(deletionId) {
							console.log(deletionId);
							facade.logout();
						}).fail(function(error) {
							c.fail(error.message);
							c.finish();
						});
					})
				})
			}).done(function(materials) {
				var keys = Object.keys(materials);
				c.assertObjectsCount(keys, 1);
				var material = materials[keys[0]];
				c.assertEqual(material.getCode(), code, "Material code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(properties["DESCRIPTION"], "Metal", "Property DESCRIPTION");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("updateMaterials", function(assert) {
			var c = new common(assert);
			c.start();

			var code = "UPDATE_JSON_MATERIAL_" + (new Date().getTime());
			var materialCreation = new c.MaterialCreation();
			materialCreation.setTypeId(new c.EntityTypePermId("COMPOUND"));
			materialCreation.setCode(code);
			materialCreation.setProperty("DESCRIPTION", "Metal");
			c.createFacadeAndLogin().then(function(facade) {
				var ids = facade.createMaterials([ materialCreation ]).then(function(permIds) {
					var materialUpdate = new c.MaterialUpdate();
					materialUpdate.setMaterialId(permIds[0]);
					materialUpdate.setProperty("DESCRIPTION", "Alloy");
					// materialUpdateModifier(c, materialUpdate);
					return facade.updateMaterials([ materialUpdate ]).then(function() {
						return permIds;
					});
				});
				$.when(ids).then(function(permIds) {
					return facade.mapMaterials(permIds, c.createMaterialFetchOptions()).done(function() {
						facade.logout();
					});
				}).done(function(materials) {
					var keys = Object.keys(materials);
					c.assertObjectsCount(keys, 1);
					var material = materials[keys[0]];
					c.assertEqual(material.getCode(), code, "Material code");
					c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
					var properties = material.getProperties();
					c.assertEqual(properties["DESCRIPTION"], "Alloy", "Property DESCRIPTION");
					c.finish();
				}).fail(function(error) {
					c.fail(error.message);
					c.finish();
				});
			});
		});
	}
});
