/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var ExperimentFetchOptions = function() {
	this['@type'] = 'ExperimentFetchOptions';
};

stjs.extend(ExperimentFetchOptions, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.type = null;
    prototype.project = null;
    prototype.properties = null;
    prototype.tags = null;
    prototype.registrator = null;
    prototype.modifier = null;
    prototype.attachments = null;
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withType = function() {
        if (this.type == null) {
            this.type = new ExperimentTypeFetchOptions();
        }
        return this.type;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withTypeUsing = function(fetchOptions) {
        return this.type = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasType = function() {
        return this.type != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withProject = function() {
        if (this.project == null) {
            this.project = new ProjectFetchOptions();
        }
        return this.project;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withProjectUsing = function(fetchOptions) {
        return this.project = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasProject = function() {
        return this.project != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withProperties = function() {
        if (this.properties == null) {
            this.properties = new PropertyFetchOptions();
        }
        return this.properties;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withPropertiesUsing = function(fetchOptions) {
        return this.properties = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasProperties = function() {
        return this.properties != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withTags = function() {
        if (this.tags == null) {
            this.tags = new TagFetchOptions();
        }
        return this.tags;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withTagsUsing = function(fetchOptions) {
        return this.tags = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasTags = function() {
        return this.tags != null;
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
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withAttachments = function() {
        if (this.attachments == null) {
            this.attachments = new AttachmentFetchOptions();
        }
        return this.attachments;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withAttachmentsUsing = function(fetchOptions) {
        return this.attachments = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasAttachments = function() {
        return this.attachments != null;
    };
}, {type: "ExperimentTypeFetchOptions", project: "ProjectFetchOptions", properties: "PropertyFetchOptions", tags: "TagFetchOptions", registrator: "PersonFetchOptions", modifier: "PersonFetchOptions", attachments: "AttachmentFetchOptions"});