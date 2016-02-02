/**
 * 
 */
define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("JS VS JAVA API");

		//
		// Ignore specific Java classes giving a custom message
		//
		var getSimpleClassName = function(fullyQualifiedClassName) {
			var idx = fullyQualifiedClassName.lastIndexOf(".");
			return fullyQualifiedClassName.substring(idx + 1, fullyQualifiedClassName.length);
		};

		var ignoreMessages = {
			"ICustomASServiceExecutor" : "Java class ignored: ",
			"ServiceContext" : "Java class ignored: ",
			"CustomASServiceContext" : "Java class ignored: ",
			"AbstractCollectionView" : "Java class ignored: ",
			"ListView" : "Java class ignored: ",
			"SetView" : "Java class ignored: ",
			"NotFetchedException" : "Java class ignored: ",
			"ObjectNotFoundException" : "Java class ignored: ",
			"UnauthorizedObjectAccessException" : "Java class ignored: ",
			"UnsupportedObjectIdException" : "Java class ignored: ",
			"IApplicationServerApi" : "Java class ignored: ",
			"DataSetFileDownloadInputStream" : "Java class ignored: ",
			"IDataStoreServerApi" : "Java class ignored: ",
			"DataSetCreation" : "Java class ignored: ",
			"LinkedDataCreation" : "Java class ignored: ",
			"PhysicalDataCreation" : "Java class ignored: ",
			"DataSetFileDownload" : "Java class not implemented in JS: ",
			"DataSetFileDownloadOptions" : "Java class not implemented in JS: ",
			"DataSetFileDownloadReader" : "Java class not implemented in JS: ",
			"DataSetFileSearchCriteria" : "Java class not implemented in JS: ",
			"DataSetFile" : "Java class not implemented in JS: ",
			"DataSetFilePermId" : "Java class not implemented in JS: ",
			"IDataSetFileId" : "Java class not implemented in JS: "
		}

		//
		// JS Classes contained into other classes
		//
		var circularDependencies = {
			"SampleChildrenSearchCriteria" : {
				containerClass : "as.dto.sample.search.SampleSearchCriteria",
				method : "withChildren"
			},
			"SampleContainerSearchCriteria" : {
				containerClass : "as.dto.sample.search.SampleSearchCriteria",
				method : "withContainer"
			},
			"SampleParentsSearchCriteria" : {
				containerClass : "as.dto.sample.search.SampleSearchCriteria",
				method : "withParents"
			},
			"DataSetChildrenSearchCriteria" : {
				containerClass : "as.dto.dataset.search.DataSetSearchCriteria",
				method : "withChildren"
			},
			"DataSetContainerSearchCriteria" : {
				containerClass : "as.dto.dataset.search.DataSetSearchCriteria",
				method : "withContainer"
			},
			"DataSetParentsSearchCriteria" : {
				containerClass : "as.dto.dataset.search.DataSetSearchCriteria",
				method : "withParents"
			},
		}

		//
		// Java VS JS Comparator
		//
		var jsComparator = function(testsResults, javaClassReport, jsObject) {
			// Check object returned
			if (!jsObject) {
				var errorResult = "JS class missing instance: " + javaClassReport.jsonObjAnnotation;
				testsResults.error.push(errorResult);
				console.info(errorResult);
				return;
			}

			var jsPrototype = null;

			if ($.isFunction(jsObject)) {
				jsPrototype = jsObject.prototype;
			} else {
				jsPrototype = jsObject;
			}

			if (!jsPrototype) {
				var errorResult = "JS class missing prototype: " + javaClassReport.jsonObjAnnotation;
				testsResults.error.push(errorResult);
				console.info(errorResult);
				return;
			}

			// Java Fields found in Javascript
			for (var fIdx = 0; fIdx < javaClassReport.fields.length; fIdx++) {
				if (jsPrototype[javaClassReport.fields[fIdx]] === undefined) {
					var errorResult = "JS class missing field: " + javaClassReport.jsonObjAnnotation + " - " + javaClassReport.fields[fIdx];
					testsResults.error.push(errorResult);
					console.info(errorResult);
				}
			}

			// Java Methods found in Javascript
			for (var fIdx = 0; fIdx < javaClassReport.methods.length; fIdx++) {
				if (!jsPrototype[javaClassReport.methods[fIdx]]) {
					var errorResult = "JS class missing method: " + javaClassReport.jsonObjAnnotation + " - " + javaClassReport.methods[fIdx];
					testsResults.error.push(errorResult);
					console.info(errorResult);
				}
			}
		}

		//
		// Main Reporting Logic
		//
		var areClassesCorrect = function(report, callback) {
			var testsToDo = [];
			var testsResults = {
				info : [],
				warning : [],
				error : []
			};

			var doNext = function() {
				if (testsToDo.length > 0) {
					var next = testsToDo.shift();
					next();
				} else {
					callback(testsResults);
				}
			}

			for (var ridx = 0; ridx < report.entries.length; ridx++) {
				var javaClassReport = report.entries[ridx];
				var testClassFunc = function(javaClassReport) {
					return function() {
						var javaClassName = javaClassReport.name;
						var javaSimpleClassName = getSimpleClassName(javaClassName);
						var ignoreMessage = ignoreMessages[javaSimpleClassName];
						var circularDependencyConfig = circularDependencies[javaSimpleClassName];

						if (ignoreMessage) {
							var warningResult = ignoreMessage + javaClassReport.name;
							testsResults.warning.push(warningResult);
							console.info(warningResult);
							doNext();
						} else {
							var jsClassName = null;
							if (circularDependencyConfig) {
								jsClassName = circularDependencyConfig.containerClass;
							} else {
								jsClassName = javaClassReport.jsonObjAnnotation;
							}

							if (jsClassName) {
								var failedLoadingErrorHandler = function(javaClassName) {
									return function() {
										var errorResult = "Java class with jsonObjectAnnotation missing in Javascript: " + javaClassName;
										testsResults.error.push(errorResult);
										console.info(errorResult);
										doNext();
									};
								};

								var loadedHandler = null;

								loadedHandler = function(circularDependencyConfig) {
									return function(javaClassReport) {
										return function(jsObject) {
											if (circularDependencyConfig) {
												var instanceJSObject = new jsObject();
												var containedJsObject = instanceJSObject[circularDependencyConfig.method]();
												jsObject = containedJsObject;
											}

											jsComparator(testsResults, javaClassReport, jsObject);
											testsResults.info.push("Java class matching JS: " + javaClassReport.name);
											doNext();
										};
									};
								}

								loadedHandler = loadedHandler(circularDependencyConfig);

								var requireJsPath = jsClassName.replace(/\./g, '/');
								require([ requireJsPath ], loadedHandler(javaClassReport), failedLoadingErrorHandler(javaClassName));
							} else {
								var errorResult = "Java class missing jsonObjectAnnotation: " + javaClassName;
								testsResults.error.push(errorResult);
								console.info(errorResult);
								doNext();
							}
						}
					}
				}
				testsToDo.push(testClassFunc(javaClassReport));
			}

			doNext();
		}

		var getPrintableReport = function(javaReport, testsResults) {
			var printableReport = "Total Java classes found " + javaReport.entries.length;
			printableReport += " - Javascript Error Msg: " + testsResults.error.length;
			printableReport += " - Javascript Warning Msg: " + testsResults.warning.length;
			printableReport += " - Javascript Info Msg: " + testsResults.info.length;
			printableReport += "\n";

			for (var edx = 0; edx < testsResults.error.length; edx++) {
				printableReport += "[ERROR] " + testsResults.error[edx] + "\n";
			}
			for (var wdx = 0; wdx < testsResults.warning.length; wdx++) {
				printableReport += "[WARNING] " + testsResults.warning[wdx] + "\n";
			}
			for (var idx = 0; idx < testsResults.info.length; idx++) {
				printableReport += "[INFO] " + testsResults.info[idx] + "\n";
			}
			return printableReport;
		}

		QUnit.test("get Java report from aggregation service", function(assert) {
			var c = new common(assert);
			c.start();

			var getV3APIReport = function(facade) {
				c.getResponseFromJSTestAggregationService(facade, {
					"method" : "getV3APIReport"
				}, function(data) {
					var javaReport = null;

					if (!data.error && data.result.columns[0].title === "STATUS" && data.result.rows[0][0].value === "SUCCESS") { // Success
						// Case
						javaReport = JSON.parse(data.result.rows[0][1].value);
					}

					if (javaReport) {
						areClassesCorrect(javaReport, function(testsResults) {
							if (testsResults.error.length > 0) {
								c.fail(getPrintableReport(javaReport, testsResults));
							} else {
								c.ok(getPrintableReport(javaReport, testsResults));
							}
							c.finish();
						});
					} else {
						c.fail("Report Missing");
						c.finish();
					}
				});
			}

			c.createFacadeAndLogin().then(getV3APIReport);
		});
	}
});