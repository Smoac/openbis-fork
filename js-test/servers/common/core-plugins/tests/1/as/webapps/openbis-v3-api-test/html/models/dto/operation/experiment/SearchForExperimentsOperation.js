/**
 *  @author pkupczyk
 */
define(["dto/operation/IOperation"], function (IOperation) {
    var SearchForExperimentsOperation = function() {};
    stjs.extend(SearchForExperimentsOperation, null, [IOperation], null, {});
    return SearchForExperimentsOperation;
})