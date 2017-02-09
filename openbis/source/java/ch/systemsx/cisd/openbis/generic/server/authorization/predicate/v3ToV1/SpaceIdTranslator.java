package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

public class SpaceIdTranslator
{

    public static SpaceIdentifier translate(ISpaceId v3SpaceId)
    {
        if (v3SpaceId instanceof CreationId)
        {

        } else if (v3SpaceId instanceof SpacePermId)
        {
            return new SpaceIdentifier(((SpacePermId) v3SpaceId).getPermId());
        }

        return new SpaceIdentifier(null);
    }
}
