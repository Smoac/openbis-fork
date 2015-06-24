define([ 'jquery', 'underscore', 'openbis', 'test/common', 'dto/entity/material/MaterialCreation', 'dto/id/entitytype/EntityTypePermId', 'dto/deletion/material/MaterialDeletionOptions', 'dto/entity/material/MaterialUpdate' ], function($, _, openbis, common, MaterialCreation, EntityTypePermId, MaterialDeletionOptions, MaterialUpdate) {
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
		
		QUnit.test("searchMaterials()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createMaterialSearchCriterion(), c.createMaterialFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("H2O");

				return facade.searchMaterials(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(materials) {
				c.assertObjectsCount(materials, 1);

				var material = materials[0];
				c.assertEqual(material.getCode(), "H2O", "Material code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(Object.keys(properties), "DESCRIPTION", "Water");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});
		
		QUnit.test("createAndDeleteAMaterial()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			var code = "CREATE_JSON_MATERIAL_" + (new Date().getTime());
			var materialCreation = new MaterialCreation();
			materialCreation.setTypeId(new EntityTypePermId("COMPOUND"));
			materialCreation.setCode(code);
			materialCreation.setProperty("DESCRIPTION", "Metal");
			
			$.when(c.createFacadeAndLogin(), c.createMaterialPermId(code, "COMPOUND"), c.createMaterialFetchOptions()).then(function(facade, identifier, fetchOptions) {
				return facade.createMaterials([ materialCreation ]).then(function(permIds) {
					return facade.mapMaterials(permIds, fetchOptions).done(function() {
						var options = new MaterialDeletionOptions();
						options.setReason("test");
						facade.deleteMaterials([ identifier ], options).then(function(deletionId) {
							console.log(deletionId);
							facade.logout();
						}).fail(function(error) {
							c.fail(error.message);
							done();
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
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});	
		
		QUnit.test("updateMaterials",  function(assert) {
			var c = new common(assert);
			var done = assert.async();

			var code = "UPDATE_JSON_MATERIAL_" + (new Date().getTime());
			var materialCreation = new MaterialCreation();
			materialCreation.setTypeId(new EntityTypePermId("COMPOUND"));
			materialCreation.setCode(code);
			materialCreation.setProperty("DESCRIPTION", "Metal");
			c.createFacadeAndLogin().then(function(facade) {
				var ids = facade.createMaterials([ materialCreation ]).then(function(permIds) {
					var materialUpdate = new MaterialUpdate();
					materialUpdate.setMaterialId(permIds[0]);
					materialUpdate.setProperty("DESCRIPTION", "Alloy");
//					materialUpdateModifier(c, materialUpdate);
					return facade.updateMaterials([ materialUpdate ]).then(function() {
						return permIds;
					});
				});
				$.when(ids, c.createMaterialFetchOptions()).then(function(permIds, fetchOptions) {
					return facade.mapMaterials(permIds, fetchOptions).done(function() {
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
					done();
				}).fail(function(error) {
					c.fail(error.message);
					done();
				});
			});
		});
	}
});
