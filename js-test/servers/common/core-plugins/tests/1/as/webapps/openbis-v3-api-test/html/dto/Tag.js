/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var Tag = function() {
	this['@type'] = 'Tag';
};

stjs.extend(Tag, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.fetchOptions = null;
    prototype.permId = null;
    prototype.code = null;
    prototype.description = null;
    prototype.isPrivate = null;
    prototype.registrationDate = null;
    prototype.owner = null;
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getFetchOptions = function() {
        return this.fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setFetchOptions = function(fetchOptions) {
        this.fetchOptions = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getPermId = function() {
        return this.permId;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setPermId = function(permId) {
        this.permId = permId;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getCode = function() {
        return this.code;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setCode = function(code) {
        this.code = code;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getDescription = function() {
        return this.description;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setDescription = function(description) {
        this.description = description;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.isPrivate = function() {
        return this.isPrivate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setPrivate = function(isPrivate) {
        this.isPrivate = isPrivate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getRegistrationDate = function() {
        return this.registrationDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setRegistrationDate = function(registrationDate) {
        this.registrationDate = registrationDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getOwner = function() {
        if (this.getFetchOptions().hasOwner()) {
            return this.owner;
        } else {
             throw new NotFetchedException("Owner has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setOwner = function(owner) {
        this.owner = owner;
    };
}, {fetchOptions: "TagFetchOptions", permId: "TagPermId", registrationDate: "Date", owner: "Person"});
