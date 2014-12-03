/**
 *  Experiment identifier.
 *  
 *  @author pkupczyk
 */
var ExperimentIdentifier = function(identifier) {
	this['@type'] = 'ExperimentIdentifier';
	this.identifier = identifier;
};
stjs.extend(ExperimentIdentifier, ObjectIdentifier, [IExperimentId], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
}, {});
