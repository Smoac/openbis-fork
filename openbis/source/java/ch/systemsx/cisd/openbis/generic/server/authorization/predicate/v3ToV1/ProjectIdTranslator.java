package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectPermIdId;

public class ProjectIdTranslator
{

    public static IProjectId translate(ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId v3projectId)
    {
        if (v3projectId instanceof CreationId)
        {

        } else if (v3projectId instanceof ProjectIdentifier)
        {
            return new ProjectIdentifierId(((ProjectIdentifier) v3projectId).getIdentifier());
        } else if (v3projectId instanceof ProjectPermId)
        {
            return new ProjectPermIdId(((ProjectPermId) v3projectId).getPermId());
        }

        return null;
    }
}
