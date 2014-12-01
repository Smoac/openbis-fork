/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var DataSetFetchOptions = function() {
	this['@type'] = 'DataSetFetchOptions';
};

stjs.extend(DataSetFetchOptions, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.parents = null;
    prototype.children = null;
    prototype.containers = null;
    prototype.contained = null;
    prototype.tags = null;
    prototype.type = null;
    prototype.modifier = null;
    prototype.registrator = null;
    prototype.experiment = null;
    prototype.properties = null;
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withParents = function() {
        if (this.parents == null) {
            this.parents = new DataSetFetchOptions();
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
            this.children = new DataSetFetchOptions();
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
    prototype.withContainers = function() {
        if (this.containers == null) {
            this.containers = new DataSetFetchOptions();
        }
        return this.containers;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withContainersUsing = function(fetchOptions) {
        return this.containers = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasContainers = function() {
        return this.containers != null;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withContained = function() {
        if (this.contained == null) {
            this.contained = new DataSetFetchOptions();
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
    prototype.withType = function() {
        if (this.type == null) {
            this.type = new DataSetTypeFetchOptions();
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
}, {parents: "DataSetFetchOptions", children: "DataSetFetchOptions", containers: "DataSetFetchOptions", contained: "DataSetFetchOptions", tags: "TagFetchOptions", type: "DataSetTypeFetchOptions", modifier: "PersonFetchOptions", registrator: "PersonFetchOptions", experiment: "ExperimentFetchOptions", properties: "PropertyFetchOptions"});
