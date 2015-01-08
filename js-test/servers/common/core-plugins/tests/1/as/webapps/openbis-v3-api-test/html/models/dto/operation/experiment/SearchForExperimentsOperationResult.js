/**
 *  @author pkupczyk
 */
define(["dto/operation/IOperationResult"], function (IOperationResult) {
    var SearchForExperimentsOperationResult = function() {};
    stjs.extend(SearchForExperimentsOperationResult, null, [IOperationResult], null, {});
    return SearchForExperimentsOperationResult;
})