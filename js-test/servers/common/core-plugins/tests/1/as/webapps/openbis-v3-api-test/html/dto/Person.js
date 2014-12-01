/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var Person = function() {
	this['@type'] = 'Person';
};

stjs.extend(Person, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.fetchOptions = null;
    prototype.userId = null;
    prototype.firstName = null;
    prototype.lastName = null;
    prototype.email = null;
    prototype.registrationDate = null;
    prototype.active = null;
    prototype.space = null;
    prototype.registrator = null;
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
    prototype.getUserId = function() {
        return this.userId;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setUserId = function(userId) {
        this.userId = userId;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getFirstName = function() {
        return this.firstName;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setFirstName = function(firstName) {
        this.firstName = firstName;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getLastName = function() {
        return this.lastName;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setLastName = function(lastName) {
        this.lastName = lastName;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getEmail = function() {
        return this.email;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setEmail = function(email) {
        this.email = email;
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
    prototype.isActive = function() {
        return this.active;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setActive = function(active) {
        this.active = active;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getSpace = function() {
        if (this.getFetchOptions().hasSpace()) {
            return this.space;
        } else {
             throw new NotFetchedException("Space has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setSpace = function(space) {
        this.space = space;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getRegistrator = function() {
        if (this.getFetchOptions().hasRegistrator()) {
            return this.registrator;
        } else {
             throw new NotFetchedException("Registrator has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setRegistrator = function(registrator) {
        this.registrator = registrator;
    };
}, {fetchOptions: "PersonFetchOptions", registrationDate: "Date", space: "Space", registrator: "Person"});
