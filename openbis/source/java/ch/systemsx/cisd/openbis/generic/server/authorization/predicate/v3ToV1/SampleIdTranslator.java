package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;

public class SampleIdTranslator
{

    public static ISampleId translate(ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId v3SampleId)
    {
        if (v3SampleId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId)
        {

        } else if (v3SampleId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier)
        {
            return new SampleIdentifierId(((ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier) v3SampleId).getIdentifier());
        } else if (v3SampleId instanceof ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId)
        {
            return new SamplePermIdId(((ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId) v3SampleId).getPermId());
        }

        return null;
    }
}
