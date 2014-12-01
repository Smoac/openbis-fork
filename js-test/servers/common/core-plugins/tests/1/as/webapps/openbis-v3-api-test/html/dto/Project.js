/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var Project = function() {
	this['@type'] = 'Project';
};

stjs.extend(Project, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.fetchOptions = null;
    prototype.permId = null;
    prototype.identifier = null;
    prototype.code = null;
    prototype.description = null;
    prototype.registrationDate = null;
    prototype.modificationDate = null;
    prototype.space = null;
    prototype.registrator = null;
    prototype.modifier = null;
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
    prototype.getIdentifier = function() {
        return this.identifier;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setIdentifier = function(identifier) {
        this.identifier = identifier;
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
    prototype.getModificationDate = function() {
        return this.modificationDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setModificationDate = function(modificationDate) {
        this.modificationDate = modificationDate;
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
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getModifier = function() {
        if (this.getFetchOptions().hasModifier()) {
            return this.modifier;
        } else {
             throw new NotFetchedException("Modifier has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setModifier = function(modifier) {
        this.modifier = modifier;
    };
}, {fetchOptions: "ProjectFetchOptions", permId: "ProjectPermId", identifier: "ProjectIdentifier", registrationDate: "Date", modificationDate: "Date", space: "Space", registrator: "Person", modifier: "Person"});
