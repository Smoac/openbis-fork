/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var PersonFetchOptions = function() {
	this['@type'] = 'PersonFetchOptions';
};

stjs.extend(PersonFetchOptions, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.space = null;
    prototype.registrator = null;
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withSpace = function() {
        if (this.space == null) {
            this.space = new SpaceFetchOptions();
        }
        return this.space;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withSpaceUsing = function(fetchOptions) {
        return this.space = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasSpace = function() {
        return this.space != null;
    };
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
}, {space: "SpaceFetchOptions", registrator: "PersonFetchOptions"});
