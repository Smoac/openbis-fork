/*
 * These tests should be run against openBIS instance  
 * with screening sprint server database version
 */

test("listPlates()", function(){
	createFacadeAndLogin(function(facade){
		
		facade.listPlates(function(response){
			assertObjectsCount(response.result, 215);
		});
	});
});

test("listPlatesForExperiment()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifier = {
			"@type" : "ExperimentIdentifier",
			spaceCode : "PLATONIC",
			projectCode : "SCREENING-EXAMPLES",
			experimentCode : "EXP-1"
		};

		facade.listPlatesForExperiment(experimentIdentifier, function(response){
			assertObjectsCount(response.result, 4);
			assertObjectsWithValues(response.result, "plateCode", ["PLATE-1","PLATE-2", "PLATE-16-BIT", "SANOFI-EXAMPLE"]);
		});
	});
});

test("getPlateMetadataList()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
		[{
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-1"
		},
		{
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-2"
		}];

		facade.getPlateMetadataList(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "plateCode", ["PLATE-1","PLATE-2"]);
		});
	});
});

test("listAllExperiments()", function(){
	createFacadeAndLogin(function(facade){
		facade.listAllExperiments(function(response){
			assertObjectsCount(response.result, 4);
			assertObjectsWithValues(response.result, "experimentCode", ["E1","EXP-1","EXP-1","TEST"]);
		});
	});
});

test("listExperimentsVisibleToUser()", function(){
	createFacadeAndLogin(function(facade){
		var userId = "power_user";
		
		facade.listExperimentsVisibleToUser(userId, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "experimentCode", ["E1"]);
		});
	});
});

test("listFeatureVectorDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
			[{
				"@type" : "PlateIdentifier",
				spaceCodeOrNull : "PLATONIC",
				plateCode : "PLATE-1"
			}]; 
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "datasetCode", ["20110913112215416-82999"]);
		});
	});
});

test("listImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
			[{
				"@type" : "PlateIdentifier",
				spaceCodeOrNull : "PLATONIC",
				plateCode : "PLATE-1"
			}]; 
		
		facade.listImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValues(response.result, "datasetCode", [ "20110913111517610-82996", "20110913112525450-83000", "20110913113026096-83001"]);
		});
	});
});

test("listRawImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
			[{
				"@type" : "PlateIdentifier",
				spaceCodeOrNull : "PLATONIC",
				plateCode : "PLATE-1"
			}]; 
		
		facade.listRawImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValues(response.result, "datasetCode", [ "20110913111517610-82996", "20110913112525450-83000", "20110913113026096-83001"]);
		});
	});
});

test("listSegmentationImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
			[{
				"@type" : "PlateIdentifier",
				spaceCodeOrNull : "PLATONIC",
				plateCode : "PLATE-1"
			}]; 
		
		facade.listSegmentationImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "datasetCode", [ "20110913111925577-82997", "20110913111955463-82998" ]);
		});
	});
});

test("getDatasetIdentifiers()", function(){
	createFacadeAndLogin(function(facade){
		var datasetCodes = ["20110913111517610-82996", "20110913111955463-82998"];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "datasetCode", datasetCodes);
		});
	});
});

test("listPlateWellsForExperimentAndMaterial()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifer = {
			"@type" : "ExperimentIdentifier",
			spaceCode : "TEST",
			projectCode : "TEST-PROJECT",
			experimentCode : "E1"
		};
		var materialIdentifier = {
			"@type" : "MaterialIdentifierScreening",
			materialTypeIdentifier : {
				"@type" : "MaterialTypeIdentifierScreening",
				materialTypeCode : "GENE"
			},
			materialCode : "1"
		};
		var findDatasets = true;
		
		facade.listPlateWellsForExperimentAndMaterial(experimentIdentifer, materialIdentifier, findDatasets, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValuesFunction(response.result, "plateCode", function(result){
				return result.experimentPlateIdentifier.plateCode;
			}, ["PLATE-1-A", "PLATE-141-A", "PLATE-71-A"])
		});
	});
});

test("listPlateWellsForMaterial()", function(){
	createFacadeAndLogin(function(facade){
		var materialIdentifier = {
			"@type" : "MaterialIdentifierScreening",
			materialTypeIdentifier : {
				"@type" : "MaterialTypeIdentifierScreening",
				materialTypeCode : "GENE"
			},
			materialCode : "1"
		};
		var findDatasets = true;
		
		facade.listPlateWellsForMaterial(materialIdentifier, findDatasets, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValuesFunction(response.result, "plateCode", function(result){
				return result.experimentPlateIdentifier.plateCode;
			}, ["PLATE-1-A", "PLATE-141-A", "PLATE-71-A"])
		});
	});
});

test("listPlateWells()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifier = {
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-1"
		};
		
		facade.listPlateWells(plateIdentifier, function(response){
			assertObjectsCount(response.result, 79);
		});
	});
});

test("getWellSample()", function(){
	createFacadeAndLogin(function(facade){
		var wellIdentifier = {
			"@type" : "WellIdentifier",
			permId : "20100823133909745-963"
		};
		
		facade.getWellSample(wellIdentifier, function(response){
			equal(response.result.code, "PLATE-1-A:A3", "Well code is correct");
		});
	});
});

test("getPlateSample()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifier = {
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-1"
		};
		
		facade.getPlateSample(plateIdentifier, function(response){
			equal(response.result.code, "PLATE-1", "Plate code is correct");
		});
	});
});

test("listPlateMaterialMapping()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [{
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-1"
		}];
		var materialTypeIdentifierOrNull = {
			"@type" : "MaterialTypeIdentifierScreening",
			materialTypeCode : "GENE"
		};
		
		facade.listPlateMaterialMapping(plateIdentifiers, materialTypeIdentifierOrNull, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValuesFunction(response.result, "plateCode", function(result){
				return result.plateIdentifier.plateCode;
			}, ["PLATE-1"])
		});
	});
});

test("getExperimentImageMetadata()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifer = {
			"@type" : "ExperimentIdentifier",
			spaceCode : "TEST",
			projectCode : "TEST-PROJECT",
			experimentCode : "E1"
		};
		
		facade.getExperimentImageMetadata(experimentIdentifer, function(response){
			equal(response.result.identifier.experimentCode, "E1", "Experiment code is correct");
			equal(response.result.plateGeometry.width, 24, "Plate width is correct");
			equal(response.result.plateGeometry.height, 16, "Plate height is correct");
		});
	});
});

test("listAvailableFeatureCodes()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
		[{
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-1"
		}]; 
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			var featureDatasets = response.result;
			
			facade.listAvailableFeatureCodes(featureDatasets, function(response){
				assertObjectsCount(response.result, 4);
				assertArrays(response.result, ["ROW_NUMBER", "COLUMN_NUMBER", "TPU", "STATE"], "Feature codes are correct");
			});
		});
	});
});

test("listAvailableFeatures()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
		[{
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-1"
		}]; 
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			var featureDatasets = response.result;
			
			facade.listAvailableFeatures(featureDatasets, function(response){
				assertObjectsCount(response.result, 4);
				assertObjectsWithValues(response.result, 'code', ["ROW_NUMBER", "COLUMN_NUMBER", "TPU", "STATE"]);
			});
		});
	});
});

test("loadFeatures()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
		[{
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-1"
		}]; 
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			var featureDatasets = response.result;
			var featureCodes = ["ROW_NUMBER", "STATE"];
			
			facade.loadFeatures(featureDatasets, featureCodes, function(response){
				assertObjectsCount(response.result, 1);
				assertArrays(response.result[0].featureCodes, featureCodes, 'Feature codes are correct');
				equal(response.result[0].featureVectors.length, 96, 'Feature vectors count is correct')
			});
		});
	});
});

test("loadFeaturesForDatasetWellReferences()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
		[{
			"@type" : "PlateIdentifier",
			spaceCodeOrNull : "PLATONIC",
			plateCode : "PLATE-1"
		}]; 
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			
			// little hack to avoid creating FeatureVectorDatasetWellReference 
			// object by hand as it is pretty complex to do
			var featureVectorDataset = response.result[0];
			featureVectorDataset["@type"] = "FeatureVectorDatasetWellReference";
			featureVectorDataset["wellPosition"] = {
				"@type" : "WellPosition",
				wellRow : 1,
				wellColumn : 2
			};

			var datasetWellReferences = [ featureVectorDataset ];
			var featureCodes = ["ROW_NUMBER", "STATE"];
			
			facade.loadFeaturesForDatasetWellReferences(datasetWellReferences, featureCodes, function(response){
				assertObjectsCount(response.result, 1);
				assertArrays(response.result[0].featureCodes, featureCodes, 'Feature codes are correct');
				equal(response.result[0].wellPosition.wellRow, 1, 'Well row is correct')
				equal(response.result[0].wellPosition.wellColumn, 2, 'Well column is correct')
			});
		});
	});
});

test("loadImagesBase64ForImageReferencesAndImageConversion()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = 
			[{
				"@type" : "PlateIdentifier",
				spaceCodeOrNull : "PLATONIC",
				plateCode : "PLATE-1"
			}]; 
		
		facade.listImageDatasets(plateIdentifiers, function(response){
			var dataSetIdentifier = response.result[0];
			var wellPositions = 
			[{
				"@type" : "WellPosition",
				wellRow : 1,
				wellColumn : 1
			}];
			var channel = "DAPI";
			
			facade.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel(dataSetIdentifier, wellPositions, channel, function(response){
				var imageReferences = response.result;
				var convertToPng = false;
				
				facade.loadImagesBase64ForImageReferencesAndImageConversion(imageReferences, convertToPng, function(response){
					alert(response.result.length);
				});
			});
		});
	});
});