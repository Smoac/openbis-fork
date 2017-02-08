package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;

public class ProjectIdTranslator {
	
	public static IProjectId translate(ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId v3projectId) {
		if(v3projectId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId) {
			
		} else if(v3projectId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier) {
			return new ProjectIdentifierId(((ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier) v3projectId).getIdentifier());
		} else if(v3projectId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId) {
			return new ProjectPermIdId(((ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId) v3projectId).getPermId());
		}
		
		return null;
	}
}
