/**
 *  @author pkupczyk
 */
define(["dto/id/ObjectTechId", "dto/id/deletion/IDeletionId"], function (ObjectTechId, IDeletionId) {
    var DeletionTechId = function(techId) {
        ObjectTechId.call(this, techId);
    };
    stjs.extend(DeletionTechId, ObjectTechId, [ObjectTechId, IDeletionId], function(constructor, prototype) {
        prototype['@type'] = 'DeletionTechId';
        constructor.serialVersionUID = 1;
    }, {});
    return DeletionTechId;
})