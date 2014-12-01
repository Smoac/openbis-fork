/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var ProjectFetchOptions = function() {
	this['@type'] = 'ProjectFetchOptions';
};

stjs.extend(ProjectFetchOptions, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.space = null;
    prototype.registrator = null;
    prototype.modifier = null;
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
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withModifier = function() {
        if (this.modifier == null) {
            this.modifier = new PersonFetchOptions();
        }
        return this.modifier;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withModifierUsing = function(fetchOptions) {
        return this.modifier = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasModifier = function() {
        return this.modifier != null;
    };
}, {space: "SpaceFetchOptions", registrator: "PersonFetchOptions", modifier: "PersonFetchOptions"});
