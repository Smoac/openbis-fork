define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Map tests");

		var testMap = function(c, fCreate, fMap) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permIds) {
					c.assertTrue(permIds != null && permIds.length > 0, "Entities were created");
					return fMap(facade, permIds).then(function(map) {
						c.assertEqual(Object.keys(map).length, permIds.length, "Entity map size is correct");
						permIds.forEach(function(permId) {
							var entity = map[permId];
							c.assertEqual(entity.getPermId().toString(), permId.toString(), "Entity perm id matches");
						});
						c.finish();
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}
		
		var testFetchOptions = function(c, fo, toTest) {
			
			if(toTest.hasSample) {
				fo.withSampleUsing(null);
				fo.withSample();
				fo.hasSample();
			}
			
			if(toTest.hasDataStore) {
				fo.withDataStoreUsing(null);
				fo.withDataStore();
				fo.hasDataStore();
			}
			
			if(toTest.hasTags) {
				fo.withTagsUsing(null);
				fo.withTags();
				fo.hasTags();
			}
			
			if(toTest.hasLinkedData) {
				fo.withLinkedDataUsing(null);
				fo.withLinkedData();
				fo.hasLinkedData();
			}
			
			if(toTest.hasPhysicalData) {
				fo.withPhysicalDataUsing(null);
				fo.withPhysicalData();
				fo.hasPhysicalData();
			}
			
			if(toTest.hasContainers) {
				fo.withContainersUsing(null);
				fo.withContainers();
				fo.hasContainers();
			}
			
			if(toTest.hasComponents) {
				fo.withComponentsUsing(null);
				fo.withComponents();
				fo.hasComponents();
			}
			
			if(toTest.hasContainer) {
				fo.withContainerUsing(null);
				fo.withContainer();
				fo.hasContainer();
			}
			
			if(toTest.hasChildren) {
				fo.withChildrenUsing(null);
				fo.withChildren();
				fo.hasChildren();
			}
			
			if(toTest.hasParents) {
				fo.withParentsUsing(null);
				fo.withParents();
				fo.hasParents();
			}
			
			if(toTest.hasSortBy) {
				fo.sortBy().code();
				c.assertEqual(true, ((fo.getSortBy())?true:false));
			}
			
			if(toTest.hasExperiments) {
				fo.withExperimentsUsing(null);
				fo.withExperiments();
				c.assertEqual(true, fo.hasExperiments());
			}
			
			if(toTest.hasSamples) {
				fo.withSamplesUsing(null);
				fo.withSamples();
				c.assertEqual(true, fo.hasSamples());
			}
			
			if(toTest.hasSpace) {
				fo.withSpaceUsing(null);
				fo.withSpace();
				c.assertEqual(true, fo.hasSpace());
			}
			
			if(toTest.hasRegistrator) {
				fo.withRegistratorUsing(null);
				fo.withRegistrator();
				c.assertEqual(true, fo.hasRegistrator());
			}
			
			if(toTest.hasModifier) {
				fo.withModifierUsing(null);
				fo.withModifier();
				c.assertEqual(true, fo.hasModifier());
			}
			
			if(toTest.hasLeader) {
				fo.withLeaderUsing(null);
				fo.withLeader();
				c.assertEqual(true, fo.hasLeader());
			}
			
			if(toTest.hasAttachments) {
				fo.withAttachmentsUsing(null);
				fo.withAttachments();
				c.assertEqual(true, fo.hasAttachments());
			}
			
			if(toTest.hasHistory) {
				fo.withHistoryUsing(null);
				fo.withHistory();
				c.assertEqual(true, fo.hasHistory());
			}
			
			if(toTest.hasType) {
				fo.withTypeUsing(null);
				fo.withType();
				c.assertEqual(true, fo.hasType());
			}
			
			if(toTest.hasProject) {
				fo.withProjectUsing(null);
				fo.withProject();
				c.assertEqual(true, fo.hasProject());
			}
			
			if(toTest.hasDataSets) {
				fo.withDataSetsUsing(null);
				fo.withDataSets();
				c.assertEqual(true, fo.hasDataSets());
			}
			
			if(toTest.hasDataSets) {
				fo.withDataSetsUsing(null);
				fo.withDataSets();
				c.assertEqual(true, fo.hasDataSets());
			}
			
			if(toTest.hasProperties) {
				fo.withPropertiesUsing(null);
				fo.withProperties();
				c.assertEqual(true, fo.hasProperties());
			}
			
			if(toTest.hasMaterialProperties) {
				fo.withMaterialPropertiesUsing(null);
				fo.withMaterialProperties();
				c.assertEqual(true, fo.hasMaterialProperties());
			}
			
			if(toTest.hasTags) {
				fo.withTagsUsing(null);
				fo.withTags();
				c.assertEqual(true, fo.hasTags());
			}
		}
		
		QUnit.test("mapSpaces()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createSpace(facade), c.createSpace(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.SpaceFetchOptions();
				
				testFetchOptions(c, fo, {
					hasRegistrator : true,
					hasProjects : true,
					hasSamples : true,
					hasSortBy : true
				});
				
				return facade.mapSpaces(permIds, fo);
			}
			
			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapProjects()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createProject(facade), c.createProject(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				var fo = new c.ProjectFetchOptions();
				
				testFetchOptions(c, fo, {
					hasExperiments : true,
					hasSpace : true,
					hasLeader : true,
					hasModifier : true,
					hasAttachments : true,
					hasRegistrator : true,
					hasProjects : true,
					hasSamples : true,
					hasSortBy : true,
					hasHistory : true
				});
				
				return facade.mapProjects(permIds, fo);
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapExperiments()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createExperiment(facade), c.createExperiment(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				var fo = new c.ExperimentFetchOptions();
				
				testFetchOptions(c, fo, {
					hasType : true,
					hasProject : true,
					hasDataSets : true,
					hasProperties : true,
					hasMaterialProperties : true,
					hasTags : true,
					hasSamples : true,
					hasHistory : true,
					hasRegistrator : true,
					hasModifier : true,
					hasAttachments : true,
					hasSortBy : true
				});
				
				return facade.mapExperiments(permIds, fo);
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapSamples()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createSample(facade), c.createSample(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.SampleFetchOptions();
				
				testFetchOptions(c, fo, {
					hasParents : true,
					hasChildren : true,
					hasContainer : true,
					hasComponents : true,
					hasType : true,
					hasProject : true,
					hasSpace : true,
					hasExperiment : true,
					hasProperties : true,
					hasMaterialProperties : true,
					hasDataSets : true,
					hasHistory : true,
					hasSortBy : true
				});
				
				return facade.mapSamples(permIds, fo);
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapDataSets()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				var fo = new c.DataSetFetchOptions();
				
				testFetchOptions(c, fo, {
					hasParents : true,
					hasChildren : true,
					hasComponents : true,
					hasType : true,
					hasHistory : true,
					hasExperiment : true,
					hasProperties : true,
					hasMaterialProperties : true,
					hasModifier : true,
					hasRegistrator : true,
					hasContainers : true,
					hasPhysicalData : true,
					hasLinkedData : true,
					hasTags : true,
					hasDataStore : true,
					hasSortBy : true
				});
				
				var result = facade.mapDataSets(permIds, fo);
				
				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.isPostRegistered(), false, "post registered for " + permId);
					});
				});
				return result;
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapMaterials()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createMaterial(facade), c.createMaterial(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				var fo = new c.MaterialFetchOptions();
				
				testFetchOptions(c, fo, {
					hasType : true,
					hasHistory : true,
					hasRegistrator : true,
					hasProperties : true,
					hasMaterialProperties : true,
					hasTags : true,
					hasSortBy : true
				});
				
				return facade.mapMaterials(permIds, fo);
			}

			testMap(c, fCreate, fMap);
		});

	}
});
