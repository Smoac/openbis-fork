package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.SampleIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

abstract class V3AbstractSamplePredicate<T> extends AbstractPredicate<List<T>>
{
    private final SampleAugmentedCodePredicate sampleAugmentedCodePredicate;
    private final SamplePermIdCollectionPredicate samplePermIdCollectionPredicate;

    public V3AbstractSamplePredicate()
    {
        this.sampleAugmentedCodePredicate = new SampleAugmentedCodePredicate(new SampleOwnerIdentifierPredicate());
        samplePermIdCollectionPredicate = new SamplePermIdCollectionPredicate(false);
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        sampleAugmentedCodePredicate.init(provider);
        samplePermIdCollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 sample update object";
    }
    
    protected Status evaluateSampleIds(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            Set<ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId> ids)
    {
        List<String> permIds = new ArrayList<>();
        for (ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId id : ids)
        {
            ISampleId sampleId = SampleIdTranslator.translate(id);
            if (sampleId instanceof SampleIdentifierId)
            {
                SampleIdentifierId identifier = (SampleIdentifierId) sampleId;
                Status status = sampleAugmentedCodePredicate.doEvaluation(person, allowedRoles,
                        identifier.getIdentifier());
                if (status.isOK() == false)
                {
                    return status;
                }
            } else if (sampleId instanceof SamplePermIdId)
            {
                permIds.add(((SamplePermIdId) sampleId).getPermId());
            } else
            {
                throw new AuthorizationFailureException("Unknown identifier: " + sampleId);
            }
        }
        if (permIds.isEmpty() == false)
        {
            return samplePermIdCollectionPredicate.doEvaluation(person, allowedRoles, permIds);
        }
        return Status.OK;
    }
}
