/**
 *  @author pkupczyk
 */
define(["dto/search/ISearchCriterion"], function (ISearchCriterion) {
    var AbstractSearchCriterion = function() {};
    stjs.extend(AbstractSearchCriterion, null, [ISearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'AbstractSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {});
    return AbstractSearchCriterion;
})