/**
 * @author Jakub Straszewski
 */
define([ "stjs", "dto/sample/id/ISampleId", "dto/dataset/id/IDataSetId", "dto/experiment/id/IExperimentId", "dto/project/id/IProjectId", "dto/space/id/ISpaceId", "dto/material/id/IMaterialId" ],
		function(stjs, ISampleId, IDataSetId, IExperimentId, IProjectId, ISpaceId, IMaterialId) {
			var CreationId = function(creationId) {
				this.creationId = creationId;
			};
			stjs.extend(CreationId, null, [ ISampleId, IDataSetId, IExperimentId, IProjectId, ISpaceId, IMaterialId ], function(constructor, prototype) {
				prototype['@type'] = 'dto.common.id.CreationId';
				constructor.serialVersionUID = 1;
				prototype.creationId = null;
				prototype.getCreationId = function() {
					return this.creationId;
				};
				prototype.setCreationId = function(creationId) {
					this.creationId = creationId;
				};
				prototype.toString = function() {
					return this.getCreationId();
				};
				prototype.hashCode = function() {
					return ((this.getCreationId() == null) ? 0 : this.getCreationId().hashCode());
				};
				prototype.equals = function(obj) {
					if (this == obj) {
						return true;
					}
					if (obj == null) {
						return false;
					}
					if (this.getClass() != obj.getClass()) {
						return false;
					}
					var other = obj;
					if (this.getCreationId() == null) {
						if (other.getCreationId() != null) {
							return false;
						}
					} else if (!this.getCreationId().equals(other.getCreationId())) {
						return false;
					}
					return true;
				};
			}, {});
			return CreationId;
		})