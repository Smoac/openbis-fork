/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(["support/stjs"], function (stjs) {
    var AttachmentFetchOptions = function() {};
    stjs.extend(AttachmentFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'AttachmentFetchOptions';
        constructor.serialVersionUID = 1;
        prototype.registrator = null;
        prototype.previousVersion = null;
        prototype.content = null;
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
        prototype.withPreviousVersion = function() {
            if (this.previousVersion == null) {
                this.previousVersion = new AttachmentFetchOptions();
            }
            return this.previousVersion;
        };
        prototype.withPreviousVersionUsing = function(fetchOptions) {
            return this.previousVersion = fetchOptions;
        };
        prototype.hasPreviousVersion = function() {
            return this.previousVersion != null;
        };
        prototype.withContent = function() {
            if (this.content == null) {
                this.content = new EmptyFetchOptions();
            }
            return this.content;
        };
        prototype.withContentUsing = function(fetchOptions) {
            return this.content = fetchOptions;
        };
        prototype.hasContent = function() {
            return this.content != null;
        };
    }, {registrator: "PersonFetchOptions", previousVersion: "AttachmentFetchOptions", content: "EmptyFetchOptions"});
    return AttachmentFetchOptions;
})