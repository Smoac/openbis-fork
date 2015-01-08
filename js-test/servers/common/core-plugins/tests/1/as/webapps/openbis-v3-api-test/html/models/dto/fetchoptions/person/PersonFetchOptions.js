/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([], function () {
    var PersonFetchOptions = function() {};
    stjs.extend(PersonFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'PersonFetchOptions';
        constructor.serialVersionUID = 1;
        prototype.space = null;
        prototype.registrator = null;
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
    }, {space: "SpaceFetchOptions", registrator: "PersonFetchOptions"});
    return PersonFetchOptions;
})