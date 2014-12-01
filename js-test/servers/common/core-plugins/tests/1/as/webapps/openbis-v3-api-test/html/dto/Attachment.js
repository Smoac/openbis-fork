/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var Attachment = function() {
	this['@type'] = 'Attachment';
};

stjs.extend(Attachment, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.fetchOptions = null;
    prototype.fileName = null;
    prototype.title = null;
    prototype.description = null;
    prototype.permlink = null;
    prototype.latestVersionPermlink = null;
    prototype.version = null;
    prototype.registrationDate = null;
    prototype.registrator = null;
    prototype.previousVersion = null;
    prototype.content = null;
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
    prototype.getFileName = function() {
        return this.fileName;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setFileName = function(fileName) {
        this.fileName = fileName;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getTitle = function() {
        return this.title;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setTitle = function(title) {
        this.title = title;
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
    prototype.getPermlink = function() {
        return this.permlink;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setPermlink = function(permlink) {
        this.permlink = permlink;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getLatestVersionPermlink = function() {
        return this.latestVersionPermlink;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setLatestVersionPermlink = function(latestVersionPermlink) {
        this.latestVersionPermlink = latestVersionPermlink;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getVersion = function() {
        return this.version;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setVersion = function(version) {
        this.version = version;
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
    prototype.getPreviousVersion = function() {
        if (this.getFetchOptions().hasPreviousVersion()) {
            return this.previousVersion;
        } else {
             throw new NotFetchedException("Previous version of attachment  has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setPreviousVersion = function(previousVersion) {
        this.previousVersion = previousVersion;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getContent = function() {
        if (this.getFetchOptions().hasContent()) {
            return this.content;
        } else {
             throw new NotFetchedException("Content has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setContent = function(content) {
        this.content = content;
    };
    prototype.toString = function() {
        return "Attachment " + this.fileName + ":" + this.version;
    };
}, {fetchOptions: "AttachmentFetchOptions", registrationDate: "Date", registrator: "Person", previousVersion: "Attachment", content: "byte[]"});
