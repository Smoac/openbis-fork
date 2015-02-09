/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "support/stjs", "dto/fetchoptions/person/PersonFetchOptions" ], function(stjs) {
	var TagFetchOptions = function() {
	};
	stjs.extend(TagFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.tag.TagFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.owner = null;
		prototype.withOwner = function() {
			if (this.owner == null) {
				this.owner = new PersonFetchOptions();
			}
			return this.owner;
		};
		prototype.withOwnerUsing = function(fetchOptions) {
			return this.owner = fetchOptions;
		};
		prototype.hasOwner = function() {
			return this.owner != null;
		};
	}, {
		owner : "PersonFetchOptions"
	});
	return TagFetchOptions;
})