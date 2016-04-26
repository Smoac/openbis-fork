/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/entitytype/search/EntityTypeSearchCriteria", "as/dto/entitytype/search/ListableSampleTypeSearchCriteria" ], 
	function(require, stjs, EntityTypeSearchCriteria) {
		var SampleTypeSearchCriteria = function() {
			EntityTypeSearchCriteria.call(this);
		};
		stjs.extend(SampleTypeSearchCriteria, EntityTypeSearchCriteria, [ EntityTypeSearchCriteria ], function(constructor, prototype) {
			prototype['@type'] = 'as.dto.entitytype.search.SampleTypeSearchCriteria';
			constructor.serialVersionUID = 1;
			prototype.withListable = function() {
				var ListableSampleTypeSearchCriteria = require("as/dto/entitytype/search/ListableSampleTypeSearchCriteria");
				return this.addCriteria(new ListableSampleTypeSearchCriteria());
			};
		}, {});
		
	return SampleTypeSearchCriteria;
})