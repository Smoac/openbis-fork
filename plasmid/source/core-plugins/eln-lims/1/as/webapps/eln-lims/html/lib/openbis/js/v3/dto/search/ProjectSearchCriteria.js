/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractObjectSearchCriteria", "dto/search/CodeSearchCriteria", "dto/search/PermIdSearchCriteria", "dto/search/SpaceSearchCriteria",
		"dto/search/AbstractCompositeSearchCriteria" ], function(stjs, AbstractObjectSearchCriteria, CodeSearchCriteria, PermIdSearchCriteria, SpaceSearchCriteria,
		AbstractCompositeSearchCriteria) {
	var ProjectSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(ProjectSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.ProjectSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withPermId = function() {
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withSpace = function() {
			return this.addCriteria(new SpaceSearchCriteria());
		};
		prototype.createBuilder = function() {
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