define([ "stjs", "dto/fetchoptions/CacheMode" ], function(stjs, CacheMode) {
	var FetchOptions = function() {
		this.count = null;
		this.from = null;
		this.cacheMode = CacheMode.NO_CACHE;
	};
	stjs.extend(FetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.FetchOptions';
		constructor.serialVersionUID = 1;
		prototype.count = function(count) {
			this.count = count;
			return this;
		};
		prototype.getCount = function() {
			return this.count;
		};
		prototype.from = function(from) {
			this.from = from;
			return this;
		};
		prototype.getFrom = function() {
			return this.from;
		};
		prototype.cacheMode = function(cacheMode) {
			this.cacheMode = cacheMode;
			return this;
		};
		prototype.getCacheMode = function() {
			return this.cacheMode;
		}
	}, {});
	return FetchOptions;
})