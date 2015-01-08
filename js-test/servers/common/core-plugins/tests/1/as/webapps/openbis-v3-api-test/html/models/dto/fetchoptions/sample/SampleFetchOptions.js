/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([        
        'dto/fetchoptions/sample/SampleTypeFetchOptions',
        'dto/fetchoptions/space/SpaceFetchOptions',
        'dto/fetchoptions/experiment/ExperimentFetchOptions',
        'dto/fetchoptions/property/PropertyFetchOptions',
        'dto/fetchoptions/sample/SampleFetchOptions',
        'dto/fetchoptions/tag/TagFetchOptions',
        'dto/fetchoptions/person/PersonFetchOptions',
        'dto/fetchoptions/attachment/AttachmentFetchOptions'], 
        function (
        		SampleTypeFetchOptions, 
        		SpaceFetchOptions, 
        		ExperimentFetchOptions, 
        		PropertyFetchOptions, 
        		SampleFetchOptions, 
        		TagFetchOptions, 
        		PersonFetchOptions, 
        		AttachmentFetchOptions) {
    var SampleFetchOptions = function() {};
    stjs.extend(SampleFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'SampleFetchOptions';
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
        prototype.withType = function() {
            if (this.type == null) {
                this.type = new SampleTypeFetchOptions();
            }
            return this.type;
        };
        prototype.withTypeUsing = function(fetchOptions) {
            return this.type = fetchOptions;
        };
        prototype.hasType = function() {
            return this.type != null;
        };
        prototype.withSpace = function() {
            if (this.space == null) {
                this.space = new SpaceFetchOptions();
            }
            return this.space;
        };
        prototype.withSpaceUsing = function(fetchOptions) {
            return this.space = fetchOptions;
        };
        prototype.hasSpace = function() {
            return this.space != null;
        };
        prototype.withExperiment = function() {
            if (this.experiment == null) {
                this.experiment = new ExperimentFetchOptions();
            }
            return this.experiment;
        };
        prototype.withExperimentUsing = function(fetchOptions) {
            return this.experiment = fetchOptions;
        };
        prototype.hasExperiment = function() {
            return this.experiment != null;
        };
        prototype.withProperties = function() {
            if (this.properties == null) {
                this.properties = new PropertyFetchOptions();
            }
            return this.properties;
        };
        prototype.withPropertiesUsing = function(fetchOptions) {
            return this.properties = fetchOptions;
        };
        prototype.hasProperties = function() {
            return this.properties != null;
        };
        prototype.withParents = function() {
            if (this.parents == null) {
                this.parents = new SampleFetchOptions();
            }
            return this.parents;
        };
        prototype.withParentsUsing = function(fetchOptions) {
            return this.parents = fetchOptions;
        };
        prototype.hasParents = function() {
            return this.parents != null;
        };
        prototype.withChildren = function() {
            if (this.children == null) {
                this.children = new SampleFetchOptions();
            }
            return this.children;
        };
        prototype.withChildrenUsing = function(fetchOptions) {
            return this.children = fetchOptions;
        };
        prototype.hasChildren = function() {
            return this.children != null;
        };
        prototype.withContainer = function() {
            if (this.container == null) {
                this.container = new SampleFetchOptions();
            }
            return this.container;
        };
        prototype.withContainerUsing = function(fetchOptions) {
            return this.container = fetchOptions;
        };
        prototype.hasContainer = function() {
            return this.container != null;
        };
        prototype.withContained = function() {
            if (this.contained == null) {
                this.contained = new SampleFetchOptions();
            }
            return this.contained;
        };
        prototype.withContainedUsing = function(fetchOptions) {
            return this.contained = fetchOptions;
        };
        prototype.hasContained = function() {
            return this.contained != null;
        };
        prototype.withTags = function() {
            if (this.tags == null) {
                this.tags = new TagFetchOptions();
            }
            return this.tags;
        };
        prototype.withTagsUsing = function(fetchOptions) {
            return this.tags = fetchOptions;
        };
        prototype.hasTags = function() {
            return this.tags != null;
        };
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
        prototype.withModifier = function() {
            if (this.modifier == null) {
                this.modifier = new PersonFetchOptions();
            }
            return this.modifier;
        };
        prototype.withModifierUsing = function(fetchOptions) {
            return this.modifier = fetchOptions;
        };
        prototype.hasModifier = function() {
            return this.modifier != null;
        };
        prototype.withAttachments = function() {
            if (this.attachments == null) {
                this.attachments = new AttachmentFetchOptions();
            }
            return this.attachments;
        };
        prototype.withAttachmentsUsing = function(fetchOptions) {
            return this.attachments = fetchOptions;
        };
        prototype.hasAttachments = function() {
            return this.attachments != null;
        };
    }, {type: "SampleTypeFetchOptions", space: "SpaceFetchOptions", experiment: "ExperimentFetchOptions", properties: "PropertyFetchOptions", parents: "SampleFetchOptions", children: "SampleFetchOptions", container: "SampleFetchOptions", contained: "SampleFetchOptions", tags: "TagFetchOptions", registrator: "PersonFetchOptions", modifier: "PersonFetchOptions", attachments: "AttachmentFetchOptions"});
    return SampleFetchOptions;
})