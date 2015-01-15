/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractObjectSearchCriterion) {
    var ProjectSearchCriterion = function() {
        AbstractObjectSearchCriterion.call(this);
    };
    stjs.extend(ProjectSearchCriterion, AbstractObjectSearchCriterion, [AbstractObjectSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'ProjectSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.withCode = function() {
            return this.with(new CodeSearchCriterion());
        };
        prototype.withPermId = function() {
            return this.with(new PermIdSearchCriterion());
        };
        prototype.withSpace = function() {
            return this.with(new SpaceSearchCriterion());
        };
        prototype.createBuilder = function() {
            var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
            builder.setName("PROJECT");
            return builder;
        };
    }, {criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return ProjectSearchCriterion;
})