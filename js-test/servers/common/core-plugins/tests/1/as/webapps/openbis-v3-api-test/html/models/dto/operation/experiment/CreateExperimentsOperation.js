/**
 *  @author pkupczyk
 */
define(["dto/operation/IOperation"], function (IOperation) {
    var CreateExperimentsOperation = function() {};
    stjs.extend(CreateExperimentsOperation, null, [IOperation], function(constructor, prototype) {
        prototype['@type'] = 'CreateExperimentsOperation';
        prototype.newExperiments = null;
    }, {newExperiments: {name: "List", arguments: ["ExperimentCreation"]}});
    return CreateExperimentsOperation;
})