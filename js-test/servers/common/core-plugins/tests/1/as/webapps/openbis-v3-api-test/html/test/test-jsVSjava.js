/**
 * 
 */
define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("JS VS JAVA API");
		
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