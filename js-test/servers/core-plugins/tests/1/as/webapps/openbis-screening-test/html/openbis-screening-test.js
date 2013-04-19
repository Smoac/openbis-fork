/*
 * These tests should be run against openBIS instance  
 * with screening sprint server database version
 */

var testProtocol = "http";
var testHost = "localhost";
var testPort = "20000";
var testUrl = testProtocol + "://" + testHost + ":" + testPort;

var testUserId = "openbis_screening_test_js";
var testUserPassword = "password";

var createFacadeAndLogin = function(action, urlOrNull, timeoutOrNull){
	var url = typeof urlOrNull == "undefined" ? testUrl : urlOrNull;
	createFacadeAndLoginForUserAndPassword(testUserId, testUserPassword, action, url, timeoutOrNull);
}

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
					format : formatResponse.result[0].imageRepresentationFormats[0]
				});
			});
		});
	});
}

test("listPlates()", function(){
	createFacadeAndLogin(function(facade){
		
		facade.listPlates(function(response){
			assertObjectsCount(response.result, 4);
			facade.close();
		});
	});
});



test("listPlatesForExperiment()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifier = createExperimentIdentfier("/PLATONIC/SCREENING-EXAMPLES/EXP-1"); 
		
		facade.listPlatesForExperiment(experimentIdentifier, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "plateCode", ["PLATE-1","PLATE-2"]);
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
			assertObjectsWithValues(response.result, "experimentCode", ["EXP-1","EXP-2","TEST-EXPERIMENT","TEST-EXPERIMENT-2"]);
			facade.close();
		});
	});
});

test("listExperimentsVisibleToUser()", function(){
	createFacadeAndLogin(function(facade){
		var userId = "test_space_admin";
		
		facade.listExperimentsVisibleToUser(userId, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "experimentCode", ["TEST-EXPERIMENT","TEST-EXPERIMENT-2"]);
			facade.close();
		});
	});
});

test("listFeatureVectorDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "datasetCode", ["20130412153659994-391"]);
			facade.close();
		});
	});
});

test("listImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "datasetCode", [ "20130412143121081-200"]);
			facade.close();
		});
	});
});

test("listRawImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listRawImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "datasetCode", [ "20130412143121081-200"]);
			facade.close();
		});
	});
});

test("listSegmentationImageDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listSegmentationImageDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "datasetCode", [ "20130412153119864-385" ]);
			facade.close();
		});
	});
});

test("getDatasetIdentifiers()", function(){
	createFacadeAndLogin(function(facade){
		var datasetCodes = ["20130412143119901-199", "20130412143121081-200"];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "datasetCode", datasetCodes);
			facade.close();
		});
	});
});

test("listPlateWellsForExperimentAndMaterial()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifer = createExperimentIdentfier("/PLATONIC/SCREENING-EXAMPLES/EXP-1"); 
		var materialIdentifier = createMaterialIdentifier("/GENE/G1"); 
		var findDatasets = true;
		
		facade.listPlateWellsForExperimentAndMaterial(experimentIdentifer, materialIdentifier, findDatasets, function(response){
			assertObjectsCount(response.result, 98);
			assertObjectsWithValuesFunction(response.result, "plateCode", function(result){
				return result.experimentPlateIdentifier.plateCode;
			}, ["PLATE-1", "PLATE-2"]);
			facade.close();
		});
	});
});

test("listPlateWellsForMaterial()", function(){
	createFacadeAndLogin(function(facade){
		var materialIdentifier = createMaterialIdentifier("/GENE/G1");
		var findDatasets = true;
		
		facade.listPlateWellsForMaterial(materialIdentifier, findDatasets, function(response){
			assertObjectsCount(response.result, 196);
			assertObjectsWithValuesFunction(response.result, "plateCode", function(result){
				return result.experimentPlateIdentifier.plateCode;
			}, ["PLATE-1", "PLATE-1A", "PLATE-2"]);
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
		var wellIdentifier = createWellIdentifier("20130412140151999-35");
		
		facade.getWellSample(wellIdentifier, function(response){
			equal(response.result.code, "PLATE-1:B2", "Well code is correct");
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
		var experimentIdentifier = createExperimentIdentfier("/PLATONIC/SCREENING-EXAMPLES/EXP-1");
		
		facade.getExperimentImageMetadata(experimentIdentifier, function(response){
			equal(response.result.identifier.experimentCode, "EXP-1", "Experiment code is correct");
			equal(response.result.plateGeometry.width, 12, "Plate width is correct");
			equal(response.result.plateGeometry.height, 8, "Plate height is correct");
			facade.close();
		});
	});
});

/*

TODO: these methods do not work for a container dataset 

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

*/

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
				assertObjectsCount(response.result, 9);
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

test("listImageMetadata()", function(){
	createFacadeAndLogin(function(facade){
		listImageDatasetReferencesForPlateIdentifier(facade, "/PLATONIC/PLATE-1", function(imageDataSets){
			facade.listImageMetadata(imageDataSets, function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithValuesFunction(response.result, "datasetCode", function(result){
					return result.imageDataset.datasetCode;
				}, ["20130412143121081-200"]);
				facade.close();
			});
		});
	});
});

test("listAvailableImageRepresentationFormats()", function(){
	createFacadeAndLogin(function(facade){
		listImageDatasetReferencesForPlateIdentifier(facade, "/PLATONIC/PLATE-1", function(imageDataSets){
			facade.listAvailableImageRepresentationFormats(imageDataSets, function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithValuesFunction(response.result, "datasetCode", function(result){
					return result.dataset.datasetCode;
				}, ["20130412143121081-200"]);
				facade.close();
			});
		});
	});
});

test("loadPhysicalThumbnailsBase64ForImageReferencesAndImageRepresentationFormat()", function(){
	createFacadeAndLogin(function(facade){
		listImageReferencesAndFormatForPlateIdentifierAndWellPositionAndChannel(facade, "/PLATONIC/PLATE-1", 1, 1, "DAPI", function(results){
			facade.loadPhysicalThumbnailsBase64ForImageReferencesAndImageRepresentationFormat(results.imageReferences, results.format, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

test("loadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize()", function(){
	createFacadeAndLogin(function(facade){
		listImageDatasetReferencesForPlateIdentifier(facade, "/PLATONIC/PLATE-1", function(imageDataSets){
			var dataSetIdentifier = imageDataSets[0];
			var wellPositions = [ createWellPosition(1, 1) ];
			var channel = "DAPI";
			var thumbnailSizeOrNull = createImageSize(100, 100);
			
			facade.loadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize(dataSetIdentifier, wellPositions , channel, thumbnailSizeOrNull, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

/*

TODO: this method works only for a container dataset

*/
test("loadImagesBase64ForDataSetIdentifierAndChannelAndImageSize()", function(){
	createFacadeAndLogin(function(facade){
		var datasetCodes = ["20130417094937144-429"];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var datasetIdentifier = response.result[0];
			var channel = "RGB";
			var thumbnailSizeOrNull = createImageSize(100, 100);
			
			facade.loadImagesBase64ForDataSetIdentifierAndChannelAndImageSize(datasetIdentifier, channel, thumbnailSizeOrNull, function(response){
				assertObjectsCount(response.result, 1);
				facade.close();
			});
		});
	});
});

/*

TODO: this method works only for a container dataset

*/
test("loadThumbnailImagesBase64ForDataSetIdentifierAndChannels()", function(){
	createFacadeAndLogin(function(facade){
		var datasetCodes = ["20130417094937144-429"];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var datasetIdentifier = response.result[0];
			var channels = ["RGB"];
			
			facade.loadThumbnailImagesBase64ForDataSetIdentifierAndChannels(datasetIdentifier, channels, function(response){
				assertObjectsCount(response.result, 1);
				facade.close();
			});
		});
	});
});

test("listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel()", function(){
	createFacadeAndLogin(function(facade){
		listImageDatasetReferencesForPlateIdentifier(facade, "/PLATONIC/PLATE-1", function(imageDataSets){
			var dataSetIdentifier = imageDataSets[0];
			var wellPositions = [ createWellPosition(1, 1) ];
			var channel = "DAPI";
			
			facade.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel(dataSetIdentifier, wellPositions , channel, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

test("listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels()", function(){
	createFacadeAndLogin(function(facade){
		listImageDatasetReferencesForPlateIdentifier(facade, "/PLATONIC/PLATE-1", function(imageDataSets){
			var dataSetIdentifier = imageDataSets[0];
			var wellPositions = [ createWellPosition(1, 1) ];
			var channels = ["DAPI"];
			
			facade.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels(dataSetIdentifier, wellPositions , channels, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
});

/*

TODO: this method works only for a container dataset

*/
test("listImageReferencesForDataSetIdentifierAndChannel()", function(){
	createFacadeAndLogin(function(facade){
		var datasetCodes = ["20130417094937144-429"];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var datasetIdentifier = response.result[0];
			var channel = "RGB";
			
			facade.listImageReferencesForDataSetIdentifierAndChannel(datasetIdentifier, channel, function(response){
				assertObjectsCount(response.result, 1);
				facade.close();
			});
		});
	});
});

/*

TODO: this method works only for a container dataset

*/
test("listImageReferencesForDataSetIdentifierAndChannels()", function(){
	createFacadeAndLogin(function(facade){
		var datasetCodes = ["20130417094937144-429"];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var datasetIdentifier = response.result[0];
			var channels = ["RGB"];
			
			facade.listImageReferencesForDataSetIdentifierAndChannels(datasetIdentifier, channels, function(response){
				assertObjectsCount(response.result, 1);
				facade.close();
			});
		});
	});
});

