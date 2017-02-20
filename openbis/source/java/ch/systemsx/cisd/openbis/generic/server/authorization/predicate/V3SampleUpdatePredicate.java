package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

@ShouldFlattenCollections(value = false)
public class V3SampleUpdatePredicate extends V3AbstractSamplePredicate<SampleUpdate>
{
    public V3SampleUpdatePredicate()
    {
        super();
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 sample update object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<SampleUpdate> value)
    {
        Set<ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId> ids = new HashSet<>();
        for (SampleUpdate update : value)
        {
            ids.add(update.getSampleId());
        }
        return evaluateSampleIds(person, allowedRoles, ids);
    }
}
