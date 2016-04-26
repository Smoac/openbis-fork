/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractSearchCriteria" ], function(stjs, AbstractSearchCriteria) {
	var ListableSampleTypeSearchCriteria = function() {
		AbstractSearchCriteria.call(this);
	};
	stjs.extend(ListableSampleTypeSearchCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.search.ListableSampleTypeSearchCriteria';
		constructor.serialVersionUID = 1;
		
		prototype.listable = true;
	    
		prototype.thatEquals = function(value) {
	        this.listable = value;
	    }

		prototype.isListable = function()
	    {
	        return this.listable;
	    }

	}, {});
	return ListableSampleTypeSearchCriteria;
})
