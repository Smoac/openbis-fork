/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var SearchResult = function() {
		this.objects = [];
		this.totalCount = 0;
	};
	stjs.extend(SearchResult, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.SearchResult';
		constructor.serialVersionUID = 1;
		prototype.getObjects = function() {
			return this.objects;
		};
		prototype.getTotalCount = function() {
			return this.totalCount;
		}
	}, {
		objects : {
			name : "Collection",
			arguments : [ "Object" ]
		}
	});
	return SearchResult;
})
