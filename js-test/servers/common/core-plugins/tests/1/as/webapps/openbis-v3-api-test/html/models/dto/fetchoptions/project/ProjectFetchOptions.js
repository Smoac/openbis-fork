/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(["support/stjs", 'dto/fetchoptions/person/PersonFetchOptions', 'dto/fetchoptions/space/SpaceFetchOptions'], function (stjs, PersonFetchOptions, SpaceFetchOptions) {
    var ProjectFetchOptions = function() {};
    stjs.extend(ProjectFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'ProjectFetchOptions';
        constructor.serialVersionUID = 1;
        prototype.space = null;
        prototype.registrator = null;
        prototype.modifier = null;
        prototype.withSpace = function() {
            if (this.space == null) {
                this.space = new SpaceFetchOptions();
            }
            return this.space;
        };
        prototype.withSpaceUsing = function(fetchOptions) {
            return this.space = fetchOptions;
        };
        prototype.hasSpace = function() {
            return this.space != null;
        };
        prototype.withRegistrator = function() {
            if (this.registrator == null) {
                this.registrator = new PersonFetchOptions();
            }
            return this.registrator;
        };
        prototype.withRegistratorUsing = function(fetchOptions) {
            return this.registrator = fetchOptions;
        };
        prototype.hasRegistrator = function() {
            return this.registrator != null;
        };
        prototype.withModifier = function() {
            if (this.modifier == null) {
                this.modifier = new PersonFetchOptions();
            }
            return this.modifier;
        };
        prototype.withModifierUsing = function(fetchOptions) {
            return this.modifier = fetchOptions;
        };
        prototype.hasModifier = function() {
            return this.modifier != null;
        };
    }, {space: "SpaceFetchOptions", registrator: "PersonFetchOptions", modifier: "PersonFetchOptions"});
    return ProjectFetchOptions;
})