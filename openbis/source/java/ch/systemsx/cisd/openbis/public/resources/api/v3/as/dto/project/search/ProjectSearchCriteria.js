/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/common/search/CodeSearchCriteria",
		"as/dto/common/search/PermIdSearchCriteria", "as/dto/space/search/SpaceSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var ProjectSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(ProjectSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.search.ProjectSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("as/dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withSpace = function() {
			var SpaceSearchCriteria = require("as/dto/space/search/SpaceSearchCriteria");
			return this.addCriteria(new SpaceSearchCriteria());
		};
		prototype.createBuilder = function() {
			var AbstractCompositeSearchCriteria = require("as/dto/common/search/AbstractCompositeSearchCriteria");
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