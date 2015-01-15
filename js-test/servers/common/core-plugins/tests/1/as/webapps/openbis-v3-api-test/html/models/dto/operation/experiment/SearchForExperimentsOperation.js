/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/operation/IOperation"], function (stjs, IOperation) {
    var SearchForExperimentsOperation = function() {};
    stjs.extend(SearchForExperimentsOperation, null, [IOperation], null, {});
    return SearchForExperimentsOperation;
})