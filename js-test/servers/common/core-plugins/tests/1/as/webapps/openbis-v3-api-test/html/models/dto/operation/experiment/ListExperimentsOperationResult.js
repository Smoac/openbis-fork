/**
 *  @author pkupczyk
 */
define(["dto/operation/IOperationResult"], function (IOperationResult) {
    var ListExperimentsOperationResult = function() {};
    stjs.extend(ListExperimentsOperationResult, null, [IOperationResult], null, {});
    return ListExperimentsOperationResult;
})