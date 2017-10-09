define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria", "as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria" ],
		function(require, stjs, AbstractObjectSearchCriteria, CodeSearchCriteria, SemanticAnnotationSearchCriteria) {
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
				prototype.withSemanticAnnotations = function() {
					var SemanticAnnotationSearchCriteria = require("as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria");
					return this.addCriteria(new SemanticAnnotationSearchCriteria());
				};
			}, {});

			return PropertyTypeSearchCriteria;
		})