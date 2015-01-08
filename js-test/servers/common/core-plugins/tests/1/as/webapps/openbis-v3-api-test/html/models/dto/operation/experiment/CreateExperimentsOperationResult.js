/**
 *  @author pkupczyk
 */
define(["dto/operation/IOperationResult"], function (IOperationResult) {
    var CreateExperimentsOperationResult = function() {};
    stjs.extend(CreateExperimentsOperationResult, null, [IOperationResult], function(constructor, prototype) {
        prototype['@type'] = 'CreateExperimentsOperationResult';
        prototype.newExperimentIds = null;
    }, {newExperimentIds: {name: "List", arguments: ["ExperimentPermId"]}});
    return CreateExperimentsOperationResult;
})