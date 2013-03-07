/*
 * These tests should be run against openBIS instance  
 * with screening sprint server database version
 */

var createExperimentIdentfier = function(identifierString){
	var parts = identifierString.split("/");
	
	return {
		"@type" : "ExperimentIdentifier",
		"spaceCode" : parts[1],
		"projectCode" : parts[2],
		"experimentCode" : parts[3]
	};
}

var createPlateIdentifier = function(identifierString){
	var parts = identifierString.split("/");
	
	return {
		"@type" : "PlateIdentifier",
		"spaceCodeOrNull" : parts[1],
		"plateCode" : parts[2]
	};
}

var createMaterialIdentifier = function(identifierString){
	var parts = identifierString.split("/");
	
	return {
		"@type" : "MaterialIdentifierScreening",
		"materialTypeIdentifier" : {
			"@type" : "MaterialTypeIdentifierScreening",
			"materialTypeCode" : parts[1]
		},
		"materialCode" : parts[2]
	};
}

var createMaterialTypeIdentifier = function(typeCode){
	return {
		"@type" : "MaterialTypeIdentifierScreening",
		"materialTypeCode" : typeCode
	};
}

var createWellIdentifier = function(permId){
	return {
		"@type" : "WellIdentifier",
		"permId" : permId
	};
}

var createWellPosition = function(wellRow, wellColumn){
	return {
		"@type" : "WellPosition",
		"wellRow" : wellRow,
		"wellColumn" : wellColumn
	};
}

var createImageSize = function(width, height){
	return {
		"@type" : "ImageSize",
		"width" : width,
		"height" : height
	};
}

var createLoadImageConfiguration = function(width, height){
	return {
		"@type" : "LoadImageConfiguration",
		"desiredImageSize" : createImageSize(width, height)
	};
}

var createImageRepresentationFormat = function(dataSetCode, width, height){
	return {
		"@type" : "ImageRepresentationFormat",
		"dataSetCode" : dataSetCode,
		"width" : width,
		"height" : height
	};
}

var createSizeCriterion = function(width, height, type){
	return {
		"@type" : "SizeCriterion",
		"width" : width,
		"height" : height,
		"type" : type
	};
}

var listImageDatasetReferencesForPlateIdentifier = function(facade, plateIdentifier, action){
	var plateIdentifiers = [ createPlateIdentifier(plateIdentifier) ];
	
	facade.listImageDatasets(plateIdentifiers, function(response){
		action(response.result);
	});
}

var listImageReferencesForPlateIdentifierAndWellPositionAndChannel = function(facade, plateIdentifier, wellRow, wellColumn, channel, action){
	listImageDatasetReferencesForPlateIdentifier(facade, plateIdentifier, function(dataSetReferences){
		var dataSetIdentifier = dataSetReferences[0];
		var wellPositions = [ createWellPosition(wellRow, wellColumn) ];
		
		facade.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel(dataSetIdentifier, wellPositions, channel, function(response){
			action(response.result);
		});
	});
}

var listImageReferencesAndFormatForPlateIdentifierAndWellPositionAndChannel = function(facade, plateIdentifier, wellRow, wellColumn, channel, action){
	listImageDatasetReferencesForPlateIdentifier(facade, plateIdentifier, function(dataSetReferences){
		var dataSetIdentifier = dataSetReferences[0];
		var wellPositions = [ createWellPosition(wellRow, wellColumn) ];
		
		facade.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel(dataSetIdentifier, wellPositions, channel, function(imageReferencesResponse){
			var imageDataSets = [ dataSetIdentifier ];
			
			facade.listAvailableImageRepresentationFormats(imageDataSets, function(formatResponse){
				action({
					imageReferences : imageReferencesResponse.result,
					formats : formatResponse.result[0].imageRepresentationFormats[0]
				});
			});
		});
	});
}

test("listPlates()", function(){
	createFacadeAndLogin(function(facade){
		
		facade.listPlates(function(response){
			assertObjectsCount(response.result, 215);
			facade.close();
		});
	});
});



test("listPlatesForExperiment()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifier = createExperimentIdentfier("/PLATONIC/SCREENING-EXAMPLES/EXP-1"); 
		
		facade.listPlatesForExperiment(experimentIdentifier, function(response){
			assertObjectsCount(response.result, 4);
			assertObjectsWithValues(response.result, "plateCode", ["PLATE-1","PLATE-2", "PLATE-16-BIT", "SANOFI-EXAMPLE"]);
			facade.close();
		});
	});
});

test("getPlateMetadataList()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1"), createPlateIdentifier("/PLATONIC/PLATE-2") ];
		
		facade.getPlateMetadataList(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "plateCode", ["PLATE-1","PLATE-2"]);
			facade.close();
		});
	});
});

test("listAllExperiments()", function(){
	createFacadeAndLogin(function(facade){
		facade.listAllExperiments(function(response){
			assertObjectsCount(response.result, 4);
			assertObjectsWithValues(response.result, "experimentCode", ["E1","EXP-1","EXP-1","TEST"]);
			facade.close();
		});
	});
});

test("listExperimentsVisibleToUser()", function(){
	createFacadeAndLogin(function(facade){
		var userId = "power_user";
		
		facade.listExperimentsVisibleToUser(userId, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "experimentCode", ["E1"]);
			facade.close();
		});
	});
});

test("listFeatureVectorDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "datasetCode", ["20110913112215416-82999"]);
			facade.close();
		});
	});
});

test("listImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValues(response.result, "datasetCode", [ "20110913111517610-82996", "20110913112525450-83000", "20110913113026096-83001"]);
			facade.close();
		});
	});
});

test("listRawImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listRawImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValues(response.result, "datasetCode", [ "20110913111517610-82996", "20110913112525450-83000", "20110913113026096-83001"]);
			facade.close();
		});
	});
});

test("listSegmentationImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listSegmentationImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "datasetCode", [ "20110913111925577-82997", "20110913111955463-82998" ]);
			facade.close();
		});
	});
});

test("getDatasetIdentifiers()", function(){
	createFacadeAndLogin(function(facade){
		var datasetCodes = ["20110913111517610-82996", "20110913111955463-82998"];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "datasetCode", datasetCodes);
			facade.close();
		});
	});
});

test("listPlateWellsForExperimentAndMaterial()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifer = createExperimentIdentfier("/TEST/TEST-PROJECT/E1"); 
		var materialIdentifier = createMaterialIdentifier("/GENE/1"); 
		var findDatasets = true;
		
		facade.listPlateWellsForExperimentAndMaterial(experimentIdentifer, materialIdentifier, findDatasets, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValuesFunction(response.result, "plateCode", function(result){
				return result.experimentPlateIdentifier.plateCode;
			}, ["PLATE-1-A", "PLATE-141-A", "PLATE-71-A"]);
			facade.close();
		});
	});
});

test("listPlateWellsForMaterial()", function(){
	createFacadeAndLogin(function(facade){
		var materialIdentifier = createMaterialIdentifier("/GENE/1");
		var findDatasets = true;
		
		facade.listPlateWellsForMaterial(materialIdentifier, findDatasets, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValuesFunction(response.result, "plateCode", function(result){
				return result.experimentPlateIdentifier.plateCode;
			}, ["PLATE-1-A", "PLATE-141-A", "PLATE-71-A"]);
			facade.close();
		});
	});
});

test("listPlateWells()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifier = createPlateIdentifier("/PLATONIC/PLATE-1");
		
		facade.listPlateWells(plateIdentifier, function(response){
			assertObjectsCount(response.result, 79);
			facade.close();
		});
	});
});

test("getWellSample()", function(){
	createFacadeAndLogin(function(facade){
		var wellIdentifier = createWellIdentifier("20100823133909745-963");
		
		facade.getWellSample(wellIdentifier, function(response){
			equal(response.result.code, "PLATE-1-A:A3", "Well code is correct");
			facade.close();
		});
	});
});

test("getPlateSample()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifier = createPlateIdentifier("/PLATONIC/PLATE-1");
		
		facade.getPlateSample(plateIdentifier, function(response){
			equal(response.result.code, "PLATE-1", "Plate code is correct");
			facade.close();
		});
	});
});

test("listPlateMaterialMapping()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		var materialTypeIdentifierOrNull = createMaterialTypeIdentifier("GENE");
		
		facade.listPlateMaterialMapping(plateIdentifiers, materialTypeIdentifierOrNull, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValuesFunction(response.result, "plateCode", function(result){
				return result.plateIdentifier.plateCode;
			}, ["PLATE-1"]);
			facade.close();
		});
	});
});

test("getExperimentImageMetadata()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifier = createExperimentIdentfier("/TEST/TEST-PROJECT/E1");
		
		facade.getExperimentImageMetadata(experimentIdentifier, function(response){
			equal(response.result.identifier.experimentCode, "E1", "Experiment code is correct");
			equal(response.result.plateGeometry.width, 24, "Plate width is correct");
			equal(response.result.plateGeometry.height, 16, "Plate height is correct");
			facade.close();
		});
	});
});

test("listAvailableFeatureCodes()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			var featureDatasets = response.result;
			
			facade.listAvailableFeatureCodes(featureDatasets, function(response){
				assertObjectsCount(response.result, 4);
				assertArrays(response.result, ["ROW_NUMBER", "COLUMN_NUMBER", "TPU", "STATE"], "Feature codes are correct");
				facade.close();
			});
		});
	});
});

test("listAvailableFeatures()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			var featureDatasets = response.result;
			
			facade.listAvailableFeatures(featureDatasets, function(response){
				assertObjectsCount(response.result, 4);
				assertObjectsWithValues(response.result, 'code', ["ROW_NUMBER", "COLUMN_NUMBER", "TPU", "STATE"]);
				facade.close();
			});
		});
	});
});

test("loadFeatures()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			var featureDatasets = response.result;
			var featureCodes = ["ROW_NUMBER", "STATE"];
			
			facade.loadFeatures(featureDatasets, featureCodes, function(response){
				assertObjectsCount(response.result, 1);
				assertArrays(response.result[0].featureCodes, featureCodes, 'Feature codes are correct');
				equal(response.result[0].featureVectors.length, 96, 'Feature vectors count is correct');
				facade.close();
			});
		});
	});
});

test("loadFeaturesForDatasetWellReferences()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			
			// little hack to avoid creating FeatureVectorDatasetWellReference 
			// object by hand as it is pretty complex to do
			var featureVectorDataset = response.result[0];
			featureVectorDataset["@type"] = "FeatureVectorDatasetWellReference";
			featureVectorDataset["wellPosition"] = createWellPosition(1, 2);

			var datasetWellReferences = [ featureVectorDataset ];
			var featureCodes = ["ROW_NUMBER", "STATE"];
			
			facade.loadFeaturesForDatasetWellReferences(datasetWellReferences, featureCodes, function(response){
				assertObjectsCount(response.result, 1);
				assertArrays(response.result[0].featureCodes, featureCodes, 'Feature codes are correct');
				equal(response.result[0].wellPosition.wellRow, 1, 'Well row is correct');
				equal(response.result[0].wellPosition.wellColumn, 2, 'Well column is correct');
				facade.close();
			});
		});
	});
});

test("loadImagesBase64ForImageReferencesAndImageConversion()", function(){
	createFacadeAndLogin(function(facade){
		listImageReferencesForPlateIdentifierAndWellPositionAndChannel(facade, "/PLATONIC/PLATE-1", 1, 1, "DAPI", function(imageReferences){
			var convertToPng = false;
			
			facade.loadImagesBase64ForImageReferencesAndImageConversion(imageReferences, convertToPng, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

test("loadThumbnailImagesBase64ForImageReferences()", function(){
	createFacadeAndLogin(function(facade){
		listImageReferencesForPlateIdentifierAndWellPositionAndChannel(facade, "/PLATONIC/PLATE-1", 1, 1, "DAPI", function(imageReferences){
			facade.loadThumbnailImagesBase64ForImageReferences(imageReferences, function(response){
				// TODO generate some thumbnails at screening sprint server
				assertObjectsCount(response.result, 0);
				facade.close();
			});
		});
	});
});

test("loadImagesBase64ForImageReferencesAndImageSize()", function(){
	createFacadeAndLogin(function(facade){
		listImageReferencesForPlateIdentifierAndWellPositionAndChannel(facade, "/PLATONIC/PLATE-1", 1, 1, "DAPI", function(imageReferences){
			var imageSize = createImageSize(100, 100);
			
			facade.loadImagesBase64ForImageReferencesAndImageSize(imageReferences, imageSize, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

test("loadImagesBase64ForImageReferences()", function(){
	createFacadeAndLogin(function(facade){
		listImageReferencesForPlateIdentifierAndWellPositionAndChannel(facade, "/PLATONIC/PLATE-1", 1, 1, "DAPI", function(imageReferences){
			facade.loadImagesBase64ForImageReferences(imageReferences, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

test("loadImagesBase64ForImageReferencesAndImageConfiguration()", function(){
	createFacadeAndLogin(function(facade){
		listImageReferencesForPlateIdentifierAndWellPositionAndChannel(facade, "/PLATONIC/PLATE-1", 1, 1, "DAPI", function(imageReferences){
			var configuration = createLoadImageConfiguration(100, 100);
			
			facade.loadImagesBase64ForImageReferencesAndImageConfiguration(imageReferences, configuration, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

test("loadImagesBase64ForImageReferencesAndImageRepresentationFormat()", function(){
	createFacadeAndLogin(function(facade){
		listImageReferencesAndFormatForPlateIdentifierAndWellPositionAndChannel(facade, "/PLATONIC/PLATE-1", 1, 1, "DAPI", function(results){
			facade.loadImagesBase64ForImageReferencesAndImageRepresentationFormat(results.imageReferences, results.format, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

test("loadImagesBase64ForImageReferencesAndImageRepresentationFormatCriteria()", function(){
	createFacadeAndLogin(function(facade){
		listImageReferencesForPlateIdentifierAndWellPositionAndChannel(facade, "/PLATONIC/PLATE-1", 1, 1, "DAPI", function(imageReferences){
			var criteria = [ createSizeCriterion(100, 100, 'SMALLEST_COVERING_BOUNDING_BOX') ];
			
			facade.loadImagesBase64ForImageReferencesAndImageRepresentationFormatCriteria(imageReferences, criteria, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});