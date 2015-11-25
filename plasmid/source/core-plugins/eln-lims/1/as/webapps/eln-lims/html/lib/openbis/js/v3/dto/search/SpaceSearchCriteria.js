/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/search/AbstractObjectSearchCriteria",
         "dto/search/CodeSearchCriteria", "dto/search/PermIdSearchCriteria", "dto/search/AbstractCompositeSearchCriteria"], 
		function(require, stjs, AbstractObjectSearchCriteria) {
	var SpaceSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(SpaceSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SpaceSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("dto/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("dto/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.createBuilder = function() {
			var AbstractCompositeSearchCriteria = require("dto/search/AbstractCompositeSearchCriteria");
			var builder = AbstractCompositeSearchCriteria.prototype.createBuilder.call(this);
			builder.setName("SPACE");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return SpaceSearchCriteria;
})