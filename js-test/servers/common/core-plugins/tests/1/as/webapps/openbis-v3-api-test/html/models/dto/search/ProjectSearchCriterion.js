/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractObjectSearchCriterion", "dto/search/CodeSearchCriterion", "dto/search/PermIdSearchCriterion", "dto/search/SpaceSearchCriterion",
		"dto/search/AbstractCompositeSearchCriterion" ], function(stjs, AbstractObjectSearchCriterion, CodeSearchCriterion, PermIdSearchCriterion, SpaceSearchCriterion,
		AbstractCompositeSearchCriterion) {
	var ProjectSearchCriterion = function() {
		AbstractObjectSearchCriterion.call(this);
	};
	stjs.extend(ProjectSearchCriterion, AbstractObjectSearchCriterion, [ AbstractObjectSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.ProjectSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			return this.addCriterion(new CodeSearchCriterion());
		};
		prototype.withPermId = function() {
			return this.addCriterion(new PermIdSearchCriterion());
		};
		prototype.withSpace = function() {
			return this.addCriterion(new SpaceSearchCriterion());
		};
		prototype.createBuilder = function() {
			var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
			builder.setName("PROJECT");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return ProjectSearchCriterion;
})