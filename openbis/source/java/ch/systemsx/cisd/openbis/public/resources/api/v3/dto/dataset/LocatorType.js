/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var LocatorType = function() {
	};
	stjs.extend(LocatorType, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.LocatorType';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.code = null;
		prototype.description = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
	}, {
		fetchOptions : "LocatorTypeFetchOptions"
	});
	return LocatorType;
})