package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1;

import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

public class SpaceIdTranslator
{

    public static SpaceIdentifier translate(ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId v3SpaceId)
    {
        if (v3SpaceId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId)
        {

        } else if (v3SpaceId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId)
        {
            return new SpaceIdentifier(((ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId) v3SpaceId).getPermId());
        }

        return null;
    }
}
