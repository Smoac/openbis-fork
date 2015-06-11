/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var SpaceCreation = function() {
	};
	stjs.extend(SpaceCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.space.SpaceCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.description = null;

		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
	}, {});
	return SpaceCreation;
})