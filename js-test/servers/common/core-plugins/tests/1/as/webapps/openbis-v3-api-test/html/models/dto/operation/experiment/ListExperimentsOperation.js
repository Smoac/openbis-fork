/**
 *  @author pkupczyk
 */
define(["dto/operation/IOperation"], function (IOperation) {
    var ListExperimentsOperation = function() {};
    stjs.extend(ListExperimentsOperation, null, [IOperation], null, {});
    return ListExperimentsOperation;
})