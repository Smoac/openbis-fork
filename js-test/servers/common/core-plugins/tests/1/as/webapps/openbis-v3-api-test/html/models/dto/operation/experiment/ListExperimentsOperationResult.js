/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/operation/IOperationResult"], function (stjs, IOperationResult) {
    var ListExperimentsOperationResult = function() {};
    stjs.extend(ListExperimentsOperationResult, null, [IOperationResult], null, {});
    return ListExperimentsOperationResult;
})