define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria" ],
		function(require, stjs, AbstractObjectSearchCriteria, CodeSearchCriteria) {
			var PropertyTypeSearchCriteria = function() {
				AbstractObjectSearchCriteria.call(this);
			};
			stjs.extend(PropertyTypeSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
				prototype['@type'] = 'as.dto.property.search.PropertyTypeSearchCriteria';
				constructor.serialVersionUID = 1;
				prototype.withCode = function() {
					var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
					return this.addCriteria(new CodeSearchCriteria());
				};
			}, {});

			return PropertyTypeSearchCriteria;
		})