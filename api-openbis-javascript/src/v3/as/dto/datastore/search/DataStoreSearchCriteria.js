/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria",
	"as/dto/datastore/search/DataStoreKindSearchCriteria", "as/dto/datastore/search/DataStoreKind"],
	function(require, stjs, AbstractObjectSearchCriteria, DataStoreKindSearchCriteria, DataStoreKind) {
	var DataStoreSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(DataStoreSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.datastore.search.DataStoreSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withCodes = function() {
			var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
			return this.addCriteria(new CodesSearchCriteria());
		};
		prototype.withKind = function() {
			var DataStoreKindSearchCriteria = require("as/dto/datastore/search/DataStoreKindSearchCriteria");
			this.criteria = this.criteria.filter(criterion => !(criterion instanceof DataStoreKindSearchCriteria));
			return this.addCriteria(new DataStoreKindSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("as/dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withSubcriteria = function(subcriteria) {
			return this.addCriteria(subcriteria);
		}
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return DataStoreSearchCriteria;
})