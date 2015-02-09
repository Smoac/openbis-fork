/**
 *  Experiment identifier.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectIdentifier", "dto/id/experiment/IExperimentId"], function (stjs, ObjectIdentifier, IExperimentId) {
    var ExperimentIdentifier = /**
     *  @param identifier Experiment identifier, e.g. "/MY_SPACE/MY_PROJECT/MY_EXPERIMENT".
     */
    function(identifier) {
        ObjectIdentifier.call(this, identifier);
    };
    stjs.extend(ExperimentIdentifier, ObjectIdentifier, [ObjectIdentifier, IExperimentId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.experiment.ExperimentIdentifier';
        constructor.serialVersionUID = 1;
    }, {});
    return ExperimentIdentifier;
})