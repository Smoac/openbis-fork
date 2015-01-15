/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/operation/IOperationResult"], function (stjs, IOperationResult) {
    var SearchForExperimentsOperationResult = function() {};
    stjs.extend(SearchForExperimentsOperationResult, null, [IOperationResult], null, {});
    return SearchForExperimentsOperationResult;
})