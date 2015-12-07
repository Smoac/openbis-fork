/**
 * 
 */
define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("JS VS JAVA API");
		
		var defaultHandler = function(testsResults, javaClassReport, jsObject) {
			//Check object returned
			if(!jsObject) {
				var errorResult = "JS class missing instance: " + javaClassReport.jsonObjAnnotation;
				testsResults.error.push(errorResult);
				console.info(errorResult);
				return;
			}
			
			//Check prototype available
			if(!jsObject.prototype) {
				var errorResult = "JS class missing prototype: " + javaClassReport.jsonObjAnnotation;
				testsResults.error.push(errorResult);
				console.info(errorResult);
				return;
			}
			
			//Java Fields found in Javascript
			for(var fIdx = 0; fIdx < javaClassReport.fields.length; fIdx++) {
				if(!jsObject.prototype[javaClassReport.fields[fIdx]]) {
					var errorResult = "JS class missing field: " + javaClassReport.jsonObjAnnotation + " - " + javaClassReport.fields[fIdx];
					testsResults.error.push(errorResult);
					console.info(errorResult);
				}
			}
			
			//Java Methods found in Javascript
			for(var fIdx = 0; fIdx < javaClassReport.methods.length; fIdx++) {
				if(!jsObject.prototype[javaClassReport.methods[fIdx]]) {
					var errorResult = "JS class missing method: " + javaClassReport.jsonObjAnnotation + " - " + javaClassReport.methods[fIdx];
					testsResults.error.push(errorResult);
					console.info(errorResult);
				}
			}
		}
		
		var areClassesCorrect = function(report, callback) {
			var testsToDo = [];
			var testsResults = {
					info : [],
					warning : [],
					error : []
			};
			
			var doNext = function() {
				if(testsToDo.length > 0) {
					var next = testsToDo.shift();
					next();
				} else {
					callback(testsResults);
				}
			}
			
			for(var ridx = 0; ridx < report.entries.length; ridx++) {
				var javaClassReport = report.entries[ridx];
				var testClassFunc = function(javaClassReport) {
					return function () {
						var javaClassName = javaClassReport.name;
						var jsClassName = javaClassReport.jsonObjAnnotation;
						
						if(jsClassName) {
							var failedLoadingErrorHandler = function(javaClassName) {
								return function() {
									var errorResult = "Java class with jsonObjectAnnotation missing in Javascript: " + javaClassName;
									testsResults.error.push(errorResult);
									console.info(errorResult);
									doNext();
								};
							};
							
							var loadedHandler = function(javaClassReport) {
								return function(jsObject) {
									defaultHandler(testsResults, javaClassReport, jsObject);
									testsResults.info.push("Java class matching JS: " + javaClassReport.name);
									doNext();
								};
							};
							
							var requireJsPath = jsClassName.replace(/\./g,'/');
							require([requireJsPath], loadedHandler(javaClassReport), failedLoadingErrorHandler(javaClassName));
						} else {
							var errorResult = "Java class missing jsonObjectAnnotation: " + javaClassName;
							testsResults.error.push(errorResult);
							console.info(errorResult);
							doNext();
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
				
				for(var edx = 0; edx < testsResults.error.length; edx++) {
					printableReport += "[ERROR] " + testsResults.error[edx] + "\n";
				}
				for(var wdx = 0; wdx < testsResults.warning.length; wdx++) {
					printableReport += "[WARNING] " + testsResults.warning[wdx] + "\n";
				}
				for(var idx = 0; idx < testsResults.info.length; idx++) {
					printableReport += "[INFO] " + testsResults.info[idx] + "\n";
				}
			return printableReport;
		}
		
		QUnit.test("get Java report from aggregation service", function(assert) {
			var c = new common(assert);
			c.start();
			
			var getV3APIReport = function(facade) {
				c.getResponseFromJSTestAggregationService(facade, {"method" : "getV3APIReport"}, function(data) {
					var javaReport = null;
					
					if (	!data.error && 
							data.result.columns[0].title === "STATUS" && 
							data.result.rows[0][0].value === "SUCCESS") { //Success Case
						javaReport = JSON.parse(data.result.rows[0][1].value);
		 			}
					
					if(javaReport) {
						areClassesCorrect(javaReport, function(testsResults) {
							if(testsResults.error.length > 0) {
								c.fail(getPrintableReport(javaReport, testsResults));
							} else {
								c.ok(getPrintableReport(javaReport, testsResults));
							}
							c.finish();
						});
					} else {
						c.fail("Report Missing");
					}
				});
			}
			
			c.createFacadeAndLogin().then(getV3APIReport);
		});
	}
});