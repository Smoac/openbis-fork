/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var TagFetchOptions = function() {
	this['@type'] = 'TagFetchOptions';
};

stjs.extend(TagFetchOptions, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.owner = null;
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withOwner = function() {
        if (this.owner == null) {
            this.owner = new PersonFetchOptions();
        }
        return this.owner;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.withOwnerUsing = function(fetchOptions) {
        return this.owner = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.hasOwner = function() {
        return this.owner != null;
    };
}, {owner: "PersonFetchOptions"});
