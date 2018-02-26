define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/IdsSearchCriteria", 
         "as/dto/plugin/search/NameSearchCriteria" ],
		function(require, stjs, AbstractObjectSearchCriteria, CodeSearchCriteria, SemanticAnnotationSearchCriteria) {
			var PluginSearchCriteria = function() {
				AbstractObjectSearchCriteria.call(this);
			};
			stjs.extend(PluginSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
				prototype['@type'] = 'as.dto.plugin.search.PluginSearchCriteria';
				constructor.serialVersionUID = 1;
				prototype.withIds = function() {
					var IdsSearchCriteria = require("as/dto/common/search/IdsSearchCriteria");
					return this.addCriteria(new IdsSearchCriteria());
				};
				prototype.withName = function() {
					var NameSearchCriteria = require("as/dto/plugin/search/NameSearchCriteria");
					return this.addCriteria(new NameSearchCriteria());
				};
			}, {});

			return PluginSearchCriteria;
		})
		