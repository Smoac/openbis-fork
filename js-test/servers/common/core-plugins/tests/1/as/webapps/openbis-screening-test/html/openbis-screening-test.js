/*
 * These tests should be run against openBIS instance  
 * with screening sprint server database version
 */

var testProtocol = window.location.protocol;
var testHost = window.location.hostname;
var testPort = window.location.port;
var testUrl = testProtocol + "//" + testHost + ":" + testPort;

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

var createFeatureVectorWellReference = function(featureVectorDatasetReference, wellRow, wellColumn){
    // little hack to avoid creating FeatureVectorDatasetWellReference
    // object by hand as it is pretty complex to do
	var wellReference = jQuery.extend(true, {}, featureVectorDatasetReference);
	wellReference["@type"] = "FeatureVectorDatasetWellReference";
	wellReference["wellPosition"] = createWellPosition(wellRow, wellColumn);
	return wellReference;
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

var listFeatureVectorPhysicalDatasets = function(facade, plateIdentifiers, action){
	facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
		var featureDatasets = response.result;
		
		var physicalFeatureDatasets = featureDatasets.filter(function(featureDataset){
			return featureDataset.dataSetType == "HCS_ANALYSIS_WELL_FEATURES";
		});
		
		action(physicalFeatureDatasets);
	});
}

var listFeatureVectorContainerDatasets = function(facade, plateIdentifiers, action){
	facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
		var featureDatasets = response.result;
		
		var containerFeatureDatasets = featureDatasets.filter(function(featureDataset){
			return featureDataset.dataSetType == "HCS_ANALYSIS_CONTAINER_WELL_FEATURES";
		});
		
		action(containerFeatureDatasets);
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
			assertObjectsCount(response.result, 5);
			assertObjectsWithValues(response.result, "experimentCode", ["EXP-1","EXP-2","TEST-EXPERIMENT","TEST-EXPERIMENT-2","TEST-EXPERIMENT-3"]);
			facade.close();
		});
	});
});

test("listExperimentsVisibleToUser()", function(){
	createFacadeAndLogin(function(facade){
		var userId = "test_space_admin";
		
		facade.listExperimentsVisibleToUser(userId, function(response){
			assertObjectsCount(response.result, 3);
			assertObjectsWithValues(response.result, "experimentCode", ["TEST-EXPERIMENT","TEST-EXPERIMENT-2","TEST-EXPERIMENT-3"]);
			facade.close();
		});
	});
});

test("listFeatureVectorDatasets()", function(){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		facade.listFeatureVectorDatasets(plateIdentifiers, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "datasetCode", ["20130412153659945-390", "20130412153659994-391"]);
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

var testListAvailableFeatureCodes = function(listFeatureVectorDatasetsFunction){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		listFeatureVectorDatasetsFunction(facade, plateIdentifiers, function(featureDatasets){
			facade.listAvailableFeatureCodes(featureDatasets, function(response){
				assertObjectsCount(response.result, 4);
				assertArrays(response.result, ["ROW_NUMBER", "COLUMN_NUMBER", "TPU", "STATE"], "Feature codes are correct");
				facade.close();
			});
		});
	});
}

test("listAvailableFeatureCodes() for physical data set", function(){
	testListAvailableFeatureCodes(listFeatureVectorPhysicalDatasets);
});

test("listAvailableFeatureCodes() for container data set", function(){
	testListAvailableFeatureCodes(listFeatureVectorContainerDatasets);
});

var testListAvailableFeatures = function(listFeatureVectorDatasetsFunction){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		listFeatureVectorDatasetsFunction(facade, plateIdentifiers, function(featureDatasets){
			facade.listAvailableFeatures(featureDatasets, function(response){
				assertObjectsCount(response.result, 4);
				assertObjectsWithValues(response.result, 'code', ["ROW_NUMBER", "COLUMN_NUMBER", "TPU", "STATE"]);
				facade.close();
			});
		});
	});
}

test("listAvailableFeatures() for physical data set", function(){
	testListAvailableFeatures(listFeatureVectorPhysicalDatasets);
});

test("listAvailableFeatures() for container data set", function(){
	testListAvailableFeatures(listFeatureVectorContainerDatasets);
});

var testLoadFeaturesForDatasetWellReferences = function(listFeatureVectorDatasetsFunction){
	createFacadeAndLogin(function(facade){
	    var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];

	    listFeatureVectorDatasetsFunction(facade, plateIdentifiers, function(featureDatasets){
	            var datasetWellReferences = [ createFeatureVectorWellReference(featureDatasets[0], 1, 2) ];
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
}

test("loadFeaturesForDatasetWellReferences() for physical data set", function(){
	testLoadFeaturesForDatasetWellReferences(listFeatureVectorPhysicalDatasets);
});

test("loadFeaturesForDatasetWellReferences() for container data set", function(){
	testLoadFeaturesForDatasetWellReferences(listFeatureVectorContainerDatasets);
});

var testLoadFeatures = function(listFeatureVectorDatasetsFunction){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		listFeatureVectorDatasetsFunction(facade, plateIdentifiers, function(featureDatasets){
			var featureCodes = ["ROW_NUMBER", "STATE"];
			
			facade.loadFeatures(featureDatasets, featureCodes, function(response){
				assertObjectsCount(response.result, 1);
				assertArrays(response.result[0].featureCodes, featureCodes, 'Feature codes are correct');
				equal(response.result[0].featureVectors.length, 96, 'Feature vectors count is correct');
				facade.close();
			});
		});
	});
}

test("loadFeatures() for physical data set", function(){
	testLoadFeatures(listFeatureVectorPhysicalDatasets);
});

test("loadFeatures() for container data set", function(){
	testLoadFeatures(listFeatureVectorContainerDatasets);
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

var testListAvailableImageRepresentationFormats = function(datasetCode){
	createFacadeAndLogin(function(facade){
		var datasetCodes = [datasetCode];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var dataSetIdentifiers = response.result; 
			
			facade.listAvailableImageRepresentationFormats(dataSetIdentifiers, function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithValuesFunction(response.result, "datasetCode", function(result){
					return result.dataset.datasetCode;
				}, [datasetCode]);
				facade.close();
			});
		});
	});
}

test("listAvailableImageRepresentationFormats() for physical data set", function(){
	testListAvailableImageRepresentationFormats("20130412143119901-199");
});

test("listAvailableImageRepresentationFormats() for container data set", function(){
	testListAvailableImageRepresentationFormats("20130412143121081-200");
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

testLoadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize = function(datasetCode){
	createFacadeAndLogin(function(facade){
		var datasetCodes = [datasetCode];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var dataSetIdentifier = response.result[0];
			var wellPositions = [ createWellPosition(1, 1) ];
			var channel = "DAPI";
			var thumbnailSizeOrNull = createImageSize(100, 100);
			
			facade.loadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize(dataSetIdentifier, wellPositions , channel, thumbnailSizeOrNull, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
}

/* FAILS
test("loadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize() for physical data set", function(){
	testLoadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize("20130412143119901-199");
});
*/

test("loadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize() for container data set", function(){
	testLoadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize("20130412143121081-200");
});

var testLoadImagesBase64ForDataSetIdentifierAndChannelAndImageSize = function(datasetCode){
	createFacadeAndLogin(function(facade){
		var datasetCodes = [datasetCode];
		
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
}

/* FAILS
test("loadImagesBase64ForDataSetIdentifierAndChannelAndImageSize() for physical data set", function(){
	testLoadImagesBase64ForDataSetIdentifierAndChannelAndImageSize("20130417094936021-428");
});
*/

test("loadImagesBase64ForDataSetIdentifierAndChannelAndImageSize() for container data set", function(){
	testLoadImagesBase64ForDataSetIdentifierAndChannelAndImageSize("20130417094937144-429");
});

var testLoadThumbnailImagesBase64ForDataSetIdentifierAndChannels = function(datasetCode){
	createFacadeAndLogin(function(facade){
		var datasetCodes = [datasetCode];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var datasetIdentifier = response.result[0];
			var channels = ["RGB"];
			
			facade.loadThumbnailImagesBase64ForDataSetIdentifierAndChannels(datasetIdentifier, channels, function(response){
				assertObjectsCount(response.result, 1);
				facade.close();
			});
		});
	});
}

/* FAILS
test("loadThumbnailImagesBase64ForDataSetIdentifierAndChannels() for physical data set", function(){
	testLoadThumbnailImagesBase64ForDataSetIdentifierAndChannels("20130417094934693-427");
});
*/

test("loadThumbnailImagesBase64ForDataSetIdentifierAndChannels() for container data set", function(){
	testLoadThumbnailImagesBase64ForDataSetIdentifierAndChannels("20130417094937144-429");
});

var testListPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel = function(datasetCode){
	createFacadeAndLogin(function(facade){
		var datasetCodes = [datasetCode];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var dataSetIdentifier = response.result[0];
			var wellPositions = [ createWellPosition(1, 1) ];
			var channel = "DAPI";
			
			facade.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel(dataSetIdentifier, wellPositions , channel, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
}

/* FAILS
test("listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel() for physical data set", function(){
	testListPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel("20130412143119901-199");
});
*/

test("listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel() for container data set", function(){
	testListPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel("20130412143121081-200");
});


var testListPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels = function(datasetCode){
	createFacadeAndLogin(function(facade){
		var datasetCodes = [datasetCode];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var dataSetIdentifier = response.result[0];
			var wellPositions = [ createWellPosition(1, 1) ];
			var channels = ["DAPI"];
			
			facade.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels(dataSetIdentifier, wellPositions , channels, function(response){
				assertObjectsCount(response.result, 9);
				facade.close();
			});
		});
	});
}

/* FAILS
test("listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels() for physical data set", function(){
	testListPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels("20130412143119901-199");
});
*/

test("listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels() for container data set", function(){
	testListPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels("20130412143121081-200");
});

var testListImageReferencesForDataSetIdentifierAndChannel = function(datasetCode){
	createFacadeAndLogin(function(facade){
		var datasetCodes = [datasetCode];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var datasetIdentifier = response.result[0];
			var channel = "RGB";
			
			facade.listImageReferencesForDataSetIdentifierAndChannel(datasetIdentifier, channel, function(response){
				assertObjectsCount(response.result, 1);
				facade.close();
			});
		});
	});
}

/* FAILS
test("listImageReferencesForDataSetIdentifierAndChannel() for physical data set", function(){
	testListImageReferencesForDataSetIdentifierAndChannel("20130417094936021-428");
});
*/

test("listImageReferencesForDataSetIdentifierAndChannel() for container data set", function(){
	testListImageReferencesForDataSetIdentifierAndChannel("20130417094937144-429");
});

var testListImageReferencesForDataSetIdentifierAndChannels = function(datasetCode){
	createFacadeAndLogin(function(facade){
		var datasetCodes = [datasetCode];
		
		facade.getDatasetIdentifiers(datasetCodes, function(response){
			var datasetIdentifier = response.result[0];
			var channels = ["RGB"];
			
			facade.listImageReferencesForDataSetIdentifierAndChannels(datasetIdentifier, channels, function(response){
				assertObjectsCount(response.result, 1);
				facade.close();
			});
		});
	});
}

/* FAILS
test("listImageReferencesForDataSetIdentifierAndChannels() for physical data set", function(){
	testListImageReferencesForDataSetIdentifierAndChannels("20130417094936021-428");
});
*/

test("listImageReferencesForDataSetIdentifierAndChannels() for container data set", function(){
	testListImageReferencesForDataSetIdentifierAndChannels("20130417094937144-429");
});

var testListAvailableFeatureLists = function(listFeatureVectorDatasetsFunction){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		listFeatureVectorDatasetsFunction(facade, plateIdentifiers, function(featureDatasets){
			facade.listAvailableFeatureLists(featureDatasets[0], function(response){
				assertObjectsCount(response.result, 2);
				assertArrays(response.result, ["BARCODE_AND_STATE_FEATURE_LIST","NUMBER_FEATURE_LIST"], "Feature lists are correct");
				facade.close();
			});
		});
	});
}

/* FAILS
test("listAvailableFeatureLists() for physical data set", function(){
	testListAvailableFeatureLists(listFeatureVectorPhysicalDatasets);
});
*/
 
test("listAvailableFeatureLists() for container data set", function(){
	testListAvailableFeatureLists(listFeatureVectorContainerDatasets);
});

var testGetFeatureList = function(listFeatureVectorDatasetsFunction){
	createFacadeAndLogin(function(facade){
		var plateIdentifiers = [ createPlateIdentifier("/PLATONIC/PLATE-1") ];
		
		listFeatureVectorDatasetsFunction(facade, plateIdentifiers, function(featureDatasets){
			var featureDataset = featureDatasets[0];
			
			facade.getFeatureList(featureDataset, "BARCODE_AND_STATE_FEATURE_LIST", function(response){
				assertObjectsCount(response.result, 2);
				assertArrays(response.result, ["barcode","STATE"], "Feature list items are correct");
				facade.close();
			});
		});
	});
}

/* FAILS
test("getFeatureList() for physical data set", function(){
	testGetFeatureList(listFeatureVectorPhysicalDatasets);
});
*/

test("getFeatureList() for container data set", function(){
	testGetFeatureList(listFeatureVectorContainerDatasets);
});
