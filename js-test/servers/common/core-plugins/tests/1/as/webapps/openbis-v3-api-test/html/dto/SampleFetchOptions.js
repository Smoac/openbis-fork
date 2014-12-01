/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var SampleFetchOptions = function() {
	this['@type'] = 'SampleFetchOptions';
};

stjs.extend(SampleFetchOptions, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.type = null;
    prototype.space = null;
    prototype.experiment = null;
    prototype.properties = null;
    prototype.parents = null;
    prototype.children = null;
    prototype.container = null;
    prototype.contained = null;
    prototype.tags = null;
    prototype.registrator = null;
    prototype.modifier = null;
    prototype.attachments = null;
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withType = function() {
        if (this.type == null) {
            this.type = new SampleTypeFetchOptions();
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
    prototype.withExperiment = function() {
        if (this.experiment == null) {
            this.experiment = new ExperimentFetchOptions();
        }
        return this.experiment;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withExperimentUsing = function(fetchOptions) {
        return this.experiment = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasExperiment = function() {
        return this.experiment != null;
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
    prototype.withParents = function() {
        if (this.parents == null) {
            this.parents = new SampleFetchOptions();
        }
        return this.parents;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withParentsUsing = function(fetchOptions) {
        return this.parents = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasParents = function() {
        return this.parents != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withChildren = function() {
        if (this.children == null) {
            this.children = new SampleFetchOptions();
        }
        return this.children;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withChildrenUsing = function(fetchOptions) {
        return this.children = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasChildren = function() {
        return this.children != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withContainer = function() {
        if (this.container == null) {
            this.container = new SampleFetchOptions();
        }
        return this.container;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withContainerUsing = function(fetchOptions) {
        return this.container = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasContainer = function() {
        return this.container != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withContained = function() {
        if (this.contained == null) {
            this.contained = new SampleFetchOptions();
        }
        return this.contained;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withContainedUsing = function(fetchOptions) {
        return this.contained = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasContained = function() {
        return this.contained != null;
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
}, {type: "SampleTypeFetchOptions", space: "SpaceFetchOptions", experiment: "ExperimentFetchOptions", properties: "PropertyFetchOptions", parents: "SampleFetchOptions", children: "SampleFetchOptions", container: "SampleFetchOptions", contained: "SampleFetchOptions", tags: "TagFetchOptions", registrator: "PersonFetchOptions", modifier: "PersonFetchOptions", attachments: "AttachmentFetchOptions"});
