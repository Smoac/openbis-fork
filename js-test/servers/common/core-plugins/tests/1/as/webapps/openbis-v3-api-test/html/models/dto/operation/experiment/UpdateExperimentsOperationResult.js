/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/operation/IOperationResult"], function (stjs, IOperationResult) {
    var UpdateExperimentsOperationResult = function() {};
    stjs.extend(UpdateExperimentsOperationResult, null, [IOperationResult], null, {});
    return UpdateExperimentsOperationResult;
})