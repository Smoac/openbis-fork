define([ "stjs", "util/Exceptions", "as/dto/common/entity/AbstractEntityPropertyHolder" ], function(stjs, exceptions, AbstractEntityPropertyHolder) {
	var AbstractEntityCreation = function() {
        AbstractEntityPropertyHolder.call(this);
	};
	stjs.extend(AbstractEntityCreation, null, [], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.common.entity.AbstractEntityCreation';
        constructor.serialVersionUID = 1;

        prototype.getProperties = function() {
          return properties;
        };

        prototype.setProperties = function(properties) {
            this.properties = properties;
        };

    }, {});
    return AbstractEntityCreation;
})