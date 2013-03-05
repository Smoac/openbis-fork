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
