define([ "require", "stjs", "dto/common/search/AbstractObjectSearchCriteria"], 
    function(require, stjs, AbstractObjectSearchCriteria) {
    var ServiceSearchCriteria = function() {
        AbstractObjectSearchCriteria.call(this);
    };
    stjs.extend(DeletionSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
        prototype['@type'] = 'dto.deletion.search.DeletionSearchCriteria';
        constructor.serialVersionUID = 1;
        prototype.createBuilder = function() {
            var AbstractCompositeSearchCriteria = require("dto/common/search/AbstractCompositeSearchCriteria");
            var builder = AbstractCompositeSearchCriteria.prototype.createBuilder.call(this);
            builder.setName("DELETION");
            return builder;
        };
    }, {
        criteria : {
            name : "Collection",
            arguments : [ "ISearchCriteria" ]
        }
    });
    return ServiceSearchCriteria;
})