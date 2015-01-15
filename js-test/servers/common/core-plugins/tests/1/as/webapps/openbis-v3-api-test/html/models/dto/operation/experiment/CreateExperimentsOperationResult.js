/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/operation/IOperationResult"], function (stjs, IOperationResult) {
    var CreateExperimentsOperationResult = function() {};
    stjs.extend(CreateExperimentsOperationResult, null, [IOperationResult], function(constructor, prototype) {
        prototype['@type'] = 'CreateExperimentsOperationResult';
        prototype.newExperimentIds = null;
    }, {newExperimentIds: {name: "List", arguments: ["ExperimentPermId"]}});
    return CreateExperimentsOperationResult;
})