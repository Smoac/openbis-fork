/**
 * 
 */
define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("JS VS JAVA API");
		
//		var packageNameHandlers = {
//		}
//		
//		var jsonObjectAnnotationHandlers = {
//		
//		}
//		
//		var standardHandler = function(javaDef, jsDef) {
//			
//		}
		
		var areClassesCorrect = function(report) {
			for(var ridx = 0; ridx < report.entries.length; ridx++) {
				var javaClassReport = report.entries[ridx];
				var javaClassName = javaClassReport.name;
				var jsClassName = javaClassReport.jsonObjAnnotation;
				if(jsClassName) {
					var errorHandler = function(javaClassName) {
						return function() {
							console.info("Java class with jsonObjectAnnotation missing in Javascript: " + javaClassName);
						};
					};
					var requireJsPath = jsClassName.replace(/\./g,'/');
					require([requireJsPath], function(myclass){
						var test = "break";
					}, errorHandler(javaClassName));
				} else {
					console.info("Java class missing jsonObjectAnnotation: " + javaClassName);
				}
				
			}
			return true;
		}
		
		QUnit.test("get Java report from aggregation service", function(assert) {
			var c = new common(assert);
			c.start();
			
			var getV3APIReport = function(facade) {
				c.getResponseFromJSTestAggregationService(facade, {"method" : "getV3APIReport"}, function(data) {
					var report = null;
					
					if (	!data.error && 
							data.result.columns[0].title === "STATUS" && 
							data.result.rows[0][0].value === "SUCCESS") { //Success Case
		 				report = JSON.parse(data.result.rows[0][1].value);
		 			}
					
					if(report) {
						areClassesCorrect(report);
						c.ok("Report received");
					} else {
						c.fail("Report Missing");
					}
					
					c.finish();
				});
			}
			
			c.createFacadeAndLogin().then(getV3APIReport);
		});
	}
});