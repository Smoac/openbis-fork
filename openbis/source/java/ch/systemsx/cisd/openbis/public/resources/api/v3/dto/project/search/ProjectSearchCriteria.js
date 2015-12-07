/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/common/search/AbstractObjectSearchCriteria", "dto/common/search/AbstractCompositeSearchCriteria", "dto/common/search/CodeSearchCriteria",
		"dto/common/search/PermIdSearchCriteria", "dto/space/search/SpaceSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var ProjectSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(ProjectSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.project.search.ProjectSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withSpace = function() {
			var SpaceSearchCriteria = require("dto/space/search/SpaceSearchCriteria");
			return this.addCriteria(new SpaceSearchCriteria());
		};
		prototype.createBuilder = function() {
			var AbstractCompositeSearchCriteria = require("dto/common/search/AbstractCompositeSearchCriteria");
			var builder = AbstractCompositeSearchCriteria.prototype.createBuilder.call(this);
			builder.setName("PROJECT");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return ProjectSearchCriteria;
})