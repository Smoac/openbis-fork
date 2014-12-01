/**
 *  Project identifier.
 *  
 *  @author pkupczyk
 */
var ProjectIdentifier = function(identifier) {
	this['@type'] = 'ProjectIdentifier';
	this.identifier = identifier;
};

stjs.extend(ProjectIdentifier, ObjectIdentifier, [IProjectId], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
}, {});
