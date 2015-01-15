/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(["support/stjs"], function (stjs) {
    var SpaceFetchOptions = function() {};
    stjs.extend(SpaceFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'SpaceFetchOptions';
        constructor.serialVersionUID = 1;
        prototype.registrator = null;
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
    }, {registrator: "PersonFetchOptions"});
    return SpaceFetchOptions;
})