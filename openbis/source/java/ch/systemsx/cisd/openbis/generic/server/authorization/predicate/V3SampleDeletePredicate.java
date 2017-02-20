package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.HashSet;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

@ShouldFlattenCollections(value = false)
public class V3SampleDeletePredicate extends V3AbstractSamplePredicate<ISampleId>
{
    public V3SampleDeletePredicate()
    {
        super();
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 sample id object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<ISampleId> value)
    {
        return evaluateSampleIds(person, allowedRoles, new HashSet<>(value));
    }
}
