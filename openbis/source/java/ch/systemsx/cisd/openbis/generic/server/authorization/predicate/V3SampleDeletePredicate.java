package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.SampleIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

@ShouldFlattenCollections(value = false)
public class V3SampleDeletePredicate extends AbstractPredicate<List<ISampleId>>
{

    protected final SampleIdPredicate sampleIdPredicate;

    public V3SampleDeletePredicate()
    {
        this.sampleIdPredicate = new SampleIdPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        sampleIdPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 sample id object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<ISampleId> value)
    {
        assert sampleIdPredicate.initialized : "Predicate has not been initialized";
        Set<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId> checked = new HashSet<>();

        for (ISampleId sampleId : value)
        {
            ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId toCheck = SampleIdTranslator.translate(sampleId);
            if (false == checked.contains(toCheck))
            {
                Status status = sampleIdPredicate.doEvaluation(person, allowedRoles, toCheck);
                if (status.isOK() == false)
                {
                    return status;
                }
                checked.add(toCheck);
            }
        }
        return Status.OK;
    }
}
