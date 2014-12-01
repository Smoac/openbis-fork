/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var AttachmentFetchOptions = function() {
	this['@type'] = 'AttachmentFetchOptions';
};

stjs.extend(AttachmentFetchOptions, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.registrator = null;
    prototype.previousVersion = null;
    prototype.content = null;
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withRegistrator = function() {
        if (this.registrator == null) {
            this.registrator = new PersonFetchOptions();
        }
        return this.registrator;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withRegistratorUsing = function(fetchOptions) {
        return this.registrator = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasRegistrator = function() {
        return this.registrator != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withPreviousVersion = function() {
        if (this.previousVersion == null) {
            this.previousVersion = new AttachmentFetchOptions();
        }
        return this.previousVersion;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withPreviousVersionUsing = function(fetchOptions) {
        return this.previousVersion = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasPreviousVersion = function() {
        return this.previousVersion != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withContent = function() {
        if (this.content == null) {
            this.content = new EmptyFetchOptions();
        }
        return this.content;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withContentUsing = function(fetchOptions) {
        return this.content = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasContent = function() {
        return this.content != null;
    };
}, {registrator: "PersonFetchOptions", previousVersion: "AttachmentFetchOptions", content: "EmptyFetchOptions"});