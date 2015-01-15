/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/operation/IOperation"], function (stjs, IOperation) {
    var UpdateExperimentsOperation = function() {};
    stjs.extend(UpdateExperimentsOperation, null, [IOperation], null, {});
    return UpdateExperimentsOperation;
})