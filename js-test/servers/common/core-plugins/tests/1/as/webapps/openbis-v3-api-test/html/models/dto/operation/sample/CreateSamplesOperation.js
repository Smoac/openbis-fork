/**
 *  @author pkupczyk
 */
define(["dto/operation/IOperation"], function (IOperation) {
    var CreateSamplesOperation = function(creations) {
        this.creations = creations;
    };
    stjs.extend(CreateSamplesOperation, null, [IOperation], function(constructor, prototype) {
        prototype['@type'] = 'CreateSamplesOperation';
        prototype.creations = null;
        prototype.getCreations = function() {
            return this.creations;
        };
    }, {creations: {name: "List", arguments: ["SampleCreation"]}});
    return CreateSamplesOperation;
})