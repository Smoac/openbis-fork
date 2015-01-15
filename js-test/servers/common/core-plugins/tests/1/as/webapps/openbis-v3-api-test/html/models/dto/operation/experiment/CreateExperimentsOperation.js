/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/operation/IOperation"], function (stjs, IOperation) {
    var CreateExperimentsOperation = function() {};
    stjs.extend(CreateExperimentsOperation, null, [IOperation], function(constructor, prototype) {
        prototype['@type'] = 'CreateExperimentsOperation';
        prototype.newExperiments = null;
    }, {newExperiments: {name: "List", arguments: ["ExperimentCreation"]}});
    return CreateExperimentsOperation;
})