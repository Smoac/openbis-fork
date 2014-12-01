/**
 *  Entity type perm id.
 *  
 *  @author pkupczyk
 */
var EntityTypePermId = function(permId) {
	this['@type'] = 'EntityTypePermId';
	this.permId = permId;
};

stjs.extend(EntityTypePermId, ObjectPermId, [IEntityTypeId], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
}, {});
