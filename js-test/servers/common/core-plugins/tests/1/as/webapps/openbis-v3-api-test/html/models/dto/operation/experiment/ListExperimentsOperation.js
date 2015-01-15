/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/operation/IOperation"], function (stjs, IOperation) {
    var ListExperimentsOperation = function() {};
    stjs.extend(ListExperimentsOperation, null, [IOperation], null, {});
    return ListExperimentsOperation;
})