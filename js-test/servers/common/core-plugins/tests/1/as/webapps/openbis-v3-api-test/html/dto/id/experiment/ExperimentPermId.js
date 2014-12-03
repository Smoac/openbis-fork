/**
 *  Experiment perm id.
 *  
 *  @author pkupczyk
 */
var ExperimentPermId = function(permId) {
	this['@type'] = 'ExperimentPermId';
	this.permId = permId;
};
stjs.extend(ExperimentPermId, ObjectPermId, [IExperimentId], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
}, {});
